package com.browseengine.bobo.facets.impl;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.BrowseSelection;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.facets.FacetCountCollector;
import com.browseengine.bobo.facets.FacetCountCollectorSource;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.FacetHandlerFactory;
import com.browseengine.bobo.facets.data.FacetDataCache;
import com.browseengine.bobo.facets.data.TermListFactory;
import com.browseengine.bobo.facets.filter.EmptyFilter;
import com.browseengine.bobo.facets.filter.FacetOrFilter;
import com.browseengine.bobo.facets.filter.FacetValueConverter;
import com.browseengine.bobo.facets.filter.RandomAccessFilter;
import com.browseengine.bobo.facets.filter.RandomAccessNotFilter;
import com.browseengine.bobo.sort.DocComparatorSource;
import com.browseengine.bobo.util.BigIntArray;

public class PathFacetHandler extends FacetHandler<FacetDataCache> implements FacetHandlerFactory<PathFacetHandler> 
{
	private static final String DEFAULT_SEP = "/";
	
	public static final String SEL_PROP_NAME_STRICT="strict";
    public static final String SEL_PROP_NAME_DEPTH="depth";
	
	private final TermListFactory _termListFactory;
	private String _separator;
	
	public PathFacetHandler(String name)
	{
		super(name);
		_termListFactory=TermListFactory.StringListFactory;
		_separator=DEFAULT_SEP;
	}
	
	public PathFacetHandler newInstance()
	{
	  return new PathFacetHandler(getName());
	}
		
	/**
     * Sets is strict applied for counting. Used if the field is of type <b><i>path</i></b>.
     * @param strict is strict applied
     */
    public static void setStrict(Properties props,boolean strict) {
      props.setProperty(PathFacetHandler.SEL_PROP_NAME_STRICT, String.valueOf(strict));
    }
    

    /**
     * Sets the depth.  Used if the field is of type <b><i>path</i></b>.
     * @param depth depth
     */
    public static void setDepth(Properties props,int depth) {
      props.setProperty(PathFacetHandler.SEL_PROP_NAME_DEPTH, String.valueOf(depth));
    }
    

