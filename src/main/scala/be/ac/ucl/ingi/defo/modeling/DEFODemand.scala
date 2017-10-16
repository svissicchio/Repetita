package be.ac.ucl.ingi.defo.modeling

import be.ac.ucl.ingi.defo.modeling.units.DEFOPassThroughBuilder
import be.ac.ucl.ingi.defo.modeling.variables.DEFOLatencyVar
import be.ac.ucl.ingi.defo.modeling.variables.DEFOLengthVar

class DEFODemand(final val demandId: Int, final override val toString: String) {

  /**  */
  final lazy val latency: DEFOLatencyVar = new DEFOLatencyVar(demandId)
  
  /**  */
  final lazy val length: DEFOLengthVar = new DEFOLengthVar(demandId)

  /**  */
  final def passThrough(nodes: DEFONode*): DEFOPassThroughBuilder = passThrough(nodes.toArray)
  
  /**  */
  final def passThrough(node: DEFONode): DEFOPassThroughBuilder = passThrough(Array(node))

  /**  */  
  final def passThrough(nodes: Traversable[DEFONode]): DEFOPassThroughBuilder = passThrough(nodes.toArray)

  /**  */
  final def passThrough(nodes: Array[DEFONode]): DEFOPassThroughBuilder = {
    val nodeIds = nodes.map(_.nodeId)
    new DEFOPassThroughBuilder(demandId, nodeIds)
  }

  /**  */
  final def avoidNode(node: DEFONode): DEFOConstraint = {
    new DEFOAvoidNode(demandId, node.nodeId)
  }

  /**  */
  final def avoidEdge(edge: DEFOEdge): DEFOConstraint = {
    new DEFOAvoidEdge(demandId, edge.edgeId)
  }
}
