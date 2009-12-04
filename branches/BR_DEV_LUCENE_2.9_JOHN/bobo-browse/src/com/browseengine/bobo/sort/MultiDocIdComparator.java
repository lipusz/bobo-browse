package com.browseengine.bobo.sort;

import org.apache.lucene.search.ScoreDoc;


public class MultiDocIdComparator extends DocComparator implements Cloneable{
	private DocComparator[] _comparators;
	public MultiDocIdComparator(DocComparator[] comparators){
		_comparators = comparators;
	}
	
	private MultiDocIdComparator(){
		
	}
	
	
	@Override
	public Object clone(){
		DocComparator[] copy = _comparators == null ? null : new DocComparator[_comparators.length];
		if (copy!=null){
			System.arraycopy(_comparators, 0, copy, 0, _comparators.length);
		}
		
		MultiDocIdComparator cloneObj = new MultiDocIdComparator();
		cloneObj._comparators = copy;
		return cloneObj;
	}
	
	public int compare(ScoreDoc doc1, ScoreDoc doc2) {
		int v=0;
		for (int i=0;i<_comparators.length;++i){
			v=_comparators[i].compare(doc1, doc2);
			if (v!=0) return v;
		}
		return v;
	}

	@Override
	public Comparable value(final ScoreDoc doc) {
		return new Comparable(){

			public int compareTo(Object o) {
				ScoreDoc otherDoc = (ScoreDoc)o;
				int v=0;
				Comparable c1,c2;
				for (int i=0;i<_comparators.length;++i){
					c1 = _comparators[i].value(doc);
					c2 = _comparators[i].value(otherDoc);
					v = c1.compareTo(c2);
					if (v!=0) {
						break;
					}
				}
				return v;
			}
			
		};
	}
}
