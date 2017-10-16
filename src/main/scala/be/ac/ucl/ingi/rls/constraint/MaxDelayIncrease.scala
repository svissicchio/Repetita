package be.ac.ucl.ingi.rls.constraint

import be.ac.ucl.ingi.rls.state.DelayState
import be.ac.ucl.ingi.rls.state.Trial
import be.ac.ucl.ingi.rls.state.DelayState

class MaxDelayIncrease(nDemands: Int, delayState: DelayState)
extends Trial 
{
  private[this] val increaseFactor = 2.0
  
  private[this] val limit = Array.tabulate(nDemands) { demand =>
    delayState.delay(demand) * increaseFactor
  }
  
  override def check(): Boolean = {
    val changed = delayState.changed
    var pChanged = delayState.nChanged
    var ok = true
    while (pChanged > 0 && ok) {
      pChanged -= 1
      val demand = changed(pChanged)
      ok &= delayState.delay(demand) < limit(demand)
      // if (!ok) println("MaxDelayIncrease rejecting move")
    }
    ok
  }
  
  override def revert() = {}
  override def commit() = {}
  override def update() = {}
}
