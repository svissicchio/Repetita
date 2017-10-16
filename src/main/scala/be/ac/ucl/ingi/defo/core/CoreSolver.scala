package be.ac.ucl.ingi.defo.core


import be.ac.ucl.ingi.defo._
import be.ac.ucl.ingi.defo.modeling.DEFOLowerLength
import be.ac.ucl.ingi.defo.utils.RichRandom
import be.ac.ucl.ingi.defo.modeling.DEFOPassThrough
import be.ac.ucl.ingi.defo.modeling.DEFOPassThroughSeq
import be.ac.ucl.ingi.defo.search.IncrPathBranchingSingle
import be.ac.ucl.ingi.defo.paths.ConnectStructure
import be.ac.ucl.ingi.defo.modeling.DEFOAvoidEdge
import be.ac.ucl.ingi.defo.modeling.DEFOLowerEqLength
import be.ac.ucl.ingi.defo.modeling.DEFOAvoidNode
import be.ac.ucl.ingi.defo.constraints.LoadToRate
import be.ac.ucl.ingi.defo.search.IncrPathBranching
import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import be.ac.ucl.ingi.defo.paths.ECMPStructure
import be.ac.ucl.ingi.defo.constraints.paths.PassThroughSeq
import be.ac.ucl.ingi.defo.constraints.paths.SegmentToNetwork
import be.ac.ucl.ingi.defo.constraints.paths.NetworkToSegment
import be.ac.ucl.ingi.defo.constraints.paths.CanReach
import be.ac.ucl.ingi.defo.constraints.paths.DAGPath
import be.ac.ucl.ingi.defo.constraints.paths.PassThrough
import oscar.algo.search.SearchStatistics
import oscar.cp._

import java.io.PrintWriter
import scala.collection.mutable.ArrayBuffer

class CoreSolver(instance: DEFOInstance, verbose: Boolean, statsFile: Option[PrintWriter]) {
  // Granularity of flow values wrt capacity: improvements must be at least one grain, and one grain = 1 percent is too large. 
  val grain = 1000 
  
  // Input data 
  private[this] val topology = instance.topology
  private[this] val weights = instance.weights
  private[this] val demandTraffics: Array[Int] = instance.demandTraffics
  private[this] val demandSrcs: Array[Int] = instance.demandSrcs
  private[this] val demandDests: Array[Int] = instance.demandDests
  private[this] val capacities: Array[Int] = instance.capacities
  private[this] val latencies: Array[Int] = instance.latencies
  private[this] val nEdges = topology.nEdges
  private[this] val nNodes = topology.nNodes

  // Preprocessed data
  private[this] val ecmpStruct = ECMPStructure(topology, weights, latencies)
  private[this] val reachStruct = ConnectStructure(topology)
  private[this] val step = Array.tabulate(nEdges)(e => math.max(capacities(e) / grain, 1))

  // Demands
  private[this] val nDemands = demandTraffics.length
  private[this] val Demands = 0 until nDemands

  // Constraints
  private[this] val demandConstraints = instance.demandConstraints
  private[this] val topologyConstraints = instance.topologyConstraints

  // Initial solution
  private[this] val initialRate = Array.ofDim[Int](nEdges)
  private[this] var initialComputed: Boolean = false
  private[this] var computeInitialTime: Long = 0

  // Best-so-far solution
  private[this] val solutionFlow = Array.ofDim[Int](nDemands, nEdges)
  private[this] val solutionLoad = Array.ofDim[Int](nEdges)
  private[this] val solutionRate = Array.ofDim[Int](nEdges)
  private[this] val solutionPath = Array.fill(nDemands)(Array.empty[Int])
  private[this] var solutionNTunnels = 0
  private[this] var solutionMaxRate = Int.MaxValue

  // Random number generator
  private[this] val rand = RichRandom(0)

  // LNS Parameters
  private[this] val minK1 = 1
  private[this] val maxK1 = 50
  private[this] val minK2 = 0
  private[this] val maxK2 = 10

  // LNS State
  private[this] var nIterations: Int = 0
  private[this] var selected: Int = 0
  private[this] var maxUsage: Int = 0
  private[this] var success = false
  private[this] var k1 = minK1
  private[this] var k2 = minK2

  /** Accessors */
  final def initialRates: Array[Int] = initialRate
  final def bestRates: Array[Int] = solutionRate
  final def bestPaths: Array[Array[Int]] = solutionPath
  
  private[this] var initTime0 = 0L

