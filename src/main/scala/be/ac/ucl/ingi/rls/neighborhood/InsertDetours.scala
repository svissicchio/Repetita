package be.ac.ucl.ingi.rls.neighborhood

import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.core.Neighborhood
import be.ac.ucl.ingi.rls.state.PathState

/*
 *  This neighborhood tries to insert paths in the given demand
 */ 

class InsertDetours(detours: Array[Node], pathState: PathState)(implicit debug: Boolean) 
extends Neighborhood[Demand] 
{
  val name = "InsertDetours"

  private val nDetours = detours.length
  
  private[this] var demand: Demand = -1
  private[this] var position = 0
  private[this] var pDetour = 0
  private[this] var size = 0
  private[this] val maxDetourSize = pathState.maxDetourSize
  
  def setNeighborhood(demand: Demand): Unit = {
    this.demand = demand
    position = 1
    pDetour = -1
    size = pathState.size(demand)
  }
  
  override def hasNext() = {
    !(pDetour == nDetours - 1 && position == size - 1) && size < maxDetourSize
  }
  
  override def next(): Unit = {
    pDetour += 1
    
    if (pDetour >= nDetours) {
      pDetour = 0
      position += 1
    }
  }
  
  override def apply() = {
    pathState.insert(demand, detours(pDetour), position)
  }
  
  private var storedPosition = 0
  private var storedPDetour = pDetour
  
  override def saveBest() = {
    storedPosition = position
    storedPDetour = pDetour
  }
  
  override def applyBest() = {
    position = storedPosition
    pDetour = storedPDetour
    
    if (debug) println(s"Inserting ${detours(pDetour)} at position $position for demand $demand")
    
    apply()
  }
}
