package be.ac.ucl.ingi.rls.neighborhood

import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.core.Neighborhood
import be.ac.ucl.ingi.rls.state.PathState

/*
 *  This neighborhood tries to change a detour in the path of the given demand
 */ 

class ReplaceDetours(detours: Array[Node], pathState: PathState)(implicit debug: Boolean) 
extends Neighborhood[Demand] 
{
  val name = "ReplaceDetours"

  private val nDetours = detours.length
  
  private[this] var demand: Demand = -1
  private[this] var source: Node = -1
  private[this] var destination: Node = -1
  private[this] var position = 0
  private[this] var pDetour = 0
  private[this] var size = 0
  private[this] val maxDetourSize = pathState.maxDetourSize
  
  def setNeighborhood(demand: Demand): Unit = {
    this.demand = demand
    source = pathState.source(demand)
    destination = pathState.destination(demand)
    position = 0
    pDetour = nDetours - 1
    size = pathState.size(demand)
  }
  
  override def hasNext() = {
    !(pDetour == nDetours - 1 && position == size - 2)
  }
  
  override def next(): Unit = {
    pDetour += 1
    
    if (pDetour >= nDetours) {
      pDetour = 0
      position += 1
    }
  }
  
  override def apply() = {
    if (pathState.path(demand)(position) != detours(pDetour)) {
      pathState.replace(demand, detours(pDetour), position)
    }
  }
  
  private var storedPosition = position
  private var storedPDetour = pDetour
  
  override def saveBest() = {
    storedPosition = position
    storedPDetour = pDetour
  }
  
  override def applyBest() = {
    position = storedPosition
    pDetour = storedPDetour
    
    if (debug) println(s"Replacing ${pathState.path(demand)(position)} by ${detours(pDetour)} at position $position for demand $demand")
    apply()
  }
}
