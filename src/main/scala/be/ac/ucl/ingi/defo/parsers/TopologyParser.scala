package be.ac.ucl.ingi.defo.parsers

import scala.io.Source
import scala.collection.mutable.ArrayBuffer
import oscar.util.OutFile

object TopologyParser {
  
  def parse(filePath: String): TopologyData = {
    
    val lines = Source.fromFile(filePath).getLines

    // Drop the first and the second lines
    lines.next
    lines.next

    // Nodes
    val nodeLabels = ArrayBuffer[String]()
    val coordinates = ArrayBuffer[(Double, Double)]()

    // Edges
    val edgeLabels = ArrayBuffer[String]()
    val srcs = ArrayBuffer[Int]()
    val dests = ArrayBuffer[Int]()
    val weights = ArrayBuffer[Int]()
    val capacities = ArrayBuffer[Int]()
    val latencies = ArrayBuffer[Int]()

    // Parsing nodes
    // label x y
    var stop = lines.isEmpty
    while (!stop) {
      val line = lines.next
      if (line.isEmpty) stop = true
      else {
        val data = line.split(" ")
        val labelSize = data(0).length
        val label = data(0)
        val x = data(1).toDouble
        val y = data(2).toDouble
        nodeLabels.append(label)
        coordinates.append((x, y))
        stop = lines.isEmpty
      }
    }
    
    // Drop the first and the second lines
    lines.next
    lines.next

    // Parsing edges
    // src dest weight bw delay
    stop = lines.isEmpty
    while (!stop) {
      val line = lines.next
      if (line.isEmpty) stop = true
      else {
        val data = line.split(" ")
        val label = data(0)
        val src = data(1).toInt
        val dest = data(2).toInt
        val weight = data(3).toInt
        val bw = data(4).toInt
        val delay = data(5).toDouble.round.toInt
        edgeLabels.append(label)
        srcs.append(src)
        dests.append(dest)
        weights.append(weight)
        capacities.append(bw)
        latencies.append(delay)
        stop = lines.isEmpty
      }
    }

    new TopologyData(
      nodeLabels.toArray,
      coordinates.toArray,
      edgeLabels.toArray,
      srcs.toArray,
      dests.toArray,
      weights.toArray,
      capacities.toArray,
      latencies.toArray
    )
  }

  def saveAs(filePath: String, topologyData: TopologyData): Unit = {
    
    val outFile = OutFile(filePath)
    val nEdges = topologyData.edgeDests.length
    val nNodes = topologyData.nodeLabels.length
    
    require(topologyData.nodeCoordinates.length == nNodes)
    require(topologyData.edgeCapacities.length == nEdges)
    require(topologyData.edgeWeights.length == nEdges)
    require(topologyData.edgeSrcs.length == nEdges)
    require(topologyData.edgeLatencies.length == nEdges)
    
    outFile.writeln("NODES")
    outFile.writeln("label x y")
    
    var i = 0
    while (i < nNodes) {
      val label = topologyData.nodeLabels(i)
      val (x, y) = topologyData.nodeCoordinates(i)
      outFile.writeln(s"$label $x $y")
      i += 1
    }
    
    outFile.writeln()
    outFile.writeln("EDGES")
    outFile.writeln("label src dest weight bw delay")
    
    i = 0
    while (i < nEdges) {
      val label = topologyData.edgeLabels(i)
      val src = topologyData.edgeSrcs(i)
      val dest = topologyData.edgeDests(i)
      val latency = topologyData.edgeLatencies(i)
      val weight = topologyData.edgeWeights(i)
      val bw = topologyData.edgeCapacities(i)
      outFile.writeln(s"$label $src $dest $weight $bw $latency")
      i += 1
    }
    
    outFile.close()
  }

}
