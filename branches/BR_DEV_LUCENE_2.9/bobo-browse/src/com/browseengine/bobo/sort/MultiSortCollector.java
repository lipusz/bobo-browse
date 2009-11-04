package com.browseengine.bobo.sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;

import com.browseengine.bobo.sort.OneSortCollector.MyScoreDoc;
import com.browseengine.bobo.util.ListMerger;

public class MultiSortCollector extends Collector {

	private final LinkedList<DocIDPriorityQueue> _pqList;
	private final int _numHits;
	private int _totalHits;
	private MyScoreDoc _bottom;
	private MyScoreDoc _tmpScoreDoc;
	private final boolean[] _ascending;
	private boolean _queueFull;
	private DocComparator[] _currentComparators;
	private DocComparatorSource[] _compSources;
	private DocIDPriorityQueue _currentQueue;
	private DocComparator _currentMultiComparator;
	private final boolean _doScoring;
	private float _maxScore;
	private Scorer _scorer;
	  
	public MultiSortCollector(DocComparatorSource[] compSources,boolean[] reverse,int numHits,boolean doScoring){
		_numHits = numHits;
		_ascending = new boolean[reverse.length];
		for (int i=0;i<reverse.length;++i){
			_ascending[i]=!reverse[i];
		}
		_compSources = compSources;
	    _pqList = new LinkedList<DocIDPriorityQueue>();
	    _totalHits = 0;
	    _maxScore = 0.0f;
	    _queueFull = false;
	    _doScoring = doScoring;
	    _currentComparators = new DocComparator[_compSources.length];
	    _currentMultiComparator = new MultiDocIdComparator(_currentComparators,reverse);
	    _tmpScoreDoc = new MyScoreDoc();
	}
	
	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void collect(int doc) throws IOException {
		_totalHits++;
		float score;
	    if (_doScoring){
	 	   score = _scorer.score();
	 	   _maxScore+=score;
	    }
	    else{
	 	   score = 0.0f;
	    }
	    if (_queueFull){
	      _tmpScoreDoc.doc=doc;
	      _tmpScoreDoc.score=score;
	      _tmpScoreDoc.queue=_currentQueue;
	      
	      if (_currentMultiComparator.compare(_bottom,_tmpScoreDoc)>=0){
	        return;
	      }
	      MyScoreDoc tmp=_bottom;
	      _bottom = (MyScoreDoc)_currentQueue.replace(_tmpScoreDoc);
	      _tmpScoreDoc = tmp;
	    }
	    else{
	      _bottom = (MyScoreDoc)_currentQueue.add(new MyScoreDoc(doc,score,_currentQueue));
	      _queueFull = (_currentQueue.size() >= _numHits);
	    }
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		for (int i=0;i<_currentComparators.length;++i){
			_currentComparators[i]=_compSources[i].getComparator(reader,docBase);
		}
		_currentQueue = new DocIDPriorityQueue(_currentMultiComparator,_numHits, docBase);
		_pqList.add(_currentQueue);
	    _queueFull = false;
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		_scorer = scorer;
		for (DocComparator comparator : _currentComparators){
			comparator.setScorer(scorer);
		}
	}

	public TopDocs topDocs(){
	    ArrayList<Iterator<MyScoreDoc>> iterList = new ArrayList<Iterator<MyScoreDoc>>(_pqList.size());
	    for (DocIDPriorityQueue pq : _pqList){
	      int count = pq.size();
	      MyScoreDoc[] resList = new MyScoreDoc[count];
	      for (int i = count - 1; i >= 0; i--) { 
	    	  resList[i] = (MyScoreDoc)pq.pop();
	      }
	      iterList.add(Arrays.asList(resList).iterator());
	    }
	    
	    ArrayList<MyScoreDoc> resList = ListMerger.mergeLists(0, _numHits, iterList, new Comparator<MyScoreDoc>() {

	        public int compare(MyScoreDoc o1, MyScoreDoc o2) {
	          Comparable s1 = o1.getValue();
	          Comparable s2 = o2.getValue();
	          int r;
	          if (s1 == null) {
	            if (s2 == null) {
	              r = 0;
	            } else {
	              r = -1;
	            }
	          } else if (s2 == null) {
	            r = 1;
	          }
	          int v = s1.compareTo(s2);
	          if (v==0){
	            r = o1.doc + o1.queue.base - o2.doc - o2.queue.base;
	          } else {
	            r = v;
	          }
	          
	          return r;
	        }
	      });
			
	    for (MyScoreDoc doc : resList){
	      doc.doc += doc.queue.base;
	    }
	    return new TopDocs(_totalHits, resList.toArray(new ScoreDoc[resList.size()]), _maxScore);
	  }
}
