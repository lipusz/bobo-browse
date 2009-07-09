package com.browseengine.bobo.docidset;

import java.io.IOException;

import org.apache.lucene.search.DocIdSetIterator;


public abstract class FilteredDocSetIterator extends DocIdSetIterator {
	protected DocIdSetIterator _innerIter;
	private int _currentDoc;
	
	public FilteredDocSetIterator(DocIdSetIterator innerIter)
	{
		if (innerIter == null)
		{
			throw new IllegalArgumentException("null iterator");
		}
		_innerIter=innerIter;
		_currentDoc=-1;
	}
	
	abstract protected boolean match(int doc);
	
	public final int doc() {
		return _currentDoc;
	}

	public final boolean next() throws IOException{
		while(_innerIter.next())
		{
			int doc=_innerIter.doc();
			if (match(doc))
			{
				_currentDoc=doc;
				return true;
			}
		}
		return false;
	}

	public final boolean skipTo(int n) throws IOException{
		boolean flag=_innerIter.skipTo(n);
		if (flag)
		{
			int doc=_innerIter.doc();
			if (match(doc))
			{
				_currentDoc=doc;
				return true;
			}
			else
			{
			  while(_innerIter.next())
		      {
		        int docid=_innerIter.doc();
		        if (match(docid))
		        {
		          _currentDoc=docid;
		          return true;
		        }
		      }
		      return false;
			}
		}
		return flag;
	}

}
