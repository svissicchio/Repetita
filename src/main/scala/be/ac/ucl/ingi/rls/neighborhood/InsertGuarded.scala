package be.ac.ucl.ingi.rls.neighborhood

import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.core.Neighborhood
import be.ac.ucl.ingi.rls.state.PathState
import be.ac.ucl.ingi.rls.constraint.MaxLoad

/*
 *  This neighborhood tries to insert paths in the given demand
 *  It uses an O(1) guard that can do nothing instead of doing the move if it would break the max edge
 *  TODO: if this works well, preventively generate the set of detours to insert at setNeighborhood call. 
 */ 

class InsertGuarded(nNodes: Int, nEdges: Int, ecmp: ECMP, pathState: PathState, maxLoad: MaxLoad)(implicit debug: Boolean) 
extends Neighborhood[Demand] 
{
  val name = "InsertGuarded"
  
  private[this] var demand: Demand = -1
  private[this] var source: Node = -1
  private[this] var destination: Node = -1
  private[this] var position = 0
  private[this] var node = 0
  private[this] var size = 0
  private[this] val maxDetourSize = pathState.maxDetourSize
  
  // precompute ECMP fractions and store them right here
  // fractions(src)(dest)(edge) = fraction used by ECMP on edge when sending 1.0 from src to dest 
  private val fractions = Array.fill(nNodes, nNodes, nEdges)(0.0)
  
  {
    val Nodes = 0 until nNodes
    val Edges = 0 until nEdges
    for (src <- Nodes) for (dest <- Nodes) {
      var p = ecmp.nEdgesToModify(src)(dest)
      while (p > 0) {
        p -= 1
        val edge = ecmp.edgesToModify(src)(dest)(p)
        val frac = ecmp.fractionToModify(src)(dest)(p)
        fractions(src)(dest)(edge) = frac
      }      
    }    
  }
  
  def setNeighborhood(demand: Demand): Unit = {
    this.demand = demand
    source = pathState.source(demand)
    destination = pathState.destination(demand)
    position = 1
    node = -1
    size = pathState.size(demand)
  }
  
  override def hasNext() = !(node == nNodes - 1 && position == size - 1) && size < maxDetourSize
  
  override def next(): Unit = {
    node += 1
    
    if (node >= nNodes) {
      node = 0
      position += 1
    }
  }
  
  // check whether inserting the detour would reduce the usage of edge; if not, do nothing instead of the move
  private var nAttempts = 0
  private var nMade = 0
  override def apply() = {
    val maxEdge = maxLoad.selectRandomMaxEdge()
    val src    = pathState.path(demand)(position - 1)
    val dest   = pathState.path(demand)(position)
    
    val loadSub = fractions(src)(dest)(maxEdge)
    val loadAdd = fractions(src)(node)(maxEdge) + fractions(node)(dest)(maxEdge)

    nAttempts += 1
    if (loadSub > loadAdd) {
      nMade += 1
      //if ((nMade & 0xFFFF) == 0) println(s"$nAttempts insertion attempts, $nMade really made")
      pathState.insert(demand, node, position)
    }
  }
  
  private var storedPosition = 0
  private var storedNode = node
  
  override def saveBest() = {
    storedPosition = position
    storedNode = node
  }
  
  override def applyBest() = {
    position = storedPosition
    node = storedNode
    
    apply()
  }
}
