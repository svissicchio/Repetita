package oscar.network.utils

class Node(val id: Int, val label: String) {
  override val toString: String = label
}

class Edge(val id: Int, val src: Node, val dest: Node) {
  override val toString: String = s"$src -> $dest"
}

class DGraph(nodeNames: Seq[String], capacitedEdges: Seq[(Int, Int)]) {

  final val nNodes = nodeNames.size
  final val nEdges = capacitedEdges.size
  
  final val Nodes = 0 until nNodes
  final val Edges = 0 until nEdges
  
  // Ensures a fast access to nodes
  private final val _nodes: Array[Node] = nodeNames.zipWithIndex.map {
    case (name, id) => new Node(id, name)
  }.toArray
  
  // Ensures a fast access to edges
  private final val _edges: Array[Edge] = capacitedEdges.zipWithIndex.map {
    case ((src, dest), id) => {
      new Edge(id, _nodes(src), _nodes(dest))
    }
  }.toArray
  
  // Ensures a fast access to adjacent edges
  private final val _outEdges: Array[IndexedSeq[Int]] = Array.tabulate(nNodes)(n => Edges.filter(e => _edges(e).src == _nodes(n))) 
  private final val _inEdges: Array[IndexedSeq[Int]] = Array.tabulate(nNodes)(n => Edges.filter(e => _edges(e).dest == _nodes(n)))
  private final val _adjEdges: Array[IndexedSeq[Int]] = Array.tabulate(nNodes)(n => _inEdges(n) ++ _outEdges(n))
  
  // Ensures a fast access to adjacent nodes
  private final val _outNodes: Array[IndexedSeq[Int]] = _outEdges.map(_.map(_edges(_).dest.id))
  private final val _inNodes: Array[IndexedSeq[Int]] = _inEdges.map(_.map(_edges(_).src.id))
  private final val _adjNodes: Array[IndexedSeq[Int]] = Array.tabulate(nNodes)(n => _inNodes(n) ++ _outNodes(n))

  final def edges: Array[Edge] = _edges
  final def nodes: Array[Node] = _nodes

  @inline final def edge(edgeId: Int): Edge = _edges(edgeId)
  @inline final def node(nodeId: Int): Node = _nodes(nodeId)
  
  final def outEdges(node: Node): Seq[Edge] = outEdges(node.id)
  final def inEdges(node: Node): Seq[Edge] = inEdges(node.id)
  final def adjEdges(node: Node): Seq[Edge] = adjEdges(node.id)
  
  final def outNodes(node: Node): Seq[Node] = outNodes(node.id)
  final def inNodes(node: Node): Seq[Node] = inNodes(node.id)
  final def adjNodes(node: Node): Seq[Node] = adjNodes(node.id)
  
  final def outEdges(nodeId: Int): Seq[Edge] = _outEdges(nodeId).map(_edges(_))
  final def inEdges(nodeId: Int): Seq[Edge] = _inEdges(nodeId).map(_edges(_))
  final def adjEdges(nodeId: Int): Seq[Edge] = _adjEdges(nodeId).map(_edges(_))
  
  final def outNodes(nodeId: Int): Seq[Node] = _outNodes(nodeId).map(_nodes(_))
  final def inNodes(nodeId: Int): Seq[Node] = _inNodes(nodeId).map(_nodes(_))
  final def adjNodes(nodeId: Int): Seq[Node] = _adjNodes(nodeId).map(_nodes(_))
  
  final def outEdgesId(node: Node): Seq[Int] = _outEdges(node.id)
  final def inEdgesId(node: Node): Seq[Int] = _inEdges(node.id)
  final def adjEdgesId(node: Node): Seq[Int] = _adjEdges(node.id)
  
  final def outNodesId(node: Node): Seq[Int] = _outNodes(node.id)
  final def inNodesId(node: Node): Seq[Int] = _inNodes(node.id)
  final def adjNodesId(node: Node): Seq[Int] = _adjNodes(node.id)
  
  final def outEdgesId(nodeId: Int): Seq[Int] = _outEdges(nodeId)
  final def inEdgesId(nodeId: Int): Seq[Int] = _inEdges(nodeId)
  final def adjEdgesId(nodeId: Int): Seq[Int] = _adjEdges(nodeId)
  
  final def outNodesId(nodeId: Int): Seq[Int] = _outNodes(nodeId)
  final def inNodesId(nodeId: Int): Seq[Int] = _inNodes(nodeId)
  final def adjNodesId(nodeId: Int): Seq[Int] = _adjNodes(nodeId)
}
