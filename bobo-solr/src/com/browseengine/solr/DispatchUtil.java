package com.browseengine.solr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.SolrParams;

import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.impl.SortedFieldBrowseHitComparator;
import com.browseengine.bobo.server.protocol.BoboRequestBuilder;
import com.browseengine.bobo.util.ListMerger;
import com.browseengine.bobo.util.XStreamDispenser;
import com.browseengine.solr.BoboRequestHandler.BoboSolrParams;
import com.thoughtworks.xstream.XStream;

public class DispatchUtil {

	private static Logger logger=Logger.getLogger(DispatchUtil.class);

	private static class ClientInfo{
		HttpClient client;
		String  baseURL;
	}
	
	private static class DispatchSolrParams extends BoboSolrParams{
		private int offset;
		private int count;
		
		DispatchSolrParams(SolrParams params){
			super(params);
			offset = params.getInt(BoboRequestBuilder.START, 0);
			count = params.getInt(BoboRequestBuilder.COUNT, 0);
		}
		
		@Override
		public String get(String name) {
			if (BoboRequestBuilder.START.equals(name)){
				return "0";
			}
			else if (BoboRequestBuilder.COUNT.equals(name)){
				return String.valueOf(offset+count);
			}
			else{
			  return _params.get(name);
			}
		}
	}
	
	public static BrowseResult broadcast(ExecutorService threadPool,BoboSolrParams boboSolrParams,BrowseRequest req,ClientInfo[] clientInfos,int maxRetry){
		long start = System.currentTimeMillis();
		Future<BrowseResult>[] futureList = (Future<BrowseResult>[]) new Future[clientInfos.length];
        for (int i = 0; i < clientInfos.length; i++)
		{
          Callable<BrowseResult> callable = newCallable(new DispatchSolrParams(boboSolrParams._params),clientInfos[i].baseURL,clientInfos[i].client,maxRetry);
          futureList[i] = threadPool.submit(callable);
		}
        
	//	List<BrowseResult> resultList=new ArrayList<BrowseResult>(clientInfos.length);
		
		ArrayList<Iterator<BrowseHit>> iteratorList = new ArrayList<Iterator<BrowseHit>>(clientInfos.length);
		int numHits = 0;
		int totalDocs = 0;
        for (int i = 0; i < futureList.length; i++)
		{
			try { 
				BrowseResult res = futureList[i].get();
				iteratorList.add(Arrays.asList(res.getHits()).iterator());
				//resultList.add(res); 
				numHits += res.getNumHits();
				totalDocs += res.getTotalDocs();
			}
			catch (InterruptedException e) { logger.error(e.getMessage(),e); }
			catch (ExecutionException e) { logger.error(e.getMessage(),e); }
		}
        
        Comparator<BrowseHit> comparator = new SortedFieldBrowseHitComparator(req.getSort());
        
        ArrayList<BrowseHit> mergedList = ListMerger.mergeLists(req.getOffset(), req.getCount(), iteratorList.toArray(new Iterator[iteratorList.size()]), comparator);
        BrowseHit[] hits = mergedList.toArray(new BrowseHit[mergedList.size()]);
        long end = System.currentTimeMillis();
        
        BrowseResult merged = new BrowseResult();
        merged.setHits(hits);
        merged.setNumHits(numHits);
        merged.setTotalDocs(totalDocs);
        merged.setTime(end-start);
        
        // TODO: merged facets
        return merged;
	}
	
	private static BrowseResult parseResponse(InputStream input, String charset) throws UnsupportedEncodingException{
		XStream parser = XStreamDispenser.getXMLXStream();
		Reader r = new InputStreamReader(input,charset);
		return (BrowseResult)(parser.fromXML(r));
	}
	
	private static BrowseResult doShardCall(BoboSolrParams boboSolrParams,String baseURL,HttpClient client,int maxRetry) throws HttpException, IOException{
		String path = "/select";
		GetMethod method = null;
		try{
			method = new GetMethod( baseURL + path + ClientUtils.toQueryString( boboSolrParams._params, false ) );
			String charset = method.getResponseCharSet();
			InputStream responseStream = null;
			while(maxRetry-- > 0){
				try
				{
				  int status = client.executeMethod(method);
				  if (HttpStatus.SC_OK != status){
					  logger.error("status: "+status+", retry #: "+maxRetry);
					  continue;
				  }
				  responseStream = method.getResponseBodyAsStream();
				  
				}
				catch(Exception e){
				  logger.error(e.getMessage()+" retry #: "+maxRetry,e);
				}
			}
			
			if (responseStream == null){
				throw new IOException("unable to perform remote request, all retries have been exhausted");
			}
			// Read the contents
		    return parseResponse(responseStream, charset);
		}
		finally{
			if (method!=null){
				method.releaseConnection();
			}
		}
	}
	
	private static Callable<BrowseResult> newCallable(final BoboSolrParams boboSolrParams,final String baseURL,final HttpClient client,final int maxRetry){
		return new Callable<BrowseResult>(){

			public BrowseResult call() throws Exception {
				return doShardCall(boboSolrParams, baseURL, client, maxRetry);
			}
			
		};
	}
}
