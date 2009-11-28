package com.browseengine.bobo.sort;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;

public class LuceneCustomDocComparatorSource extends DocComparatorSource {
	private final FieldComparatorSource _luceneComparatorSource;
	private final String _fieldname;
	public LuceneCustomDocComparatorSource(String fieldname,FieldComparatorSource luceneComparatorSource){
		_fieldname = fieldname;
		_luceneComparatorSource = luceneComparatorSource;
	}
	
	@Override
	public DocComparator getComparator(IndexReader reader, int docbase)
			throws IOException {
		final FieldComparator compr = _luceneComparatorSource.newComparator(_fieldname, 10, 0, LuceneCustomDocComparatorSource.this.isReverse());
		compr.setNextReader(reader, docbase);
		return new DocComparator() {
			
			@Override
			public Comparable value(ScoreDoc doc) {
				return compr.value(doc.doc);
			}
			
			@Override
			public int compare(ScoreDoc doc1, ScoreDoc doc2) {
				return compr.compare(doc1.doc, doc2.doc);
			}

			@Override
			public void setScorer(Scorer scorer) {
				compr.setScorer(scorer);
			}
		};
	}

}
