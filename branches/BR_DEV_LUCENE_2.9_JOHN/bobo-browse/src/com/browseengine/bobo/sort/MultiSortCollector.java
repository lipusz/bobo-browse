package com.browseengine.bobo.sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;

import com.browseengine.bobo.api.Browsable;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.MultiBoboBrowser;
import com.browseengine.bobo.impl.SortedFieldBrowseHitComparator;
import com.browseengine.bobo.util.ListMerger;

public class MultiSortCollector extends SortCollector {

	private static Logger logger = Logger.getLogger(MultiSortCollector.class);
    private int _totalCount;
    private final MultiBoboBrowser _multiBrowser;
    private final SortCollector[] _subCollectors;
    private final int[] _starts;
    private final int _offset;
    private final int _count;
    private final SortField[] _sort;
    private Scorer _scorer;
    private int _docBase;
    
	public MultiSortCollector(MultiBoboBrowser multiBrowser,SortField[] sort,int offset,int count,boolean doScoring){
		_sort = sort;
	    _offset=offset;
	    _count=count;
	    _multiBrowser = multiBrowser;
	    Browsable[] subBrowsers = _multiBrowser.getSubBrowsers();
	    _subCollectors = new SortCollector[subBrowsers.length];
	    for (int i=0;i<subBrowsers.length;++i)
	    {
	      _subCollectors[i] = subBrowsers[i].getSortCollector(sort, 0, _offset+_count,doScoring);
	    }
	    _starts = _multiBrowser.getStarts();
	    _totalCount = 0; 
	    _docBase = 0;
	}

	@Override
	public int getTotalHits() {
		return _totalCount;
	}

	@Override
	public TopDocs topDocs() {
		ArrayList<Iterator<BrowseHit>> iteratorList = new ArrayList<Iterator<BrowseHit>>(_subCollectors.length);
	    
	    for (int i=0;i<_subCollectors.length;++i)
	    {
	      int base = _starts[i];
	      try
	      {
	    	TopDocs subHits = _subCollectors[i].topDocs();
	    	
	    	ScoreDoc[] scoreDocs = subHits.scoreDocs;
	        for (ScoreDoc hit : scoreDocs)
	        {
	          hit.doc += base;
	          hit.setStoredFields(hit.getStoredFields());
	        }
	        iteratorList.add(Arrays.asList(subHits).iterator());
	      }
	      catch(IOException ioe)
	      {
	        logger.error(ioe.getMessage(),ioe);
	      }
	    }
	    
	    SortField[] sf = _sort;
	    if (sf==null || sf.length == 0)
	    {
	      sf=new SortField[]{SortField.FIELD_SCORE};
	    }
	    Comparator<BrowseHit> comparator = new SortedFieldBrowseHitComparator(sf);
	    
	    ArrayList<BrowseHit> mergedList = ListMerger.mergeLists(_offset, _count, iteratorList.toArray(new Iterator[iteratorList.size()]), comparator);
	    return mergedList.toArray(new BrowseHit[mergedList.size()]);
	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return _collector == null ? true : _collector.acceptsDocsOutOfOrder();
	}

	@Override
	public void collect(int doc) throws IOException {
		int docid = doc+_docBase;
	    int mapped = _multiBrowser.subDoc(docid);
	    int index = _multiBrowser.subSearcher(docid);
	    _subCollectors[index].setScorer(_scorer);
	    _subCollectors[index].collect(mapped);
	    _totalCount++;
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		_docBase = docBase;
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		_scorer = scorer;
	}
	
	
}
