package com.browseengine.solr;

import java.net.URL;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SolrIndexReader;
import org.apache.solr.search.SolrIndexSearcher;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.BrowseException;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.impl.DefaultBrowseServiceImpl;
import com.browseengine.bobo.server.protocol.BoboParams;
import com.browseengine.bobo.server.protocol.BoboQueryBuilder;
import com.browseengine.bobo.server.protocol.BoboRequestBuilder;

public class BoboRequestHandler implements SolrRequestHandler {
	
	private static final String VERSION="2.0.4";
	private static final String NAME="Bobo-Browse";
	
	static final String BOBORESULT="boboresult";
	
	private static Logger logger=Logger.getLogger(BoboRequestHandler.class);
	
	private static class BoboSolrParams extends BoboParams{
		SolrParams _params;
		BoboSolrParams(SolrParams params){
			_params=params;
		}
		
		@Override
		public String get(String name) {
			return _params.get(name);
		}

		@Override
		public Iterator<String> getParamNames() {
			return _params.getParameterNamesIterator();
		}

        @Override
        public String[] getStrings(String name)
        {
          return _params.getParams(name);
        }
		
		
	}
	
	private static class BoboSolrQueryBuilder extends BoboQueryBuilder{
		SolrQueryRequest _req;
		BoboSolrQueryBuilder(SolrQueryRequest req){
			_req=req;
		}

		@Override
		public Query parseQuery(String query, String defaultField) {
			return QueryParsing.parseQuery(query, defaultField, _req.getParams(), _req.getSchema());
		}

		@Override
		public Sort parseSort(String sortStr) {
			Sort sort=null;
			if( sortStr != null ) {
		        sort = QueryParsing.parseSort(sortStr, _req.getSchema());
		    }
			return sort;
		}
	}
	
	public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
		
		SolrIndexSearcher searcher=req.getSearcher();
		
		SolrIndexReader solrReader = searcher.getReader();
		IndexReader reader = solrReader.getWrappedReader();
		
		if (reader instanceof BoboIndexReader){
			BrowseRequest br=BoboRequestBuilder.buildRequest(new BoboSolrParams(req.getParams()),new BoboSolrQueryBuilder(req));
			
		    DefaultBrowseServiceImpl svc=new DefaultBrowseServiceImpl((BoboIndexReader)reader);
		    svc.setCloseReaderOnCleanup(false);
		    
		    try {
				BrowseResult res=svc.browse(br);
				rsp.add(BOBORESULT, res);
				 /*
				if(HighlightingUtils.isHighlightingEnabled(req) && query != null) {
					NamedList sumData = HighlightingUtils.doHighlighting(
					        results.docList, query.rewrite(req.getSearcher().getReader()), req, new String[]{defaultField});
					      if(sumData != null)
					        rsp.add("highlighting", sumData);
				}
				*/
				
			} catch (BrowseException e) {
				logger.error(e.getMessage(),e);
				throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,e.getMessage(),e);
			}
		   
		}
		else{
	        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,"invalid reader, please make sure BoboIndexReaderFactory is set.");
		}
	}

	public void init(NamedList params) {
		// TODO Auto-generated method stub

	}

	public Category getCategory() {
		return Category.QUERYHANDLER;
	}

	public String getDescription() {
		return "Bobo browse facetted search implementation";
	}

	public URL[] getDocs() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return NAME;
	}

	public String getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSourceId() {
		// TODO Auto-generated method stub
		return null;
	}

	public NamedList getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getVersion() {
		return VERSION;
	}

}
