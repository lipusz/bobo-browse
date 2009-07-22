package com.browseengine.bobo.service;

import com.browseengine.bobo.api.BrowseFacet;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class BrowseFacetConverter implements Converter {

	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext ctx) {
		BrowseFacet facet = (BrowseFacet)obj;
		writer.addAttribute("value", String.valueOf(facet.getValue()));
		writer.addAttribute("count", String.valueOf(facet.getHitCount()));
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext ctx) {
		BrowseFacet facet=new BrowseFacet();
		
		String valueString=reader.getAttribute("value");
		facet.setValue(valueString);
		
		String countString=reader.getAttribute("count");
		if (countString!=null){
			facet.setHitCount(Integer.parseInt(countString));
		}
		
		return facet;
	}

	public boolean canConvert(Class clazz) {
		return BrowseFacet.class.equals(clazz);
	}

}
