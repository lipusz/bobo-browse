package com.browseengine.bobo.api;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.ScoreDocComparator;
import org.apache.lucene.search.SortField;

import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.query.FastMatchAllDocsQuery;

public class MultiBoboReader extends MultiReader implements FacetHandlerContext,FacetHandlerHome{
	private Set<String> _facetNames;
	public MultiBoboReader(BoboIndexReader[] subReaders) {
		super(subReaders);
		init();
	}

	public MultiBoboReader(BoboIndexReader[] subReaders, boolean closeSubReaders) {
		super(subReaders, closeSubReaders);
		init();
	}
	
	private void init(){
		_facetNames = new HashSet<String>();
		for (IndexReader subReader : subReaders){
			_facetNames.addAll(((BoboIndexReader)subReader).getFacetNames());
		}
	}

	public ScoreDocComparator getDefaultScoreDocComparator(SortField f)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public FacetHandlerHome getFacetHandlerHome() {
		return this;
	}

	public FastMatchAllDocsQuery getFastMatchAllDocsQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	public IndexReader getIndexReader() {
		return this;
	}

	public FacetHandler getFacetHandler(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getFacetNames() {
		return _facetNames;
	}
}
