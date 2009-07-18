package com.browseengine.bobo.query.scoring;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.BrowseSelection;
import com.browseengine.bobo.facets.FacetHandler;

public class FacetTermQuery extends Query {
	private final String _name;
	private final BrowseSelection _sel;
	private final FacetTermScoringFunctionFactory _scoringFactory;
	private final Map<String,Float> _boostMap;
	
	public FacetTermQuery(String name,BrowseSelection sel,Map<String,Float> boostMap,FacetTermScoringFunctionFactory scoringFactory){
		_name = name;
		_sel = sel;
		_scoringFactory = scoringFactory;
		_boostMap = boostMap;
	}
	
	private final float getBoost(String term){
		if (_boostMap == null){
			return 1.0f;
		}
		Float boost = _boostMap.get(term);
		if (boost!=null){
			return boost.floatValue();
		}
		return 1.0f;
	}

	@Override
	public String toString(String fieldname) {
		return String.valueOf(_sel);
	}
	
	private class FacetTermWeight implements Weight{

		public Explanation explain(IndexReader reader, int arg1)
				throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		public Query getQuery() {
			return FacetTermQuery.this;
		}

		public float getValue() {
			// TODO Auto-generated method stub
			return 0;
		}

		public void normalize(float score) {
			// TODO Auto-generated method stub
			
		}

		public Scorer scorer(IndexReader reader) throws IOException {
			if (reader instanceof BoboIndexReader){
			  BoboIndexReader boboReader = (BoboIndexReader)reader;
			  FacetHandler fhandler = boboReader.getFacetHandler(FacetTermQuery.this._name);
			  if (fhandler!=null){
				  
			  }
			  return null;
			}
			else{
			  throw new IOException("index reader not instance of "+BoboIndexReader.class);
			}
		}

		public float sumOfSquaredWeights() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
	private class FacetTermScorer extends Scorer{

		protected FacetTermScorer(Similarity similarity) {
			super(similarity);
		}

		@Override
		public Explanation explain(int arg0) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public float score() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int doc() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean next() throws IOException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean skipTo(int arg0) throws IOException {
			// TODO Auto-generated method stub
			return false;
		}
		
	}

}
