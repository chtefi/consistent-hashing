package com.ctheu.ch

object Stats {
  def meanDeviation(distribution: List[Int]): Double = {
    val mean = distribution.sum * 1.0f / distribution.length
    val variance = distribution.map(_ - mean).map(x => x * x).sum / distribution.length
    val deviation = Math.sqrt(variance)
    deviation / mean
  }
}
