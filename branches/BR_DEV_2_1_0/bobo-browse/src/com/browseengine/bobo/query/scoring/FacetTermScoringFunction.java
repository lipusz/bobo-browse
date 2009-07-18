package com.browseengine.bobo.query.scoring;

import org.apache.lucene.search.Explanation;

public interface FacetTermScoringFunction {
	public float score(int df,float boost);
	public Explanation explain(int df,float boost);
	public float combine(float... scores);
	public Explanation explain(float...scores);
}
