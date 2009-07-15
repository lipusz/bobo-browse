package com.browseengine.bobo.query;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.Random;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.ToStringUtils;

import com.browseengine.bobo.docidset.FilteredDocSetIterator;

/**
 * A query that matches all documents.
 * 
 */
public final class FastMatchAllDocsQuery extends Query
{

  private static final long serialVersionUID = 1L;
  private final int[] _deletedDocs;
  
  public FastMatchAllDocsQuery(int[] deletedDocs, int maxDoc)
  {
    _deletedDocs = deletedDocs;
  }

  public final static class FastMatchAllScorer extends Scorer
  {
    private int _deletedIndex;
    private boolean _moreDeletions;
    int               _doc;
    final float       _score;
    final int[] _deletedDocs;
    private final int _maxDoc;
    private final int _delLen;

    public FastMatchAllScorer(int maxdoc, int[] delDocs, float score)
    {
      this(maxdoc,delDocs,new DefaultSimilarity(),score);	
    }
    
    public FastMatchAllScorer(int maxdoc, int[] delDocs,Similarity similarity, float score)
    {
      super(similarity);
      _doc = -1;
      _deletedDocs = delDocs;
      _deletedIndex = 0;
      _moreDeletions = _deletedDocs != null && _deletedDocs.length > 0;
      _delLen = _deletedDocs != null ? _deletedDocs.length : 0;
      _score = score;
      _maxDoc = maxdoc;
    }

    public Explanation explain(int doc)
    {
      return null; // not called... see MatchAllDocsWeight.explain()
    }

    public final int doc()
    {
      return _doc;
    }

    public final boolean next()
    {
      while(++_doc < _maxDoc)
      {
        if(!_moreDeletions || _doc < _deletedDocs[_deletedIndex]) 
        {
          return true;
        }
        else // _moreDeletions == true && _doc >= _deletedDocs[_deletedIndex]
        {
          while(_moreDeletions && _doc > _deletedDocs[_deletedIndex]) // catch up _deletedIndex to _doc
          {
            _deletedIndex++;
            _moreDeletions = _deletedIndex < _delLen;
          }
          if(!_moreDeletions || _doc < _deletedDocs[_deletedIndex])
          { 
            return true;
          }
        }
      }
      return false;
    }

    public final float score()
    {
      return _score;
    }
    
    public final boolean skipTo(int target)
    {
      if(target > _doc)
      {
        _doc = target - 1;
        return next();
      }
      
      return (target == _doc) ? next() : false;
    }

  }

  private class FastMatchAllDocsWeight implements Weight
  {
    private static final long serialVersionUID = 1L;
    private Similarity _similarity;
    private float      _queryWeight;
    private float      _queryNorm;

    public FastMatchAllDocsWeight(Searcher searcher)
    {
      this._similarity = searcher.getSimilarity();
    }

    public String toString()
    {
      return "weight(" + FastMatchAllDocsQuery.this + ")";
    }

    public Query getQuery()
    {
      return FastMatchAllDocsQuery.this;
    }

    public float getValue()
    {
      return _queryWeight;
    }

    public float sumOfSquaredWeights()
    {
      _queryWeight = getBoost();
      return _queryWeight * _queryWeight;
    }

    public void normalize(float queryNorm)
    {
      this._queryNorm = queryNorm;
      _queryWeight *= this._queryNorm;
    }

    public Scorer scorer(IndexReader reader)
    {
      return new FastMatchAllScorer(reader.maxDoc(), _deletedDocs, _similarity, getValue());
    }

    public Explanation explain(IndexReader reader, int doc)
    {
      // explain query weight
      Explanation queryExpl = new Explanation(getValue(), "FastMatchAllDocsQuery");
      if (getBoost() != 1.0f)
      {
        queryExpl.addDetail(new Explanation(getBoost(), "boost"));
      }
      queryExpl.addDetail(new Explanation(_queryNorm, "queryNorm"));

      return queryExpl;
    }
  }

  protected Weight createWeight(Searcher searcher)
  {
    return new FastMatchAllDocsWeight(searcher);
  }

  public void extractTerms(Set terms)
  {
  }

  public String toString(String field)
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append("MatchAllDocsQuery");
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

  public boolean equals(Object o)
  {
    if (!(o instanceof FastMatchAllDocsQuery))
      return false;
    FastMatchAllDocsQuery other = (FastMatchAllDocsQuery) o;
    return this.getBoost() == other.getBoost();
  }

  public int hashCode()
  {
    return Float.floatToIntBits(getBoost()) ^ 0x1AA71190;
  }
  
  private static class TestDocIdSetIterator extends FilteredDocSetIterator
  {

	private final IntSet _dupDocs;
	private final int _min;
	private final int _max;
    public TestDocIdSetIterator(IntSet dupDocs,DocIdSetIterator innerIter)
    {
      super(innerIter);
      _dupDocs = dupDocs;
      if (_dupDocs!=null && _dupDocs.size() > 0)
      {
    	int[] arr=_dupDocs.toArray(new int[_dupDocs.size()]);  
    	_min=arr[0];
    	_max=arr[arr.length-1];
      }
      else
      {
    	  _min=Integer.MAX_VALUE;
    	  _max=-1;
      }
    }
    
    @Override
    protected final boolean match(int docid)
    {
      return !(_dupDocs != null && docid>=_min && docid<=_max && _dupDocs.contains(docid));
    //	 return !(_dupDocs != null && _dupDocs.contains(docid));
    //	return true;
    }
  }
  
  public static void main(String[] args) throws Exception{
	int maxDoc = 5000000;
	IntSet delSet = new IntOpenHashSet();
	int numDel = 1000;
	Random rand = new Random();
	for (int i=0;i<numDel;++i)
	{
		delSet.add(i*30);
	}
	long numIter = 1000000;
	int[] delArray = delSet.toIntArray();
	for (long i=0;i<numIter;++i)
	{
		long start=System.currentTimeMillis();
	  FastMatchAllScorer innerIter = new FastMatchAllScorer(maxDoc,delArray,1.0f);
	//  TestDocIdSetIterator testIter = new TestDocIdSetIterator(delSet,innerIter);
/*	  while(testIter.next())
	  {
		  testIter.doc();
	  }
*/
	  for (int k=0;k<maxDoc;++k)
	  {
	    innerIter.skipTo(k*3);
	  }
		long end=System.currentTimeMillis();
		System.out.println("took: "+(end-start));
	}
  }
}
