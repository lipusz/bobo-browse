package com.browseengine.bobo.facets.filter;

import java.io.IOException;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BitVector;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.docidset.EmptyDocIdSet;
import com.browseengine.bobo.docidset.RandomAccessDocIdSet;
import com.browseengine.bobo.facets.data.FacetDataCache;
import com.browseengine.bobo.facets.data.MultiValueFacetDataCache;
import com.browseengine.bobo.facets.filter.FacetOrFilter.FacetOrDocIdSetIterator;
import com.browseengine.bobo.facets.impl.MultiValueFacetHandler;
import com.browseengine.bobo.util.BigNestedIntArray;

public class MultiValueORFacetFilter extends RandomAccessFilter
{

  private static final long serialVersionUID = 1L;
  private final MultiValueFacetHandler _facetHandler;
  private final String[] _vals;
  
  public MultiValueORFacetFilter(MultiValueFacetHandler facetHandler,String[] vals)
  {
	_facetHandler = facetHandler;
	_vals = vals;
  }
  
  private final static class MultiValueFacetDocIdSetIterator extends FacetOrDocIdSetIterator
  {
      private final BigNestedIntArray _nestedArray;
      public MultiValueFacetDocIdSetIterator(MultiValueFacetDataCache dataCache, int[] index,BitVector bs) 
      {
        super(dataCache,index,bs);
        _nestedArray = dataCache._nestedArray;
      }
      
      @Override
      final public int nextDoc() throws IOException {
          while(_doc < _maxID) // not yet reached end
          {
              if (_nestedArray.contains(++_doc, _bitset)){
                  return _doc;
              }
          }
          return DocIdSetIterator.NO_MORE_DOCS;
      }

      @Override
      final public int advance(int id) throws IOException {
        if (_doc < id)
        {
          _doc=id-1;
        }
        
        while(_doc < _maxID) // not yet reached end
        {
          if (_nestedArray.contains(++_doc, _bitset)){
            return _doc;
          }
        }
        return DocIdSetIterator.NO_MORE_DOCS;
      }
  }
  
  @Override
  public RandomAccessDocIdSet getRandomAccessDocIdSet(BoboIndexReader reader) throws IOException
  {
	final MultiValueFacetDataCache dataCache = _facetHandler.getFacetData(reader);
	final int[] index = FacetDataCache.convert(dataCache, _vals);
	final BigNestedIntArray nestedArray = dataCache._nestedArray;
	final BitVector bitset = new BitVector(dataCache.valArray.size());
	
	for (int i : index){
		bitset.set(i);
    } 
	
    if (index.length == 0)
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
		    public DocIdSetIterator iterator() throws IOException
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
                return new MultiValueFacetDocIdSetIterator(dataCache,index,bitset);
            }

            @Override
            final public boolean get(int docId)
            {
              return nestedArray.contains(docId,bitset);
            }
        };
    }
  }

}
