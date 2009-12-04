package com.browseengine.bobo.perf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.index.IndexReader;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.perf.BrowseThread.Stats;
import com.browseengine.bobo.perf.BrowseThread.StatsCollector;
import com.browseengine.bobo.perf.RequestFactory.ReqIterator;

public class BoboPerf implements StatsCollector{
  
  private PropertiesConfiguration _propConf;
  
  public static final String QUERY_LOG_FILE="query.log.file";
  public static final String INDEX_DIR="index.dir";
  public static final String NUM_REQ = "num.req";
  public static final String NUM_THREADS = "num.threads";
  public static final String THROTTLE_WAIT = "throttle.wait";
  
  private File qlogFile;
  private File idxDir;
  private int numReq;
  private int numThreads;
  private long throttleWait;
  
  private RequestFactory _reqFactory;
  
  public BoboPerf(PropertiesConfiguration propConf) throws IOException{
	  _propConf = propConf;
	  init();
  }
  
  private void init() throws IOException{ 
	  String qlogFileName=_propConf.getString(QUERY_LOG_FILE);
	  qlogFile = new File(qlogFileName);
	  if (!qlogFile.isAbsolute()){
		  qlogFile = new File(new File("conf"),qlogFileName);
	  }
	 
	  String idxDirName = _propConf.getString(INDEX_DIR);
	  idxDir = new File(idxDirName);
	  if (!idxDir.isAbsolute()){
		  idxDir = new File(new File("conf"),idxDirName);
	  }
	  numReq = _propConf.getInt(NUM_REQ);
	  numThreads = _propConf.getInt(NUM_THREADS,10);
	  throttleWait = _propConf.getLong(THROTTLE_WAIT, 500L);
	  
	  System.out.println("query log file: "+qlogFile.getAbsolutePath());
	  System.out.println("index dir: "+idxDir.getAbsolutePath());
	  System.out.println("number of reqs: "+numReq);
	  System.out.println("number of threads: "+numThreads);
	  System.out.println("throttle wait: "+throttleWait);
	  
	  _reqFactory = RequestFactory.load(qlogFile, numReq);
	  
	  
  }
  
  LinkedList<Stats> statsList = new LinkedList<Stats>();
  public void collect(Stats stats) {
	synchronized(statsList){
	  statsList.add(stats);
	}
  }
  
  public void start()  throws IOException{

	  BoboIndexReader boboReader = null;
	  System.out.println("loading index...");
	  System.out.println("Press key to continue...");

	  {
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
	    int ch = br.read(); 
	    char c = (char) ch; 
	  }
	  
	  IndexReader r = IndexReader.open(idxDir);
	  try{
		  boboReader = BoboIndexReader.getInstance(r);
	  }
	  catch(IOException ioe){
		  r.close();
		  throw ioe;
	  }
	  
	  System.out.println("initializing threads...");
	  
	  ReqIterator iter = _reqFactory.iterator();
	  Thread[] threadPool = new Thread[numThreads];
	  for (int i =0;i < threadPool.length;++i){
		  threadPool[i]=new BrowseThread(boboReader,iter,throttleWait,this);
	  }
	  
	  System.out.println("press key to start load test... ");
	  {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
		int ch = br.read(); 
		char c = (char) ch; 
	  }
	  
	  long start = System.currentTimeMillis();
	  for (int i =0;i<threadPool.length;++i){
		  threadPool[i].start();
	  }
	  try {
		  for (int i =0;i<threadPool.length;++i){
			  threadPool[i].join();
		  }
	  } catch (InterruptedException e) {
		e.printStackTrace();	
	  }

	  long end = System.currentTimeMillis();
	  System.out.println("finished ... ");
	  
	  printSummary(statsList, (end-start));
  }
  
  void printSummary(List<Stats> stats,long totalTime){
	  System.out.println("======= Performance Report=========");
	  System.out.println("total time: "+totalTime);
	  System.out.println("total reqs processed: "+stats.size());
	  System.out.println("QPS: "+stats.size()*1000/(totalTime) + "  (max: "+numThreads*(1000/throttleWait)+")");
	  Stats[] statsArray = stats.toArray(new Stats[stats.size()]);
	  long sum = 0L;
	  int errCount = 0;
	  for (Stats stat : statsArray){
		  sum += stat.getTime();
		  if (stat.getException() != null){
			  errCount++;
		  }
	  }
	  
	  Arrays.sort(statsArray, new Comparator<Stats>(){
		public int compare(Stats s1, Stats s2) {
			long val = s1.getTime() - s2.getTime();
			if (val == 0L){
				val = s1.getCreateTime() - s2.getCreateTime();
			}
			if (val>0L) return 1;
			if (val == 0L) return 0;
			return -1;
		}
	  });
	  
	  System.out.println("median time: "+statsArray[statsArray.length/2].getTime());
	  System.out.println("average time: "+(sum/statsArray.length));
	  System.out.println("error count: "+errCount);
  }
  
  public static void main(String[] args) throws Exception{
	  File propFile = new File(args[0]);
	  BoboPerf perf = new BoboPerf(new PropertiesConfiguration(propFile));
	  perf.start();
  }
}
