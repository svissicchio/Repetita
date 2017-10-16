package be.ac.ucl.ingi.rls.neighborhood

import be.ac.ucl.ingi.rls.state.PathState
import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.core.Neighborhood
import be.ac.ucl.ingi.rls.state.PathState

/*
 *  This neighborhood tries to insert paths in the given demand
 */ 

class Remove(pathState: PathState)(implicit debug: Boolean) 
extends Neighborhood[Demand] 
{
  val name = "Remove"

  private[this] var demand: Demand = -1
  private[this] var source: Node = -1
  private[this] var destination: Node = -1
  private[this] var size = 0
  private[this] var position = 0
  
  def setNeighborhood(demand: Demand): Unit = {
    this.demand = demand
    source = pathState.source(demand)
    destination = pathState.destination(demand)
    position = 0
    size = pathState.size(demand) 
  }
  
  override def hasNext() = {
    size > 2 && position < size - 2
  }
  
  override def next(): Unit = {
    position += 1
  }
  
  override def apply() = {
    pathState.remove(demand, position)
  }
  
  private var storedPosition = 0
  
  override def saveBest() = {
    storedPosition = position
  }
  
  override def applyBest() = {
    position = storedPosition
    if (debug) println(s"Removing detour at position at $position for demand $demand")
    apply()
  }
}