  final def search(timeLimit: Int, loadObjectivePercent: Double): Unit = {
    if (verbose) {
      println
      println("              OPTIMIZATION              ")
      println("----------------------------------------")
      println("max maxLinkLoad\t#tunnels\ttime (ms)")
      println("----------------------------------------")
    }
    
    val loadObjective = loadObjectivePercent * grain / 100   // convert from percentage to pergrainage
    
    initTime0 = System.currentTimeMillis()
    while (solutionMaxRate > loadObjective && System.currentTimeMillis() - initTime0 < timeLimit) {
      improve()
    }
  }

  final def improve(): Unit = {
    // New iteratons
    nIterations += 1

    if (success) {
      k2 = minK2
      k1 = math.max(k1 - 1, minK1)
      success = false
    }
    else {
      k1 += 1
      if (k1 > maxK1) {
        k1 = maxK1
        k2 = math.min(k2 + 1, maxK2)
      }
    }

    val max = solutionMaxRate
    val maxLinks = topology.Edges.filter(l => solutionRate(l) == max)
    val maxLink = maxLinks(rand.nextInt(maxLinks.size))

    val demandOnMaxLink = Demands.filter(d => solutionFlow(d)(maxLink) > 0).toArray
    val maxDemands = rand.weightedShuffle(demandOnMaxLink, i => -demandTraffics(i)).take(k1)
    val relaxedDemands = rand.weightedShuffle(Demands, i => -demandTraffics(i)).take(k2)

    val neighborhood = (relaxedDemands ++ maxDemands).distinct

    selected = maxLink
    maxUsage = max

    if (!neighborhood.isEmpty) solve(neighborhood, maxLinks)
  }

