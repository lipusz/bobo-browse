package com.browseengine.bobo.api;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDocComparator;
import org.apache.lucene.search.SortField;

import com.browseengine.bobo.query.FastMatchAllDocsQuery;

public interface FacetHandlerContext {
  IndexReader getIndexReader();
  FacetHandlerHome getFacetHandlerHome();
  ScoreDocComparator getDefaultScoreDocComparator(SortField f) throws IOException;
  FastMatchAllDocsQuery getFastMatchAllDocsQuery();
}
