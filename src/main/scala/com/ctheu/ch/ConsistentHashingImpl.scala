package com.ctheu.ch

import scala.collection.immutable.SortedMap

case class ConsistentHashingImpl(
                            replicaCount: Int,
                            hashFn: String => Int,
                            circle: SortedMap[Int, VNode]
                            ) extends ConsistentHashing {

  require(replicaCount >= 0)

  def withNodes(nodes: Node*): ConsistentHashing = {
    //require(nodes.map(_.name).toSet.size == nodes.length)
    copy(circle = nodes.foldLeft(circle)(_ ++ makeVNodes(_)))
  }

  def lookup(key: String): Option[VNode] = {
    require(key.length > 0)
    if (empty) None
    else Some(circle.getOrElse(hashFn(key), nearest(key)))
  }

  lazy val nodeCount: Int = circle.keys.size / (replicaCount + 1)

  private def empty: Boolean = circle.headOption.isEmpty

  private def nodeName(name: String, replica: Int): String = { s"$name $replica" }

  private def makeVNodes(node: Node): SortedMap[Int, VNode] = {
    (0 to replicaCount).map { replica =>
      val str = nodeName(node.name, replica)
      hashFn(str) -> VNode(str, node)
    }(collection.breakOut)
  }

  private def nearest(key: String): VNode = {
    val hash = hashFn(key)
    if (hash > circle.lastKey) circle(circle.firstKey)
    else circle.keys.find(_ > hash).map(circle(_)).get
  }
}