  private def solve(demandsId: Array[Int], maxLinks: IndexedSeq[Int]): SearchStatistics = {

    implicit val solver = new NetworkStore(Strong)
    //solver.silent = true

    val preferences = Array.fill(demandsId.length, nNodes)(false)

    // Path variables
    val paths = demandsId.map(i => {
      val demand = i
      IncrPathVar(demandSrcs(demand), demandDests(demand), topology.nNodes, "Path(" + demandSrcs(demand) + " => " + demandDests(demand) + ")")
    })

    // Flow variables
    val flows = demandsId.map(i => {
      Array.fill(topology.nEdges)(CPIntVar(0, demandTraffics(i)))
    })

    // Load variables
    val flowsT = flows.transpose
    val loads = Array.tabulate(topology.nEdges)(i => {
      var load = solutionLoad(i)
      for (d <- demandsId) load -= solutionFlow(d)(i)
      val all = flowsT(i) ++ Array(CPIntVar(load))
      sum(all)
    })

    // Rate variables
    val rates = Array.tabulate(topology.nEdges)(l => {
      val rate = CPIntVar(0 to loads(l).max / step(l))
      solver.add(new LoadToRate(loads(l), rate, step(l)))
      rate
    })

    // Latency variables

    val latencies = Array.tabulate(paths.size)(i => {
      val path = paths(i)
      val latency = ecmpStruct.latency(path.origId, path.destId)
      val max = math.max((latency * 1.2).toInt, 20)
      CPIntVar(0 to max)
    })

    // Tunnels variables
    val tunnels = paths.map(_.length isGrEq 3)
    val residual = Demands.filter(i => !demandsId.contains(i)).map(i => {
      if (solutionPath(i).size > 2) 1 else 0
    }).sum
    val nTunnels = sum(tunnels) + residual

    // Objectives
    val objective = maximum(rates)

    // Search
    solver.search {
      new IncrPathBranching(paths, i => -demandTraffics(demandsId(i)), (i, to) => {
        val path = paths(i)
        val from = path.lastVisited
        val isMax = solutionRate(selected) == solutionMaxRate
        if (to == path.destId) Int.MinValue
        else if (!isMax) maxLinks.map(l => (ecmpStruct.flow(from, to, l) * grain).toInt).max
        else (ecmpStruct.flow(from, to, selected) * grain).toInt
      })
    }

    solver.onSolution {
      success = true

      for (d <- 0 until demandsId.size) {
        // Paths 
        solutionPath(demandsId(d)) = paths(d).visited
        // Flow
        for (l <- topology.Edges) {
          solutionFlow(demandsId(d))(l) = flows(d)(l).min
        }
      }
      for (l <- topology.Edges) {
        // Load
        solutionLoad(l) = loads(l).min
        // Rate
        solutionRate(l) = rates(l).min
      }

      solutionNTunnels = nTunnels.min

      val newMaxRate = objective.min
      if (newMaxRate < solutionMaxRate) {
        solutionMaxRate = newMaxRate
        val time = System.currentTimeMillis() - initTime0
        if (verbose) println(newMaxRate + "\t\t" + solutionNTunnels + "\t\t" + time)
        statsFile foreach { file =>
          file.println(s"OBJECTIVE ${newMaxRate.toDouble / grain} TIME $time DETOURS $solutionNTunnels")
        }
        
      }
      solutionMaxRate = objective.min
    }
    // Run !
    val stat = solver.startSubjectTo(nSols = 1, timeLimit = 1) {

      val constraints: ArrayBuffer[Constraint] = ArrayBuffer()

      // Path constraints 
      for (i <- 0 until demandsId.size) {

        val demand = demandsId(i)

        // Path length
        constraints.append(paths(i).length <= 4)
        // Reach
        constraints.append(new CanReach(paths(i), reachStruct))
        // The graph of links is acyclic
        constraints.append(new DAGPath(paths(i), flows(i), ecmpStruct))
        // From segment to network 
        constraints.append(new SegmentToNetwork(paths(i), flows(i), ecmpStruct, demandTraffics(demand)))
        // From network to segment
        constraints.append(new NetworkToSegment(paths(i), flows(i), ecmpStruct, demandTraffics(demand)))

        // Specific constraints

        for (constraint <- demandConstraints(demand)) {
          // Avoid node
          if (constraint.isInstanceOf[DEFOAvoidNode]) {
            val avoid = constraint.asInstanceOf[DEFOAvoidNode]
            val nodeId = avoid.nodeId
            val outEdges = topology.outEdges(nodeId)
            val inEdges = topology.inEdges(nodeId)
            for (edge <- outEdges) constraints.append(flows(i)(edge) === 0)
            for (edge <- inEdges) constraints.append(flows(i)(edge) === 0)
          }
          // Avoid edge
          else if (constraint.isInstanceOf[DEFOAvoidEdge]) {
            val avoid = constraint.asInstanceOf[DEFOAvoidEdge]
            val edgeId = avoid.edgeId
            constraints.append(flows(i)(edgeId) === 0)
          }
          // Pass through
          else if (constraint.isInstanceOf[DEFOPassThrough]) {
            val pass = constraint.asInstanceOf[DEFOPassThrough]
            val nodes = pass.nodes
            for (n <- nodes) preferences(i)(n) = true
            constraints.append(new PassThrough(paths(i), nodes.toSet))
          }
          // Pass through seq
          else if (constraint.isInstanceOf[DEFOPassThroughSeq]) {
            val pass = constraint.asInstanceOf[DEFOPassThroughSeq]
            constraints.append(new PassThroughSeq(paths(i), pass.seqNodes))
          }
          // Pass lower length
          else if (constraint.isInstanceOf[DEFOLowerLength]) {
            val lower = constraint.asInstanceOf[DEFOLowerLength]
            constraints.append(paths(i).length < lower.length)
          }
          // Pass lower eq length
          else if (constraint.isInstanceOf[DEFOLowerEqLength]) {
            val lower = constraint.asInstanceOf[DEFOLowerEqLength]
            constraints.append(paths(i).length <= lower.length)
          }
        }
      }


      //constraints.append(nTunnels < 10)

      // Improvement
      for (link <- topology.Edges) {
        if (solutionRate(link) == maxUsage) constraints.append(rates(link) <= maxUsage)
        else if (solutionRate(link) <= grain && maxUsage > grain) constraints.append(rates(link) <= grain)
        else constraints.append(rates(link) < maxUsage)
      }

      constraints.append(rates(selected) < maxUsage)

      solver.add(constraints)
    }
    stat

  }

  /** Compute the initial solution */
  final def searchInitialSol(): Unit = {
    if (initialComputed) warning("Initial solution already computed.")
    else {

      // Initial solution
      var i = nDemands
      while (i > 0) {
        i -= 1
        val constrained = demandConstraints(i).length > 0
        if (constrained) placeConstrainedDemand(i)
        else placeUnconstrainedDemand(i)
      }

      // Compute initial rates
      i = nEdges
      solutionMaxRate = 0
      while (i > 0) {
        i -= 1
        val rate = solutionLoad(i) / step(i)
        solutionRate(i) = rate
        if (rate > solutionMaxRate) solutionMaxRate = rate
      }

      // Store initial rates
      System.arraycopy(solutionRate, 0, initialRate, 0, nEdges)

      // Computation state
      initialComputed = true
    }
  }

