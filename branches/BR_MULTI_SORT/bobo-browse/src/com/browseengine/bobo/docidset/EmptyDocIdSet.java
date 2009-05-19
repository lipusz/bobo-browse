package com.browseengine.bobo.docidset;

import java.io.IOException;

import org.apache.lucene.search.DocIdSetIterator;


public class EmptyDocIdSet extends RandomAccessDocIdSet 
{
  private static EmptyDocIdSet SINGLETON=new EmptyDocIdSet();

  private static class EmptyDocIdSetIterator extends DocIdSetIterator
  {
    @Override
    public int doc() {	return -1; }

    @Override
    public boolean next() throws IOException { return false;  }

    @Override
    public boolean skipTo(int target) throws IOException { return false; }
  }

  private static EmptyDocIdSetIterator SINGLETON_ITERATOR = new EmptyDocIdSetIterator();

  private EmptyDocIdSet() { }

  public static EmptyDocIdSet getInstance()
  {
    return SINGLETON;
  }

  @Override
  public DocIdSetIterator iterator() 
  {
    return SINGLETON_ITERATOR;
  }

  @Override
  public boolean get(int docId)
  {
    return false;
  }

}
