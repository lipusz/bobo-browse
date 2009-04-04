package com.browseengine.bobo.service;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;

import com.browseengine.bobo.api.BoboBrowser;
import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.BrowseException;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.facets.FacetHandler;


public class DefaultBrowseServiceImpl implements BoboBrowseService {
	private BoboIndexReader _reader;
	private static Logger log = Logger.getLogger(DefaultBrowseServiceImpl.class);
	public DefaultBrowseServiceImpl(File idxDir,List<FacetHandler> facetHandlerList) throws IOException{
		IndexReader luceneReader = IndexReader.open(idxDir);
		try{
		  _reader = BoboIndexReader.getInstance(luceneReader, facetHandlerList);
		}
		catch(IOException ioe){
		  luceneReader.close();
		  throw ioe;
		}
		log.info("bobo service created");
	}
	
	public DefaultBrowseServiceImpl(File idxDir) throws IOException{
		this(idxDir,null);
	}
	
	public BrowseResult browse(BrowseRequest req) throws BrowseException {
		BoboBrowser browser = null;
		try{
			browser = new BoboBrowser(_reader);
			return browser.browse(req);
		}
		finally{
			if (browser!=null){
				try {
					browser.close();
				} catch (IOException e) {
					log.error(e.getMessage(),e);
				}
			}
		}
	}
	
	public void close() throws IOException{
		if (_reader!=null)
		{
			_reader.close();
		}
		log.info("bobo service shutdown successfully");
	}
}
