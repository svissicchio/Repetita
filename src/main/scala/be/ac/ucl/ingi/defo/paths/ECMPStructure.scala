package be.ac.ucl.ingi.defo.paths

import be.ac.ucl.ingi.defo.core.Topology

abstract class ECMPStructure {
  
  /** Returns the topology of the network */
  def topology: Topology
  
  /** Returns the weight of the link */
  def weight(linkId: Int): Int
 
  /** Returns the number of segments. */
  def nSegments: Int
  
  /** Returns the source's id of the segment. */
  def segmentSrc(segmentId: Int): Int
  
  /** Returns the destination's id of the segment. */
  def segmentDest(segmentId: Int): Int
    
  /** 
   *  Returns the id of the corresponding segment or 
   *  throws an error if the segment does not exist. 
   */
  def segmentId(src: Int, dest: Int): Int
  
  /** Returns the source's id of the link. */
  def linkSrc(linkId: Int): Int
  
  /** Returns the destination's id of the link. */
  def linkDest(linkId: Int): Int
  
  /** 
   *  Returns the id of the corresponding link or 
   *  throws an error if the link does not exist. 
   */
  def linkId(src: Int, dest: Int): Int
  
  /** Returns the latency of the segment. */
  def latency(segmentId: Int): Int
  
  /** Returns the latency of the segment. */
  def latency(segmentSrc: Int, segmentDest: Int): Int 

  /** Returns the id of the segments that contain the link. */
  def segments(linkId: Int): List[Int]
  
  /** Returns the id of the links contained in the segment. */
  def links(segmentId: Int): Set[Int]
  
  /** Returns the id of the links contained in the segment. */
  def links(segmentSrc: Int, segmentDest: Int): Set[Int]
  
  /** Returns the quantity of traffic sent on the link by the segment. */
  def flow(segmentId: Int, linkId: Int): Double
  
  /** Returns the quantity of traffic sent on the link by the segment. */
  def flow(segmentSrc: Int, segmentDest: Int, linkId: Int): Double
}

object ECMPStructure { 
  def apply(topology: Topology, weights: Array[Int], latencies: Array[Int]): ECMPStructure = ECMPStructureLL(topology, weights, latencies) 
}
