package be.ac.ucl.ingi.rls.preprocessing

import be.ac.ucl.ingi.rls.ShortestPaths
import be.ac.ucl.ingi.rls.ShortestPaths
import be.ac.ucl.ingi.rls._

object DetoursFilter {
  private val ratio = 1.0
  
  def apply(sp: ShortestPaths)(debug: Boolean): Array[Node] = {
    val nNodes = sp.nSuccessors.length
    
    val usageCounter = Array.fill(nNodes)(0)
    val pathCount = Array.fill(nNodes)(0)
    val Nodes = 0 until nNodes
    
    for (dest <- Nodes) {
      for (node <- Nodes) pathCount(node) = 1      

      sp.makeTopologicalOrdering(dest)
      val ordering = sp.topologicalOrdering
      val successors = sp.successorNodes(dest)
      
      var pOrder = nNodes
      while (pOrder > 0) {
        pOrder -= 1
        val node = ordering(pOrder)
        var pSucc = sp.nSuccessors(dest)(node)
        while (pSucc > 0) {
          pSucc -= 1
          val succ = successors(node)(pSucc)
          pathCount(succ) += pathCount(node)
        }
      }
      
      for (node <- Nodes) usageCounter(node) += pathCount(node) 
    }
    
    val selectableUsage = usageCounter.max * ratio
    
    val sortedDetours = Array.tabulate(nNodes)(identity).sortBy(usageCounter)
    val filteredDetours = sortedDetours.slice(0, (ratio * sortedDetours.length).toInt)
    
    if (debug) println(s"Selected a ratio of ${filteredDetours.length.toDouble / nNodes} nodes as acceptable detours")
    
    filteredDetours
  }
}
