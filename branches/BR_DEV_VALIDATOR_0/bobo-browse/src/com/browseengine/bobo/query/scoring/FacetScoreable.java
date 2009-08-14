package com.browseengine.bobo.query.scoring;

import java.util.Map;

public interface FacetScoreable {
	 BoboDocScorer getDocScorer(FacetTermScoringFunctionFactory scoringFunctionFactory,Map<String,Float> boostMap);
}
