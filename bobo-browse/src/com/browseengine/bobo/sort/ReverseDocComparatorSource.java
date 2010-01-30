package com.browseengine.bobo.sort;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;

public class ReverseDocComparatorSource extends DocComparatorSource {
	private final DocComparatorSource _inner;
	public ReverseDocComparatorSource(DocComparatorSource inner) {
		_inner = inner;
	}

	@Override
	public DocComparator getComparator(IndexReader reader, int docbase)
			throws IOException {
		return new ReverseDocComparator(_inner.getComparator(reader, docbase));
	}
	
	public static class ReverseDocComparator extends DocComparator{
		private final DocComparator _comparator;
		public ReverseDocComparator(DocComparator comparator){
			_comparator = comparator;
		}
		
		@Override
		public int compare(ScoreDoc doc1, ScoreDoc doc2) {
			return -1*_comparator.compare(doc1, doc2);
		}

		@Override
		public Comparable value(final ScoreDoc doc) {
			return new ReverseComparable(_comparator.value(doc));
			
		}
		
		private static class ReverseComparable implements Comparable{
			final Comparable _inner;
			
			ReverseComparable(Comparable inner){
				_inner = inner;
			}
			
			public int compareTo(Object o) {
				if (o instanceof ReverseComparable){
					Comparable inner = ((ReverseComparable)o)._inner;
					return -1*_inner.compareTo(inner);
				}
				else{
					throw new IllegalStateException("expected instanace of "+ReverseComparable.class);
				}
			}
			
			@Override
			public String toString(){
				StringBuilder buf = new StringBuilder();
				buf.append("!").append(_inner);
				return buf.toString();
			}
		}
		
	}

}
