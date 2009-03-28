package com.browseengine.solr;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.solr.core.IndexReaderFactory;
import org.apache.solr.util.NamedList;

import com.browseengine.bobo.api.BoboIndexReader;

public class BoboIndexReaderFactory implements IndexReaderFactory {

	public void init(NamedList args) {
		// TODO Auto-generated method stub
		
	}

	public IndexReader newReader(Directory indexDir) throws IOException {
		IndexReader reader=IndexReader.open(indexDir);
		BoboIndexReader boboReader=null;
		try{
			boboReader=BoboIndexReader.getInstance(reader);
			return boboReader;
		}
		catch(IOException e){
			if (reader!=null){
				reader.close();
			}
			throw e;
		}
	}

}
