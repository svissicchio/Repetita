package be.ac.ucl.ingi.rls.constraint

import be.ac.ucl.ingi.rls._
import scala.util.Random
import be.ac.ucl.ingi.rls.state.Objective
import be.ac.ucl.ingi.rls.state.Trial


/*
 *  Takes a sequence of objective trials, composes them in lexicographic order:
 *  check() returns true if new state lexicographically better than old one. 
 */

class Lexicographic(trials: (Trial with Objective)*)
extends Trial {
  private[this] val trials_ = trials.toArray
  private[this] val nTrials = trials_.length
  
  private[this] var nDetours = 0
  private[this] var oldNDetours = nDetours
  
  private[this] val scores = Array.tabulate(nTrials)(i => trials_(i).score)
  private[this] val oldScores = Array.tabulate(nTrials)(scores)
  
  private def updateFrom(index: Int): Unit = {
    var p = index
    while (p < nTrials) {
      trials_(p).update()
      scores(p) = trials_(p).score
      p += 1
    }
  }
  
  override def update(): Unit = updateFrom(0)

  
  private def revertBefore(index: Int): Unit = {
    var p = index
    while (p > 0) {
      p -= 1
      trials_(p).revert()
      scores(p) = oldScores(p)
    }
  }
  
  override def revert(): Unit = revertBefore(nTrials)

  
  override def check(): Boolean = {
    // call check until a trial passes or fails.
    var answer = true
    var continue = true
    
    var p = 0
    while (p < nTrials && continue) {
      trials_(p).update()
      scores(p) = trials_(p).score
      continue = scores(p) == oldScores(p)
      p += 1
    }
    
    if (continue) false   // p == nTrials
    else {
      // p is after trial that made loop break
      if (scores(p-1) < oldScores(p-1)) {  // passed, update remaining trials
        if (p > 1) println("accepted on secondary objective")

        updateFrom(p)
        true
      }
      else {        // failed, revert previous trials
        revertBefore(p)
        false
      }
    }
  }
  
  
  override def commit(): Unit = {
    var p = 0
    while (p != nTrials) {
      trials_(p).commit()
      oldScores(p) = scores(p)
      p += 1
    }
  }  
}
