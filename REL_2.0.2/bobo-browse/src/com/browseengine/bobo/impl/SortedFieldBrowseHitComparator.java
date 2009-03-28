package com.browseengine.bobo.impl;

import java.util.Comparator;

import org.apache.log4j.Logger;
import org.apache.lucene.search.SortField;

import com.browseengine.bobo.api.BrowseHit;

public class SortedFieldBrowseHitComparator implements Comparator<BrowseHit>
{
  private static final Logger logger = Logger.getLogger(SortedFieldBrowseHitComparator.class);
  
  private final SortField[] _sortFields;
  
  public SortedFieldBrowseHitComparator(SortField[] sortFields)
  {
    _sortFields = sortFields;
  }
  
  private int compare(BrowseHit h1, BrowseHit h2, SortField sort)
  {
    int type = sort.getType();
    
    int c = 0;
    
    switch (type) {
      case SortField.SCORE:{
        float r1 = h1.getScore();
        float r2 = h2.getScore();
        if (r1 > r2) c = -1;
        if (r1 < r2) c = 1;
        break;
      }
      case SortField.DOC:{
        int i1 = h1.getDocid();
        int i2 = h2.getDocid();
        c = i2 - i1;
        break;
      }
      case SortField.INT:{
        int i1 = ((Integer)h1.getComparable(sort.getField())).intValue();
        int i2 = ((Integer)h2.getComparable(sort.getField())).intValue();
        c = i1 - i2;
        break;
      }
      case SortField.LONG:{
        long l1 = ((Long)h1.getComparable(sort.getField())).longValue();
        long l2 = ((Long)h2.getComparable(sort.getField())).longValue();
        if (l1 < l2) c = -1;
        if (l1 > l2) c = 1;
        break;
      }
      case SortField.STRING:{
        String s1 = (String) h1.getField(sort.getField());
        String s2 = (String) h2.getField(sort.getField());
        if (s1==null)
        {
          if (s2 == null) c = 0;
          else c = 1;
        }
        else
        {
          c = s1.compareTo(s2);
        }
        break;
      }
      case SortField.FLOAT:{
        float f1 = ((Float)h1.getComparable(sort.getField())).floatValue();
        float f2 = ((Float)h2.getComparable(sort.getField())).floatValue();
        if (f1 < f2) c = -1;
        if (f1 > f2) c = 1;
        break;
      }
      case SortField.DOUBLE:{
        double d1 = ((Double)h1.getComparable(sort.getField())).doubleValue();
        double d2 = ((Double)h2.getComparable(sort.getField())).doubleValue();
        if (d1 < d2) c = -1;
        if (d1 > d2) c = 1;
        break;
      }
      case SortField.BYTE:{
        int i1 = ((Byte)h1.getComparable(sort.getField())).byteValue();
        int i2 = ((Byte)h2.getComparable(sort.getField())).byteValue();
        c = i1 - i2;
        break;
      }
      case SortField.SHORT:{
        int i1 = ((Short)h1.getComparable(sort.getField())).shortValue();
        int i2 = ((Short)h2.getComparable(sort.getField())).shortValue();
        c = i1 - i2;
        break;
      }
      case SortField.CUSTOM:
      case SortField.AUTO:{
        String field = sort.getField();
        Comparable obj1 = h1.getComparable(field);
        Comparable obj2 = h2.getComparable(field);
        if (obj1 == null)
        {
          if (obj2 == null) c = 0;
          else c = 1;
        }
        else
        {
          c = obj1.compareTo(obj2);
        }
        break;
      }
      default:{
              throw new RuntimeException ("invalid SortField type: "+type);
      }
    }
    
    if (sort.getReverse()) {
      c = -c;
    }
    
    return c;
  }

  public int compare(BrowseHit h1, BrowseHit h2)
  {
    for (SortField sort : _sortFields)
    {
      int val = compare(h1,h2,sort);
      if (val != 0) return val;
    }
    return h2.getDocid() - h1.getDocid();
  }
}
