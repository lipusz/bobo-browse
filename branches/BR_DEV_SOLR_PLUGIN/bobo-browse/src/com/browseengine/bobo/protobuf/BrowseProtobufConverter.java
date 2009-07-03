package com.browseengine.bobo.protobuf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.api.FacetAccessible;

public class BrowseProtobufConverter {
	
	private static class FacetContainerAccessible implements FacetAccessible{
		private BrowseResultBPO.FacetContainer _facetContainer;
		FacetContainerAccessible(BrowseResultBPO.FacetContainer facetContainer){
			_facetContainer = facetContainer;
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
		return breq;
	}
}
