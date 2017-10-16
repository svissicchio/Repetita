package be.ac.ucl.ingi.defo.modeling

import defo.modeling.variables.DEFOLoadVar

class DEFOEdge( final val edgeId: Int, final override val toString: String) {
  final lazy val load: DEFOLoadVar = new DEFOLoadVar(edgeId)
}
