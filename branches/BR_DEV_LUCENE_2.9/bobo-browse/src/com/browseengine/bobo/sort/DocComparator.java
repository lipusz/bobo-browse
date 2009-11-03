package com.browseengine.bobo.sort;

import org.apache.lucene.search.Scorer;

public abstract class DocComparator implements DocIdComparator{
  public abstract int compare(int doc1, int doc2);
  
  public abstract Comparable value(int doc);
  
  public void setScorer(Scorer scorer){
	  
  }
}
