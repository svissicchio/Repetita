package be.ac.ucl.ingi.rls

import scala.util.Random
import be.ac.ucl.ingi.rls.neighborhood._
import be.ac.ucl.ingi.rls.constraint._
import be.ac.ucl.ingi.rls.core._
import be.ac.ucl.ingi.rls.io._
import be.ac.ucl.ingi.rls.state._
import be.ac.ucl.ingi.rls.preprocessing._
import be.ac.ucl.ingi.rls.structure.SortUtils
import java.io.PrintWriter

import be.ac.ucl.ingi.rls.constraint.MaxLoad
import be.ac.ucl.ingi.rls.core.Neighborhood
import be.ac.ucl.ingi.rls.io.DemandsData
import be.ac.ucl.ingi.rls.metaheuristic.WeightedDemands

/*
 *  LoadOptimizer tries to find paths so that the max maxLinkLoad of edges is minimal.
 */

class LoadOptimizer(topologyData: TopologyData, decisionDemands: DemandsData, maxSeg: Int)(debug: Boolean) {
  private val topology = Topology(topologyData)
  
  /*
   *   Data preprocessing
   */
  
  // TODO: simplify graphs
  
  // simplify demands
  private val capacityData = new CapacityData {
    private val capa    = topologyData.edgeCapacities
    private val invcapa = topologyData.edgeCapacities.map(1.0 / _)
    def capacity(): Array[Double] = capa
    def invCapacity(): Array[Double] = invcapa
  }
  
  /*
   *   Build Structures, set up constraints
   */ 
  
  private val shortestPaths = new ShortestPaths(topology, topologyData.edgeWeights)
  
  private val allowedDetours = DetoursFilter(shortestPaths)(debug)
  
  private val nNodes = topology.nNodes
  private val nEdges = topology.nEdges
  private val nDemands = decisionDemands.nDemands
  
  if (debug) println(s"$nNodes nodes, $nEdges edges, $nDemands demands")
  
  // val delays = new DelayDataImpl(topology.nNodes, shortestPaths, topologyData)
  
  // initialize network state
  private val pathState  = new PathState(decisionDemands, maxSeg)
  // val delayState = new DelayState(nDemands, delays, pathState)
  
//  val flowState  = new FlowStateRecomputeDAG(nNodes, nEdges, shortestPaths, pathState, decisionDemands)
  private val flowState  = new FlowStatePrecomputeDAG(nNodes, nEdges, shortestPaths, pathState, decisionDemands) // faster, reasonable memory
  
  
//  val edgeDemandState = new EdgeDemandStateDummy(nDemands, nEdges)
//  val edgeDemandState = new EdgeDemandStateSimple(nDemands, nEdges, capacityData)
  private val edgeDemandState = new EdgeDemandStateTree(nDemands, nEdges, capacityData)
  private val flowStateOnCommit = new FlowStateRecomputeDAGOnCommit(nNodes, nEdges, shortestPaths, pathState, decisionDemands, edgeDemandState)
  
  // val noDuplicate = new NoDuplicate(pathState)
  
  // pathState.addTrial(noDuplicate) // TODO: add automatically?
  // pathState.addTrial(delayState) // no delays
  pathState.addTrial(flowState)
  pathState.addTrial(flowStateOnCommit)
  
  // val sumDelays = new SumDelays(delayState, nDemands)
  // val maxDelayIncrease = new MaxDelayIncrease(nDemands, delayState)
  // delayState.addTrial(maxDelayIncrease)
  
  val maxLoad = new MaxLoad(topology, capacityData, flowState, shortestPaths)(debug)
  // val nDetours = new NDetours(pathState)
  
  //val mainObjective = new Lexicographic(maxLoad, nDetours)
  //flowState.addTrial(mainObjective)
  flowState.addTrial(maxLoad)
  
  val bestPaths = new SavedPathState(pathState)
  pathState.addTrial(bestPaths)

  implicit private val neighborhoodDebug = false
  
  val ecmp = new ECMP(nNodes, nEdges, shortestPaths)
  
  val neighborhoods = Array[Neighborhood[Demand]](
      new Reset(pathState), 
      new Remove(pathState),
      new ReplaceGuarded(nNodes, nEdges, ecmp, pathState, maxLoad),
      new InsertGuarded(nNodes, nEdges, ecmp, pathState, maxLoad)
  )
  
