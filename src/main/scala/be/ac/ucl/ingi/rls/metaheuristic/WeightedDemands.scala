package be.ac.ucl.ingi.rls.metaheuristic

import scala.util.Random

class WeightedDemands(weights: Array[Double]) {
  private val nDemands = weights.length
  private val partialSums = Array.ofDim[Double](nDemands)
  
  initialize()
  
  private def initialize() = {
    var sum = 0.0
    partialSums(0) = 0.0
    
    var p = 0
    while (p < nDemands) {
      sum += weights(p)
      partialSums(p) = sum
      p += 1
    }
  }
  
  // choose a point in [0, weights.sum], do binary search to find an index i s.t. partialSum(i) <= point < partialSum(i+1)
  def weightedChoice(): Int = {
    val point = Random.nextDouble() * partialSums(nDemands - 1)
    
    // binary search
    var left = 0
    var right = nDemands
    while (left + 1 < right) {
      val middle = left + (right - left) / 2
      if (partialSums(middle) <= point) 
        left = middle
      else 
        right = middle
    }
    left
  }
}