	/**
     * Gets if strict applied for counting. Used if the field is of type <b><i>path</i></b>.
     * @return is strict applied
     */
    public static boolean isStrict(Properties selectionProp) {
    	try
    	{
          return Boolean.valueOf(selectionProp.getProperty(PathFacetHandler.SEL_PROP_NAME_STRICT));
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
    }
    
    /**
     * Gets the depth.  Used if the field is of type <b><i>path</i></b>.
     * @return depth
     */
    public static int getDepth(Properties selectionProp) {
      try
      {
        return Integer.parseInt(selectionProp.getProperty(PathFacetHandler.SEL_PROP_NAME_DEPTH));
      }
      catch(Exception e)
      {
        return 1;
      }
    }
    
	@Override
	public DocComparatorSource getDocComparatorSource()  
	{
		return new FacetDataCache.FacetDocComparatorSource(this);
	}
	
	@Override
	public String[] getFieldValues(BoboIndexReader reader,int id) 
	{
		FacetDataCache dataCache = getFacetData(reader);
		return new String[]{dataCache.valArray.get(dataCache.orderArray.get(id))};
	}
	
	public void setSeparator(String separator)
	{
		_separator = separator;
	}
	
	public String getSeparator()
	{
		return _separator;
	}
	
	private static int getPathDepth(String path,String separator)
	{
		return path.split(String.valueOf(separator)).length;
	}
	
	
	
	private static class PathValueConverter implements FacetValueConverter{
		private final boolean _strict;
		private final String _sep;
		private final int _depth;
		PathValueConverter(int depth,boolean strict,String sep){
			_strict = strict;
			_sep = sep;
			_depth = depth;
		}
		
		private void getFilters(FacetDataCache dataCache,IntSet intSet,String[] vals, int depth, boolean strict)
	    {
		 for (String val : vals)
		 {
		   getFilters(dataCache,intSet,val,depth,strict);
		 }
	    }
		
		private void getFilters(FacetDataCache dataCache,IntSet intSet,String val, int depth, boolean strict)
		{
		    List<String> termList = dataCache.valArray;
			int index = termList.indexOf(val);

			int startDepth = getPathDepth(val,_sep);
			
			if (index < 0)
			{
				int nextIndex = -(index + 1);
				if (nextIndex == termList.size())
				{
					return;
				}	
				index = nextIndex;
			}
			

			for (int i=index; i<termList.size(); ++i)
			{
				String path = termList.get(i);
				if (path.startsWith(val))
				{
					if (!strict || getPathDepth(path,_sep) - startDepth == depth)
					{
					  intSet.add(i);
					}
				}
				else
				{
					break;
				}	
			}
		}
		
		public int[] convert(FacetDataCache dataCache, String[] vals) {
			IntSet intSet = new IntOpenHashSet();
		    getFilters(dataCache,intSet,vals, _depth, _strict);
		    return intSet.toIntArray();
		}
		
	}
	
	
	
  @Override
  public RandomAccessFilter buildRandomAccessFilter(String value,Properties props) throws IOException
  {
    int depth = getDepth(props);
    boolean strict = isStrict(props);
    return new FacetOrFilter(this,new String[]{value},false,new PathValueConverter(depth,strict,_separator));
  }
  
  @Override
  public RandomAccessFilter buildRandomAccessAndFilter(String[] vals,Properties prop) throws IOException
  {
    if (vals.length > 1)
    {
      return EmptyFilter.getInstance();
    }
    else
    {
      RandomAccessFilter f = buildRandomAccessFilter(vals[0], prop);
      if (f!=null)
      {
        return f;
      }
      else
      {
        return EmptyFilter.getInstance();
      }
    }
  }

  @Override
  public RandomAccessFilter buildRandomAccessOrFilter(String[] vals,Properties prop,boolean isNot) throws IOException
  {
    if (vals.length > 1)
    {
      int depth = getDepth(prop);
      boolean strict = isStrict(prop);
      if (vals.length>0)
      {
    	return new FacetOrFilter(this,vals,isNot,new PathValueConverter(depth,strict,_separator));
      }
      else
      {
        if (isNot)
        {
          return null;
        }
        else
        {
          return EmptyFilter.getInstance();
        }
      }
    }
    else
    {
      RandomAccessFilter f = buildRandomAccessFilter(vals[0], prop);
      if (f == null) return f;
      if (isNot)
      {
        f = new RandomAccessNotFilter(f);
      }
      return f;
    }
  }
	
	@Override
	public FacetCountCollectorSource getFacetCountCollectorSource(final BrowseSelection sel,final FacetSpec ospec) 
	{
		return new FacetCountCollectorSource() {
			
			@Override
			public FacetCountCollector getFacetCountCollector(BoboIndexReader reader,
					int docBase) {
				FacetDataCache dataCache = PathFacetHandler.this.getFacetData(reader);
				return new PathFacetCountCollector(_name,dataCache,docBase,_separator,sel,ospec);
			}
		};
	}

	@Override
	public FacetDataCache load(BoboIndexReader reader) throws IOException {
	    FacetDataCache dataCache = new FacetDataCache();
	    dataCache.load(_name, reader, _termListFactory);
	    return dataCache;
	}
	
	private final static class PathFacetCountCollector implements FacetCountCollector
	{
		private final BrowseSelection _sel;
		private final FacetSpec _ospec;
		private int[] _count;
		private final String _name;
		private final String _sep;
		private BigIntArray _orderArray;
		private FacetDataCache _dataCache;
		private final int _docBase;
		
		PathFacetCountCollector(String name,FacetDataCache dataCache,int docBase,String sep,BrowseSelection sel,FacetSpec ospec)
		{
			_sel = sel;
			_ospec=ospec;
			_name = name;
            _dataCache = dataCache;
            _sep = sep;
            _count = new int[_dataCache.freqs.length];
            _orderArray = _dataCache.orderArray;
      	    _docBase = docBase;
		}
		
		public int[] getCountDistribution()
		{
		  return _count;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public final void collect(int docid) {
			_count[_orderArray.get(docid)]++;
		}
		
		public final void collectAll()
		{
		    _count = _dataCache.freqs; 
		}
		public BrowseFacet getFacet(String value)
		{
		  return null;	
		}
		
		private List<BrowseFacet> getFacetsForPath(String selectedPath,int depth,boolean strict,int minCount)
		{
			LinkedList<BrowseFacet> list=new LinkedList<BrowseFacet>();
			
			String[] startParts=null;
			int startDepth=0;
			
			if (selectedPath!=null && selectedPath.length()>0){					
				startParts=selectedPath.split(_sep);
				startDepth=startParts.length;		
				if (!selectedPath.endsWith(_sep)){
					selectedPath+=_sep;
				}
			}	
			
			String currentPath=null;
			int currentCount=0;
			
			int wantedDepth=startDepth+depth;
			
			int index=0;
			if (selectedPath!=null && selectedPath.length()>0){		
				index=_dataCache.valArray.indexOf(selectedPath);
				if (index<0)
				{
					index=-(index + 1);
				}
			}
			
			for (int i=index;i<_count.length;++i){
				if (_count[i] >= minCount){
					String path=_dataCache.valArray.get(i);
					//if (path==null || path.equals(selectedPath)) continue;						
					
					int subCount=_count[i];
				
					String[] pathParts=path.split(_sep);
					
					int pathDepth=pathParts.length;
								
					if ((startDepth==0) || (startDepth>0 && path.startsWith(selectedPath))){
							StringBuffer buf=new StringBuffer();
							int minDepth=Math.min(wantedDepth, pathDepth);
							for(int k=0;k<minDepth;++k){
								buf.append(pathParts[k]);
								if (!pathParts[k].endsWith(_sep)){
									if (pathDepth!=wantedDepth || k<(wantedDepth-1))
										buf.append(_sep);
								}
							}
							String wantedPath=buf.toString();
							if (currentPath==null){
								currentPath=wantedPath;
								currentCount=subCount;
							}
							else if (wantedPath.equals(currentPath)){
								if (!strict){
									currentCount+=subCount;
								}
							}
							else{	
								boolean directNode=false;
								
								if (wantedPath.endsWith(_sep)){
									if (currentPath.equals(wantedPath.substring(0, wantedPath.length()-1))){
										directNode=true;
									}
								}
								
								if (strict){
									if (directNode){
										currentCount+=subCount;
									}
									else{
										BrowseFacet ch=new BrowseFacet(currentPath,currentCount);
										list.add(ch);
										currentPath=wantedPath;
										currentCount=subCount;
									}
								}
								else{
									if (!directNode){
										BrowseFacet ch=new BrowseFacet(currentPath,currentCount);
										list.add(ch);
										currentPath=wantedPath;
										currentCount=subCount;
									}
									else{
										currentCount+=subCount;
									}
								}
							}
					}
					else{
						break;
					}
				}
			}
			
			if (currentPath!=null && currentCount>0){
				list.add(new BrowseFacet(currentPath,currentCount));
			}
			
			return list;
		}

		public List<BrowseFacet> getFacets() {
			int minCount=_ospec.getMinHitCount();
			
			Properties props = _sel == null ? null : _sel.getSelectionProperties();
			int depth = PathFacetHandler.getDepth(props);
			boolean strict = PathFacetHandler.isStrict(props);
			
			String[] paths= _sel == null ? null : _sel.getValues();
			if (paths==null || paths.length == 0)
			{
				return getFacetsForPath(null, depth, strict, minCount);
			}
			

			LinkedList<BrowseFacet> finalList=new LinkedList<BrowseFacet>();
			for (String path : paths)
			{
				List<BrowseFacet> subList=getFacetsForPath(path, depth, strict, minCount);
				if (subList.size() > 0)
				{
				  finalList.addAll(subList);
				}
			}
			return finalList;
		}
		
	}

}
