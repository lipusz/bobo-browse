package com.browseengine.bobo.perf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;

import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.protobuf.BrowseProtobufConverter;
import com.browseengine.bobo.protobuf.BrowseRequestBPO;
import com.google.protobuf.TextFormat;

public class BoboPerf {
  public static void main(String[] args) throws Exception{
	File f = new File("/Users/john/opensource/bobo-solr/logs/zoie-server.log");
	InputStreamReader ireader = new InputStreamReader(new FileInputStream(f),"UTF-8");
	
	BufferedReader br = new BufferedReader(ireader);

	StringBuffer buf = null;
	while(true){
		String line = br.readLine();
		if (line == null) break;
		if (buf!=null){
			if (line.contains(" |>]")){
				buf.append(" ").append(line.substring(0, line.indexOf(" |>]")));
				String reqString = buf.toString();
				System.out.println(reqString);
				BrowseRequestBPO.Request.Builder builder = BrowseRequestBPO.Request.newBuilder();
				TextFormat.merge(reqString, builder);
				BrowseRequestBPO.Request bpo = builder.build();
				BrowseRequest req = BrowseProtobufConverter.convert(bpo,new QueryParser("contents",new StandardAnalyzer()));
				System.out.println(req);
				buf = null;
			}
			else{
				buf.append(" ").append(line);
			}
		}
		else if (line.contains("[<| ")){
			buf = new StringBuffer();
			buf.append(" ").append(line.substring(line.indexOf("[<| ")+4));
		}
		
	}
	ireader.close();
  }
}
