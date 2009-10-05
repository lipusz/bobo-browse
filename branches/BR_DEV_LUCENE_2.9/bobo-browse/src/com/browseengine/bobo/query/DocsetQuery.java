package com.browseengine.bobo.query;

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
import org.apache.lucene.util.ToStringUtils;

public class DocsetQuery extends Query {
	private final DocIdSetIterator _iter;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DocsetQuery(DocIdSet docSet) throws IOException{
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
	public Weight createWeight(Searcher searcher) throws IOException {
	    return new DocSetIteratorWeight(this,searcher.getSimilarity(),_iter);
	}
	
	private static class DocSetIteratorWeight extends Weight
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

		@Override
		public Scorer scorer(IndexReader reader,boolean scoreDocsInOrder,
			      boolean topScorer) throws IOException {
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
			public int docID() {
				return _iter.docID();
			}

			@Override
			public Explanation explain(int doc) throws IOException {
				return null;
			}

			@Override
			public int nextDoc() throws IOException {
				while(true)
				{
					int docid = _iter.nextDoc();
					if (docid==DocIdSetIterator.NO_MORE_DOCS)
					{
						return DocIdSetIterator.NO_MORE_DOCS;
					}
					else
					{
						if (!_reader.isDeleted(docid))
						{
							return docid;
						}
					}
				}
			}

			@Override
			public float score() throws IOException {
				return _score;
			}

			@Override
			public int advance(int target) throws IOException {
				int docid = _iter.advance(target);
				if (docid!=DocIdSetIterator.NO_MORE_DOCS)
				{
					if (_reader.isDeleted(docid))
					{
						return nextDoc();
					}
					else
					{
						return docid;
					}
				}
				else
				{
					return docid;
				}
			}
			
		}
	}
}
