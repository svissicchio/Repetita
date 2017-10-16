package be.ac.ucl.ingi.defo.core

import scala.collection.mutable.ArrayBuffer
import scala.Range

class Topology(
  // Parameters
  @inline final val nodeLabels: Array[String],
  @inline final val edgeLabels: Array[String],
  private[this] val _edgeSrc: Array[Int],
  private[this] val _edgeDest: Array[Int],
  private[this] val _outEdges: Array[Array[Int]],
  private[this] val _inEdges: Array[Array[Int]],
  private[this] val _outNodes: Array[Array[Int]],
  private[this] val _inNodes: Array[Array[Int]]) {
  // Functions
  @inline final val nNodes: Int = nodeLabels.length
  @inline final val nEdges: Int = edgeLabels.length
  @inline final val Edges: Range = Range(0, nEdges)
  @inline final val Nodes: Range = Range(0, nNodes)
  @inline final def edgeSrc(edgeId: Int): Int = _edgeSrc(edgeId)
  @inline final def edgeDest(edgeId: Int): Int = _edgeDest(edgeId)
  @inline final def outEdges(nodeId: Int): Array[Int] = _outEdges(nodeId)
  @inline final def inEdges(nodeId: Int): Array[Int] = _inEdges(nodeId)
  @inline final def outNodes(nodeId: Int): Array[Int] = _outNodes(nodeId)
  @inline final def inNodes(nodeId: Int): Array[Int] = _inNodes(nodeId)
}

object Topology {
  
  def apply(edgeSrcs: Array[Int], edgeDests: Array[Int]): Topology = {
    // Inner structure
    val nEdges = edgeSrcs.length
    val outEdges = Array.fill(nEdges)(new ArrayBuffer[Int])
    val inEdges = Array.fill(nEdges)(new ArrayBuffer[Int])
    val outNodes = Array.fill(nEdges)(new ArrayBuffer[Int])
    val inNodes = Array.fill(nEdges)(new ArrayBuffer[Int])
    // Edge labels
    val edgeLabels = new Array[String](nEdges)
    // Build structure
    var i = nEdges
    var nNodes = 0
    while (i > 0) {
      i -= 1
      val src = edgeSrcs(i)
      val dest = edgeDests(i)
      outEdges(src).append(i)
      inEdges(dest).append(i)
      outNodes(src).append(dest)
      inNodes(dest).append(src)
      if (src > nNodes) nNodes = src
      if (dest > nNodes) nNodes = dest
      edgeLabels(i) = s"Link_N${src}_N${dest}"
    }
    // Node labels
    val nodeLabels = Array.tabulate(nNodes+1)(i => s"N$i")
    new Topology(
      nodeLabels,
      edgeLabels,
      edgeSrcs,
      edgeDests,
      outEdges.map(_.toArray),
      inEdges.map(_.toArray),
      outNodes.map(_.toArray),
      inNodes.map(_.toArray)
    )
  }
  
  def apply(edgeSrcs: Array[Int], edgeDests: Array[Int], nodeLabels: Array[String], edgeLabels: Array[String]): Topology = {
    // Inner structure
    val nEdges = edgeSrcs.length
    val outEdges = Array.fill(nEdges)(new ArrayBuffer[Int])
    val inEdges = Array.fill(nEdges)(new ArrayBuffer[Int])
    val outNodes = Array.fill(nEdges)(new ArrayBuffer[Int])
    val inNodes = Array.fill(nEdges)(new ArrayBuffer[Int])
    // Build structure
    var i = nEdges
    while (i > 0) {
      i -= 1
      val src = edgeSrcs(i)
      val dest = edgeDests(i)
      outEdges(src).append(i)
      inEdges(dest).append(i)
      outNodes(src).append(dest)
      inNodes(dest).append(src)
    }
    new Topology(
      nodeLabels,
      edgeLabels,
      edgeSrcs,
      edgeDests,
      outEdges.map(_.toArray),
      inEdges.map(_.toArray),
      outNodes.map(_.toArray),
      inNodes.map(_.toArray)
    )
  }
}
