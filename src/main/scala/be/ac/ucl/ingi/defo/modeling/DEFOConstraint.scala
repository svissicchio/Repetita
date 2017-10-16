package be.ac.ucl.ingi.defo.modeling

abstract class DEFOConstraint
abstract class DEFODemandConstraint extends DEFOConstraint { def demandId: Int }

class DEFOAvoidNode(final override val demandId: Int, val nodeId: Int) extends DEFODemandConstraint

class DEFOAvoidEdge(final override val demandId: Int, val edgeId: Int) extends DEFODemandConstraint

class DEFOPassThrough(final override val demandId: Int, val nodes: Array[Int]) extends DEFODemandConstraint

class DEFOPassThroughSeq(final override val demandId: Int, val seqNodes: Array[Array[Int]]) extends DEFODemandConstraint 

class DEFOLowerLatency(final override val demandId: Int, val latency: Int, val relative: Boolean) extends DEFODemandConstraint

class DEFOLowerEqLatency(final override val demandId: Int, val latency: Int, val relative: Boolean) extends DEFODemandConstraint

class DEFOGreaterLatency(final override val demandId: Int, val latency: Int, val relative: Boolean) extends DEFODemandConstraint

class DEFOGreaterEqLatency(final override val demandId: Int, val latency: Int, val relative: Boolean) extends DEFODemandConstraint

class DEFOLowerLoad(val edgeId: Int, val load: Int, val relative: Boolean) extends DEFOConstraint

class DEFOLowerEqLoad(val edgeId: Int, val load: Int, val relative: Boolean) extends DEFOConstraint

class DEFOGreaterLoad(val edgeId: Int, val load: Int, val relative: Boolean) extends DEFOConstraint

class DEFOGreaterEqLoad(val edgeId: Int, val load: Int, val relative: Boolean) extends DEFOConstraint

class DEFOLowerLength(final override val demandId: Int, val length: Int) extends DEFODemandConstraint

class DEFOLowerEqLength(final override val demandId: Int, val length: Int) extends DEFODemandConstraint

class DEFOGreaterLength(final override val demandId: Int, val length: Int) extends DEFODemandConstraint

class DEFOGreaterEqLength(final override val demandId: Int, val length: Int) extends DEFODemandConstraint
