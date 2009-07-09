package com.browseengine.bobo.perf;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class ExpandIndex {
  public static void main(String[] args) throws IOException {
	File srcIdx = new File(args[0]);
	File targetIndx = new File(args[1]);
	
	int times = Integer.parseInt(args[2]);
	
	Directory[] multipler = new Directory[times];
	
	IndexWriter writer = new IndexWriter(targetIndx,new StandardAnalyzer(),true,MaxFieldLength.UNLIMITED);
	for (int i = 0;i<times;++i){
		multipler[i]=FSDirectory.getDirectory(srcIdx);
	}
	writer.addIndexesNoOptimize(multipler);
	System.out.println("optimizing...");
	writer.optimize();
	writer.close();
  }
}
