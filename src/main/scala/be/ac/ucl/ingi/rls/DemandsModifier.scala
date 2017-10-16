package be.ac.ucl.ingi.rls

import be.ac.ucl.ingi.rls.state.{FlowStateChecker, FlowStateRecomputeDAGOnCommit}
import be.ac.ucl.ingi.rls.state._
import be.ac.ucl.ingi.rls.io.DemandsData

class DemandsModifier(pathState: PathState, flowState: FlowStateChecker, flowState2: FlowStateRecomputeDAGOnCommit, demandsData: DemandsData) 
{
  def add(demand: Demand, diffTraffic: Double) =
  {
    demandsData.demandTraffics(demand) += diffTraffic
    
    val path = pathState.path(demand)
    
    var pDetour = pathState.size(demand) - 1  // loop starts at size - 2, so that it explores size-2 -> size-1, size-3 -> size-2 ...
    
    while (pDetour > 0) {
      pDetour -= 1
      val source      = path(pDetour)
      val destination = path(pDetour + 1)
      flowState.modify(source, destination, diffTraffic)
      flowState2.modify(demand, source, destination, diffTraffic)
    }      

    // TODO: batch demands to factorize the commits
    flowState.update()
    flowState.commit()
    
    flowState2.update()
    flowState2.commit()    
  }
}
