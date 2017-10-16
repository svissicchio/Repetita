package edu.repetita.utils.datastructures;

import java.util.NoSuchElementException;


/** 
 * @author Steven Gay 
 **/

public class ArrayHeapInt {
  int nValues;
  int indexLast = 0;
  
  private int[] values;
  private int[] indexOf;
  private int[] keys;
  
  public ArrayHeapInt(int nValues) {
    this.nValues = nValues;
    values = new int[nValues+1];   // element 0 is never used, shifting by 1 makes computations easier
    indexOf = new int[nValues];    // initialized to 0, which means "not in here"
    keys = new int[nValues];
  }

  public boolean inHeap(int value) {
    return indexOf[value] != 0;
  }

  public int valueMin() {
    if (indexLast != 0) return values[1];
    else throw new NoSuchElementException("Empty heap");
  }

  public int keyMin() {
    if (indexLast != 0) return keys[values[1]];
    else throw new NoSuchElementException("empty");
  }

  public int size() {
    return indexLast;
  }

  public boolean isEmpty() {
    return indexLast == 0;
  }

  public void clear() {
    indexLast = 0;
  }
  
  public void enqueue(int key, int value) {
    assert indexLast <= nValues : "Heap is full";
    assert 0 <= value && value < nValues : "Illegal value";
    assert indexOf[value] == 0 : "Value must not already be in heap";
    
    // put element at end of heap
    indexLast++;
    keys[value] = key;
    values[indexLast] = value;
    indexOf[value] = indexLast;
    
    // reorder heap
    heapifyBottomUp(indexLast);
  }

  public int dequeue() {
    if (indexLast == 0) throw new NoSuchElementException("");
    
    // remove min value
    int value = values[1];
    indexOf[value] = 0;
    
    if (indexLast > 1) {
      // overwrite it with last value = value at indexLast
      values[1] = values[indexLast];
      indexOf[values[1]] = 1;
    
      // reorder heap
      heapifyTopDown(1);
    }
    
    indexLast--;

    return value;
  }

  public void decreaseKey(int keyUpdate, int value) {
    assert 0 <= value && value < nValues : "Illegal value";
    assert indexOf[value] != 0 : "Value must be in heap";
    assert keyUpdate < keys[value] : "New key must be smaller than old key";
    
    keys[value] = keyUpdate;
    heapifyBottomUp(indexOf[value]);
  }

  private void heapifyTopDown(int index) {
    int value = values[index];
    int key = keys[value];

    int indexMaxParent = indexLast >> 1;
    while (index <= indexMaxParent) {
      int indexMin = indexOfMinChild(index);
      int valueMin = values[indexMin]; 
      if (keys[valueMin] >= key) break;
      
      values[index] = valueMin;
      indexOf[valueMin] = index;

      index = indexMin;
    }
    
    values[index] = value;
    indexOf[value] = index;
  }

  private void heapifyBottomUp(int index) {
    int value = values[index];
    int key = keys[value];
    
    // move elements value's ancestors down until it can be placed
    while (index > 1) {
      int indexParent = index >> 1;
      if (keys[values[indexParent]] <= key) break;
      
      values[index] = values[indexParent];
      indexOf[values[indexParent]] = index;

      index = indexParent;
    }
    
    values[index] = value;
    indexOf[value] = index;
  }

  final private int indexOfMinChild(int index) {
    int indexLeft = index << 1;
    assert indexLeft <= indexLast : "i has no children"; 
    int indexRight = indexLeft + 1; // right child
    
    return (indexRight <= indexLast && keys[values[indexRight]] < keys[values[indexLeft]]) ? indexRight : indexLeft; 
  }
}
