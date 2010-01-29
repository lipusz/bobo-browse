package com.browseengine.bobo.util;

import java.util.Arrays;

import org.apache.lucene.util.OpenBitSet;

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
	  public final int findValue(int val, int docId, int maxId)
	  {
	    while(docId <= maxId && _array[docId >> SHIFT_SIZE][docId & MASK] != val)
	    {
	      docId++;
	    }
	    return docId;
	  }
	  
	  @Override
	  public final int findValues(OpenBitSet bitset, int docId, int maxId)
	  {
	    while(docId <= maxId && !bitset.fastGet(_array[docId >> SHIFT_SIZE][docId & MASK]))
	    {
	      docId++;
	    }
	    return docId;
	  }
	  
	  @Override
	  public final int findValueRange(int minVal, int maxVal, int docId, int maxId)
	  {
	    while(docId <= maxId)
	    {
	      int val = _array[docId >> SHIFT_SIZE][docId & MASK];
	      if(val >= minVal && val <= maxVal) break;
	      docId++;
	    }
	    return docId;
	  }
	  
	  @Override
	  public final int findBits(int bits, int docId, int maxId)
	  {
	    while(docId <= maxId && (_array[docId >> SHIFT_SIZE][docId & MASK] & bits) == 0)
	    {
	      docId++;
	    }
	    return docId;
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

	  @Override
	  public int maxValue() {
		return Short.MAX_VALUE;
	  }
}
