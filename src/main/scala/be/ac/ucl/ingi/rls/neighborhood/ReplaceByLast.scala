package be.ac.ucl.ingi.rls.neighborhood

import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.core.Neighborhood
import be.ac.ucl.ingi.rls.state.PathState
import be.ac.ucl.ingi.rls.state.Trial

/*
 *  This neighborhood tries to replace a detour in given demand by the last detour committed
 */ 

class ReplaceByLast(pathState: PathState)(implicit debug: Boolean) 
extends Neighborhood[Demand] with Trial
{
  val name = "ReplaceByLast"
  
  var lastDetour: Node = 0
  
  // First part: listen the pathState to infer latest detour.
  override def update() = ()
  override def revert() = ()
  override def check() = true
  override def commit() = {
    // only get the detour of the first path
    val nChanged = pathState.nChanged()
    if (nChanged > 0) {
      val demand = pathState.changed(0)
      
      val path = pathState.path(demand)
      val oldPath = pathState.oldPath(demand)
      
      val limit = math.min(pathState.size(demand), pathState.oldSize(demand))
      
      var p = 1
      while (p < limit && path(p) == oldPath(p)) p += 1
      
      if (p < limit) lastDetour = path(p)
    }
  }
  

  // Second part:
  private[this] var demand: Demand = -1
  private[this] var position = 0
  private[this] var size = 0
  private[this] val maxDetourSize = pathState.maxDetourSize
  
  def setNeighborhood(demand: Demand): Unit = {
    this.demand = demand
    position = 0
    size = pathState.size(demand)
  }
  
  override def hasNext() = {
    position != size - 2
  }
  
  override def next(): Unit = { 
    position += 1
  }
  
  override def apply() = {
    if (pathState.path(demand)(position) != lastDetour) {
      pathState.replace(demand, lastDetour, position)
    }
  }
  
  private var storedPosition = position
  private var storedDetour = lastDetour
  
  override def saveBest() = {
    storedPosition = position
    storedDetour = lastDetour
  }
  
  override def applyBest() = {
    position = storedPosition
    lastDetour = storedDetour
    
    if (debug) println(s"Replacing by ${lastDetour} at position $position for demand $demand")
    apply()
  }
}
