package be.ac.ucl.ingi.defo

import be.ac.ucl.ingi.defo.parsers.TopologyParser
import be.ac.ucl.ingi.defo.parsers.DemandParser
import be.ac.ucl.ingi.defo._
import be.ac.ucl.ingi.defo.parsers.ConstraintParser
import be.ac.ucl.ingi.defo.parsers.ParsedConstraint
import be.ac.ucl.ingi.defo.parsers.ParsedPassThrough
import be.ac.ucl.ingi.defo.modeling.DEFOPassThroughSeq
import be.ac.ucl.ingi.defo.modeling.units.LoadUnit
import be.ac.ucl.ingi.defo.modeling.units.TimeUnit
import be.ac.ucl.ingi.defo.modeling.units.RelativeUnit
import be.ac.ucl.ingi.defo.parsers.TopologyData
import be.ac.ucl.ingi.defo.parsers.DemandsData
import be.ac.ucl.ingi.defo.core.Topology
import java.io.PrintWriter

object Main extends App {

  // val fileName = "data/topologies/defo/synth50_opt_hard"
  // val fileName = args(0)

  //solve(Array("-t", "300", "-l", "0", s"$fileName.graph", s"$fileName.demands"))
  solve(args)

  final def solve(args: Array[String]): Unit = {
    var time = 30.s
    var load = 90.pct
    var scaling = 1
    var graphFile = "" 
    var demandsFile = "" 
    var verbose = false
    var statsFile: Option[String] = None
    var pathsFile: Option[String] = None
    
    try {
      if (args.length <= 1) {
        printHelp()
        System.exit(1)
      }
      else {
        var i = 0
//        while (i < args.length - 2) {
        while (i < args.length) {
          val arg = args(i)
          arg match {
            case "-h" => printHelp()
            case "-l" => {
              i += 1
              load = parseInt(args, i, "-l", s => s"$s is not a valid maxLinkLoad rate.").pct
            }
            case "-t" => {
              i += 1
              time = parseInt(args, i, "-t", s => s"$s is not a valid time limit.").s
            }
            case "-s" => {
              i += 1
              scaling = parseInt(args, i, "-s", s => s"$s is not a valid scaling coefficient.")
            }
            case "-f" => {
              i += 1
              graphFile = args(i) + ".graph"
              demandsFile = args(i) + ".demands"
            }
            case "-graph" => {
              i += 1
              graphFile = args(i)
            }
            case "-demands" => {
              i += 1
              demandsFile = args(i)
            }
            case "-stats" => {
              i += 1
              statsFile = Option(args(i))
            }
            case "-paths" => {
              i += 1
              pathsFile = Option(args(i))
            }
            case "-verbose" => {
              verbose = true
            }
            case _ => throw new Exception("unknown parameter " + arg + ".")
          }
          i += 1
        }
      }

      solve(time, load, scaling, graphFile, demandsFile)(verbose, pathsFile, statsFile)
    }
    catch {
      case e: Exception => {
        println("Error: " + e.getStackTraceString) // e.getMessage)
        System.exit(1)
      }
    }
  }

  private def parseInt(args: Array[String], i: Int, param: String, msg: String => String): Int = {
    if (i > args.length) println(s"missing value for parameter $param.")
    val s = args(i)
    try { s.toInt }
    catch { case e: Exception => throw new Exception(msg(s)) }
  }

  private def printHelp(): Unit = {
    println("Syntaxe: defo.jar [-parameters] <topologyFile> <demandsFile>")
    println("-h       print help message.")
    println("-l       maxLinkLoad rate to reach in percents (default: 90).")
    println("-t       optimization time limit in seconds (default: 30 secs).")
    println("-s       positive integer coefficient to rescale demands (default: 1).")
    println("-f       instance stem: will use stem.graph stem.demands ")
    println("-graph   instance.graph")
    println("-demands instance.demands")
    println("-verbose emit messages during computation")
    println("-stats   instance.stats put progress info in a file")
  }

  final def solve(time: TimeUnit, load: RelativeUnit, scaling: Int, topologyFile: String, demandsFile: String)
                 (verbose: Boolean, pathsFilename: Option[String], statsFilename: Option[String]): Unit = 
  {

    // Data file
    val topologyDataParsed = TopologyParser.parse(topologyFile)
    val demandsDataParsed = DemandParser.parse(demandsFile)

    val demandsData = new DemandsData(
      demandsDataParsed.demandLabels,
      demandsDataParsed.demandSrcs,
      demandsDataParsed.demandDests,
      demandsDataParsed.demandTraffics.map(_ * scaling)
    )

    val topologyData = new TopologyData(
      topologyDataParsed.nodeLabels,
      topologyDataParsed.nodeCoordinates,
      topologyDataParsed.edgeLabels,
      topologyDataParsed.edgeSrcs,
      topologyDataParsed.edgeDests,
      topologyDataParsed.edgeWeights,
      topologyDataParsed.edgeCapacities.map(_ * scaling),
      topologyDataParsed.edgeLatencies
    )

     // Constraint file
    var constraints: Array[ParsedConstraint] = null
    //if (constraintFile != null) constraints = ConstraintParser.parse(constraintFile)

    val topology = Topology(topologyData.edgeSrcs, topologyData.edgeDests, topologyData.nodeLabels, topologyData.edgeLabels)
    val Demands = 0 until demandsData.demandTraffics.length

    // Declare a new Middlepoint Routing Problem
    val problem: MRProblem = new MRProblem(topology) {

      for (demandId <- Demands) {
        newDemand(
          demandsData.demandLabels(demandId),
          demandsData.demandSrcs(demandId),
          demandsData.demandDests(demandId),
          demandsData.demandTraffics(demandId).kbps
        )
      }

      // Constraints if any
      if (constraints != null) {
        for (constraint <- constraints if constraint.isInstanceOf[ParsedPassThrough]) {
          val pass = constraint.asInstanceOf[ParsedPassThrough]
          if (existsDemand(pass.demand)) {
            val demand = label2Demand(pass.demand).demandId
            val nodes = pass.sets.map(_.map(n => label2Node(n).nodeId))
            add(new DEFOPassThroughSeq(demand, nodes))
          }
        }
      }
    }

    // Solver
    val statsFile = statsFilename.map(new PrintWriter(_))
    val solver = DEFOptimizer(problem, topologyData.edgeWeights, topologyData.edgeCapacities, topologyData.edgeLatencies, verbose, statsFile)
    solver.solve(time, load)
    statsFile.map(_.close)

    val initRates = solver.core.initialRates
    val finalRates = solver.core.bestRates

    val edges = topology.Edges
    val initMost = edges.sortBy(e => -initRates(e)).take(40)
    val finalMost = edges.sortBy(e => -finalRates(e)).take(40)
    val allEdges = (initMost ++ finalMost).distinct
    
    pathsFilename foreach { name =>
      val file = new PrintWriter(name)
      
      // compute final max maxLinkLoad
      var worstLoad = 0.0
      for (edge <- topology.Edges) worstLoad = math.max(worstLoad, finalRates(edge).toDouble / solver.core.grain)
      file.println("MAXLOAD")
      file.println(worstLoad)
      file.println()
  
      // Print assigned paths
      var nPaths = 0
      file.println("PATHS")
      for (demand <- problem.demands) {
        val path = problem.assignedPath(demand, solver)
        if (path.length > 2) {
          nPaths += 1
          file.println(demand + " " + path.map(i => topologyData.nodeLabels(i)).mkString(" "))
        }
      }
      file.println()
      
      file.println("NPATHS")
      file.println(nPaths)
      file.println()
      file.close()
    }
  }
}
