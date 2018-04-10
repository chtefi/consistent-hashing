package com.ctheu.ch

import org.scalacheck.{Arbitrary, Gen, Shrink}
import shapeless.tag
import shapeless.tag.@@

object Generators {

  val replicaCountGen = Gen.choose(0, 10)

  val nonEmptyStringGen: Gen[String @@ NonEmpty] = for {
    length <- Gen.choose(1, 100)
    chars <- Gen.listOfN(length, Gen.alphaChar)
    key = chars.mkString("")
  } yield tag[NonEmpty](key)

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
  trait NonEmpty
  implicit val nonEmptyStringArb = Arbitrary(nonEmptyStringGen)

  implicit val noShrink: Shrink[Int] = Shrink.shrinkAny
}