  val kickNeighborhoods = Array[Neighborhood[Demand]](
      new Reset(pathState), 
      new Remove(pathState)
//      new ReplaceDetours(allowedDetours, pathState),
//      new InsertDetours(allowedDetours, pathState)
  )
  
  def startMoving(timeLimit: Long, objectiveLimit: Double): Unit = {
    if (debug) println(s"Starting with maxLinkLoad ${maxLoad.score}")
    
    // try moves until stop condition, time for now
    val startTime = System.nanoTime()
    val stopTime = startTime + (timeLimit * 1000000L)
    var bestLoad = maxLoad.score
    var nIterations = 0L
    var bestIteration = 0L
    
    while (System.nanoTime() < stopTime && bestLoad > objectiveLimit) {
      nIterations += 1
      
      if (maxLoad.score > bestLoad && nIterations > bestIteration + 1000) {
        bestPaths.restorePaths()
        pathState.update()
        pathState.commit()
        bestIteration = nIterations - 1
      }
      
      val demand = selectDemand()
      
      if (maxLoad.score == bestLoad && nIterations > bestIteration + 3) {
        bestIteration = nIterations
        kick(kickNeighborhoods, maxLoad, demand)
      }
      
      var improvementFound = false
      var pNeighborhood = 0
      while (!improvementFound && pNeighborhood < neighborhoods.length) {
        val neighborhood = neighborhoods(pNeighborhood)
        
        improvementFound = visitNeighborhood(neighborhood, demand)
        
        if (improvementFound) {
          neighborhood.applyBest()
          pathState.update()
          pathState.commit()
          
          if (maxLoad.score < bestLoad) {
            bestPaths.savePaths()
            bestLoad = maxLoad.score
            bestIteration = nIterations
          }
        }
        
        pNeighborhood += 1
      }
    }
    if (debug) println(s"Finished with maxLinkLoad ${maxLoad.score}")
  }

  // do the best move of the neighborhood, even if it degrades the objective
  private def kick(neighborhoods: IndexedSeq[Neighborhood[Demand]], maxLoad: MaxLoad, demand: Int) = {
    maxLoad.active = false
    val choice = Random.nextInt(neighborhoods.length)
    val neighborhood = neighborhoods(choice)
    if (visitNeighborhood(neighborhood, demand)) {
      neighborhood.applyBest()
      pathState.update()
      pathState.commit()
    }
    maxLoad.active = true
  }
  
  private def visitNeighborhood[T](neighborhood: Neighborhood[T], setter: T): Boolean = {
    var nBestMoves = 0
    var bestNeighborhoodLoad = Double.MaxValue
    var improvementFound = false

    neighborhood.setNeighborhood(setter)
    while (neighborhood.hasNext()) {
      neighborhood.next()
      neighborhood.apply()
      
      // to find random best movement in neighborhood
      if (pathState.nChanged > 0 && pathState.check()) {
        val score = maxLoad.score
        
        if (score == bestNeighborhoodLoad) {
          nBestMoves += 1
          if (Random.nextInt(nBestMoves) == 0) neighborhood.saveBest()
        }
        else if (score < bestNeighborhoodLoad) {
          nBestMoves = 1
          improvementFound = true
          neighborhood.saveBest()
          bestNeighborhoodLoad = maxLoad.score
        }
        
        pathState.revert()
      }
    }
    
    improvementFound    
  }

  
  private def selectDemand(): Demand = {
    val edge = maxLoad.selectRandomMaxEdge()
    edgeDemandState.selectRandomDemand(edge)
  }
  
  private val demandsModifier = new DemandsModifier(pathState, flowState, flowStateOnCommit, decisionDemands)
  
  def setDemandBandwidth(demand: Int, newTraffic: Double): Unit = {
    val diffTraffic = newTraffic - decisionDemands.demandTraffics(demand)
    demandsModifier.add(demand, diffTraffic)
    // println(s"Added $diffTraffic traffic on demand $demand, new max usage is ${maxLoad.score()}")
  }
  
  def solve(timeLimit: Long, objectiveLimit: Double): PathState = {
    startMoving(timeLimit, objectiveLimit)
    bestPaths.restorePaths()
    pathState.update()
    pathState.commit()
    pathState
  }
}
