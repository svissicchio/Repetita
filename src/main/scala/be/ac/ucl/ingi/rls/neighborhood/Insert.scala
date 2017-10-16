package be.ac.ucl.ingi.rls.neighborhood

import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.core.Neighborhood
import be.ac.ucl.ingi.rls.state.PathState

/*
 *  This neighborhood tries to insert paths in the given demand
 */ 

class Insert(nNodes: Int, pathState: PathState)(implicit debug: Boolean) 
extends Neighborhood[Demand] 
{
  val name = "Insert"
  
  private[this] var demand: Demand = -1
  private[this] var source: Node = -1
  private[this] var destination: Node = -1
  private[this] var position = 0
  private[this] var node = 0
  private[this] var size = 0
  private[this] val maxDetourSize = pathState.maxDetourSize
  
  def setNeighborhood(demand: Demand): Unit = {
    this.demand = demand
    source = pathState.source(demand)
    destination = pathState.destination(demand)
    position = 1
    node = -1
    size = pathState.size(demand)
  }
  
  override def hasNext() = {
    !(node == nNodes - 1 && position == size - 1) && size < maxDetourSize
  }
  
  override def next(): Unit = {
    node += 1
    
    if (node >= nNodes) {
      node = 0
      position += 1
    }
  }
  
  override def apply() = {
    pathState.insert(demand, node, position)
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
