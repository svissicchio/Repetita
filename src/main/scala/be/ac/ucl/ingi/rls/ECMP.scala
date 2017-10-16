package be.ac.ucl.ingi.rls

/*
 * To route traffic from src to dest, ECMP uses all shortest paths.
 * At every node, it splits the upcoming traffic equally among successors on the DAG of shortest paths to dest. 
 */
class ECMP(nNodes: Int, nEdges: Int, sp: ShortestPaths) {
  val nEdgesToModify   = Array.ofDim[Int](nNodes, nNodes)
  val edgesToModify    = Array.ofDim[Array[Edge]](nNodes, nNodes)    // edges to modify when adding flow from source to destination
  val fractionToModify = Array.ofDim[Array[Double]](nNodes, nNodes)  // fraction of the flow to put on each corresponding edge
  
  private[this] val toRoute = Array.fill(nNodes)(0.0)
  
  initialize()
    
  private def initialize() = {
    val toRoute = Array.fill(nNodes)(0.0)
    val fractionDAG = Array.ofDim[Double](nEdges)
    val edgesDAG = Array.ofDim[Int](nEdges)  // fraction of the flow on edges, basically a stack with nEdgesDAG pointing to top
    
    var nEdgesDAG = 0
    
    val Nodes = 0 until nNodes
    for (source <- Nodes) for (destination <- Nodes) {
      nEdgesDAG = 0
      
      val ordering = sp.topologicalOrdering
      var nOrdering = sp.makeTopologicalOrdering(source, destination)
      
      // simulate the routing of of flow of size 1.0 from source to destination
      toRoute(source) = 1.0
      while (nOrdering > 0) {
        nOrdering -= 1
        val node = ordering(nOrdering)
        
        var pSucc = sp.nSuccessors(destination)(node)
        val amountToRoute = toRoute(node) / pSucc
        while (pSucc > 0) {
          pSucc -= 1
          val succNode = sp.successorNodes(destination)(node)(pSucc)
          toRoute(succNode) += amountToRoute
          
          val succEdge = sp.successorEdges(destination)(node)(pSucc)
          edgesDAG(nEdgesDAG) = succEdge
          fractionDAG(nEdgesDAG) = amountToRoute
          nEdgesDAG += 1
        }
        
        toRoute(node) = 0.0
      }
      
      // copy edges from temp array to final one
      nEdgesToModify(source)(destination) = nEdgesDAG
      edgesToModify(source)(destination) = Array.ofDim[Int](nEdgesDAG)
      System.arraycopy(edgesDAG, 0, edgesToModify(source)(destination), 0, nEdgesDAG)
      
      // copy flow from temp array, reset array as we go
      fractionToModify(source)(destination) = Array.ofDim[Double](nEdgesDAG)
      var pEdge = nEdgesDAG
      while (pEdge > 0) {
        pEdge -= 1
        val edge = edgesDAG(pEdge)
        fractionToModify(source)(destination)(pEdge) = fractionDAG(pEdge)
      }
    }    
  }
}