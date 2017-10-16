package be.ac.ucl.ingi.defo.utils

import be.ac.ucl.ingi.defo.core.Topology

object Dijkstra {

  def shortestPathTo(nodeId: Int, topology: Topology, weights: IndexedSeq[Int]): (Array[List[Int]], Array[Int]) = {

    val dist: Array[Int] = Array.fill(topology.nNodes)(Int.MaxValue)
    val prevs: Array[List[Int]] = Array.fill(topology.nNodes)(List())
    val queue = new BinaryHeap[Int](topology.nNodes)
    
    dist(nodeId) = 0

    
    def reachable(v: Int): Set[Int] = {
      var visited = Set[Int]()
      def dfs(n: Int) {
        visited += n
        val inEdges = topology.inEdges(n)
        for (e <- inEdges) {
          val srcId = topology.edgeSrc(e)
          if (!visited.contains(srcId)) {
           dfs(srcId)
          }
        }
      }
      dfs(v)
      visited
    }
    
    // we only put in the queue nodes with a path to nodeId
    for (node <- reachable(nodeId)) {
      queue.enqueue(dist(node), node)
    }

    while (!queue.isEmpty) {

      val nodeId = queue.dequeue()
      val inEdges = topology.inEdges(nodeId)

      for (edgeId <- inEdges) {
        val srcId = topology.edgeSrc(edgeId)

        // Checks overflows
        //assert(dist(srcId) == Int.MaxValue || dist(srcId) <= Int.MaxValue - weights(edgeId))

        // Avoids overflow
        val newDist = if (dist(nodeId) > Int.MaxValue - weights(edgeId)) Int.MaxValue
        else dist(nodeId) + weights(edgeId)

        if (newDist < dist(srcId)) {
          queue.changeKey(dist(srcId), newDist, srcId)
          dist(srcId) = newDist
          prevs(srcId) = List(edgeId)
        } else if (newDist == dist(srcId)) {
          prevs(srcId) = edgeId :: prevs(srcId)
        }
      }
    }

    (prevs, dist)
  }
}
