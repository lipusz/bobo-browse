package com.browseengine.solr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.SolrParams;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.api.FacetAccessible;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.api.MappedFacetAccessible;
import com.browseengine.bobo.impl.SortedFieldBrowseHitComparator;
import com.browseengine.bobo.protobuf.BrowseResultBPO.FacetContainer;
import com.browseengine.bobo.server.protocol.BoboRequestBuilder;
import com.browseengine.bobo.util.ListMerger;
import com.browseengine.bobo.util.XStreamDispenser;
import com.browseengine.solr.BoboRequestHandler.BoboSolrParams;
import com.thoughtworks.xstream.XStream;

public class DispatchUtil {

	private static Logger logger=Logger.getLogger(DispatchUtil.class);

	 static HttpClient client;
	 
	  static int soTimeout = 0; //current default values
	  static int connectionTimeout = 0; //current default values

	  // these values can be made configurable
	static {
	    MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();
	    mgr.getParams().setDefaultMaxConnectionsPerHost(20);
	    mgr.getParams().setMaxTotalConnections(10000);
	    mgr.getParams().setConnectionTimeout(connectionTimeout);
	    mgr.getParams().setSoTimeout(soTimeout);
	    // mgr.getParams().setStaleCheckingEnabled(false);
	    client = new HttpClient(mgr);    
	}
	
	private static class DispatchSolrParams extends SolrParams{
		private int offset;
		private int count;
		private SolrParams _params;
		
		DispatchSolrParams(SolrParams params){
			_params = params;
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
			else if (BoboRequestHandler.SHARD_PARAM.equals(name)){
				return null;
			}
			else{
			  return _params.get(name);
			}
		}

		@Override
		public Iterator<String> getParameterNamesIterator() {
			return _params.getParameterNamesIterator();
		}

		@Override
		public String[] getParams(String name) {
			if (BoboRequestHandler.SHARD_PARAM.equals(name)){
				return null;
			}
			return _params.getParams(name);
		}
	}
	
	private static Map<String,FacetAccessible> mergeFacetContainer(Collection<Map<String,FacetAccessible>> subMaps,BrowseRequest req)
	  {
	    Map<String, Map<String, Integer>> counts = new HashMap<String, Map<String, Integer>>();
	    for (Map<String,FacetAccessible> subMap : subMaps)
	    {
	      for(Map.Entry<String, FacetAccessible> entry : subMap.entrySet())
	      {
	        Map<String, Integer> count = counts.get(entry.getKey());
	        if(count == null)
	        {
	          count = new HashMap<String, Integer>();
	          counts.put(entry.getKey(), count);
	        }
	        for(BrowseFacet facet : entry.getValue().getFacets())
	        {
	          String val = facet.getValue();
	          int oldValue = count.containsKey(val) ? count.get(val) : 0;
	          count.put(val, oldValue + facet.getHitCount());
	        }
	      }
	    }

	    Map<String, FacetAccessible> mergedFacetMap = new HashMap<String, FacetAccessible>();
	    for(String facet : counts.keySet())
	    {
	      Map<String, Integer> facetValueCounts = counts.get(facet);
	      List<BrowseFacet> facets = new ArrayList<BrowseFacet>(facetValueCounts.size());
	      for(Entry<String, Integer> entry : facetValueCounts.entrySet())
	      {
	        facets.add(new BrowseFacet(entry.getKey(), entry.getValue()));
	      }
	      Collections.sort(facets, new Comparator<BrowseFacet>()
	      {
	        public int compare(BrowseFacet f1, BrowseFacet f2)
	        {
	          int h1 = f1.getHitCount();
	          int h2 = f2.getHitCount();

	          int val = h2 - h1;

	          if (val == 0)
	          {
	            val = f1.getValue().compareTo(f2.getValue());
	          }
	          return val;
	        }
	      });
	      if (req != null)
	      {
	        FacetSpec fspec = req.getFacetSpec(facet);
	        if (fspec!=null){
	          int maxCount = fspec.getMaxCount();
	          int numToShow = facets.size();
	          if (maxCount>0){
	        	  numToShow = Math.min(maxCount,numToShow);
	          }
	          facets = facets.subList(0, numToShow);
	        }
	      }
	      MappedFacetAccessible mergedFacetAccessible = new MappedFacetAccessible(facets.toArray(new BrowseFacet[facets.size()]));
	      mergedFacetMap.put(facet, mergedFacetAccessible);
	    }
	    return mergedFacetMap;
	  }
	
