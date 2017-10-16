package be.ac.ucl.ingi.rls.neighborhood

import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.core.Neighborhood
import be.ac.ucl.ingi.rls.state.PathState

/*
 *  This neighborhood tries to move a detour from one demand to another.
 */ 

class MoveDetour(pathState: PathState)(implicit debug: Boolean) 
extends Neighborhood[(Demand, Demand)] 
{
  val name = "MoveDetour"

  private[this] var demand1: Demand = -1
  private[this] var demand2: Demand = -1
  
  private[this] var position1 = 0
  private[this] var position2 = 0
  
  private[this] var size1 = 0
  private[this] var size2 = 0
  
  private[this] val maxDetourSize = pathState.maxDetourSize
  
  def setNeighborhood(demands: (Demand, Demand)): Unit = {
    this.demand1 = demands._1
    this.demand2 = demands._2
    
    position1 = 0
    position2 = 1
    
    size1 = pathState.size(demand1)
    size2 = pathState.size(demand2)
  }
  
  override def hasNext() = {
    size2 < maxDetourSize && size1 > 2 && demand1 != demand2 && (position1 != size1 - 2 || position2 != size2 - 1)
  }
  
  override def next(): Unit = {
    position1 += 1
    
    if (position1 >= size1 - 1) {
      position1 = 1
      position2 += 1
    }
  }
  
  override def apply() = {
    // println(s"Moving detour from $size1 to $size2, from position $position1 to position $position2.")
    val node = pathState.path(demand1)(position1)
    pathState.remove(demand1, position1)
    pathState.insert(demand2, node, position2)
  }
  
  private var storedPosition1 = position1
  private var storedPosition2 = position2
  
  override def saveBest() = {
    storedPosition1 = position1
    storedPosition2 = position2
  }
  
  override def applyBest() = {
    position1 = storedPosition1 
    position2 = storedPosition2
    
    //if (debug) println(s"Replacing by ${detours(pDetour)} at position $position for demand $demand")
    apply()
  }
}
