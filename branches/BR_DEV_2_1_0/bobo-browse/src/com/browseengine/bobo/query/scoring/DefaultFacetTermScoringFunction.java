package com.browseengine.bobo.query.scoring;

import java.util.Arrays;

import org.apache.lucene.search.Explanation;

public class DefaultFacetTermScoringFunction implements FacetTermScoringFunction {

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

	public Explanation explain(int df, float boost) {
		Explanation expl = new Explanation();
		expl.setValue(score(df,boost));
		expl.setDescription("boost value of: "+boost);
		return expl;
	}

	public Explanation explain(float... scores) {
		Explanation expl = new Explanation();
		expl.setValue(combine(scores));
		expl.setDescription("sum of: "+Arrays.toString(scores));
		return expl;
	}
}
