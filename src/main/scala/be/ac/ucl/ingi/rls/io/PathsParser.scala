package be.ac.ucl.ingi.rls.io

import scala.io.Source
import be.ac.ucl.ingi.rls._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayStack

class PathsData(val maxLoad: Double, pathMap: HashMap[Demand, Array[Node]], demands: Seq[Demand]) {
  def hasPath(demand: Demand) = pathMap.isDefinedAt(demand)
  def pathOf(demand: Demand) = pathMap(demand)
  val demandsWithPaths = demands.toArray
}

object PathsParser {
  private def expect(condition: Boolean, msg: String) = {
    if (!condition) println(msg)
  }
  
  final def parse(filepath: String, topologyData: TopologyData, demandsData: DemandsData): PathsData = {
    // map from labels to nodes
    val nameToNode = HashMap.empty[String, Node]
    
    val nNodes = topologyData.nodeLabels.length
    for (node <- 0 until nNodes) nameToNode += topologyData.nodeLabels(node) -> node
    
    // map from labels to demands
    val nameToDemand = HashMap.empty[String, Demand]
    val nDemands = demandsData.nDemands
    for (demand <- 0 until nDemands) nameToDemand += demandsData.demandLabels(demand) -> demand
    
    // read MAXLOAD section
    val lines = Source.fromFile(filepath).getLines
    expect(lines.next() == "MAXLOAD", "ERROR: expected MAXLOAD section")
    val maxLoad = lines.next().toDouble
    expect(lines.next() == "", "expected separator after MAXLOAD data")
    
    // read PATHS section
    expect(lines.next() == "PATHS", "ERROR: expected PATHS section")
    
    val paths = HashMap.empty[Demand, Array[Node]]
    val demands = ArrayStack.empty[Demand]
    
    var line = lines.next()
    while (line != "") {
      val items = line.split(' ')
      val demand = nameToDemand(items(0))
      val nodes = items.tail.map(nameToNode)
      
      paths += demand -> nodes
      demands.push(demand)
      
      line = lines.next()
    }
    
    // don't care about the rest, return now.
    new PathsData(maxLoad, paths, demands)
 }
}
