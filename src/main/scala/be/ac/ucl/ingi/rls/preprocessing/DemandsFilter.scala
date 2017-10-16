package be.ac.ucl.ingi.rls.preprocessing

import be.ac.ucl.ingi.rls.io.DemandsData
import be.ac.ucl.ingi.rls.core.CapacityData

object DemandsFilter {
  def apply(input: DemandsData, capacity: CapacityData): (DemandsData, DemandsData) = {
    //fixedRatio(input, capacity, 0.00)
    //fixedSmallest(input, capacity, 0)
    doNothing(input)
  }
  
  def doNothing(input: DemandsData): (DemandsData, DemandsData) = {
    val emptyDemandsData = new DemandsData(Array.empty[String], Array.empty[Int], Array.empty[Int], Array.empty[Double])
    (input, emptyDemandsData)
  }
  
  
  def fixedSmallest(input: DemandsData, capacity: CapacityData, nRemoved: Int): (DemandsData, DemandsData) = {
    val indices = Array.tabulate(input.nDemands)(identity)
    val sortedIndices = indices.sortBy(i => input.demandTraffics(i))
    val (indicesUnder, indicesAbove) = sortedIndices.splitAt(nRemoved)
    val demandsAbove = new DemandsData(indicesAbove map input.demandLabels, indicesAbove map input.demandSrcs, indicesAbove map input.demandDests, indicesAbove map input.demandTraffics) 
    val demandsUnder = new DemandsData(indicesUnder map input.demandLabels, indicesUnder map input.demandSrcs, indicesUnder map input.demandDests, indicesUnder map input.demandTraffics)
    (demandsAbove, demandsUnder)
  }
  
  
  def fixedRatio(input: DemandsData, capacity: CapacityData, minRatio: Double)(debug: Boolean): (DemandsData, DemandsData) = {
    // find min capacity
    val minCapacity = capacity.capacity.min * minRatio
    
    // split indices between demands under ratio and those above
    val (indicesUnder, indicesAbove) = 
      Array.tabulate(input.nDemands)(identity)
      .partition(demand => input.demandTraffics(demand) >= minCapacity)
      
    if (debug) println(s"${indicesAbove.length} demands selected, ${indicesUnder.length} demands fixed, selected ratio: ${indicesAbove.length.toDouble / input.nDemands}")
    
    val demandsAbove = new DemandsData(indicesAbove map input.demandLabels, indicesAbove map input.demandSrcs, indicesAbove map input.demandDests, indicesAbove map input.demandTraffics) 
    val demandsUnder = new DemandsData(indicesUnder map input.demandLabels, indicesUnder map input.demandSrcs, indicesUnder map input.demandDests, indicesUnder map input.demandTraffics)
    (demandsAbove, demandsUnder)
  }
}
