package com.browseengine.bobo.facets.filter;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSetIterator;

import com.browseengine.bobo.docidset.RandomAccessDocIdSet;
import com.browseengine.bobo.facets.data.FacetDataCache;
import com.browseengine.bobo.util.BigIntArray;

public final class FacetRangeFilter extends RandomAccessFilter 
{

	private static final long serialVersionUID = 1L;
	private final FacetDataCache _dataCache;
	private final int _start;
	private final int _end;
	
	public FacetRangeFilter(FacetDataCache dataCache, int start, int end)
	{
		_dataCache = dataCache;
		_start = start;
		_end = end;
	}
	
	private final static class FacetRangeDocIdSetIterator extends DocIdSetIterator
	{
		private int _doc = -1;
		private int _totalFreq;
		private int _minID = Integer.MAX_VALUE;
		private int _maxID = -1;
		private final int _start;
		private final int _end;
		private final BigIntArray _orderArray;
		
		
		FacetRangeDocIdSetIterator(int start,int end,FacetDataCache dataCache)
		{
			_totalFreq = 0;
			_start=start;
			_end=end;
			for (int i=start;i<=end;++i)
			{
				_totalFreq +=dataCache.freqs[i];
				_minID = Math.min(_minID, dataCache.minIDs[i]);
				_maxID = Math.max(_maxID, dataCache.maxIDs[i]);
			}
			_doc=Math.max(-1,_minID-1);
			_orderArray = dataCache.orderArray;
		}
		
		@Override
		final public int doc() {
			return _doc;
		}

		@Override
		final public boolean next() throws IOException {
			int index;
            while(_doc < _maxID) // not yet reached end
			{
				index=_orderArray.get(++_doc);
				if (index>=_start && index<=_end) return true;
			}
			return false;
		}

		@Override
		final public boolean skipTo(int id) throws IOException {
		  if (_doc < id)
		  {
		    _doc=id-1;
		  }
		  
		  int index;
		  while(_doc < _maxID) // not yet reached end
		  {
		    index=_orderArray.get(++_doc);
		    if (index>=_start && index<=_end) return true;
		  }
		  return false;
		}
		
	}

  @Override
  public RandomAccessDocIdSet getRandomAccessDocIdSet(IndexReader reader) throws IOException
  {
    return new RandomAccessDocIdSet()
    {
      @Override
      final public boolean get(int docId)
      {
        int index = _dataCache.orderArray.get(docId);
        return index >= _start && index <= _end;
      }

      @Override
      public DocIdSetIterator iterator()
      {
        return new FacetRangeDocIdSetIterator(_start,_end,_dataCache);
      }
      
    };
  }

	
}