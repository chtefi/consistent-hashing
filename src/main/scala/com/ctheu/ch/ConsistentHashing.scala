package com.ctheu.ch

import java.security.MessageDigest

import scala.collection.immutable.SortedMap
import scala.util.hashing.{Hashing, MurmurHash3}

trait ConsistentHashing {
  def withNodes(nodes: Node*): ConsistentHashing
  def lookup(key: String): Option[VNode]
  def nodeCount: Int
  def replicaCount: Int
}

object ConsistentHashing {
  def apply(replicaCount: Int): ConsistentHashing = {
    ConsistentHashingImpl(
      replicaCount,
      hashFn = MurmurHash3.stringHash,
      SortedMap())
  }
}
