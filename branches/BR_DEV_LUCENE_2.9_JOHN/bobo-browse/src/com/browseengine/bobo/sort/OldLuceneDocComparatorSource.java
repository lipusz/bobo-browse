package com.browseengine.bobo.sort;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreDocComparator;
import org.apache.lucene.search.SortComparatorSource;

public class OldLuceneDocComparatorSource extends DocComparatorSource {

	private final SortComparatorSource _luceneComparatorSource;
	private final String _fieldname;
	public OldLuceneDocComparatorSource(String fieldname,SortComparatorSource luceneComparatorSource){
		_fieldname = fieldname;
		_luceneComparatorSource = luceneComparatorSource;
	}

	@Override
	public DocComparator getComparator(IndexReader reader, int docbase)
			throws IOException {
		final ScoreDocComparator compr = _luceneComparatorSource.newComparator(reader, _fieldname);
		return new DocComparator() {
			
			@Override
			public Comparable value(ScoreDoc doc) {
				return compr.sortValue(doc);
			}
			
			@Override
			public int compare(ScoreDoc doc1, ScoreDoc doc2) {
				return compr.compare(doc1,doc2);
			}
		};
	}

}
