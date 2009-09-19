package com.browseengine.bobo.util;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 
 * @author femekci
 * This class is written for a special purpose. No check is done in insertion and getting a value
 * for performance reasons. Be careful if you are going to use this class 
 */
public final class BigIntArray extends BigSegmentedArray implements Serializable
{
  
  private static final long serialVersionUID = 1L;
	
  private int[][] _array;
  /* Remember that 2^SHIFT_SIZE = BLOCK_SIZE */
  final private static int BLOCK_SIZE = 1024;
  final private static int SHIFT_SIZE = 10; 
  final private static int MASK = BLOCK_SIZE -1;
  
  public BigIntArray(int size)
  {
	super(size);
    _array = new int[_numrows][];
    for (int i = 0; i < _numrows; i++)
    {
      _array[i]=new int[BLOCK_SIZE];
    }
  }
  
  @Override
  public void add(int docId, int val)
  {
    _array[docId >> SHIFT_SIZE][docId & MASK] = val;
  }
  
  @Override
  public final int get(int docId)
  {
    return _array[docId >> SHIFT_SIZE][docId & MASK];
  }

  @Override
  public final void fill(int val)
  {
    for(int[] block : _array)
    {
      Arrays.fill(block, val);
    }
  }

  @Override
  public void ensureCapacity(int size)
  {
    int newNumrows = (size >> SHIFT_SIZE) + 1;
    if (newNumrows > _array.length)
    {
      int[][] newArray = new int[newNumrows][];           // grow
      System.arraycopy(_array, 0, newArray, 0, _array.length);
      for (int i = _array.length; i < newNumrows; ++i)
      {
        newArray[i] = new int[BLOCK_SIZE];
      }
      _array = newArray;
    }
    _numrows = newNumrows;
  }

  @Override
  int getBlockSize() {
	return BLOCK_SIZE;
  }
	
  @Override
  int getShiftSize() {
	return SHIFT_SIZE;
  }
}
