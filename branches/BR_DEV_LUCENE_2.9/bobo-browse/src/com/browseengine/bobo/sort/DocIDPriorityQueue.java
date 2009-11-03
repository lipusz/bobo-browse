package com.browseengine.bobo.sort;


/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** A PriorityQueue maintains a partial ordering of its elements such that the
 * least element can always be found in constant time.  Put()'s and pop()'s
 * require log(size) time.
 *
 * <p><b>NOTE</b>: This class pre-allocates a full array of
 * length <code>maxSize+1</code>, in {@link #initialize}.
  * 
*/
public class DocIDPriorityQueue {
  private int size;
  private int maxSize;
  final protected int[] heap;
  public final int base;

  private final DocComparator comparator;

  public DocIDPriorityQueue(DocComparator comparator, int maxSize, int base) {
    this.comparator = comparator;
    size = 0;
    this.base = base;
    int heapSize;
    if (0 == maxSize)
      // We allocate 1 extra to avoid if statement in top()
      heapSize = 2;
    else
      heapSize = maxSize + 1;
    heap = new int[heapSize];
    this.maxSize = maxSize;
  }

  /**
   * Adds an Object to a PriorityQueue in log(size) time. If one tries to add
   * more objects than maxSize from initialize an
   * {@link ArrayIndexOutOfBoundsException} is thrown.
   * 
   * @return the new 'bottom' element in the queue.
   */
  public final int add(int element) {
    size++;
    heap[size] = element;
    upHeap();
    return heap[1];
  }

  public Comparable<?> sortValue(int doc) {
    return comparator.value(doc);
  }

  private final int compare(int doc1, int doc2) {
    final int cmp = comparator.compare(doc1, doc2);
    if (cmp != 0) {
      return cmp;
    } else {
      return doc2 - doc1;
    }
  }

  public int replace(int element) {
    heap[1] = element;
    downHeap();
    return heap[1];
  }

  /** Returns the least element of the PriorityQueue in constant time. */
  public final int top() {
    // We don't need to check size here: if maxSize is 0,
    // then heap is length 2 array with both entries null.
    // If size is 0 then heap[1] is already null.
    return heap[1];
  }

  /** Removes and returns the least element of the PriorityQueue in log(size)
    time. */
  public final int pop() {
    if (size > 0) {
      int result = heap[1];			  // save first value
      heap[1] = heap[size];			  // move last to first
      heap[size] = -1;			  // permit GC of objects
      size--;
      downHeap();				  // adjust heap
      return result;
    } else
      return -1;
  }

  /**
   * Should be called when the Object at top changes values. Still log(n) worst
   * case, but it's at least twice as fast to
   * 
   * <pre>
   * pq.top().change();
   * pq.updateTop();
   * </pre>
   * 
   * instead of
   * 
   * <pre>
   * o = pq.pop();
   * o.change();
   * pq.push(o);
   * </pre>
   * 
   * @return the new 'top' element.
   */
  public final int updateTop() {
    downHeap();
    return heap[1];
  }

  /** Returns the number of elements currently stored in the PriorityQueue. */
  public final int size() {
    return size;
  }

  /** Removes all entries from the PriorityQueue. */
  public final void clear() {
    for (int i = 0; i <= size; i++) {
      heap[i] = -1;
    }
    size = 0;
  }

  private final void upHeap() {
    int i = size;
    int node = heap[i];			  // save bottom node
    int j = i >>> 1;
    while (j > 0 && compare(node, heap[j]) < 0) {
      heap[i] = heap[j];			  // shift parents down
      i = j;
      j = j >>> 1;
    }
    heap[i] = node;				  // install saved node
  }

  private final void downHeap() {
    int i = 1;
    int node = heap[i];			  // save top node
    int j = i << 1;				  // find smaller child
    int k = j + 1;
    if (k <= size && compare(heap[k], heap[j]) < 0) {
      j = k;
    }
    while (j <= size && compare(heap[j], node) < 0) {
      heap[i] = heap[j];			  // shift up child
      i = j;
      j = i << 1;
      k = j + 1;
      if (k <= size && compare(heap[k], heap[j]) < 0) {
        j = k;
      }
    }
    heap[i] = node;				  // install saved node
  }
}
