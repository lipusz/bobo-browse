package com.browseengine.bobo.facets.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreDocComparator;
import org.apache.lucene.search.SortField;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.BrowseSelection;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.facets.FacetCountCollector;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.FacetHandlerFactory;
import com.browseengine.bobo.facets.filter.RandomAccessAndFilter;
import com.browseengine.bobo.facets.filter.RandomAccessFilter;

public class GroupbyFacetHandler extends FacetHandler implements FacetHandlerFactory{
	private final LinkedHashSet<String> _fieldsSet;
	private ArrayList<FacetHandler> _facetHandlers;
	private Map<String,FacetHandler> _facetHandlerMap;
	public GroupbyFacetHandler(String name, LinkedHashSet<String> dependsOn) {
		super(name, dependsOn);
		_fieldsSet = dependsOn;
		_facetHandlers = null;
		_facetHandlerMap = null;
	}

	@Override
	public RandomAccessFilter buildRandomAccessFilter(String value,
			Properties selectionProperty) throws IOException {
		List<RandomAccessFilter> filterList = new ArrayList<RandomAccessFilter>();
		//RandomAccessAndFilter andFilter = new RandomAccessAndFilter(filters)
		return null;
	}

	@Override
	public FacetCountCollector getFacetCountCollector(BrowseSelection sel,
			FacetSpec fspec) {
		ArrayList<FacetCountCollector> collectorList = new ArrayList<FacetCountCollector>(_facetHandlers.size());
		for (FacetHandler facetHandler : _facetHandlers){
			collectorList.add(facetHandler.getFacetCountCollector(sel, fspec));
		}
		return new GroupbyFacetCountCollector(_name, collectorList.toArray(new FacetCountCollector[collectorList.size()]));
	}

	@Override
	public String[] getFieldValues(int id) {
		ArrayList<String> valList = new ArrayList<String>();
		for (FacetHandler handler : _facetHandlers){
			StringBuffer buf = new StringBuffer();
			boolean firsttime = true;
			String[] vals = handler.getFieldValues(id);
			if (vals!=null && vals.length > 0){
				if (!firsttime){
					buf.append(",");
				}
				else{
					firsttime=false;
				}
				for (String val : vals){
					buf.append(val);
				}
			}
			valList.add(buf.toString());
		}
		return valList.toArray(new String[valList.size()]);
	}

	@Override
	public ScoreDocComparator getScoreDocComparator() {
		ArrayList<ScoreDocComparator> comparatorList = new ArrayList<ScoreDocComparator>(_fieldsSet.size());
		for (FacetHandler handler : _facetHandlers){
			comparatorList.add(handler.getScoreDocComparator());
		}
		return new GroupbyScoreDocComparator(comparatorList.toArray(new ScoreDocComparator[comparatorList.size()]));
	}

	@Override
	public void load(BoboIndexReader reader) throws IOException {
		_facetHandlers = new ArrayList<FacetHandler>(_fieldsSet.size());
		_facetHandlerMap = new HashMap<String,FacetHandler>(_fieldsSet.size());
		for (String name : _fieldsSet){
			FacetHandler handler = reader.getFacetHandler(name);
			_facetHandlers.add(handler);
			_facetHandlerMap.put(name, handler);
		}
	}

	public FacetHandler newInstance() {
		return new GroupbyFacetHandler(_name,_fieldsSet);
	}
	
	
	private static class GroupbyScoreDocComparator implements ScoreDocComparator{
		private ScoreDocComparator[] _comparators;

		public GroupbyScoreDocComparator(ScoreDocComparator[] comparators) {
			_comparators = comparators;
		}
		
		public final int compare(ScoreDoc d1, ScoreDoc d2) {
			int retval=0;
			for (ScoreDocComparator comparator : _comparators){
				retval = comparator.compare(d1, d2);
				if (retval!=0) break;
			}
			return retval;
		}

		public final int sortType() {
			return SortField.CUSTOM;
		}

		public final Comparable sortValue(final ScoreDoc doc) {
			return new Comparable(){

				public int compareTo(Object o) {
					int retval = 0;
					for (ScoreDocComparator comparator : _comparators){
						retval = comparator.sortValue(doc).compareTo(o);
						if (retval != 0 ) break;
					}
					return retval;
				}
				
			};
		}
	}
	
	private static class GroupbyFacetCountCollector implements FacetCountCollector{
		private final FacetCountCollector[] _subcollectors;
		private final String _name;
		public GroupbyFacetCountCollector(String name,FacetCountCollector[] subcollectors){
			_name = name;
			_subcollectors = subcollectors;
		}
		
		final public void collect(int docid) {
			for (FacetCountCollector collector : _subcollectors){
				collector.collect(docid);
			}
		}

		public void collectAll() {
			for (FacetCountCollector collector : _subcollectors){
				collector.collectAll();
			}
		}

		public int[] getCountDistribution() {
			if (_subcollectors.length>0){
			    return _subcollectors[_subcollectors.length-1].getCountDistribution();	
			}
			else{
				return new int[0];
			}
		}

		public String getName() {
			return _name;
		}

		public BrowseFacet getFacet(String value) {
			// TODO Auto-generated method stub
			return null;
		}

		public List<BrowseFacet> getFacets() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
