package com.browseengine.bobo.protobuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.api.BrowseSelection;
import com.browseengine.bobo.api.FacetAccessible;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.api.BrowseSelection.ValueOperation;
import com.browseengine.bobo.api.FacetSpec.FacetSortSpec;

public class BrowseProtobufConverter {
	
	private static class FacetContainerAccessible implements FacetAccessible{
		private Map<String,BrowseFacet> _data;
		FacetContainerAccessible(BrowseResultBPO.FacetContainer facetContainer){
			_data = new HashMap<String,BrowseFacet>();
			if (facetContainer!=null){
				List<BrowseResultBPO.Facet> facetList = facetContainer.getFacetsList();
				if (facetList!=null){
					for (BrowseResultBPO.Facet facet : facetList){
						BrowseFacet bfacet = new BrowseFacet();
						String val = facet.getVal();
						bfacet.setValue(val);
						bfacet.setHitCount(facet.getCount());
						_data.put(val,bfacet);
					}
				}
			}
		}
		public BrowseFacet getFacet(String value) {
			return _data.get(value);
		}

		public List<BrowseFacet> getFacets() {
			Collection<BrowseFacet> set = _data.values();
			ArrayList<BrowseFacet> list = new ArrayList<BrowseFacet>(set.size());
			list.addAll(set);
			return list;
		}
	}
	
	public static BrowseHit convert(BrowseResultBPO.Hit hit){
		BrowseHit bhit = new BrowseHit();
		bhit.setDocid(hit.getDocid());
		bhit.setScore(hit.getScore());
		List<BrowseResultBPO.FieldVal> fieldValueList = hit.getFieldValuesList();
		Map<String,String[]> fielddata = new HashMap<String,String[]>();
		for (BrowseResultBPO.FieldVal fieldVal : fieldValueList){
			List<String> valList = fieldVal.getValsList();
			fielddata.put(fieldVal.getName(), valList.toArray(new String[valList.size()]));
		}
		bhit.setFieldValues(fielddata);
		return bhit;
	}
	
	public static BrowseResult convert(BrowseResultBPO.Result result){
		long time = result.getTime();
		int numhits = result.getNumhits();
		int totaldocs = result.getTotaldocs();
		List<BrowseResultBPO.FacetContainer> facetList = result.getFacetContainersList();
		List<BrowseResultBPO.Hit> hitList = result.getHitsList();
		BrowseResult res = new BrowseResult();
		res.setTime(time);
		res.setTotalDocs(totaldocs);
		res.setNumHits(numhits);
		for (BrowseResultBPO.FacetContainer facetContainer : facetList)
		{
			res.addFacets(facetContainer.getName(), new FacetContainerAccessible(facetContainer));
		}
		
		BrowseHit[] browseHits = new BrowseHit[hitList==null ? 0 : hitList.size()];
		int i=0;
		for (BrowseResultBPO.Hit hit : hitList)
		{
			browseHits[i++] = convert(hit);
		}
		res.setHits(browseHits);
		return res;
	}
	
	public static BrowseRequest convert(BrowseRequestBPO.Request req,QueryParser qparser) throws ParseException{
		BrowseRequest breq = new BrowseRequest();
		String query = req.getQuery();
		
		if (query!=null && query.length() > 0){
			Query q = qparser.parse(query);
			breq.setQuery(q);
		}
		breq.setOffset(req.getOffset());
		breq.setCount(req.getCount());
		
		int i = 0;
		
		List<BrowseRequestBPO.Sort> sortList = req.getSortList();
		SortField[] sortFields = new SortField[sortList == null ? 0 : sortList.size()];
		for (BrowseRequestBPO.Sort s : sortList){
			SortField sf = new SortField(s.getField(),s.getReverse());
			sortFields[i++] = sf;
		}
		if (sortFields.length > 0){
		 breq.setSort(sortFields);
		}
		
		List<BrowseRequestBPO.FacetSpec> fspecList = req.getFacetSpecsList();
		for (BrowseRequestBPO.FacetSpec fspec : fspecList){
			FacetSpec facetSpec = new FacetSpec();
			facetSpec.setExpandSelection(fspec.getExpand());
			facetSpec.setMaxCount(fspec.getMax());
			facetSpec.setMinHitCount(fspec.getMinCount());
			BrowseRequestBPO.FacetSpec.SortSpec fsort = fspec.getOrderBy();
			if (fsort == BrowseRequestBPO.FacetSpec.SortSpec.HitsDesc)
			{
				facetSpec.setOrderBy(FacetSortSpec.OrderHitsDesc);
			}
			else
			{
				facetSpec.setOrderBy(FacetSortSpec.OrderValueAsc);
				
			}
		}
		
		List<BrowseRequestBPO.Selection> selList = req.getSelectionsList();
		for (BrowseRequestBPO.Selection sel : selList){
			BrowseSelection bsel = null;
			
			List<String> vals = sel.getValuesList();
			if (vals!=null)
			{
				if (bsel!=null)
				{
					bsel = new BrowseSelection(sel.getName());
				}
				bsel.setValues(vals.toArray(new String[vals.size()]));
				
			}
			vals = sel.getNotValuesList();
			if (vals!=null)
			{
				if (bsel!=null)
				{
					bsel = new BrowseSelection(sel.getName());
				}
				bsel.setNotValues(vals.toArray(new String[vals.size()]));
				
			}
			
			if (bsel!= null){
				BrowseRequestBPO.Selection.Operation operation = sel.getOp();
				if (operation == BrowseRequestBPO.Selection.Operation.OR){
					bsel.setSelectionOperation(ValueOperation.ValueOperationOr);
				}
				else{
					bsel.setSelectionOperation(ValueOperation.ValueOperationAnd);
				}
				List<BrowseRequestBPO.Property> props = sel.getPropsList();
				if (props!=null)
				{
				  for (BrowseRequestBPO.Property prop : props){
					  bsel.setSelectionProperty(prop.getKey(), prop.getVal());
				  }
				}
				breq.addSelection(bsel);
			}
			
		}
		return breq;
	}
}
