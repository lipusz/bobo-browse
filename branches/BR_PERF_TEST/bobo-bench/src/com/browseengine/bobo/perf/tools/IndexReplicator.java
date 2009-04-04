package com.browseengine.bobo.perf.tools;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexReplicator {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		File srcIndex = new File(args[0]);
		File targetIndex = new File(args[1]);
		int numReplicas = Integer.parseInt(args[2]);
		System.out.println("source index: "+srcIndex.getAbsolutePath());
		System.out.println("target index: "+targetIndex.getAbsolutePath());
		System.out.println("num replications: "+numReplicas);
		IndexReader reader = null;
		try
		{
			Directory dir = FSDirectory.getDirectory(srcIndex);
			reader=IndexReader.open(dir);
			System.out.println("source index, numdocs: "+reader.numDocs());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (reader!=null)
			{
				reader.close();
			}
			reader = null;
		}
		targetIndex.mkdirs();
		Directory targetDir = FSDirectory.getDirectory(targetIndex);
		
		IndexWriter writer = null;
		try
		{
			writer = new IndexWriter(targetDir,new StandardAnalyzer(),true,MaxFieldLength.UNLIMITED);
			for (int i=0;i<numReplicas;++i)
			{
			  System.out.println("replicating "+(i+1)+" time(s)");
			  writer.addIndexesNoOptimize(new Directory[]{FSDirectory.getDirectory(srcIndex)});
			}
			System.out.println("optimizing....");
			writer.optimize();
			System.out.println("done optimizing....");
		}
		finally
		{
			if (writer!=null)
			{
				writer.close();
			}
		}
	
		try
		{
			reader=IndexReader.open(targetDir);
			System.out.println("target index, numdocs: "+reader.numDocs());
		}
		finally
		{
			if (reader!=null)
			{
				reader.close();
			}
		}
	}
}
