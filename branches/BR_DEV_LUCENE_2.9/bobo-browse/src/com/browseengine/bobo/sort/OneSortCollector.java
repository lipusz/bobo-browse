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

import com.browseengine.bobo.util.ListMerger;

public class OneSortCollector extends Collector {
  private final LinkedList<DocIDPriorityQueue> _pqList;
  private final int _numHits;
  private int _totalHits;
  private int _bottom;
  private final boolean _ascending;
  private boolean _queueFull;
  private DocComparator _currentComparator;
  private DocComparatorSource _compSource;
  private DocIDPriorityQueue _currentQueue;
	
  static class NonScoreDoc extends ScoreDoc {
    final DocIDPriorityQueue queue;
    final Comparable value;

    public NonScoreDoc(int docid, DocIDPriorityQueue queue) {
      super(docid, 0.0f);
      this.queue = queue;
      this.value = queue.sortValue(docid);
    }
  }
	
  public OneSortCollector(DocComparatorSource compSource,int numHits, boolean reverse) {
    _compSource = compSource;
    _pqList = new LinkedList<DocIDPriorityQueue>();
    _numHits = numHits;
    _totalHits = 0;
    _queueFull = false;
    _ascending = !reverse;
  }

  @Override
  public boolean acceptsDocsOutOfOrder() {
    return true;
  }

  @Override
  public void collect(int doc) throws IOException {
    _totalHits++;
    if (_queueFull){
      int v = _currentComparator.compare(_bottom,doc);
      if (v==0 || ((v>0) && _ascending)){
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
    _currentComparator = _compSource.getComparator(reader,docBase);
    _currentQueue = new DocIDPriorityQueue(_currentComparator,
                                           _numHits, docBase);
    _pqList.add(_currentQueue);
    _queueFull = false;
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException {
	  _currentComparator.setScorer(scorer);
  }

  public int getTotalHits(){
    return _totalHits;
  }
	
  public ArrayList<NonScoreDoc> getTop(){
    ArrayList<Iterator<NonScoreDoc>> iterList = new ArrayList<Iterator<NonScoreDoc>>(_pqList.size());
    for (DocIDPriorityQueue pq : _pqList){
      int count = pq.size();
      NonScoreDoc[] resList = new NonScoreDoc[count];
      for (int i = count - 1; i >= 0; i--) { 
        final int docID = pq.pop();
        resList[i] = new NonScoreDoc(docID, pq);
      }
      iterList.add(Arrays.asList(resList).iterator());
    }
    
    final int revMult = _ascending ? 1 : -1;
    ArrayList<NonScoreDoc> resList = ListMerger.mergeLists(0, _numHits, iterList, new Comparator<NonScoreDoc>() {

        public int compare(NonScoreDoc o1, NonScoreDoc o2) {
          Comparable s1 = o1.value;
          Comparable s2 = o2.value;
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
          
          return revMult * r;
        }
      });
		
    for (NonScoreDoc doc : resList){
      doc.doc += doc.queue.base;
    }
    return resList;
  }
}
