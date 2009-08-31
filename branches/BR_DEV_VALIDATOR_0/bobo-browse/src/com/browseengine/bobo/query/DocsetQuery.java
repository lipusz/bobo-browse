package com.browseengine.bobo.query;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ComplexExplanation;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.ToStringUtils;

import com.browseengine.bobo.api.BoboIndexReader;

public class DocsetQuery extends Query {
	private final DocIdSetIterator _iter;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DocsetQuery(DocIdSet docSet) {
		this(docSet.iterator());
	}
	
	public DocsetQuery(DocIdSetIterator iter)
	{
		_iter=iter;
	}

	@Override
	public String toString(String field) {
		StringBuilder buffer = new StringBuilder();
	    buffer.append("docset query:");
	    buffer.append(ToStringUtils.boost(getBoost()));
	    return buffer.toString();
	}
	
	@Override
	protected Weight createWeight(Searcher searcher) throws IOException {
	    return new DocSetIteratorWeight(this,searcher.getSimilarity(),_iter);
	}
	
	private static class DocSetIteratorWeight implements Weight
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private final Query _query;
		private final DocIdSetIterator _iter;
		private final Similarity _similarity;
		
		private float _queryWeight;
		private float _queryNorm;
		
		DocSetIteratorWeight(Query query,Similarity similarity,DocIdSetIterator iter)
		{
			_query=query;
			_similarity=similarity;
			_iter=iter;
			_queryNorm=1.0f;
			_queryWeight=_query.getBoost();
		}
		
		public Explanation explain(IndexReader reader, int doc)
				throws IOException {
			// explain query weight
		      Explanation queryExpl = new ComplexExplanation
		        (true, getValue(), "docset query, product of:");
		      float boost=_query.getBoost();
		      if (boost != 1.0f) {
		        queryExpl.addDetail(new Explanation(boost,"boost"));
		      }
		      queryExpl.addDetail(new Explanation(_queryNorm,"queryNorm"));

		      return queryExpl;
		}

		public Query getQuery() {
			return _query;
		}

		public float getValue() {
			return _queryWeight;
		}

		public void normalize(float norm) {
			// we just take the boost, not going to normalize the score
			
			//_queryNorm = norm;
			//_queryWeight *= _queryNorm;
		}

		public Scorer scorer(IndexReader reader) throws IOException {
			return new DocSetIteratorScorer(_similarity,_iter,this,reader);
		}

		public float sumOfSquaredWeights() throws IOException {
			return _queryWeight * _queryWeight;
		}
		
		private static class DocSetIteratorScorer extends Scorer
		{
			private final DocIdSetIterator _iter;
			private final float _score;
			private final IndexReader _reader;
			DocSetIteratorScorer(Similarity similarity,DocIdSetIterator iter,Weight weight,IndexReader reader)
			{
				super(similarity);
				_iter=iter;
				_score=weight.getValue();
				_reader=reader;
			}
			
			@Override
			public int doc() {
				return _iter.doc();
			}

			@Override
			public Explanation explain(int doc) throws IOException {
				return null;
			}

			@Override
			public boolean next() throws IOException {
				while(true)
				{
					boolean hasNext=_iter.next();
					if (!hasNext)
					{
						return false;
					}
					else
					{
						if (!_reader.isDeleted(_iter.doc()))
						{
							return true;
						}
					}
				}
			}

			@Override
			public float score() throws IOException {
				return _score;
			}

			@Override
			public boolean skipTo(int target) throws IOException {
				boolean flag = _iter.skipTo(target);
				if (flag)
				{
					if (_reader.isDeleted(_iter.doc()))
					{
						return next();
					}
					else
					{
						return true;
					}
				}
				else
				{
					return false;
				}
			}
			
		}
	}
}
