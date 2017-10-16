package be.ac.ucl.ingi.rls.io

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object DemandParser {

  final def parse(filepath: String): DemandsData = {

    val lines = Source.fromFile(filepath).getLines

    // Drop the first and the second lines
    lines.next
    lines.next

    // Demands
    val labels = ArrayBuffer[String]()
    val srcs = ArrayBuffer[Int]()
    val dests = ArrayBuffer[Int]()
    val bws = ArrayBuffer[Double]()

    // Parsing demands
    // label src dest bw
    var stop = lines.isEmpty
    while (!stop) {
      val line = lines.next
      if (line.isEmpty) stop = true
      else {
        val data = line.split(" ")
        val labelSize = data(0).length
        val label = data(0)
        val src = data(1).toInt
        val dest = data(2).toInt
        val bw = data(3).toDouble
        labels.append(label)        
        srcs.append(src)
        dests.append(dest)
        bws.append(bw)
        stop = lines.isEmpty
      }
    }

    new DemandsData(
      labels.toArray,
      srcs.toArray,
      dests.toArray,
      bws.toArray
    )
  }
}
