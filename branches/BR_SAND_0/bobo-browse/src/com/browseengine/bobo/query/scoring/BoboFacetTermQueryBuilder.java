package com.browseengine.bobo.query.scoring;

import org.apache.lucene.search.Query;

public interface BoboFacetTermQueryBuilder {
	Query buildFacetTermQuery(FacetTermScoringFunctionFactory scoreFunctionFactory);
}
