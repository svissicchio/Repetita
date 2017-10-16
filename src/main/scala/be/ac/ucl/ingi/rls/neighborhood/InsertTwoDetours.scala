package be.ac.ucl.ingi.rls.neighborhood

import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.core.Neighborhood
import be.ac.ucl.ingi.rls.state.PathState

/*
 *  This neighborhood tries to insert paths in the given demand
 */ 

class InsertTwoDetours(detours: Array[Node], pathState: PathState)(implicit debug: Boolean) 
extends Neighborhood[Demand] 
{
  val name = "InsertTwoDetours"

  private val nDetours = detours.length
  
  private[this] var demand: Demand = -1
  private[this] var source: Node = -1
  private[this] var destination: Node = -1
  private[this] var position = 0
  private[this] var pDetour1 = 0
  private[this] var pDetour2 = 0
  private[this] var size = 0
  private[this] val maxDetourSize = pathState.maxDetourSize
  
  def setNeighborhood(demand: Demand): Unit = {
    this.demand = demand
    source = pathState.source(demand)
    destination = pathState.destination(demand)
    position = 1
    pDetour1 = -1
    pDetour2 = 0
    size = pathState.size(demand)
  }
  
  override def hasNext() = {
    !(pDetour1 == nDetours - 1 && pDetour2 == nDetours - 1 && position == size - 1) && size < maxDetourSize - 1
  }
  
  override def next(): Unit = {
    pDetour1 += 1
    
    if (pDetour1 == nDetours) {
      pDetour1 = 0
      pDetour2 += 1
    }
    
    if (pDetour2 == nDetours) {
      pDetour1 = 0
      pDetour2 = 0
      position += 1
    }
  }
  
  override def apply() = {
    // reverse insert order so that we go to pDetour1, then pDetour2
    pathState.insert(demand, detours(pDetour2), position)
    pathState.insert(demand, detours(pDetour1), position)
  }
  
  private var storedPosition = 0
  private var storedPDetour1 = pDetour1
  private var storedPDetour2 = pDetour2
  
  override def saveBest() = {
    storedPosition = position
    storedPDetour1 = pDetour1
    storedPDetour2 = pDetour2
  }
  
  override def applyBest() = {
    position = storedPosition
    pDetour1 = storedPDetour1
    pDetour2 = storedPDetour2
    
    apply()
  }
}
