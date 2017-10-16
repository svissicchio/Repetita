package be.ac.ucl.ingi.rls.neighborhood

import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.core.Neighborhood
import be.ac.ucl.ingi.rls.state.PathState

/*
 *  This neighborhood tries to insert paths in the given demand
 */ 

class Reset(pathState: PathState)(implicit debug: Boolean)
extends Neighborhood[Demand] 
{
  val name = "Reset"

  private[this] var demand: Demand = -1
  private[this] var source: Node = -1
  private[this] var destination: Node = -1
  
  private var neverTried = false
  
  override def setNeighborhood(demand: Demand): Unit = {
    this.demand = demand
    source = pathState.source(demand)
    destination = pathState.destination(demand)
    neverTried = true
  }
  
  override def hasNext() = {
    pathState.size(demand) > 2 && neverTried
  }
  
  override def next(): Unit = neverTried = false
  
  override def apply() = {
    pathState.reset(demand)
  }
  
  override def saveBest() = { }
  
  override def applyBest() = {
    if (debug) println(s"Resetting demand $demand")
    apply()
  }
}
