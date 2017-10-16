package be.ac.ucl.ingi.rls

import scala.xml.XML
import scala.xml.Elem
import java.io.PrintWriter

object GraphML2DEFO extends App {
  def print_help() = {
    println("Usage: GraphML2DEFO file.graphml output.graph")
    sys.exit(1)
  }
  
  if (args.length < 2) print_help()
    
  val graphml = XML.loadFile(args(0))
  
  // every node is basically a key -> data map
  // make inverse mapping from names ("label", "Latitude", ...) to keys ("d32", "d8", ...)
  val keys = graphml \ "key"
  
  val nodeKeys = keys filter(_ \@ "for" == "node")
  val nodeKeysMap = nodeKeys.map(node => (node \@ "attr.name") -> (node \@ "id")).toMap
  
  val edgeKeys = keys filter(_ \@ "for" == "edge")
  val edgeKeysMap = edgeKeys.map(node => (node \@ "attr.name") -> (node \@ "id")).toMap
  
  // extract keys of interest
  val keyNodeLabel = nodeKeysMap.getOrElse("label", "")
  val keyNodeLatitude = nodeKeysMap.getOrElse("Latitude", "")
  val keyNodeLongitude = nodeKeysMap.getOrElse("Longitude", "")
  val keyNodeID = nodeKeysMap.getOrElse("id", "")
  
  val keyEdgeLinkLabel = edgeKeysMap.getOrElse("LinkLabel", "")
  val keyEdgeLinkSpeedRaw = edgeKeysMap.getOrElse("LinkSpeedRaw", "")
  val keyEdgeId = edgeKeysMap.getOrElse("id", "")

  
  // extract general info about graph
  val graph = graphml \ "graph"
  val isDirected = (graph \@ "edgedefault") == "directed"
  assert(isDirected)
  
  // extract nodes and edges
  def dataSequence2Map(xmlNode: scala.xml.Node) = {
    val data = xmlNode \ "data"
    val pairs = data map (datum => (datum \@ "key") -> datum.text)
    pairs.toMap
  }
  
  val nodes = (graph \ "node").toArray 
  val nodeData = nodes map dataSequence2Map
  val id2index = Array.tabulate(nodes.length)(i => (nodes(i) \@ "id") -> i).toMap 
  
  val edges = (graph \ "edge").toArray 
  val edgeData = edges map dataSequence2Map
  val edgesSrc  = edges map (edge => id2index(edge \@ "source"))
  val edgesDest = edges map (edge => id2index(edge \@ "target"))
  
  val nNodes = nodeData.length
  val nEdges = edgeData.length
  val Edges = 0 until nEdges
  
  // remove unconnected nodes
  val Nodes = generateConnected(nNodes, edgesSrc, edgesDest)
  if (Nodes.size < nNodes) println(s"Removed ${nNodes - Nodes.size} unconnected nodes") 
  
  val index2defo = Array.tabulate(Nodes.length)(i => Nodes(i) -> i).toMap
  
  def generateConnected(n: Int, src: Array[Int], dest: Array[Int]): Array[Int] = {
    val represent = Array.tabulate(n)(identity)
    
    // union find compression
    def representing(i: Int): Int = {
      if (represent(i) == i) i
      else {
        val j = representing(represent(i))
        represent(i) = j
        j
      }
    }
    
    // union find merge
    def merge(a: Int, b: Int) = {
      val ra = representing(a)
      val rb = representing(b)
      
      val ma = math.min(ra, rb)
      val mb = math.max(ra, rb)
      
      represent(mb) = ma      
    }
    
    // merge all edges
    var edge = src.length
    while (edge > 0) {
      edge -= 1
      merge(src(edge), dest(edge))
    }
    
    // count which part has the most representants
    val count = Array.fill(n)(0)
    var node = n
    while (node > 0) {
      node -= 1
      count(representing(node)) += 1
    }
    
    var bestRep = -1
    var bestCount = 0
    node = n
    while (node > 0) {
      node -= 1
      if (count(node) > bestCount) {
        bestCount = count(node)
        bestRep = node
      }
    }
    
    // select only nodes of the best rep
    Array.tabulate(n)(identity).filter(node => representing(node) == bestRep)    
  }
  
  // extract position from nodes info
  val nodeLatitude  = Array.fill(nNodes)(0.0)
  val nodeLongitude = Array.fill(nNodes)(0.0)
  val nodeHasPosition = Array.ofDim[Boolean](nNodes)
  
  for (i <- Nodes) {
    extractPosition(nodeData(i), keyNodeLongitude, keyNodeLatitude) match {
      case None => nodeHasPosition(i) = false
      case Some((lon, lat)) => 
        nodeHasPosition(i) = true
        nodeLongitude(i) = lon
        nodeLatitude(i) = lat
    }
    
    // if (nodeHasPosition(i)) println(s"Node $i has position " + nodeLongitude(i) + " " + nodeLatitude(i))
    // else println(s"Node $i has no position")
  }
  
  // extract bandwidth info for edges
  val edgeBandwidth = Array.fill(nEdges)(0.0)
  val edgeHasBandwidth = Array.ofDim[Boolean](nEdges)
  var nEdgeHasBandwidth = 0
  
