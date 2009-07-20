package com.browseengine.solr;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.Logger;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;

import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.server.protocol.BoboRequestBuilder;

public class MultiShardBoboRequestHandler extends BoboRequestHandler {
	private static final String VERSION="2.0.4";
	private static final String NAME="Bobo-Browse";
	private static final String SHARD_PARAM = "shards";
	static final String BOBORESULT="boboresult";
	
	private static Logger logger=Logger.getLogger(MultiShardBoboRequestHandler.class);
	
	private HashMap<URL,HttpClient> _connMap;
	
	public void handleRequest(SolrQueryRequest req, SolrQueryResponse resp) {
		BrowseRequest br=BoboRequestBuilder.buildRequest(new BoboSolrParams(req.getParams()),new BoboSolrQueryBuilder(req));
		String shardsVal = req.getParam(SHARD_PARAM);
		String[] shards = shardsVal.split(",");
		
		
		LinkedList<URL> shardURLs = new LinkedList<URL>();
		
		for (String shard : shards){
			int idx = shard.indexOf('/');
			if (idx>=0){
				String host_port = shard.substring(0,idx);
				
			}
		}
		
		HttpClientParams httpParams = new HttpClientParams();
		HostConfiguration hostConf = new HostConfiguration();
		
		HttpClient httpClient = new HttpClient();
		
	//	httpClient.setHostConfiguration(hostConfiguration)
	}

	public void init(NamedList nameList) {
		_connMap = new HashMap<URL,HttpClient>();
	}

	
}
