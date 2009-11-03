package com.browseengine.bobo.sort;


public class MultiDocIdComparator implements DocIdComparator{
	private DocIdComparator[] _comparators;
	private int[] _revMult;
	public MultiDocIdComparator(DocIdComparator[] comparators,boolean[] reverse){
		_comparators = comparators;
		_revMult = new int[reverse.length];
		for (int i=0;i<_revMult.length;++i){
			_revMult[i]=reverse[i] ? -1 : 1;
		}
	}
	
	public int compare(int doc1, int doc2) {
		int v=0;
		for (int i=0;i<_comparators.length;++i){
			v=_comparators[i].compare(doc1, doc2);
			if (v!=0) return (v*_revMult[i]);
		}
		return v;
	}
}
