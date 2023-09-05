package be.ac.ucl.ingi.defo.parsers


import java.io.{BufferedWriter, FileWriter}

import scala.io.Source
import scala.collection.mutable.ArrayBuffer

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
    val bws = ArrayBuffer[Int]()

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
        val bw = data(3).toInt
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
  
  final def saveAs(filePath: String, demandsData: DemandsData): Unit = {
    
    val outFile = OutFile(filePath)
    val nDemands = demandsData.demandLabels.length
    
    require(demandsData.demandSrcs.length == nDemands)
    require(demandsData.demandDests.length == nDemands)
    require(demandsData.demandTraffics.length == nDemands)
    
    outFile.writeln("DEMANDS")
    outFile.writeln("label src dest bw")
    
    var i = 0
    while (i < nDemands) {
      val label = demandsData.demandLabels(i)
      val src = demandsData.demandSrcs(i)
      val dest = demandsData.demandDests(i)
      val traffic = demandsData.demandTraffics(i)
      outFile.writeln(s"$label $src $dest $traffic")
      i += 1
    }

    outFile.close()
  }
}



