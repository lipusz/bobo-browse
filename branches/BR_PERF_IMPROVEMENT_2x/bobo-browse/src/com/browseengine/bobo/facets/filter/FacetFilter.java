package com.browseengine.bobo.facets.filter;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;

import com.browseengine.bobo.docidset.EmptyDocIdSet;
import com.browseengine.bobo.docidset.RandomAccessDocIdSet;
import com.browseengine.bobo.facets.data.FacetDataCache;
import com.browseengine.bobo.util.BigIntArray;

public class FacetFilter extends RandomAccessFilter 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected final FacetDataCache _dataCache;
	protected final int _index;
	
	public FacetFilter(FacetDataCache dataCache, int index)
	{
		_dataCache = dataCache;
		_index=index;
	}
	
	public static class FacetDocIdSetIterator extends DocIdSetIterator
	{
		protected int _doc;
		protected final FacetDataCache _dataCache;
		protected final int _index;
		protected final int _maxID;
		protected final BigIntArray _orderArray;
		
		public FacetDocIdSetIterator(FacetDataCache dataCache,int index)
		{
			_dataCache=dataCache;
			_index=index;
			_doc=Math.max(-1,_dataCache.minIDs[_index]-1);
			_maxID = _dataCache.maxIDs[_index];
			_orderArray = dataCache.orderArray;
		}
		
		@Override
		final public int doc() {
			return _doc;
		}
		/*
		protected boolean validate(int docid){
			return _dataCache.orderArray.get(docid) == _index;
		}
*/
		@Override
		public boolean next() throws IOException {
		    while(_doc < _maxID) // not yet reached end
			{
				_doc++;
				if (_orderArray.get(_doc) == _index){
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean skipTo(int id) throws IOException {
		  if (_doc < id)
		  {
		    _doc=id-1;
		  }
		  
		  while(_doc < _maxID) // not yet reached end
		  {
		    _doc++;
		    if (_orderArray.get(_doc) == _index){
		      return true;
		    }
		  }
		  return false;
		}

	}

	@Override
	public RandomAccessDocIdSet getRandomAccessDocIdSet(IndexReader reader) throws IOException 
	{
		if (_index < 0)
		{
		  final DocIdSet empty = EmptyDocIdSet.getInstance();
			return new RandomAccessDocIdSet()
			{
        @Override
        public boolean get(int docId)
        {
          return false;
        }

        @Override
        public DocIdSetIterator iterator()
        {
          return empty.iterator();
        }		  
			};
		}
		else
		{
			return new RandomAccessDocIdSet()
			{
				@Override
				public DocIdSetIterator iterator() 
				{
					return new FacetDocIdSetIterator(_dataCache,_index);
				}

        @Override
        final public boolean get(int docId)
        {
          return _dataCache.orderArray.get(docId) == _index;
        }
			};
		}
	}

}
