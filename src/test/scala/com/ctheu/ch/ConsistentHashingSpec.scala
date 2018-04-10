package com.ctheu.ch

import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import shapeless.tag.@@

import scala.util.Random

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
    forAll { (node: String @@ NonEmpty) =>
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
    forAll { (ch: ConsistentHashing, key: String @@ NonEmpty) =>
      ch.lookup(key) should not be empty
    }
  }

  it should "distribute the keys not really evenly by default" in {
    import Stats._

    forAll { (ch: ConsistentHashing) =>
      forAll(keyInsertionCountGen) { count =>
        val (nodes, vnodes) = (1 to count).map { _ =>
          val vnode = ch.lookup(Random.nextString(10)).value
          (vnode.node, vnode)
        }.unzip

        val List(nodeDistribution, vnodeDistribution) = Seq(nodes, vnodes)
          .map(
            _.groupBy(identity).values
              .map(_.length)
              .toList)

        val (nodeMeanDev, vnodeMeanDev) =
          (meanDeviation(nodeDistribution), meanDeviation(vnodeDistribution))

        // println(s"nodes ($nodeDistribution): $nodeMeanDev, vnodes ($vnodeDistribution): $vnodeMeanDev")

        // stupidly high and useless because sometimes the distribution per node really sucks!
        nodeMeanDev should be < 2d

      }
    }
  }

  it should "distribute the keys evenly with jump hash" in {
    import Stats._

    forAll { (ch: ConsistentHashing @@ Jump) =>
      forAll(keyInsertionCountGen) { count =>
        val (nodes, vnodes) = (1 to count).map { _ =>
          val vnode = ch.lookup(Random.nextString(10)).value
          (vnode.node, vnode)
        }.unzip

        val List(nodeDistribution, vnodeDistribution) = Seq(nodes, vnodes)
          .map(
            _.groupBy(identity).values
              .map(_.length)
              .toList)

        val (nodeMeanDev, vnodeMeanDev) =
          (meanDeviation(nodeDistribution), meanDeviation(vnodeDistribution))

        // println(s"nodes ($nodeDistribution): $nodeStddev, vnodes ($vnodeDistribution) $vnodeStdddev")

        // still, not really distributed
        nodeMeanDev should be < 1d
      }
    }
  }

}
