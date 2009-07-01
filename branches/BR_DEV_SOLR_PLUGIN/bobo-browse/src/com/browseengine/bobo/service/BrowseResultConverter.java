package com.browseengine.bobo.service;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.api.FacetAccessible;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class BrowseResultConverter implements Converter {

	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext ctx) {
		BrowseResult result=(BrowseResult)obj;
		writer.addAttribute("numhits", String.valueOf(result.getNumHits()));
		writer.addAttribute("totaldocs", String.valueOf(result.getTotalDocs()));
		writer.addAttribute("time", String.valueOf(result.getTime()));
		
		writer.startNode("facets");
		Set<Entry<String,FacetAccessible>> facetAccessors=result.getFacetMap().entrySet();
		
		writer.addAttribute("count", String.valueOf(facetAccessors.size()));
		
		for (Entry<String,FacetAccessible> entry : facetAccessors){
			String choiceName=entry.getKey();
			FacetAccessible facetAccessor = entry.getValue();
			
			List<BrowseFacet> facetList = facetAccessor.getFacets();
			
			writer.startNode("facet");
			writer.addAttribute("name", choiceName);
			
			writer.addAttribute("facetcount", String.valueOf(facetList.size()));
			
			for (BrowseFacet facet : facetList){
				writer.startNode("facet");
				writer.addAttribute("value", String.valueOf(facet.getValue()));
				writer.addAttribute("count", String.valueOf(facet.getHitCount()));
				writer.endNode();
			}
			writer.endNode();
		}
		writer.endNode();
		writer.startNode("hits");
		BrowseHit[] hits=result.getHits();
		ctx.convertAnother(hits);
		writer.endNode();
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext ctx) {		
		BrowseResult res=new BrowseResult();
		
		
		
		BrowseHit[] hits=(BrowseHit[])ctx.convertAnother(res,BrowseHit[].class);
		res.setHits(hits);
		return res;
	}

	public boolean canConvert(Class clazz) {
		return BrowseResult.class.equals(clazz);
	}

}
