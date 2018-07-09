package be.ac.ucl.ingi.rls

import be.ac.ucl.ingi.rls.core._
import be.ac.ucl.ingi.rls.io._
import scala.collection.mutable.ArrayStack
import java.io.PrintWriter
import scala.util.Random

/*
 *  TrafficMatrixGenerator takes a .graph and a number, .XXXX.demands files
 *  For now, only gravity model are generated, normalized so that 
 */

object TrafficMatrixGenerator extends App {
  def print_help() = {
    println("Usage: TrafficMatrixGenerator input.graph [number of matrices, default 1]")
    sys.exit(1)
  }
  
  if (args.length < 1) print_help()
  
  final val debug = true
  val fileName = args(0).split('.').init.mkString(".")
  val nMatrices = if (args.length > 1) args(1).toInt else 1
  
  if (! (0 < nMatrices && nMatrices <= 1000)) print_help()
  
  val topologyData  = TopologyParser.parse(fileName + ".graph")
  
  val nNodes = topologyData.nodeLabels.length
  val Nodes = 0 until nNodes
  val nEdges = topologyData.edgeLabels.length
  
  // collect node sum of incoming capacities and sum of outgoing capacities
  val sumIncoming = Array.fill(nNodes)(0.0)
  val sumOutgoing = Array.fill(nNodes)(0.0)
  
  for (edge <- 0 until nEdges) {
    sumIncoming(topologyData.edgeDests(edge)) += topologyData.edgeCapacities(edge)
    sumOutgoing(topologyData.edgeSrcs(edge))  += topologyData.edgeCapacities(edge)
  }
  
  //
  val matrix = Array.ofDim[Int](nNodes, nNodes)
  
  val trafficIn  = Array.ofDim[Double](nNodes)
  val trafficOut = Array.ofDim[Double](nNodes)
  val meanEdgeCapacities = topologyData.edgeCapacities.map(_.toLong).sum / nEdges
  
  // population is along an exponential distribution with parameter incoming/outgoing capacity at node
  def generate_matrix_gravity(topologyData: TopologyData) = {
    for (i <- Nodes) {
//      trafficIn(i)  = -math.log(1 - Random.nextDouble()) // * sumIncoming(i)
//      trafficOut(i) = -math.log(1 - Random.nextDouble()) // * sumOutgoing(i)
      
      trafficIn(i)  = Random.nextDouble() * sumIncoming(i)
      trafficOut(i) = Random.nextDouble() * sumOutgoing(i)
      
//      trafficIn(i)  = math.exp(-Random.nextDouble()) * sumIncoming(i)
//      trafficOut(i) = math.exp(-Random.nextDouble()) * sumOutgoing(i)
    }
    
    val sumTrafficIn  = trafficIn.sum
    val sumTrafficOut = trafficOut.sum
    
    for (i <- Nodes) for (j <- Nodes) {
      matrix(i)(j) = (trafficOut(i) * trafficIn(j) / sumTrafficIn).toInt
//      matrix(i)(j) = (meanEdgeCapacities * trafficOut(i) * trafficIn(j)).toInt
    }
  }
  
  for (i <- 0 until nMatrices) {
    generate_matrix_gravity(topologyData)
    
    val outFileName = fileName + f".$i%04d" + ".demands"
    println(outFileName)
    val outFile = new PrintWriter(outFileName)
    
    outFile.println("DEMANDS")
    outFile.println("label src dest bw")
    
    var nDemand = 0
    for (i <- Nodes) for (j <- Nodes) if (i != j) {
      outFile.println(s"demand_$nDemand $i $j ${matrix(i)(j)}")
      nDemand += 1
    }
    
    outFile.close()
  }
}