  for (j <- Edges) {
    extractBandwidth(edgeData(j), keyEdgeLinkLabel, keyEdgeLinkSpeedRaw) match {
      case None => edgeHasBandwidth(j) = false
      case Some(bw) =>
        nEdgeHasBandwidth += 1
        edgeHasBandwidth(j) = true
        edgeBandwidth(j) = bw
    }
  }
  
  // rescale link capacities
  val maxDiscrepancy = 20.0
  val maxBandwidth = edgeBandwidth.max
  for (edge <- Edges) {
    if (edgeHasBandwidth(edge) && edgeBandwidth(edge) * maxDiscrepancy < maxBandwidth) {
      println(s"Edge $edge has relative bandwidth ${edgeBandwidth(edge) / maxBandwidth} < ${1.0 / maxDiscrepancy}, correcting...")
      edgeBandwidth(edge) = maxBandwidth / maxDiscrepancy      
    }
  }
  
  // fill no info bandwidth edges with mean/random from other edges; if all edges have no info, make uniform network
  if (nEdgeHasBandwidth == 0) {  // no info, 1Gbps Ethernet for everyone!
    for (j <- Edges) {
      edgeBandwidth(j) = 1e6
    }    
  }
  else if (nEdgeHasBandwidth < nEdges) {  // some info
    val meanBandwidths = edgeBandwidth.sum / nEdgeHasBandwidth
    for (j <- Edges) if (!edgeHasBandwidth(j)) {
      edgeBandwidth(j) = meanBandwidths
    }
  }
  
  // fill lag from position of extremities, if no info mean/random lag of links
  val edgeLag    = Array.fill(nEdges)(0.0)
  val edgeHasLag = Array.fill(nEdges)(false) 
  var nEdgesHasLag = 0
  
  for (j <- Edges) {
    val src  = edgesSrc(j)
    val dest = edgesDest(j)
    
    if (nodeHasPosition(src) && nodeHasPosition(dest)) {
      nEdgesHasLag += 1
      edgeHasLag(j) = true
      edgeLag(j) = positions2Lag(nodeLongitude(src), nodeLatitude(src), nodeLongitude(dest), nodeLatitude(dest))
//      println(s"Edge $j has lag ${edgeLag(j)} going from ${(nodeLongitude(src), nodeLatitude(src))} to ${(nodeLongitude(dest), nodeLatitude(dest))}")
    }
  }
  
  if (nEdgesHasLag == 0) {              // no info, 10ms for everyone
    for (j <- Edges) edgeLag(j) = 10
  }
  else if (nEdgesHasLag < nEdges) {     // some info, fill with mean lag
    val meanLag = edgeLag.sum / nEdgesHasLag
    for (j <-Edges) if (!edgeHasLag(j)) edgeLag(j) = meanLag
  }
  
  // fill weight has different heuristics: unary weight, inverse of capacity.
  val edgeWeight = Array.fill(nEdges)(1)  // unary for now
  
  val invCapa = true
  if (invCapa) {
    val maxCapa = edgeBandwidth.max
    val minCapa = edgeBandwidth.min
    if (maxCapa / minCapa > 100) println(s"Warning: model ${args(0)} has relative capacities extremum ${maxCapa / minCapa}")
    for (edge <- 0 until edgeWeight.length) edgeWeight(edge) = (10 * maxCapa / edgeBandwidth(edge)).toInt
  }
  
  // print out network!
  val outFile = new PrintWriter(args(1))

  outFile.println("NODES " + Nodes.length)
  outFile.println("label x y")
  
  for (i <- Nodes) {
    val label = nodeData(i).getOrElse(keyNodeLabel, "node__" + i)
    val id = nodes(i) \@ "id"
    val finalLabel = (id + '_' + label).replace(' ', '_')
    outFile.println(finalLabel + " " + nodeLongitude(i) + " " + nodeLatitude(i))
  }
  
  outFile.println
  
  outFile.println("EDGES " + nEdges)
  outFile.println("label src dest weight bw delay")
  
  var edgeCounter = 0
  for (j <- Edges) {
    val src  = edgesSrc(j)
    val dest = edgesDest(j)
    
    if (index2defo.isDefinedAt(src) && index2defo.isDefinedAt(dest)) {
      // output 2 symmetric directed edges (we asserted isDirected above)
      outFile.println(s"edge_$edgeCounter ${index2defo(src)} ${index2defo(dest)} ${edgeWeight(j)} ${edgeBandwidth(j).toInt} ${edgeLag(j).toInt}")
      edgeCounter += 1
      outFile.println(s"edge_$edgeCounter ${index2defo(dest)} ${index2defo(src)} ${edgeWeight(j)} ${edgeBandwidth(j).toInt} ${edgeLag(j).toInt}")
      edgeCounter += 1
    }
  }
  
  outFile.close()

  // from node as map of xml node's data, return Longitude/Latitude
  def extractPosition(node: Map[String, String], keyLongitude: String, keyLatitude: String): Option[(Double, Double)] = {
    (node.get(keyLongitude), node.get(keyLatitude)) match {
      case (Some(lon), Some(lat)) => Option((lon.toDouble, lat.toDouble))
      case _ => None      
    }    
  }  
  
