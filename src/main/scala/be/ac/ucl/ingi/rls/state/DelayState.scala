package be.ac.ucl.ingi.rls.state

import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.core.DelayData

/*
 *  A structure to store the current paths (detour sequences) in the network.
 */ 


class DelayState(nDemands: Int, delays: DelayData, pathState: PathState)
extends TrialState {
  val delay    = Array.ofDim[Int](nDemands) 
  val oldDelay = Array.ofDim[Int](nDemands)
  private[this] val markedDemands = Array.fill(nDemands)(false)
  val changed = Array.ofDim[Demand](nDemands)
  private[this] var nChanged_ = 0
  def nChanged = nChanged_
  
  def initialize() = {
    var demand = nDemands
    while (demand > 0) {
      demand -= 1
      delay(demand) = computeDelay(demand)
    }
  }
  
  private def computeDelay(demand: Demand): Int = {
    var delay = 0
    val path = pathState.path(demand)
    
    var p = pathState.size(demand) - 1
    while (p > 0) {
      p -= 1
      delay += delays.delay(path(p), path(p+1))
    }
    
    delay
  }
  
  @inline private def update(demand: Demand, newDelay: Int) = {
    if (!markedDemands(demand)) {
      markedDemands(demand) = true
      changed(nChanged_) = demand
      nChanged_ += 1
      oldDelay(demand) = delay(demand)
    }
    delay(demand) = newDelay
  }
  
  override def updateState() = {
    // update
    val changedDemand = pathState.changed
    var p = pathState.nChanged
    while (p > 0) {
      p -= 1
      val demand = changedDemand(p)
      val newDelay = computeDelay(demand)
      if (newDelay != delay(demand)) update(demand, newDelay)
    }
  }
  
  override def check() = {
    updateState()
    // call checkers
    super.check()
  }
  
  override def commitState() = {
    while (nChanged_ > 0) {
      nChanged_ -= 1
      markedDemands(changed(nChanged_)) = false
    }
  }
  
  override def revertState() = {
    while (nChanged_ > 0) {
      nChanged_ -= 1
      val demand = changed(nChanged_)
      markedDemands(demand) = false
      delay(demand) = oldDelay(demand)
    }
  }
  
  initialize()
}
