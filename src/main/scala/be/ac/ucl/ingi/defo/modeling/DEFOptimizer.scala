package be.ac.ucl.ingi.defo.modeling

import be.ac.ucl.ingi.defo.core.DEFOInstance
import be.ac.ucl.ingi.defo.core.CoreSolver
import be.ac.ucl.ingi.defo.modeling.units.TimeUnit
import be.ac.ucl.ingi.defo.modeling.units.LoadUnit
import be.ac.ucl.ingi.defo.modeling.units.RelativeUnit
import scala.collection.mutable.Set
import be.ac.ucl.ingi.defo.paths.SimplePaths
import be.ac.ucl.ingi.defo.DEFOInstance
import be.ac.ucl.ingi.defo.paths.ECMPStructure
import java.io.PrintWriter

class DEFOptimizer(instance: DEFOInstance, verbose: Boolean, statsFile: Option[PrintWriter]) {
  final val core = new CoreSolver(instance, verbose, statsFile)
  private[this] val topology = instance.topology
  
  def firstSolution = core.searchInitialSol()

  def solve(timeLimit: TimeUnit, maxLoad: RelativeUnit): Unit = {

    // Find first solution
    val t0 = System.currentTimeMillis()
    core.searchInitialSol()
    val preTime = System.currentTimeMillis() - t0

    // Optimization
    val t1 = System.currentTimeMillis()
    core.search(timeLimit.value, maxLoad.value)
    val optTime = System.currentTimeMillis() - t1

    // Process results
    val sortedEdges = (0 until topology.nEdges).sortBy(e => -core.initialRates(e)).toArray

    val sota = sortedEdges.map(core.initialRates(_))
    val srte = sortedEdges.map(core.bestRates(_))

    val filteredPath = core.bestPaths.filter(_.length > 2)

    val simplePaths = new SimplePaths(topology, instance.weights)
    
    val nSimplePaths = filteredPath.map(path => {
      var n = 1
      var i = 1
      while (i < path.length) {
        val src = path(i - 1)
        val dest = path(i)
        val nPaths = simplePaths.nPaths(src, dest)
        assert(nPaths > 0)
        n *= nPaths
        i += 1
      }
      n
    })

    val sumPaths = filteredPath.map(path => {
      var n = 0
      var i = 1
      while (i < path.length) {
        val src = path(i - 1)
        val dest = path(i)
        val nPaths = simplePaths.nPaths(src, dest)
        assert(nPaths > 0)
        if (nPaths > 1) n += nPaths
        i += 1
      }
      if (n == 0) 1 else n
    })
    
    val ecmpStruct = ECMPStructure(topology, instance.capacities, instance.latencies)
    
    val numberOfNodes = filteredPath.map(path => {
      val visited = Set[Int]()
      var i = 1
      while (i < path.length) {
        val src = path(i - 1)
        val dest = path(i)
        val edges = ecmpStruct.links(src, dest)
        for (edge <- edges) {
          val src = topology.edgeSrc(edge)
          val dest = topology.edgeDest(edge)
          visited.add(src)
          visited.add(dest)
        }
        i += 1
      }
      visited.size
    })

    if (verbose) {
      println
      println("Optimization completed")
      println("----------------------")
      println("number of nodes     : " + topology.nNodes)
      println("number of edges     : " + topology.nEdges)
      println("number of demands   : " + instance.demandDests.length)
      println("first solution time : " + preTime)
      println("optimization time   : " + optTime)
      println("initial max maxLinkLoad    : " + core.initialRates.max)
      println("final max maxLinkLoad      : " + core.bestRates.max)
      println("number of tunnels   : " + filteredPath.length)
      //println("number of s/d paths : " + nSimplePaths.sum)
      //println("number of mp paths  : " + sumPaths.sum)
      //println("number of nodes     : " + numberOfNodes.sum)
    }
    
    
  }

}

object DEFOptimizer {
  def apply(problem: MRProblem, edgeWeights: Array[Int], edgeCapacities: Array[Int], edgeLatencies: Array[Int], verbose: Boolean, statsFile: Option[PrintWriter]): DEFOptimizer = {
    val instance = problem.toInstance(edgeWeights, edgeCapacities, edgeLatencies)
    new DEFOptimizer(instance, verbose, statsFile)
  }
}