  // extract bandwidth info from edge represented by map of xml node's data.
  // return result in kbps
  def extractBandwidth(edge: Map[String, String], keyLinkLabel: String, keyLinkSpeedRaw: String): Option[Double] = {
    if (edge.isDefinedAt(keyLinkSpeedRaw)) return Option(edge(keyLinkSpeedRaw).toDouble / 1e3)
    
    if (!edge.isDefinedAt(keyLinkLabel)) return None
    
    val linkLabel = edge(keyLinkLabel)
    
    /*
     *  LinkLabel usually has the info, examples:
     *    10G Ethernet
     *    < 155 Mbps
     *    < 2.5 Gbps
     *    10Gbps
     *  
     *  not taken care of (yet?):
     *    Leased Wavelength/Managed Service
     *    DWDM
     *    72 Lambda ROADM Network
     *    Planned Wavelength  
     */
    
    val namedOC        = """OC-(\d+).*""".r
    val namedOCNoDash  = """OC(\d+).*""".r
    val namedSTM       = """STM-(\d+).*""".r
    val namedSTMMult   = """STM-(\d+)x(\d\d?).*""".r
    
    val named = linkLabel match {
      // OC-n is n * 51.84 Mbps
      case namedOC(n)       => Option(n.toDouble * 51840)
      case namedOCNoDash(n) => Option(n.toDouble * 51840)
      
      // STM-n is n * 155.520 Mbit/s
      case namedSTM(n) =>           Option(n.toDouble * 155520)
      case namedSTMMult(n, mult) => Option(n.toDouble * 155520 * mult.toDouble)

      case "T1" => Option(1544.0)
      case _ => None
    }
    
    if (named.isDefined) return named
    
    // change Mb/s to Mbps, Gbit/s => Gbps
    val removeSlashSecond1 = """(.*)([MG])b/s(.*)""".r 
    val removeSlashSecond2 = """(.*)([MG])bit/s(.*)""".r
    val removeSlashSecond3 = """(.*)([MG])B/s(.*)""".r
    
    val clean1 = linkLabel match {
      case removeSlashSecond1(beginning, modif, end) => beginning + modif + "bps" + end
      case removeSlashSecond2(beginning, modif, end) => beginning + modif + "bps" + end
      case removeSlashSecond3(beginning, modif, end) => beginning + modif + "Bps" + end
      case _ @ s => s
    }
    
    val removeDash = """(.*?)(\d+)-(\d+)(.*)""".r
    val clean2 = clean1 match {
      case removeDash(begin, number1, number2, end) => begin + number2 + end
      case _ @ s => s
    }
    
    val removeLessThan = """<=?(.*)""".r
    val removeGreaterThan = """>=?(.*)""".r
    val clean3 = clean2 match {
      case removeLessThan(other) => other
      case removeGreaterThan(other) => other
      case _ @ s => s
    }
    
    val simplifyProducts = """(.*?)(\d+)\s?[x\*]\s?(\d+)(.*)""".r
    val clean4 = clean3 match {
      case simplifyProducts(begin, first, second, end) => begin + (first.toInt * second.toInt) + end
      case _ @ s => s
    }
    
    val mbps = """.*?(\d+(\.\d+)?)\s*Mbps.*""".r
    val gbps = """.*?(\d+(\.\d+)?)\s*Gbps.*""".r
    val gBps = """.*?(\d+(\.\d+)?)\s*GBps.*""".r

    val translated = clean4 match {
      case mbps(number, no) => Option(number.toDouble * 1e3)
      case gbps(number, no) => Option(number.toDouble * 1e6)
      case gBps(number, no) => Option(number.toDouble * 1e6 * 8)
      case _ => None
    }
    
    translated
  }
  
  // compute estimation of delay from position 1 to position 2 in µs
  // the cable goes along a great circle of the sphere, using Haversine distance
  // the signal goes at the speed of light, on Earth 300 000 km/s = 15 pi radians per sec
  def positions2Lag(lng1: Double, lat1: Double, lng2: Double, lat2: Double): Double = {
    import scala.math._
    
    val degreeToRadian = Pi / 180.0
    
    val lng1r = lng1 * degreeToRadian
    val lat1r = lat1 * degreeToRadian
    val lng2r = lng2 * degreeToRadian
    val lat2r = lat2 * degreeToRadian    
    
    // compute haversine distance, thx Santa
    val lat = lat2r - lat1r
    val lng = lng2r - lng1r
    
    val sinlat = sin(lat / 2)
    val sinlng = sin(lng / 2)
    val d = sinlat * sinlat + cos(lat1r) * cos(lat2r) * (sinlng * sinlng)
    val angle = asin(sqrt(d))
    
    // println(s"($lng1, $lat1) -> ($lng2, $lat2): $angle")
    
    val delayPerRadian = 1e6 / (15 * Pi)  // 1e6 µs per 15 pi radians
    angle * delayPerRadian + 5    // adding 5 µs for link delay
  }
}
