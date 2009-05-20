package com.browseengine.bobo.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class ComparableBlender<C>
{
	private static final class Key<C>
	{
		C obj;
		final int src;
		Key(int src)
		{
			this.obj = null;
			this.src = src;
		}
	}

	private static final class KeyCompare<C>
		implements Comparator<Key<C>>
	{
		private final Comparator<C> m_hcmp;
		KeyCompare(Comparator<C> hcmp)
		{
			m_hcmp = hcmp;
		}
		public int compare(Key<C> k1, Key<C> k2)
		{
			final int cmp = m_hcmp.compare(k1.obj, k2.obj);
			return 0 != cmp ? cmp : (k1.src - k2.src); // tie-breaker
		}
	}

	private static final class ObjIterator<C> implements Iterator<C>
	{
		final C[] m_objs;
		int m_idx = 0;
		ObjIterator(C[] objs)
		{
			m_objs = objs;
		}
		public C next()
		{
			return hasNext() ?  m_objs[++m_idx] : null;
		}
		
		public boolean hasNext() {
			return m_idx<(m_objs.length-1);
		}
		
		public void remove() {
		}
		
		@Override
		public String toString(){
			StringBuffer buffer=new StringBuffer();
			if (hasNext()){
				for (int i=m_idx;i<m_objs.length;++i){
					buffer.append(m_objs[i]+",");
				}
			}
			else{
				buffer.append("empty");
			}
			return buffer.toString();
		}
	}

	private static final class Merger<C>
	{
		private final TreeMap<Key<C>, Iterator<C>> m_merger;
		Merger(Comparator<C> cmp, C[][] objs)
		{
			m_merger = new TreeMap<Key<C>, Iterator<C>>(new KeyCompare<C>(cmp));
			for (int i = 0; i != objs.length; ++i){
				if (objs[i]!=null && objs[i].length>0){
					Key<C> key=new Key<C>(i);
					key.obj=objs[i][0];
					m_merger.put(key, new ObjIterator<C>(objs[i]));
				}
			}
		}
		
		Key<C> pop()
		{
			// the next best hit
			final Iterator<Map.Entry<Key<C>, Iterator<C>>>
				it = m_merger.entrySet().iterator();
			if (!it.hasNext())
				return null;

			final Map.Entry<Key<C>, Iterator<C>> entry = it.next();
			final Key<C> key = entry.getKey();
			final Iterator<C> obj_it = entry.getValue();
			it.remove();
			
			if (obj_it.hasNext()){
				Key<C> newKey=new Key<C>(key.src);
				newKey.obj=obj_it.next();
				m_merger.put(newKey, obj_it);
			}
			return key;
		}
		
		@Override
		public String toString(){
			StringBuffer buffer=new StringBuffer();
			final Iterator<Map.Entry<Key<C>, Iterator<C>>>
				it = m_merger.entrySet().iterator();
			int c=0;
			while(it.hasNext()){
				Map.Entry<Key<C>, Iterator<C>> entry = it.next();
				Iterator<C> it2=entry.getValue();
				buffer.append("node "+c+":\n");
				buffer.append("hits: "+it2+"\n");
				c++;
			}
			return buffer.toString();
		}
	}

	private final Comparator<C> m_cmp;

	public ComparableBlender(Comparator<C> cmp)
	{
		m_cmp = cmp;
	}
	
	@SuppressWarnings("unchecked")
	public <D extends C> void blend(D[][] objs, int offset, int num, Collector<D> res)
	{
		if (0 >= num)
			return;

		final Merger<C> merger = new Merger<C>(m_cmp, objs);

		for (int idx = -offset; ; )
		{
			// the next best hit
			final Key<C> key = merger.pop();
			
			if (null == key)
				break;
			// save results from [0, num) range
			final boolean bskip = 0 > idx;
			if (res.collect((D) key.obj, bskip))
				if (num <= ++idx) // we don't need any more results
					break;
		}
	}
}
