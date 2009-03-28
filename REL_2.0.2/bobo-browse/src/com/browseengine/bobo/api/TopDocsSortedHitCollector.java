package com.browseengine.bobo.api;

import java.io.IOException;

import org.apache.lucene.search.HitCollector;

public abstract class TopDocsSortedHitCollector extends HitCollector
{

  @Override
  abstract public void collect(int doc, float score);
  
  abstract public BrowseHit[] getTopDocs() throws IOException;
  
  abstract public int getTotalHits();  
}
