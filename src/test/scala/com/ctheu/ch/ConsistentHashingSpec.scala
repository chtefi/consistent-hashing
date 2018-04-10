package com.ctheu.ch

import com.github.ssedano.hash.JumpConsistentHash
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import org.scalacheck.Arbitrary.arbitrary
import shapeless.tag
import shapeless.tag.@@

import scala.collection.mutable.ListBuffer
import scala.util.Random

object Generators {

  val replicaCountGen = Gen.choose(0, 10)

  val nonEmptyStringGen: Gen[String] = for {
    length <- Gen.choose(1, 100)
    chars <- Gen.listOfN(length, Gen.alphaChar)
    key = chars.mkString("")
  } yield key

  val nodeGen = nonEmptyStringGen.map(Node)

  val chGen: Gen[ConsistentHashing] = for {
    replicaCount <- replicaCountGen
    nodes <- Gen.nonEmptyListOf(nodeGen)
  } yield ConsistentHashing(replicaCount).withNodes(nodes: _*)

  trait Jump
  val chJumpGen: Gen[ConsistentHashing @@ Jump] = for {
    replicaCount <- replicaCountGen
    nodes <- Gen.nonEmptyListOf(nodeGen)
  } yield tag[Jump](ConsistentHashing(replicaCount, nodes).withNodes(nodes: _*))

  def keyInsertionCountGen: Gen[Int] = Gen.choose(100, 10000)

  implicit val chArb = Arbitrary(chGen)
  implicit val chJumpArb = Arbitrary(chJumpGen)

  implicit val noShrink: Shrink[Int] = Shrink.shrinkAny
}

class ConsistentHashingSpec
    extends FlatSpec
    with Matchers
    with OptionValues
    with PropertyChecks {

  import Generators._

  override implicit val generatorDrivenConfig = PropertyCheckConfiguration(
    minSuccessful = 100,
    maxDiscardedFactor = 1000d
  )

  "ConsistentHashing" should "not find anything if empty" in {
    forAll { (node: String) =>
      ConsistentHashing(10).lookup(node) shouldBe empty
    }
  }

  it should "find nodes back if we look for their name" in {
    forAll(nonEmptyStringGen, replicaCountGen) {
      (name: String, replicaCount: Int) =>
        val node = Node(name)
        val ch = ConsistentHashing(replicaCount).withNodes(node)
        ch.lookup(name).value.node === node
    }
  }

  it should "find a VNode for any keys" in {
    forAll { (ch: ConsistentHashing, key: String) =>
      ch.lookup(key) should not be empty
    }
  }

  private def meanDeviation(distribution: List[Int]): Double = {
    val mean = distribution.sum * 1.0f / distribution.length
    val variance = distribution.map(_ - mean).map(x => x * x).sum / distribution.length
    val deviation = Math.sqrt(variance)
    deviation / mean
  }

  it should "distribute the keys not really evenly by default" in {
    forAll { (ch: ConsistentHashing) =>
      forAll(keyInsertionCountGen) { count =>
        val (nodes, vnodes) = (1 to count)
          .map { _ =>
            val vnode = ch.lookup(Random.nextString(10)).value
            (vnode.node, vnode)
          }
          .unzip

        val List(nodeDistribution, vnodeDistribution) = Seq(nodes, vnodes)
          .map(_.groupBy(identity)
            .values
            .map(_.length)
            .toList)

        val (nodeMeanDev, vnodeMeanDev) = (meanDeviation(nodeDistribution), meanDeviation(vnodeDistribution))

        // println(s"nodes ($nodeDistribution): $nodeMeanDev, vnodes ($vnodeDistribution): $vnodeMeanDev")

        // stupidly high and useless because sometimes the distribution per node really sucks!
        nodeMeanDev should be < 2d

      }
    }
  }

  it should "distribute the keys evenly with jump hash" in {
    forAll { (ch: ConsistentHashing @@ Jump) =>
      forAll(keyInsertionCountGen) { count =>
        val (nodes, vnodes) = (1 to count)
          .map { _ =>
            val vnode = ch.lookup(Random.nextString(10)).value
            (vnode.node, vnode)
          }
          .unzip

        val List(nodeDistribution, vnodeDistribution) = Seq(nodes, vnodes)
          .map(_.groupBy(identity)
            .values
            .map(_.length)
            .toList)

        val (nodeMeanDev, vnodeMeanDev) = (meanDeviation(nodeDistribution), meanDeviation(vnodeDistribution))

        // println(s"nodes ($nodeDistribution): $nodeStddev, vnodes ($vnodeDistribution) $vnodeStdddev")

        // still, not really distributed
        nodeMeanDev should be < 1d
      }
    }
  }

}