  @inline private def placeUnconstrainedDemand(i: Int): Unit = {
    val demand = i
    val src = demandSrcs(demand)
    val dest = demandDests(demand)
    solutionPath(i) = Array(src, dest)
    for (l <- ecmpStruct.links(src, dest)) {
      val flow = (ecmpStruct.flow(src, dest, l) * demandTraffics(demand)).ceil.toInt
      solutionFlow(i)(l) = flow
      solutionLoad(l) += flow
    }
  }

  @inline private def placeConstrainedDemand(demand: Int): Unit = {

    val src = demandSrcs(demand)
    val dest = demandDests(demand)

    implicit val solver = new NetworkStore(Strong)
    solver.silent = true

    // Path of the demand
    val path = IncrPathVar(src, dest, nNodes, s"Path($src => $dest)")

    // Flow from the demand on each edge
    val flows = Array.fill(nEdges)(CPIntVar(0, demandTraffics(demand)))

    // Load of each edge
    val loads = Array.tabulate(nEdges)(e => flows(e) + solutionLoad(e))

    // Total latency of the path
    val latency = CPIntVar(0, 100000) // FIXME Correct initialization

    // Constraints
    val allConstraints: ArrayBuffer[Constraint] = ArrayBuffer()

    // Path length
    allConstraints.append(path.length <= 4)
    
    // Reach
    allConstraints.append(new CanReach(path, reachStruct))

    // The graph of links is acyclic
    allConstraints.append(new DAGPath(path, flows, ecmpStruct))

    // From segment to network 
    allConstraints.append(new SegmentToNetwork(path, flows, ecmpStruct, demandTraffics(demand)))

    // From network to segment
    allConstraints.append(new NetworkToSegment(path, flows, ecmpStruct, demandTraffics(demand)))

    // Specific constraints
    val constraints = demandConstraints(demand)

    for (constraint <- constraints) {
      // Avoid node
      if (constraint.isInstanceOf[DEFOAvoidNode]) {
        val avoid = constraint.asInstanceOf[DEFOAvoidNode]
        val nodeId = avoid.nodeId
        val outEdges = topology.outEdges(nodeId)
        val inEdges = topology.inEdges(nodeId)
        for (edge <- outEdges) allConstraints.append(flows(edge) === 0)
        for (edge <- inEdges) allConstraints.append(flows(edge) === 0)
      }
      // Avoid edge
      else if (constraint.isInstanceOf[DEFOAvoidEdge]) {
        val avoid = constraint.asInstanceOf[DEFOAvoidEdge]
        val edgeId = avoid.edgeId
        allConstraints.append(flows(edgeId) === 0)
      }
      // Pass through
      else if (constraint.isInstanceOf[DEFOPassThrough]) {
        val pass = constraint.asInstanceOf[DEFOPassThrough]
        val nodes = pass.nodes
        allConstraints.append(new PassThrough(path, nodes.toSet))
      }
      // Pass through seq
      else if (constraint.isInstanceOf[DEFOPassThroughSeq]) {
        val pass = constraint.asInstanceOf[DEFOPassThroughSeq]
        allConstraints.append(new PassThroughSeq(path, pass.seqNodes))
      }
      // Pass lower length
      else if (constraint.isInstanceOf[DEFOLowerLength]) {
        val lower = constraint.asInstanceOf[DEFOLowerLength]
        allConstraints.append(path.length < lower.length)
      }
      // Pass lower eq length
      else if (constraint.isInstanceOf[DEFOLowerEqLength]) {
        val lower = constraint.asInstanceOf[DEFOLowerEqLength]
        allConstraints.append(path.length <= lower.length)
      }
    }

    // Search
    solver.search {
      new IncrPathBranchingSingle(path, (path, to) => {
        val from = path.lastVisited
        if (to == path.destId) Int.MinValue
        else ecmpStruct.links(from, to).size
      })
    }

    // On solution
    var success = false

    solver.onSolution {
      success = true
      solutionPath(demand) = path.visited
      for (l <- topology.Edges) {
        // Flow
        solutionFlow(demand)(l) = flows(l).value
        // Load
        solutionLoad(l) = loads(l).value
      }
      if (solutionPath(demand).length > 2) {
        solutionNTunnels += 1
      }
    }

    // First propagate
    solver.add(allConstraints)

    // Search
    solver.start(nSols = 1)

    if (!success) sys.error("the demand is over-constrained.")
  }
}
