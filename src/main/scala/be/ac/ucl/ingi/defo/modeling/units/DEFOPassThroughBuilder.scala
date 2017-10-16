package be.ac.ucl.ingi.defo.modeling.units

import scala.collection.mutable.ArrayBuffer
import be.ac.ucl.ingi.defo.modeling.DEFONode
import be.ac.ucl.ingi.defo.modeling.DEFOPassThroughSeq
import be.ac.ucl.ingi.defo.modeling.DEFODemandConstraint
import be.ac.ucl.ingi.defo.modeling.DEFOPassThrough

class DEFOPassThroughBuilder(demandId: Int, nodes: Array[Int]) {

  private[this] val seqNodes = ArrayBuffer[Array[Int]](nodes)
  
  def then(node: DEFONode): DEFOPassThroughBuilder = {
    val n = Array(node.nodeId)
    seqNodes.append(n)
    this
  }
  
  def then(nodes: DEFONode*): DEFOPassThroughBuilder = then(nodes.toArray)
  
  def then(nodes: Traversable[DEFONode]): DEFOPassThroughBuilder = then(nodes.toArray)
  
  def then(nodes: Array[DEFONode]): DEFOPassThroughBuilder = {
    val n = nodes.map(_.nodeId)
    seqNodes.append(n)
    this
  }
  
  def toConstraint: DEFODemandConstraint = {
    if (seqNodes.length == 1) new DEFOPassThrough(demandId, seqNodes(0))
    else new DEFOPassThroughSeq(demandId, seqNodes.toArray)
  }
}

object DEFOPassThroughBuilder {
  implicit final def ptBuilder2Constraint(builder: DEFOPassThroughBuilder): DEFODemandConstraint = builder.toConstraint
}