	public static BrowseResult broadcast(ExecutorService threadPool,BoboSolrParams boboSolrParams,BrowseRequest req,String[] baseURL,int maxRetry){
		long start = System.currentTimeMillis();
		Future<BrowseResult>[] futureList = (Future<BrowseResult>[]) new Future[baseURL.length];
        for (int i = 0; i < baseURL.length; i++)
		{
          SolrParams dispatchParams = new DispatchSolrParams(boboSolrParams._params);
          Callable<BrowseResult> callable = newCallable(new BoboSolrParams(dispatchParams),baseURL[i],maxRetry);
          futureList[i] = threadPool.submit(callable);
		}
        
		List<Map<String,FacetAccessible>> facetList=new ArrayList<Map<String,FacetAccessible>>(baseURL.length);
		
		ArrayList<Iterator<BrowseHit>> iteratorList = new ArrayList<Iterator<BrowseHit>>(baseURL.length);
		int numHits = 0;
		int totalDocs = 0;
        for (int i = 0; i < futureList.length; i++)
		{
			try { 
				BrowseResult res = futureList[i].get();
				BrowseHit[] hits = res.getHits();
				if (hits!=null){
				  for (BrowseHit hit : hits){
					hit.setDocid(hit.getDocid()+totalDocs);
				  }
				}
				iteratorList.add(Arrays.asList(res.getHits()).iterator());
				
				Map<String,FacetAccessible> facetMap = res.getFacetMap();
				if (facetMap!=null){
					facetList.add(facetMap);
				}
				//resultList.add(res); 
				numHits += res.getNumHits();
				totalDocs += res.getTotalDocs();
			}
			catch (InterruptedException e) { logger.error(e.getMessage(),e); }
			catch (ExecutionException e) { logger.error(e.getMessage(),e); }
		}
        
        Map<String,FacetAccessible> mergedFacetMap = mergeFacetContainer(facetList,req);
        Comparator<BrowseHit> comparator = new SortedFieldBrowseHitComparator(req.getSort());
        
        ArrayList<BrowseHit> mergedList = ListMerger.mergeLists(req.getOffset(), req.getCount(), iteratorList.toArray(new Iterator[iteratorList.size()]), comparator);
        BrowseHit[] hits = mergedList.toArray(new BrowseHit[mergedList.size()]);
        long end = System.currentTimeMillis();
        
        BrowseResult merged = new BrowseResult();
        merged.setHits(hits);
        merged.setNumHits(numHits);
        merged.setTotalDocs(totalDocs);
        merged.setTime(end-start);
        merged.addAll(mergedFacetMap);
        return merged;
	}
	
	private static BrowseResult parseResponse(InputStream input, String charset) throws UnsupportedEncodingException{
		XStream parser = XStreamDispenser.getXMLXStream();
		Reader r = new InputStreamReader(input,charset);
		return (BrowseResult)(parser.fromXML(r));
	}
	
	private static BrowseResult doShardCall(BoboSolrParams boboSolrParams,String baseURL,int maxRetry) throws HttpException, IOException{
		String path = "/select";
		GetMethod method = null;
		try{
			method = new GetMethod("http://"+baseURL + path + ClientUtils.toQueryString( boboSolrParams._params, false ) );
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
				  break;
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
		catch(IOException ioe ){
			ioe.printStackTrace();
			throw ioe;
		}
		catch(RuntimeException e){
			e.printStackTrace();
			throw e;
		}
		finally{
			if (method!=null){
				method.releaseConnection();
			}
		}
	}
	
	private static Callable<BrowseResult> newCallable(final BoboSolrParams boboSolrParams,final String baseURL,final int maxRetry){
		return new Callable<BrowseResult>(){

			public BrowseResult call() throws Exception {
				return doShardCall(boboSolrParams, baseURL, maxRetry);
			}
			
		};
	}
}
