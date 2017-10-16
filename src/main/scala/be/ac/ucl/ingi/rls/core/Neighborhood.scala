package be.ac.ucl.ingi.rls.core

abstract class Neighborhood[T] {
  def setNeighborhood(substate: T)  // give a part of the state that the neighborhood can modify, to call before trying moves
  
  def hasNext(): Boolean   // neighborhoods are like iterators, once the substate is set, it generates moves
  def next(): Unit         // set internal state to next move
  
  def apply(): Unit        // apply move on state's path
  
  def saveBest(): Unit        // remember current move for future application
  def applyBest(): Unit
  
  val name: String
}
