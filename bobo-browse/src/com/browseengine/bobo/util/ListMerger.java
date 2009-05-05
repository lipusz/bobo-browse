package com.browseengine.bobo.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.util.PriorityQueue;

/**
 * @author ymatsuda
 *
 */
public class ListMerger
{
  public static class MergedIterator<T> implements Iterator<T>
  {
    private class IteratorNode
    {
      public Iterator<T> _iterator;
      public T _curVal;
      
      public IteratorNode(Iterator<T> iterator)
      {
        _iterator = iterator;
        _curVal = null;
      }
        
      public boolean fetch()
      {
        if(_iterator.hasNext())
        {
          _curVal = _iterator.next();
          return true;
        }
        _curVal = null;
        return false;
      }
    }

    private final PriorityQueue _queue;

    private MergedIterator(final int length, final Comparator<T> comparator)
    {
      _queue = new PriorityQueue()
      {
        {
          this.initialize(length);
        }
      
        @SuppressWarnings("unchecked")
        @Override
        protected boolean lessThan(Object o1, Object o2)
        {
          T v1 = ((IteratorNode)o1)._curVal;
          T v2 = ((IteratorNode)o2)._curVal;
          
          return (comparator.compare(v1, v2) < 0);
        }
      };
    }
    
    public MergedIterator(final List<Iterator<T>> iterators, final Comparator<T> comparator)
    {
      this(iterators.size(), comparator);
      for(Iterator<T> iterator : iterators)
      {
        IteratorNode ctx = new IteratorNode(iterator);
        if(ctx.fetch()) _queue.insert(ctx);
      }
    }
    
    public MergedIterator(final Iterator<T>[] iterators, final Comparator<T> comparator)
    {
      this(iterators.length, comparator);
      for(Iterator<T> iterator : iterators)
      {
        IteratorNode ctx = new IteratorNode(iterator);
        if(ctx.fetch()) _queue.insert(ctx);
      }
    }

    public boolean hasNext()
    {
      return _queue.size() > 0;
    }

    @SuppressWarnings("unchecked")
    public T next()
    {
      IteratorNode ctx = (IteratorNode)_queue.top();
      T val = ctx._curVal;
      if (ctx.fetch())
      {
        _queue.adjustTop();
      }
      else
      {
        _queue.pop();
      }
      return val;
    }
    
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
  
  private ListMerger() { }
 
  public static <T> Iterator<T> mergeLists(final Iterator<T>[] iterators, final Comparator<T> comparator)
  {
    return new MergedIterator<T>(iterators, comparator);
  }
  
  public static <T> Iterator<T> mergeLists(final List<Iterator<T>> iterators, final Comparator<T> comparator)
  {
    return new MergedIterator<T>(iterators, comparator);
  }
  
  public static <T> ArrayList<T> mergeLists(int offset, int count, Iterator<T>[] iterators, Comparator<T> comparator)
  {
    return mergeLists(offset, count, new MergedIterator<T>(iterators, comparator));
  }
  
  public static <T> ArrayList<T> mergeLists(int offset, int count, List<Iterator<T>> iterators, Comparator<T> comparator)
  {
    return mergeLists(offset, count, new MergedIterator<T>(iterators, comparator));
  }
  
  private static <T> ArrayList<T> mergeLists(int offset, int count, Iterator<T> mergedIter)
  {
    for (int c = 0; c < offset && mergedIter.hasNext(); c++)
    {
      mergedIter.next();
    }
    
    ArrayList<T> mergedList = new ArrayList<T>();
    
    for (int c = 0; c < count && mergedIter.hasNext(); c++)
    {
      mergedList.add(mergedIter.next());
    }
    
    return mergedList;
  }
}
