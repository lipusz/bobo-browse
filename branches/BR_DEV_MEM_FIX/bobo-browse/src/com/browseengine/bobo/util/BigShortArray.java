package com.browseengine.bobo.util;

import java.util.Arrays;

public class BigShortArray extends BigSegmentedArray {
	 private static final long serialVersionUID = 1L;
		
	  private short[][] _array;
	  
	  /* Remember that 2^SHIFT_SIZE = BLOCK_SIZE */
	  final private static int BLOCK_SIZE = 2048;
	  final private static int SHIFT_SIZE = 11; 
	  final private static int MASK = BLOCK_SIZE -1;
	  
	  public BigShortArray(int size)
	  {
	    super(size);
	    _array = new short[_numrows][];
	    for (int i = 0; i < _numrows; i++)
	    {
	      _array[i]=new short[BLOCK_SIZE];
	    }
	  }

	  @Override
	  public final void add(int docId, int val)
	  {
	    _array[docId >> SHIFT_SIZE][docId & MASK] = (byte)val;
	  }

	  @Override
	  public final int get(int docId)
	  {
	    return _array[docId >> SHIFT_SIZE][docId & MASK];
	  }

	  @Override
	  public final void fill(int val)
	  {
		short shortVal = (short)val;
	    for(short[] block : _array)
	    {
	      Arrays.fill(block, shortVal);
	    }
	  }

	  @Override
	  public void ensureCapacity(int size)
	  {
	    int newNumrows = (size >> SHIFT_SIZE) + 1;
	    if (newNumrows > _array.length)
	    {
	      short[][] newArray = new short[newNumrows][];           // grow
	      System.arraycopy(_array, 0, newArray, 0, _array.length);
	      for (int i = _array.length; i < newNumrows; ++i)
	      {
	        newArray[i] = new short[BLOCK_SIZE];
	      }
	      _array = newArray;
	    }
	    _numrows = newNumrows;
	  }
	  
	  @Override
	  final int getBlockSize() {
		return BLOCK_SIZE;
	  }
		
	  @Override
	  final int getShiftSize() {
		return SHIFT_SIZE;
	  }
}
