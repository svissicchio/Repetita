package be.ac.ucl.ingi.defo.core

import be.ac.ucl.ingi.defo.modeling.DEFOConstraint
import be.ac.ucl.ingi.defo.parsers.TopologyData
import be.ac.ucl.ingi.defo.parsers.DemandsData

class DEFOInstance(
  val topology: Topology,
  val weights: Array[Int],
  val demandTraffics: Array[Int],
  val demandSrcs: Array[Int],
  val demandDests: Array[Int],
  val demandConstraints: Array[Array[DEFOConstraint]],
  val topologyConstraints: Array[DEFOConstraint],
  val capacities: Array[Int],
  val latencies: Array[Int],
  val maxSeg: Int = 2)
  
object DEFOInstance {
  def apply(topologyData: TopologyData, demandsData: DemandsData, maxSeg: Int): DEFOInstance = {
    val topology = Topology(topologyData.edgeSrcs, topologyData.edgeDests)
    new DEFOInstance(
      topology,
      topologyData.edgeWeights,
      demandsData.demandTraffics,
      demandsData.demandSrcs,
      demandsData.demandDests,
      Array.fill(demandsData.demandTraffics.length)(Array.empty),
      Array.empty,
      topologyData.edgeCapacities,
      topologyData.edgeLatencies,
      maxSeg
    )
  }
}
