package com.browseengine.bobo.profile;

import java.io.File;

import org.apache.lucene.index.IndexReader;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.bench.BoboPlugin;
import com.browseengine.bobo.server.protocol.BrowseJSONHandler;
import com.browseengine.bobo.server.protocol.ProtocolHandler;
import com.browseengine.bobo.server.qlog.QueryLog;
import com.browseengine.bobo.server.qlog.QueryLog.LogLine;
import com.browseengine.bobo.service.BrowseService;
import com.browseengine.bobo.service.BrowseServiceFactory;

public class BoboProfile {

	static{
		ProtocolHandler.registerProtocolHandler(new BrowseJSONHandler());
	}
	
	private static void startWork(int max,BrowseService svc,String[] loglines){
		for (int i=0;i<max;++i){				
			LogLine logLine=QueryLog.readLog(loglines[i]);
			String protocol=logLine.getProtocol();
			String req=logLine.getRequest();
			
			try {
				if (protocol != null){
					ProtocolHandler protoHandler=ProtocolHandler.getProtocolHandler(protocol);
					
					if (protoHandler==null) throw new Exception("Unsupported protocol: "+protocol);
																	
					BrowseRequest request=(BrowseRequest)protoHandler.deserializeRequest(BrowseRequest.class,req.getBytes("UTF-8"));
					BrowseResult result=null;
					try{
						result=svc.browse(request);
					}
					catch(Exception e){
						e.printStackTrace();
					}													
				} else {
					throw new Exception("no \"proto\" request parameter was specified");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		File logFile=new File(args[0]);
		File idxDir=new File(args[1]);

		String[] loglines=BoboPlugin.loadLogfile(logFile);


		int max=Math.min(loglines.length,10);
		IndexReader reader=null;
		BoboIndexReader boboReader=null;
		try{
			if (IndexReader.indexExists(idxDir)){
				reader=IndexReader.open(idxDir);
				try{
					boboReader=BoboIndexReader.getInstance(reader);
				}
				catch(Exception e){
					reader.close();
				}
			}
			
			BrowseService svc=BrowseServiceFactory.createBrowseService(boboReader);
			
			startWork(max,svc,loglines);
			
		}
		finally{
			if (boboReader!=null){
				boboReader.close();
			}
		}					
	}
}
