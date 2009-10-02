package com.browseengine.bobo.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.ReaderUtil;

import com.browseengine.bobo.api.BoboIndexReader;

public class BoboSearcher extends IndexSearcher{
	protected List<FacetHitCollector> _facetCollectors;
	protected IndexReader[] _subReaders;
	protected int[] _docStarts;
	
	public BoboSearcher(BoboIndexReader reader)
	{
		super(reader);
		_facetCollectors = new LinkedList<FacetHitCollector>();
		List<IndexReader> readerList = new ArrayList<IndexReader>();
		ReaderUtil.gatherSubReaders(readerList, reader);
		_subReaders = (IndexReader[])readerList.toArray(new IndexReader[readerList.size()]);
		_docStarts = new int[_subReaders.length];
	    int maxDoc = 0;
	    for (int i = 0; i < _subReaders.length; i++) {
	      _docStarts[i] = maxDoc;
	      maxDoc += _subReaders[i].maxDoc();
	    }
	}
	
	public void setFacetHitCollectorList(List<FacetHitCollector> facetHitCollectors)
	{
		if (facetHitCollectors != null)
		{
			_facetCollectors = facetHitCollectors;
		}
	}
	
	/**
	 * This method validates the doc against any multi-select enabled fields.
	 * @param docid
	 * @return false if not all a match on all fields. Facet count may still be collected.
	 */
	private static boolean validateAndIncrement(int docid,FacetHitCollector[] facetCollectors,boolean doValidate) throws IOException
	{
		if (doValidate)
		{
			int misses = 0;
			int _marker=-1;
			for (int i=0;i<facetCollectors.length;++i)
			{
				FacetHitCollector facetCollector = facetCollectors[i];
				if (facetCollector._postDocIDSetIterator==null) continue;
				if (facetCollector._postDocIDSetIterator.skipTo(docid))
				{
					if (facetCollector._postDocIDSetIterator.doc() != docid)
					{
						misses++;
						if (misses > 1) return false;
						else _marker=i;
					}
				}
				else
				{
					misses++;
					if (misses > 1) return false;
					else _marker=i;
				}
			}
			if (misses==1)
			{
				for (int i=0;i<facetCollectors.length;++i)
				{
					FacetHitCollector facetCollector = facetCollectors[i];
					if (_marker == i)
					{
						facetCollector._facetCountCollector.collect(docid);
					}
				}
			}
			else
			{
				for (FacetHitCollector facetCollector : facetCollectors)
				{
					facetCollector._facetCountCollector.collect(docid);
				}
			}
			return misses == 0;
		}
		else
		{
			for (FacetHitCollector facetCollector : facetCollectors)
			{
				facetCollector._facetCountCollector.collect(docid);
			}
			return true;
		}
	}

	@Override
	public void search(Weight weight, Filter filter, Collector collector)
			throws IOException {
		IndexReader reader=getIndexReader();
		
		boolean doValidate=false;
		FacetHitCollector[] facetCollectors=_facetCollectors.toArray(new FacetHitCollector[_facetCollectors.size()]);
		for (FacetHitCollector facetCollector : facetCollectors)
		{
			if (facetCollector._postDocIDSetIterator != null) 
			{
				doValidate=true;
				break;
			}
		}
		/*
		Scorer scorer = weight.scorer(reader,!collector.acceptsDocsOutOfOrder(), true);
	    if (scorer == null)
	      return;
*/
	    if (filter == null) {
	      for (int i = 0; i < _subReaders.length; i++) { // search each subreader
	            collector.setNextReader(_subReaders[i], _docStarts[i]);
	            Scorer scorer = weight.scorer(_subReaders[i], !collector.acceptsDocsOutOfOrder(), true);
	            if (scorer != null) {
	              scorer.score(collector);
	            }
	      }
	    	
	      while (scorer.next()) {
	    	int doc=scorer.doc();
	    	if (validateAndIncrement(doc,facetCollectors,doValidate))
	    	{
	    	  collector.collect(doc, scorer.score());
	    	}
	      }
	      return;
	    }

	    DocIdSetIterator filterDocIdIterator = filter.getDocIdSet(reader).iterator(); // CHECKME: use ConjunctionScorer here?
	    
	    boolean more = filterDocIdIterator.next() && scorer.skipTo(filterDocIdIterator.doc());

	    while (more) {
	      int filterDocId = filterDocIdIterator.doc();
	      if (filterDocId > scorer.doc() && !scorer.skipTo(filterDocId)) {
	        more = false;
	      } else {
	        int scorerDocId = scorer.doc();
	        if (scorerDocId == filterDocId) { // permitted by filter
	          if (validateAndIncrement(scorerDocId,facetCollectors,doValidate))
		      {
	            results.collect(scorerDocId, scorer.score());
		      }
	          more = filterDocIdIterator.next();
	        } else {
	          more = filterDocIdIterator.skipTo(scorerDocId);
	        }
	      }
	    }
	}
}
