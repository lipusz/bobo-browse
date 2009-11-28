package com.browseengine.bobo.sort;

import org.apache.lucene.search.ScoreDoc;


public class MultiDocIdComparator extends DocComparator implements Cloneable{
	private DocComparator[] _comparators;
	private int[] _revMult;
	public MultiDocIdComparator(DocComparator[] comparators,boolean[] reverse){
		_comparators = comparators;
		_revMult = new int[reverse.length];
		for (int i=0;i<_revMult.length;++i){
			_revMult[i]=reverse[i] ? -1 : 1;
		}
	}
	
	private MultiDocIdComparator(){
		
	}
	
	
	@Override
	public Object clone(){
		DocComparator[] copy = _comparators == null ? null : new DocComparator[_comparators.length];
		if (copy!=null){
			System.arraycopy(_comparators, 0, copy, 0, _comparators.length);
		}
		int[] copyRev = _revMult == null ? null : new int[_revMult.length];
		if (copyRev!=null){
			System.arraycopy(_revMult, 0, copyRev, 0, _revMult.length);
		}
		MultiDocIdComparator cloneObj = new MultiDocIdComparator();
		cloneObj._comparators = copy;
		cloneObj._revMult = copyRev;
		return cloneObj;
	}
	
	public int compare(ScoreDoc doc1, ScoreDoc doc2) {
		int v=0;
		for (int i=0;i<_comparators.length;++i){
			v=_comparators[i].compare(doc1, doc2);
			if (v!=0) return (v*_revMult[i]);
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
						v*=_revMult[i];
						break;
					}
				}
				return v;
			}
			
		};
	}
}
