package be.ac.ucl.ingi.defo.search

import scala.util.Random
import oscar.algo.search._
import oscar.algo.search.Branching
import be.ac.ucl.ingi.defo.core.variables.IncrPathVar
import be.ac.ucl.ingi.defo.constraints.paths.Visit

class IncrPathBranchingSingle(path: IncrPathVar, valSelect: (IncrPathVar, Int) => Int) extends Branching {
  
  override def alternatives(): Seq[Alternative] = {
    val nodes = path.possible.sortBy(i => valSelect(path, i))
    branchAll(nodes)(node => path.store.post(new Visit(path, node)))
  }
  
}


class IncrPathBranching(val paths: Array[IncrPathVar], val varSelect: Int => Int, val valSelect: (Int, Int) => Int) extends Branching {

  override def alternatives(): Seq[Alternative] = {
    val i = selectMin()
    if (i == -1) noAlternative
    else {
      val path = paths(i)
      val nodes = path.possible.sortBy(n => valSelect(i, n))
      branchAll(nodes)(node => path.store.post(new Visit(path, node)))
    }
  }

  @inline
  private def selectMin(): Int = {
    var minId = -1
    var min = Int.MaxValue
    var i = 0
    while (i < paths.length) {
      if (!paths(i).isBound) {
        val m = varSelect(i)
        if (m < min) {
          min = m
          minId = i
        }
      }
      i += 1
    }
    minId
  }
}

class IncrPathBranching2(val paths: Array[IncrPathVar], val varSelect: Int => Int, val solutionPaths: Array[Array[Int]], val rand: Random) extends Branching {

  override def alternatives(): Seq[Alternative] = {
    val i = selectMin()
    if (i == -1) noAlternative
    else {
      val path = paths(i)
      val from = path.lastVisited
      if (solutionPaths(i).size > 2) {
        val pos = path.position(from)
        val next = solutionPaths(i)(pos + 1)
        if (path.nVisited > 1) branchOne { path.store.post(new Visit(path, next)) }
        else branch { path.store.post(new Visit(path, path.destId)) } { path.store.post(new Visit(path, next)) }
      }
      else {
        val nodes = path.possible.sortBy(i => {
          if (i == path.destId) Int.MinValue 
          else rand.nextInt(100)
        })
        branchAll(nodes)(node => path.store.post(new Visit(path, node)))
      }
    }
  }

  @inline
  private def selectMin(): Int = {
    var minId = -1
    var min = Int.MaxValue
    var i = 0
    while (i < paths.length) {
      if (!paths(i).isBound) {
        val m = varSelect(i)
        if (m < min) {
          min = m
          minId = i
        }
      }
      i += 1
    }
    minId
  }
}
