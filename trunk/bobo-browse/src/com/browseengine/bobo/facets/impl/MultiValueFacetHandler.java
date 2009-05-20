package com.browseengine.bobo.facets.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ScoreDocComparator;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.BrowseSelection;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.api.BoboIndexReader.WorkArea;
import com.browseengine.bobo.facets.FacetCountCollector;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.FacetHandlerFactory;
import com.browseengine.bobo.facets.data.FacetDataCache;
import com.browseengine.bobo.facets.data.MultiValueFacetDataCache;
import com.browseengine.bobo.facets.data.TermListFactory;
import com.browseengine.bobo.facets.filter.EmptyFilter;
import com.browseengine.bobo.facets.filter.MultiValueFacetFilter;
import com.browseengine.bobo.facets.filter.MultiValueORFacetFilter;
import com.browseengine.bobo.facets.filter.RandomAccessAndFilter;
import com.browseengine.bobo.facets.filter.RandomAccessFilter;
import com.browseengine.bobo.facets.filter.RandomAccessNotFilter;
import com.browseengine.bobo.util.BigNestedIntArray;

public class MultiValueFacetHandler extends FacetHandler implements FacetHandlerFactory 
{
  private static Logger logger = Logger.getLogger(MultiValueFacetHandler.class);

  @Override
  public ScoreDocComparator getScoreDocComparator() 
  {
    return new MultiValueFacetDataCache.MultiFacetScoreDocComparator(_dataCache);
  }

  private final TermListFactory _termListFactory;
  private final String _indexFieldName;

  private int _maxItems = BigNestedIntArray.MAX_ITEMS;
  protected MultiValueFacetDataCache _dataCache;
  private Term _sizePayloadTerm;
  protected Set<String> _depends;
  
  public MultiValueFacetHandler(String name, 
                                String indexFieldName, 
                                TermListFactory termListFactory, 
                                Term sizePayloadTerm,
                                Set<String> depends) 
  {
    super(name, depends);
    _depends = depends;
    _indexFieldName = (indexFieldName != null ? indexFieldName : name);
    _termListFactory = termListFactory;
    _sizePayloadTerm = sizePayloadTerm;
    _dataCache = null;
  }
  
  public MultiValueFacetHandler(String name, String indexFieldName, TermListFactory termListFactory, Term sizePayloadTerm)
  {
    this(name, indexFieldName, termListFactory, sizePayloadTerm, null);
  }

  public MultiValueFacetHandler(String name, TermListFactory termListFactory, Term sizePayloadTerm) 
  {
    this(name, name, termListFactory, sizePayloadTerm, null);
  }

  public MultiValueFacetHandler(String name, String indexFieldName, TermListFactory termListFactory) 
  {
    this(name, indexFieldName, termListFactory, null, null);
  }

  public MultiValueFacetHandler(String name, TermListFactory termListFactory)
  {
    this(name, name, termListFactory);
  }

  public MultiValueFacetHandler(String name, String indexFieldName)
  {
    this(name, indexFieldName, null);
  }

  public MultiValueFacetHandler(String name)
  {
    this(name, name, null);
  }
  
  public MultiValueFacetHandler(String name, Set<String> depends)
  {
    this(name, name, null, null, depends);
  }

  public FacetHandler newInstance()
  {
    return new MultiValueFacetHandler(getName(), _indexFieldName, _termListFactory, _sizePayloadTerm, _depends);
  }

  public final MultiValueFacetDataCache getDataCache()
  {
    return _dataCache;
  }

  public void setMaxItems(int maxItems)
  {
    _maxItems = Math.min(maxItems,BigNestedIntArray.MAX_ITEMS);
  }

  @Override
  public String[] getFieldValues(int id) 
  {
    return _dataCache._nestedArray.getTranslatedData(id, _dataCache.valArray);
  }

  @Override
  public FacetCountCollector getFacetCountCollector(BrowseSelection sel, FacetSpec ospec)  
  {
    return new MultiValueFacetCountCollector(sel, _dataCache, _name, ospec);
  }

  @Override
  public void load(BoboIndexReader reader) throws IOException
  {
    load(reader, new WorkArea());
  }

  @Override
  public void load(BoboIndexReader reader, WorkArea workArea) throws IOException
  {
    if(_dataCache == null)
    {
      _dataCache = new MultiValueFacetDataCache();
    }

    _dataCache.setMaxItems(_maxItems);

    if(_sizePayloadTerm == null)
    {
      _dataCache.load(_indexFieldName, reader, _termListFactory, workArea);
    }
    else
    {
      _dataCache.load(_indexFieldName, reader, _termListFactory, _sizePayloadTerm);
    }
  }

  @Override
  public RandomAccessFilter buildRandomAccessFilter(String value, Properties prop) throws IOException
  {
    int index = _dataCache.valArray.indexOf(value);
    if(index >= 0) 
      return new MultiValueFacetFilter(_dataCache, index);
    else 
      return null;
  }

  @Override
  public RandomAccessFilter buildRandomAccessAndFilter(String[] vals,Properties prop) throws IOException
  {

    ArrayList<RandomAccessFilter> filterList = new ArrayList<RandomAccessFilter>(vals.length);

    for (String val : vals)
    {
      RandomAccessFilter f = buildRandomAccessFilter(val, prop);
      if(f != null) 
      {
        filterList.add(f); 
      }
      else
      {
        return EmptyFilter.getInstance();
      }
    }
    if (filterList.size() == 1) return filterList.get(0);
    return new RandomAccessAndFilter(filterList);
  }

  @Override
  public RandomAccessFilter buildRandomAccessOrFilter(String[] vals,Properties prop,boolean isNot) throws IOException
  {
    if (vals.length > 1)
    {
      RandomAccessFilter f = new MultiValueORFacetFilter(_dataCache,FacetDataCache.convert(_dataCache,vals));
      if (isNot)
      {
        f = new RandomAccessNotFilter(f);
      }
      return f;
    }
    else
    {
      RandomAccessFilter filter = buildRandomAccessFilter(vals[0],prop);
      if (filter == null) return filter;
      if (isNot)
      {
        filter = new RandomAccessNotFilter(filter);
      }
      return filter;
    }
  }



  private static final class MultiValueFacetCountCollector extends DefaultFacetCountCollector
  {
    private final BigNestedIntArray _array;
    MultiValueFacetCountCollector(BrowseSelection sel,
                                  FacetDataCache dataCache,
                                  String name,
                                  FacetSpec ospec)
                                  {
      super(sel,dataCache,name,ospec);
      _array = ((MultiValueFacetDataCache)(_dataCache))._nestedArray;
                                  }

    @Override
    public final void collect(int docid) 
    {
      if(_array.count(docid, _count) == 0) _count[0]++;
    }

    @Override
    public final void collectAll()
    {
      _count = _dataCache.freqs;
    }
  }
}
