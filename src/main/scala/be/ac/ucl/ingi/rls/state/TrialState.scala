package be.ac.ucl.ingi.rls.state

/*
 *  A trial either just checks or maintains a state and delegates check to it
 */

trait Trial {
  def update(): Unit          
  def check(): Boolean   // update and return true/false depending on result
  def revert(): Unit
  def commit(): Unit
}

/*
 *  A trait for trials that can score the current state.
 */ 
trait Objective {
  def score(): Double
}

/*
 *  A TrialState typically can be modified,
 *  then serves as a hub for several Trials that it passes messages to.
 */ 
abstract class TrialState extends Trial {
  def updateState(): Unit   // puts object in a state unilaterally; do not call check
  def commitState(): Unit   // save this state for future revert
  def revertState(): Unit   // revert to last saved state
  
  private var nTrial = 0
  private var maxTrial = 16
  
  private var trials = new Array[Trial](maxTrial)
  
  def addTrial(trial: Trial) = {
    if (nTrial == maxTrial) {
      maxTrial <<= 1
      val newTrials = new Array[Trial](maxTrial)
      System.arraycopy(trials, 0, newTrials, 0, nTrial)
      trials = newTrials
    }
    trials(nTrial) = trial
    nTrial += 1
  }
  
  override def update() = {
    updateState()
    
    var pTrial = 0
    while (pTrial < nTrial) {
      trials(pTrial).update()
      pTrial += 1
    }
  }
  
  override def check() = {
    // try to check every monitoring trial
    var pTrial = 0
    while (pTrial < nTrial && trials(pTrial).check()) pTrial += 1
    
    // if some trial failed, revert all trials that have been called and revert yourself
    val pass = pTrial == nTrial
    if (!pass) {
      // not reverting the trial that failed, it must revert itself
      while (pTrial > 0) { 
        pTrial -= 1
        trials(pTrial).revert()
      }
      revertState()
    }
    
    pass
  }
  
  override def commit() = {
    commitAll()
    commitState()
  }
  
  override def revert() = {
    revertAll()
    revertState()
  }
  
  private def revertAll() = {
    var pTrial = nTrial
    while (pTrial > 0) {
      pTrial -= 1
      trials(pTrial).revert()
    }
  }
  
  private def commitAll() = {
    var p = 0
    while (p < nTrial) {
      trials(p).commit()
      p += 1
    }
  }
}
