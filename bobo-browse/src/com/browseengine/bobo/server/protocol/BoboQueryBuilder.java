package com.browseengine.bobo.server.protocol;

import org.apache.lucene.search.Query;

public abstract class BoboQueryBuilder {
	public abstract Query parseQuery(String query,String defaultField);
}
