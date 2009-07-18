package com.browseengine.bobo.query.scoring;

public class DefaultFacetTermScoringFunction implements
		FacetTermScoringFunction {

	public float score(int df, float boost) {
		return boost;
	}

	public float combine(float... scores) {
		float sum=0.0f;
		for (float score : scores){
			sum+=score;
		}
		return sum;
	}
}
