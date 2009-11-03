package com.browseengine.bobo.sort;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

public class MultiSortCollector extends Collector {

	private final LinkedList<DocIDPriorityQueue> _pqList;
	private final int _numHits;
	private int _totalHits;
	private int _bottom;
	private final boolean[] _ascending;
	private boolean _queueFull;
	private DocComparator[] _currentComparators;
	private DocComparatorSource[] _compSources;
	private DocIDPriorityQueue _currentQueue;
	private DocIdComparator _currentMultiComparator;
	  
	public MultiSortCollector(DocComparatorSource[] compSources,boolean[] reverse,int numHits){
		_numHits = numHits;
		_ascending = new boolean[reverse.length];
		for (int i=0;i<reverse.length;++i){
			_ascending[i]=!reverse[i];
		}
		_compSources = compSources;
	    _pqList = new LinkedList<DocIDPriorityQueue>();
	    _totalHits = 0;
	    _queueFull = false;
	    _currentComparators = new DocComparator[_compSources.length];
	    _currentMultiComparator = new MultiDocIdComparator(_currentComparators,reverse);
	}
	
	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void collect(int doc) throws IOException {
		_totalHits++;
	    if (_queueFull){
	      if (_currentMultiComparator.compare(_bottom,doc)>=0){
	        return;
	      }
	      _bottom = _currentQueue.replace(doc);
	    }
	    else{
	      _bottom = _currentQueue.add(doc);
	      _queueFull = (_currentQueue.size() >= _numHits);
	    }
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		for (int i=0;i<_currentComparators.length;++i){
			_currentComparators[i]=_compSources[i].getComparator(reader,docBase);
		}
	    _queueFull = false;
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		for (DocComparator comparator : _currentComparators){
			comparator.setScorer(scorer);
		}
	}

}
