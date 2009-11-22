package com.browseengine.bobo.sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.facets.FacetHandler;

public abstract class SortCollector extends Collector {
	protected Collector _collector = null;
	protected Map<String,DocComparator> _comparatorMap = new HashMap<String,DocComparator>();
	private SortField[] _sortFields;
	
	abstract public TopDocs topDocs();

	abstract public int getTotalHits();
	
	public static SortCollector buildSortCollector(SortField[] sort,int offset,int count,boolean forceScoring){
		boolean doScoring=forceScoring;
		
		for (SortField sf : sort){
			if (sf.getType() == SortField.SCORE) {
				doScoring= true;
			}
			
		}
		
		return null;
	}
	
	public void setCollector(Collector collector){
		_collector = collector;
	}
	
	public Collector getCollector(){
		return _collector; 
	}
	
	private static void fillInRuntimeFacetValues(BrowseHit[] hits,BoboIndexReader reader,Map<String,FacetHandler<?>> runtimeFacetHandlerMap){
	    for (BrowseHit hit : hits)
	    {
	      for (FacetHandler<?> facetHandler : runtimeFacetHandlerMap.values())
	      {
	        String[] values = facetHandler.getFieldValues(reader,hit.getDocid());
	        if (values != null && hit.getFieldValues() != null)
	        {
	          hit.getFieldValues().put(facetHandler.getName(), values);
	        }
	      }
	    }
	  }
	
	public BrowseHit[] buildHits(TopDocs topDocs,BoboIndexReader reader,Map<String,FacetHandler<?>> runtimeFacetHandlerMap,boolean fetchStoredFields) throws IOException{
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
	    ArrayList<BrowseHit> hitList = new ArrayList<BrowseHit>(scoreDocs.length);

	    Collection<FacetHandler<?>> facetHandlers= reader.getFacetHandlerMap().values();
	    for (ScoreDoc fdoc : scoreDocs)
	    {
	      BrowseHit hit=new BrowseHit();
	      if (fetchStoredFields){
	         hit.setStoredFields(reader.document(fdoc.doc));
	      }
	      Map<String,String[]> map = new HashMap<String,String[]>();
	      for (FacetHandler<?> facetHandler : facetHandlers)
	      {
	          map.put(facetHandler.getName(),facetHandler.getFieldValues(reader,fdoc.doc));
	      }
	      hit.setFieldValues(map);
	      hit.setDocid(fdoc.doc);
	      hit.setScore(fdoc.score);
	      for (SortField f : _sortFields)
	      {
	        if (f.getType() != SortField.SCORE && f.getType() != SortField.DOC)
	        {
	          String fieldName = f.getField();
	          DocComparator comparator = _comparatorMap.get(fieldName);
	          if (comparator!=null){
	            hit.addComparable(fieldName, comparator.value(fdoc));
	          }
	        }
	      }
	      hitList.add(hit);
	    }
	    BrowseHit[] hits = hitList.toArray(new BrowseHit[hitList.size()]);
	    if (runtimeFacetHandlerMap!=null && runtimeFacetHandlerMap.size()>0){
	      fillInRuntimeFacetValues(hits,reader,runtimeFacetHandlerMap);
	    }
	    return hits;
	  }
}
