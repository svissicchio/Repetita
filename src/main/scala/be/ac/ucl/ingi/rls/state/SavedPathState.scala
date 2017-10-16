package be.ac.ucl.ingi.rls.state

import be.ac.ucl.ingi.rls._

/*
 *  A structure to store the saved paths (detour sequences) in the network.
 */ 

class SavedPathState(pathState: PathState)
extends Trial
{
  val nDemands = pathState.nDemands
  private[this] val changed = Array.fill(nDemands)(false)
  private[this] val changedStack = Array.ofDim[Demand](nDemands)
  private[this] var nChanged = 0
  
  private[this] val paths = Array.ofDim[Node](nDemands, pathState.maxDetourSize)  // last saved paths
  private[this] val lengthPaths = Array.ofDim[Int](nDemands)         // last saved paths' size
  
  private def changePath(demand: Demand) = {
    if (!changed(demand)) {
      changedStack(nChanged) = demand
      nChanged += 1
    }
    changed(demand) = true
  }
  
  private def initialize() = {
    var demand = nDemands
    while (demand > 0) {
      demand -= 1
      System.arraycopy(pathState.path(demand), 0, paths(demand), 0, pathState.size(demand))
      lengthPaths(demand) = pathState.size(demand)
    }
  }
  
  initialize()
  
  // copy paths that changed since last save, returns the real number of paths that changed
  def savePaths(): Int = {
    var count = 0
    while (nChanged > 0) {
      nChanged -= 1
      
      val demand = changedStack(nChanged)      
      val path = pathState.path(demand)
      val size = pathState.size(demand)
      changed(demand) = false
      
      // check whether path really changed
      var pathChanged = size != lengthPaths(demand)
      
      if (!pathChanged) {
        var p = size
        while (p > 0) {
          p -= 1
          pathChanged ||= (path(p) != paths(demand)(p))
        }
      }
      
      if (pathChanged) count += 1
      
      // save path
      System.arraycopy(path, 0, paths(demand), 0, size)
      lengthPaths(demand) = size
    }
    
    count
  }
  
  def restorePaths() = {
    while (nChanged > 0) {
      nChanged -= 1
      val demand = changedStack(nChanged)
      changed(demand) = false
      
      pathState.setPath(demand, paths(demand), lengthPaths(demand))
    }
  }
  
  override def check() = { true }
  override def revert() = { }
  
  override def update() = { }
  override def commit() = { 
    val currentChanged = pathState.changed
    var p = pathState.nChanged()
    while (p > 0) {
      p -= 1
      val demand = currentChanged(p)
      changePath(demand)
    }
  }
}
