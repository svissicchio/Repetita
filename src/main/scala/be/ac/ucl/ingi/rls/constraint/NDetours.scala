package be.ac.ucl.ingi.rls.constraint

import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.state._

/*
 *  Minimizes the number of detours
 */ 

class NDetours(pathState: PathState)
extends Trial with Objective
{
  private[this] var nDetours = 0
  private[this] var oldNDetours = nDetours
  
  private val nDemands = pathState.nDemands
  
  override def score() = nDetours
  
  private def initialize() = {
    var counter = 0
    
    var demand = nDemands
    while (demand > 0) {
      demand -= 1
      if (pathState.size(demand) > 2) counter += 1
    }
    
    nDetours = counter
    oldNDetours = counter
  }
  
  initialize()
  
  override def update(): Unit = {
    val demands = pathState.changed
    var p = pathState.nChanged()
    while (p > 0) {
      p -= 1
      val demand = demands(p)
      
      val hadDetour = pathState.oldSize(demand) > 2
      val hasDetour = pathState.size(demand) > 2
      
      if (hadDetour && !hasDetour) nDetours -= 1
      if (!hadDetour && hasDetour) nDetours += 1
    }
  }
  
  override def check(): Boolean = {
    update()    
    oldNDetours >= nDetours
  }
  
  override def revert(): Unit = {
    nDetours = oldNDetours 
  }
  
  override def commit(): Unit = {
    oldNDetours = nDetours
  }  
}
