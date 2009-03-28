package com.browseengine.bobo.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

public class ListMerger
{
  private ListMerger()
  {
    
  }
  
  public static <T> Iterator<T> mergeLists(final Iterator<T>[] iterators,final Comparator<T> comparator){
    
    return new Iterator<T>(){
        TreeMap<T,Iterator<T>> map=new TreeMap<T,Iterator<T>>(comparator);
        {
            for (Iterator<T> result : iterators)
            {
                if (result.hasNext())
                {
                    map.put(result.next(),result);
                }
            }
        }
        
        public boolean hasNext() {
            return map.size()>0;
        }

        public T next() {
            T first=map.firstKey();
            Iterator<T> iter=map.remove(first);
            while (iter.hasNext())
            {
                T next=iter.next();
                if (!map.containsKey(next))
                {
                    map.put(next,iter);
                    break;
                }
            }
            return first;
        }

        public void remove() {
            T first=map.firstKey();
            Iterator<T> iter=map.remove(first);
            while (iter.hasNext())
            {
                T next=iter.next();
                if (!map.containsKey(next))
                {
                    map.put(next,iter);
                    break;
                }
            }
        }
    };
}
  
  public static <T> ArrayList<T> mergeLists(int offset,int count,Iterator<T>[] iterators,Comparator<T> comparator)
  {
    Iterator<T> mergedIter=mergeLists(iterators, comparator);
    
    int c=0;
    while(c<offset && mergedIter.hasNext())
    {
      mergedIter.next();
      c++;
    }
    
    ArrayList<T> mergedList=new ArrayList<T>();
    
    c=0;
    while(mergedIter.hasNext() && c<count)
    {
      mergedList.add(mergedIter.next());
      c++;
    }
    
    return mergedList;
    
  }
}
