package be.ac.ucl.ingi.defo.paths

import scala.collection.mutable.Queue
import be.ac.ucl.ingi.defo.core.Topology

class ConnectStructure(val graph: Topology) {
  
  private var nStrong: Int = 0

  private val reach = Array.tabulate(graph.nNodes)(n => {
    var reachable: Set[Int] = Set()
    val buffer = Queue.empty[Int]
    reachable += (n)
    buffer.enqueue(n)
    while (!buffer.isEmpty) {
      val n = buffer.dequeue
      val succ = graph.outNodes(n)
      for (s <- succ if !reachable.contains(s)) {
        buffer.enqueue(s)
        reachable += (s)
      }
    }
    
    if (reachable.size == graph.nNodes) nStrong += 1
    reachable
  })
  
  def isStronglyConnected: Boolean = nStrong == graph.nNodes
  def reachable(fromId: Int, toId: Int): Boolean = reach(fromId).contains(toId)
}

object ConnectStructure {
  def apply(graph: Topology, strongCheck: Boolean = false): ConnectStructure = new ConnectStructure(graph)
}
