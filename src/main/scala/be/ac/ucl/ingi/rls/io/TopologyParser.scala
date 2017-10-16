package be.ac.ucl.ingi.rls.io

import scala.io.Source
import scala.collection.mutable.ArrayBuffer

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
    val capacities = ArrayBuffer[Double]()
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
        val bw = data(4).toDouble
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
}
