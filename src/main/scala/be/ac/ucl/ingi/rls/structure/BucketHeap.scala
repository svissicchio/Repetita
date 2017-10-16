package be.ac.ucl.ingi.rls.structure

class BucketHeap[V](val maxKey: Int) extends LightPriorityQueue[V] {

  case class Node(key: Int, value: V)
  
  val bucketSize: Int = math.sqrt(maxKey + 1).ceil.toInt

  val topBucket: Array[List[Node]] = Array.fill(bucketSize)(List.empty)
  private var topNEntries: Int = 0
  private var topBucketId: Int = 0

  val lowBucket: Array[List[Node]] = Array.fill(bucketSize)(List.empty)
  private var lowNEntries: Int = 0
  private var lowBucketId: Int = 0

  def size: Int = topNEntries
  def isEmpty: Boolean = topNEntries == 0

  override def enqueue(key: Int, value: V): Unit = {

    topNEntries += 1
    val topId = key / bucketSize
    topBucketId = math.min(topBucketId, topId)

    if (topId != topBucketId) topBucket(topId) = Node(key, value) :: topBucket(topId)
    else {
      lowNEntries += 1
      val lowId = key % bucketSize
      lowBucket(lowId) = Node(key, value) :: lowBucket(lowId)
      lowBucketId = math.min(lowBucketId, lowId)
    }
  }

  override def head: V = {
    if (isEmpty) throw new Exception("Empty")
    else {
      // Updates the ids
      if (lowNEntries == 0) {
        nextTopId()
        expand(topBucket(topBucketId))
      } else if (lowBucket(lowBucketId).isEmpty) {
        nextLowId()
      } 
      lowBucket(lowBucketId).head.value
    }
  }

  def dequeue(): V = {
    if (isEmpty) throw new Exception("Empty")
    else {
      val entry = head
      lowBucket(lowBucketId) = lowBucket(lowBucketId).tail
      lowNEntries -= 1
      topNEntries -= 1
      if (isEmpty) {
        topBucketId = 0
        lowBucketId = 0
      } 
      entry
    }
  }
  
  override def removeAll(): Unit = {
    topBucketId = 0
    lowBucketId = 0
    topNEntries = 0
    lowNEntries = 0
    for (i <- 0 until bucketSize) {
      topBucket(i) = List.empty
      lowBucket(i) = List.empty
    }
  }
  
  override def remove(key: Int, value: V): Boolean = {
    ???
  }
  
  override def changeKey(oldKey: Int, newKey: Int, value: V): Boolean = ???

  private def nextTopId(): Unit = {
    while (topBucket(topBucketId).isEmpty) {
      topBucketId += 1
    }
  }

  private def nextLowId(): Unit = {
    while (lowBucket(lowBucketId).isEmpty) {
      lowBucketId += 1
    }
  }

  private def expand(entries: List[Node]): Unit = {
    topBucket(topBucketId) = List.empty
    lowBucketId = Int.MaxValue
    for (entry <- entries) {
      val lowId = entry.key % bucketSize
      lowBucket(lowId) = entry :: lowBucket(lowId)
      lowNEntries += 1
      lowBucketId = math.min(lowBucketId, lowId)
    }
  }
  
  // Returns the list of element in the low bucket and removes it from this structure
  // lowNEntries = 0 at the end
  private def compress(): List[Node] = {
    var entries: List[Node] = List.empty
    while (lowNEntries > 0) {
      nextLowId()
      entries = lowBucket(lowBucketId).head :: entries
      lowBucket(lowBucketId) = lowBucket(lowBucketId).tail
      lowNEntries -= 1
    }
    entries
  }
}
