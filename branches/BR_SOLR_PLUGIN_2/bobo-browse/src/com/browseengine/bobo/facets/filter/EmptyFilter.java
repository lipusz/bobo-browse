package com.browseengine.bobo.facets.filter;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;


import com.browseengine.bobo.docidset.EmptyDocIdSet;
import com.browseengine.bobo.docidset.RandomAccessDocIdSet;

public class EmptyFilter extends RandomAccessFilter 
{
	private static final long serialVersionUID = 1L;

	private static EmptyFilter instance = new EmptyFilter();
	
	private EmptyFilter()
	{
		
	}

	@Override
	public RandomAccessDocIdSet getRandomAccessDocIdSet(IndexReader reader) throws IOException 
	{
		return EmptyDocIdSet.getInstance();
	}
	
	public static EmptyFilter getInstance()
	{
		return instance;
	}
	
}
