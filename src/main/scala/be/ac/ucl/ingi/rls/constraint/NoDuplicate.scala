package be.ac.ucl.ingi.rls.constraint

import be.ac.ucl.ingi.rls.state.{PathState, Trial}
import be.ac.ucl.ingi.rls.state.PathState
import be.ac.ucl.ingi.rls.state.Trial

/*
 * Checks that paths do not have the same node twice
 */ 

class NoDuplicate(pathState: PathState)
extends Trial {
  override def check(): Boolean = {
    val changed = pathState.changed
    var pChanged = pathState.nChanged
    
    while (pChanged > 0) {
      pChanged -= 1
      val demand = changed(pChanged)
      
      val path = pathState.path(demand)
      var p = pathState.size(demand)
      while (p > 0) {
        p -= 1
        var q = p
        while (q > 0)
        {
          q -= 1
          if (path(p) == path(q)) return false
        }
      }
    }
    
    true
  }
  
  override def revert() = {}
  override def commit() = {}
  override def update() = {}
}
