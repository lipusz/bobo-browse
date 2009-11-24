package com.browseengine.bobo.sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;

import com.browseengine.bobo.api.BoboBrowser;
import com.browseengine.bobo.api.BoboCustomSortField;
import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.Browsable;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.sort.DocComparatorSource.DocIdDocComparatorSource;
import com.browseengine.bobo.sort.DocComparatorSource.RelevanceDocComparatorSource;

public abstract class SortCollector extends Collector {
	protected Collector _collector = null;
	protected Map<String,DocComparator> _comparatorMap = new HashMap<String,DocComparator>();
	private SortField[] _sortFields;
	
	abstract public TopDocs topDocs();

	abstract public int getTotalHits();
	
	private static DocComparatorSource getNonFacetComparatorSource(SortField sf){
		String fieldname = sf.getField();
		Locale locale = sf.getLocale();
		if (locale != null) {
	      // TODO: it'd be nice to allow FieldCache.getStringIndex
	      // to optionally accept a Locale so sorting could then use
	      // the faster StringComparator impls
	      return new DocComparatorSource.StringLocaleComparatorSource(fieldname, locale);
		}
		
		int type = sf.getType();

	    switch (type) {
	    case SortField.INT:
	      return new DocComparatorSource.IntDocComparatorSource(fieldname);
	
	    case SortField.FLOAT:
	      return new DocComparatorSource.FloatDocComparatorSource(fieldname);
	
	    case SortField.LONG:
	      return new DocComparatorSource.LongDocComparatorSource(fieldname);
	
	    case SortField.DOUBLE:
	      return new DocComparatorSource.LongDocComparatorSource(fieldname);
	
	    case SortField.BYTE:
	      return new DocComparatorSource.ByteDocComparatorSource(fieldname);
	
	    case SortField.SHORT:
	      return new DocComparatorSource.ShortDocComparatorSource(fieldname);
	
	    case SortField.CUSTOM:
	      FieldComparatorSource compSource = sf.getComparatorSource();
	      assert compSource != null;
	      return new LuceneCustomDocComparatorSource(fieldname, compSource, sf.getReverse());
	      
	    case SortField.STRING:
	      return new DocComparatorSource.StringOrdComparatorSource(fieldname);
	
	    case SortField.STRING_VAL:
	      return new DocComparatorSource.StringValComparatorSource(fieldname);
	        
	    default:
	      throw new IllegalStateException("Illegal sort type: " + type);
	    }
	}
	
	private static DocComparatorSource getComparatorSource(Browsable browser,SortField sf){
		DocComparatorSource compSource = null;
		if (SortField.FIELD_DOC.equals(sf)){
			compSource = new DocIdDocComparatorSource();
		}
		else if (SortField.FIELD_SCORE.equals(sf)){
			compSource = new RelevanceDocComparatorSource();
		}
		else if (sf instanceof BoboCustomSortField){
			BoboCustomSortField custField = (BoboCustomSortField)sf;
			DocComparatorSource src = custField.getCustomComparatorSource();
			assert src!=null;
			return src;
		}
		else{
			Set<String> facetNames = browser.getFacetNames();
			String sortName = sf.getField();
			if (facetNames.contains(sortName)){
				FacetHandler<?> handler = browser.getFacetHandler(sortName);
				assert handler!=null;
				return handler.getDocComparatorSource();
			}
			else{		// default lucene field
				return getNonFacetComparatorSource(sf);
			}
		}
		compSource.setReverse(sf.getReverse());
		return compSource;
	}
	
	public static SortCollector buildSortCollector(Browsable browser,SortField[] sort,int offset,int count,boolean forceScoring){
		boolean doScoring=forceScoring;
		
		for (SortField sf : sort){
			if (sf.getType() == SortField.SCORE) {
				doScoring= true;
			}	
		}

		if (sort==null || sort.length==0){
			sort = new SortField[]{SortField.FIELD_DOC};
		}
		
		SortCollector collector = null;
		if (sort.length==1){
			SortField sf = sort[0];
			collector = new OneSortCollector(getComparatorSource(browser,sf), offset, count, doScoring);
		}
		else{
			DocComparatorSource[] compSources = new DocComparatorSource[sort.length];
			for (int i = 0; i<sort.length;++i){
				compSources[i]=getComparatorSource(browser,sort[i]);
			}
			collector = new MultiFieldSortCollector(compSources, offset, count, doScoring);
		}
		
		collector._sortFields = sort;
		return collector;
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
	
	public BrowseHit[] buildHits(ScoreDoc[] scoreDocs,BoboIndexReader reader,Map<String,FacetHandler<?>> runtimeFacetHandlerMap,boolean fetchStoredFields) throws IOException{
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
