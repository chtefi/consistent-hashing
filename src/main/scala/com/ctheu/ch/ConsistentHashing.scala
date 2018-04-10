package com.ctheu.ch

import java.security.MessageDigest

import com.github.ssedano.hash.JumpConsistentHash

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

  def apply(replicaCount: Int, nodes: List[Node]): ConsistentHashing = {
    ConsistentHashingImpl(
      replicaCount,
      hashFn = x => JumpConsistentHash.jumpConsistentHash(MurmurHash3.stringHash(x), (replicaCount + 1) * nodes.length),
      SortedMap())
  }
}
