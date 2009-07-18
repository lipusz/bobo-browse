package com.browseengine.bobo.query.scoring;

public interface FacetTermScoringFunction {
	public float score(int df,float boost);
	public float combine(float... scores);
}
