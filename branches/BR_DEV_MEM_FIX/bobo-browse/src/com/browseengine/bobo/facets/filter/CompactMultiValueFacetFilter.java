package com.browseengine.bobo.facets.filter;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSetIterator;

import com.browseengine.bobo.docidset.EmptyDocIdSet;
import com.browseengine.bobo.docidset.RandomAccessDocIdSet;
import com.browseengine.bobo.facets.data.FacetDataCache;
import com.browseengine.bobo.util.BigSegmentedArray;

public class CompactMultiValueFacetFilter extends RandomAccessFilter {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final FacetDataCache _dataCache;
	private int _bits;
	private final int[] _index;
	private final BigSegmentedArray _orderArray;
	
	public CompactMultiValueFacetFilter(FacetDataCache dataCache,int index)
    {
      this(dataCache,new int[]{index});
    }
	
	public CompactMultiValueFacetFilter(FacetDataCache dataCache,int[] index)
	{
		_dataCache = dataCache;
		_orderArray = _dataCache.orderArray;
		_index = index;
		_bits = 0x0;
		for (int i : index)
		{
		  _bits |= 0x00000001 << (i-1);  
		}
	}
	
	private final static class CompactMultiValueFacetDocIdSetIterator extends DocIdSetIterator
	{
	    private final int _bits;
	    private int _doc;
	    private int _maxID;
	    private final BigSegmentedArray _orderArray;
	    
		public CompactMultiValueFacetDocIdSetIterator(FacetDataCache dataCache,int[] index,int bits) {
			_bits = bits;
			_doc = Integer.MAX_VALUE;
	        _maxID = -1;
	        _orderArray = dataCache.orderArray;
	        for (int i : index)
	        {
	          if (_doc > dataCache.minIDs[i]){
	            _doc = dataCache.minIDs[i];
	          }
	          if (_maxID < dataCache.maxIDs[i])
	          {
	            _maxID = dataCache.maxIDs[i];
	          }
	        }
	        _doc--;
	        if (_doc<0) _doc=-1;
		}
		
		@Override
		public final int doc()
		{
		  return _doc;
		}

		@Override
        public final boolean next() throws IOException {
		    while(_doc < _maxID) // not yet reached end
            {
                if ((_orderArray.get(++_doc) & _bits) != 0x0){
                    return true;
                }
            }
            return false;
        }

        @Override
        public final boolean skipTo(int id) throws IOException {
          if (_doc < id)
          {
            _doc=id-1;
          }
          
          while(_doc < _maxID) // not yet reached end
          {
            if ((_orderArray.get(++_doc) & _bits) != 0x0){
              return true;
            }
          }
          return false;
        }
	}
	
	@Override
	public RandomAccessDocIdSet getRandomAccessDocIdSet(IndexReader reader) throws IOException 
	{
		if (_index.length == 0)
		{
			return EmptyDocIdSet.getInstance();
		}
		else
		{
			return new RandomAccessDocIdSet()
			{
				@Override
				public DocIdSetIterator iterator() 
				{
					return new CompactMultiValueFacetDocIdSetIterator(_dataCache,_index,_bits);
				}
        @Override
        final public boolean get(int docId)
        {
          return (_orderArray.get(docId) & _bits) != 0x0;
        }
			};
		}
	}

}