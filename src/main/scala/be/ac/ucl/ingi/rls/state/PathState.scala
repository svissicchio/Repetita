package be.ac.ucl.ingi.rls.state

import be.ac.ucl.ingi.rls.io.DemandsData
import be.ac.ucl.ingi.rls._
import be.ac.ucl.ingi.rls.io.DemandsData

/*
 *  A structure to store the current paths (detour sequences) in the network.
 */ 

class PathState(demands: DemandsData, maxSeg: Int)
extends TrialState
{
  val nDemands = demands.nDemands
  val maxDetourSize = 2 + maxSeg
  
  def source(demand: Demand): Node = demands.demandSrcs(demand)
  def destination(demand: Demand): Node = demands.demandDests(demand)
  
  // A detour is represented as source, detour_1 ... detour_n, destination
  def size(demand: Demand): Int = paths(demand).size                // size returns n + 2
  def path(demand: Demand): Array[Node] = paths(demand).path        // returns array whose 0 to size elements are the current path
  
  def oldSize(demand: Demand): Int = paths(demand).oldSize          // size returns n + 2
  def oldPath(demand: Demand): Array[Node] = paths(demand).oldPath  // returns array whose 0 to size elements are the current path
  
  private[this] val paths = Array.tabulate(nDemands) { d =>
    new Path(d, demands.demandSrcs(d), demands.demandDests(d), maxDetourSize)
  }
  
  def insert(demand: Demand, node: Node, position: Int) = {
    addChanged(demand)
    paths(demand).insert(node, position)
  }
  
  def replace(demand: Demand, node: Node, position: Int) = {
    addChanged(demand)
    paths(demand).replace(node, position)
  }
  
  def remove(demand: Demand, position: Int) = {
    addChanged(demand)
    paths(demand).remove(position)
  }
  
  def reset(demand: Demand) = {
    addChanged(demand)
    paths(demand).reset()
  }
  
  def setPath(demand: Demand, path: Array[Node], size: Int) = {
    addChanged(demand)
    paths(demand).setPath(path, size)
  }
  
  val changed = Array.ofDim[Demand](nDemands)
  private var nChanged_ = 0
  private val markedChanged = Array.fill(nDemands)(false)
  def nChanged() = nChanged_
  
  private def addChanged(demand: Demand) = {
    if (!markedChanged(demand)) {
      paths(demand).save()
      markedChanged(demand) = true
      changed(nChanged) = demand
      nChanged_ += 1
    }
  }
  
  override def updateState() = { }
  
  override def commitState() = {
    while (nChanged > 0) {
      nChanged_ -= 1
      val demand = changed(nChanged)
      markedChanged(demand) = false
    }
  }
  
  override def revertState() = {
    while (nChanged > 0) {
      nChanged_ -= 1
      val demand = changed(nChanged)
      paths(demand).restore()
      markedChanged(demand) = false
    }
  }
  
  // TODO: uninternalize this from PathState?
  private class Path(
      val demand: Demand, 
      val source: Node, 
      val destination: Node, 
      val maxSize: Int
      )
  {
    require(maxSize >= 2)
    
    def size = currentSize
    def path = currentPath
    def oldSize = savedSize
    def oldPath = savedPath
    
    private[this] var currentSize = 2
    private[this] val currentPath = Array.fill[Node](maxSize)(0)
    currentPath(0) = source
    currentPath(1) = destination
    
    private[this] var savedSize = 2
    private[this] val savedPath = Array.fill[Node](maxSize)(0)
    savedPath(0) = source
    savedPath(1) = destination
    
    def insert(node: Node, position: Int): Unit = {
      assert(0 < position)
      assert(position < size)
      assert(size < maxSize)
      System.arraycopy(currentPath, position, currentPath, position + 1, size - position)
      currentPath(position) = node
      currentSize += 1
    }
    
    def replace(node: Node, position: Int): Unit = {
      assert(position > 0)
      assert(position < size - 1)
      currentPath(position) = node
    }
    
    def remove(position: Int): Unit = {
      assert(0 < position && position < size - 1)
      System.arraycopy(currentPath, position + 1, currentPath, position, size - position - 1)
      currentSize -= 1
    }
    
    def reset(): Unit = {
      currentPath(0) = source
      currentPath(1) = destination
      currentSize = 2
    }
    
    def setPath(newPath: Array[Node], newSize: Int) = {
      assert(newSize <= maxDetourSize)
      assert(newPath(0) == source)
      assert(newPath(newSize - 1) == destination)
      
      System.arraycopy(newPath, 0, path, 0, newSize)
      currentSize = newSize
    }
    
    def save(): Unit = {
      savedSize = currentSize
      System.arraycopy(currentPath, 0, savedPath, 0, savedSize)
    }
    
    def restore(): Unit = {
      currentSize = savedSize
      System.arraycopy(savedPath, 0, currentPath, 0, savedSize)
    }
  }
}
