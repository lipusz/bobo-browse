package com.browseengine.bobo.demo.cardemo;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

public class MultiValuedFieldBridge implements FieldBridge {
	
	public void set(String name, Object value, Document document, LuceneOptions opt) {
		List<String> list = (List<String>)value;
		for (String val : list){
			Field f = new Field(name,val,opt.getStore(),opt.getIndex(),opt.getTermVector());
			f.setBoost(opt.getBoost());
			document.add(f);
		}
	}
}
