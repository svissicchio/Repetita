package be.ac.ucl.ingi.rls.state

import be.ac.ucl.ingi.rls._
import scala.util.Random
import be.ac.ucl.ingi.rls.core.CapacityData
import scala.annotation.tailrec

/*
 * This class tracks which demands use which state. 
 */

abstract class EdgeDemandState(nDemands: Int, nEdges: Int) 
{
  def updateEdgeDemand(edge: Edge, demand: Demand, flowDelta: Double): Unit  // add increment to the flow on given edge from given demand
  
  def selectRandomDemand(edge: Edge): Demand // choose among demands on edge with probability weighted by their amount of flow on said edge
  
  def flowOnEdgeDemand(edge: Edge, demand: Demand): Double
  
  def restrictDemands(set: Array[Boolean]) = {
    val nDemands = set.length
    
    var edge = nEdges
    while (edge > 0) {
      edge -= 1
      
      var demand = nDemands
      while (demand > 0) {
        demand -= 1
        
        if (!set(demand) && flowOnEdgeDemand(edge, demand) != 0.0) updateEdgeDemand(edge, demand, -flowOnEdgeDemand(edge, demand))
      }
    }
  }
}


class EdgeDemandStateSimple(nDemands: Int, nEdges: Int, capacity: CapacityData)
extends EdgeDemandState(nDemands, nEdges)
{
  override def flowOnEdgeDemand(edge: Edge, demand: Demand) = flowOnEdgeDemand_(edge)(demand) 
  private[this] val flowOnEdgeDemand_ = Array.ofDim[Double](nEdges, nDemands)
  private val epsilon = 1e-6
  
  override def updateEdgeDemand(edge: Edge, demand: Demand, flowDelta: Double) = {
    flowOnEdgeDemand_(edge)(demand) += flowDelta
  }
  
  override def selectRandomDemand(edge: Edge): Demand = {
    var chosenDemand = -1
    var chosenFlow = epsilon
    var totalFlow = 0.0
    
    var demand = nDemands
    while (demand > 0) {
      demand -= 1
      val flow = flowOnEdgeDemand_(edge)(demand)
//      val flow = math.pow(flowOnEdgeDemand(edge)(demand), 2.0)
      totalFlow += flow
      
      if (Random.nextDouble() * totalFlow < flow) {
        chosenDemand = demand
        chosenFlow = flow 
      }
    }
    chosenDemand
  }
}

class EdgeDemandStateTree(nDemands: Int, nEdges: Int, capacity: CapacityData)
extends EdgeDemandState(nDemands, nEdges)
{
  private def log2(x: Int): Int = math.ceil(math.log(x) / math.log(2)).toInt
  
  // DELETE ME
//  private val totalLeaves = nEdges * nDemands
//  private var usedLeaves = 0

  /*
   *  Structures to maintain edge utilization per demand
   */ 
  private val logDemands = log2(nDemands)
  private val baseDemands = 1 << logDemands
  
  private val treeFlowOnEdgeDemand = Array.ofDim[Double](nEdges, 2 * baseDemands)
  override def flowOnEdgeDemand(edge: Edge, demand: Demand) = treeFlowOnEdgeDemand(edge)(baseDemands + demand)

  private val epsilon = 1e-6
  
  private def modifyFlowDemand(edge: Edge, demand: Demand, newFlow: Double) = {
    assert(newFlow >= -epsilon)
    treeFlowOnEdgeDemand(edge)(baseDemands + demand) = newFlow
    modifyFlowDemandTree(treeFlowOnEdgeDemand(edge), (baseDemands + demand) >> 1)
  }
  
  @tailrec private def modifyFlowDemandTree(tree: Array[Double], node: Int): Unit = {
    val left = node << 1
    val right = left | 1
    tree(node) = tree(left) + tree(right)
    assert(tree(node) >= -epsilon)
    if (node > 1) modifyFlowDemandTree(tree, node >> 1)
  }

  // updates directly write to trees and do recursive updates
  override def updateEdgeDemand(edge: Edge, demand: Demand, flowDelta: Double) = {
    val oldFlow = flowOnEdgeDemand(edge, demand)
    val newFlow = oldFlow + flowDelta
//    if (oldFlow < epsilon && newFlow > epsilon) usedLeaves += 1
//    else if (oldFlow > epsilon && newFlow < epsilon) usedLeaves -= 1
    modifyFlowDemand(edge, demand, newFlow)
  }
  
  override def selectRandomDemand(edge: Edge): Demand = {
//    println(s"Selecting demand, $usedLeaves used leaves over $totalLeaves")
    val r = Random.nextDouble() * treeFlowOnEdgeDemand(edge)(1) - epsilon
    // println(s"selecting demand at $r over ${treeFlowOnEdgeDemand(edge)(1)}")
    val selectedNode = selectDemand(treeFlowOnEdgeDemand(edge), 1, 2 * baseDemands, r)
    // println(s"selected demand uses ${treeFlowOnEdgeDemand(edge)(selectedNode) / treeFlowOnEdgeDemand(edge)(1)} of total flow")
    val selectedDemand = selectedNode - baseDemands
    
    selectedDemand
  }
  
  @tailrec private def selectDemand(tree: Array[Double], node: Int, limit: Int, r: Double): Demand = {
    val left = node << 1
    if (left >= limit) node
    else {
      if (r <= tree(left)) selectDemand(tree, left, limit, r)
      else {
        val right = left | 1
        selectDemand(tree, right, limit, r - tree(left))
      }
    }
  }
  
}
