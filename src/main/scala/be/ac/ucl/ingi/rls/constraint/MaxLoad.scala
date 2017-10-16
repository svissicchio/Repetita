package be.ac.ucl.ingi.rls.constraint

import be.ac.ucl.ingi.rls.core._
import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.core.Topology
import scala.util.Random
import be.ac.ucl.ingi.rls.state.FlowStateChecker
import be.ac.ucl.ingi.rls.state.Objective
import be.ac.ucl.ingi.rls.state.Trial

class MaxLoad(topology: Topology, capacityData: CapacityData, flowState: FlowStateChecker, sp: ShortestPaths)(debug: Boolean)
extends Trial with Objective 
{
  private[this] val nNodes = topology.nNodes
  private[this] val nEdges = topology.nEdges

  private[this] var nMaxLoad = 0
  private[this] var maxLoad = 0.0
  private[this] var maxEdge = 0
  private[this] var oldNMaxLoad = nMaxLoad
  private[this] var oldMaxLoad = maxLoad
  private[this] var oldMaxEdge = maxEdge

  var active = true
  var relaxed = false 
  
  override def score() = maxLoad
  
  def initialize(): Unit = {
    maxLoad = 0
    nMaxLoad = 0
    
    val flow = flowState.values
    val invcapa = capacityData.invCapacity
    
    var edge = nEdges
    while (edge > 0) {
      edge -= 1
      val load = flow(edge) * invcapa(edge)
      if (load > maxLoad) {
        maxEdge = edge
        maxLoad = load
        nMaxLoad = 1
      }
      else if (load == maxLoad) nMaxLoad += 1
    }
  }
  
  initialize()
  commit()
  
  def selectRandomMaxEdge(): Edge = maxEdge

  override def update(): Unit = {
    val flow = flowState.values
    val oldFlow = flowState.oldValues
    val invcapa = capacityData.invCapacity
    
    // compute new stats
    // first see if old stats can be refreshed from the delta
    val changed = flowState.deltaElements
    var p = flowState.nDelta
    while (p > 0) {
      p -= 1
      val edge = changed(p)
      
      if (flow(edge) != oldFlow(edge)) {
        val load    = flow(edge)    * invcapa(edge)
        val oldLoad = oldFlow(edge) * invcapa(edge)

        if (load > maxLoad) {
          maxEdge = edge
          maxLoad = load
          nMaxLoad = 1
        }
        else if (load == maxLoad) nMaxLoad += 1
        else if (oldLoad == maxLoad) nMaxLoad -= 1   // maxLinkLoad < maxLoad
      }
    }
    
    // if new stats are not usable, recompute from scratch. Happens when better solution is found, for instance.
    if (nMaxLoad == 0) initialize()
  }
  
  override def check(): Boolean = {
    update()
    
    val improved = !active || (relaxed && maxLoad <= oldMaxLoad) || maxLoad < oldMaxLoad
    if (!improved) revert()
    improved
  }
  
  override def commit() = {
    oldMaxLoad = maxLoad
    oldNMaxLoad = nMaxLoad
    oldMaxEdge = maxEdge
    
    // if (debug) println(s"maxLoad is $maxLoad at edge $maxEdge, nMaxLoad is $nMaxLoad")
  }
  
  override def revert() = {
    maxLoad = oldMaxLoad
    nMaxLoad = oldNMaxLoad
    maxEdge = oldMaxEdge
  }
}
