package com.browseengine.bobo.search;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.docidset.RandomAccessDocIdSet;
import com.browseengine.bobo.facets.FacetCountCollector;

public class BoboSearcher2 extends BoboSearcher{
    public BoboSearcher2(BoboIndexReader reader)
    {
        super(reader);
    }

    private class FacetValidator
    {
      protected final FacetHitCollector[] _collectors;
      protected final FacetCountCollector[] _countCollectors;
      private final int _numPostFilters;
      public int _nextTarget;
      
      public FacetValidator() throws IOException
      {
        _collectors = new FacetHitCollector[_facetCollectors.size()];
        _countCollectors = new FacetCountCollector[_collectors.length];
        
        int i = 0;
        int j = _collectors.length;
        
        for (FacetHitCollector facetCollector : _facetCollectors)
        {
          if (facetCollector._postDocIDSetIterator != null) 
          {
            facetCollector._more = facetCollector._postDocIDSetIterator.next();
            facetCollector._doc = (facetCollector._more ? facetCollector._postDocIDSetIterator.doc() : Integer.MAX_VALUE);
            _collectors[i] = facetCollector;
            _countCollectors[i]=facetCollector._facetCountCollector;
            i++;
          }
          else
          {
            j--;
            _collectors[j] = facetCollector;
            _countCollectors[j] = facetCollector._facetCountCollector;
          }
        }
        _numPostFilters = i;
      }
      /**
       * This method validates the doc against any multi-select enabled fields.
       * @param docid
       * @return true if all fields matched
       */
      public boolean validate(final int docid)
        throws IOException
      {
        FacetHitCollector miss = null;
        
        final int len = _numPostFilters;
        if (len == 1)
        {
            FacetHitCollector facetCollector = _collectors[0];
            RandomAccessDocIdSet set = facetCollector._docidSet;
            if (set!=null && !set.get(docid))
            {
              miss = facetCollector;
            }
        }
        else
        {
            for(int i = 0; i < len; i++)
            {
              FacetHitCollector facetCollector = _collectors[i];
              if(facetCollector._more)
              {
                int sid = facetCollector._doc;
                if(sid == docid) continue; // matched
                
                if(sid < docid)
                {
                  DocIdSetIterator iterator = facetCollector._postDocIDSetIterator;
                  if(iterator.skipTo(docid))
                  {
                    facetCollector._more = true;
                    sid = iterator.doc();
                    facetCollector._doc = sid;
                    if(sid == docid) continue; // matched
                  }
                  else
                  {
                    facetCollector._more = false;
                    facetCollector._doc = Integer.MAX_VALUE;
                  }
                }  
              }
              
              if(miss != null)
              {
                // failed because we already have a mismatch
                _nextTarget = (miss._doc < facetCollector._doc ? miss._doc : facetCollector._doc);
                return false;
              }
              miss = facetCollector;
            }
        }
        
        _nextTarget = docid + 1;

        if(miss != null)
        {
          miss._facetCountCollector.collect(docid);
          return false;
        }
        else
        {
          for (FacetCountCollector collector : _countCollectors)
          {
        	 collector.collect(docid);
          }
          return true;
        }
      }
    }
    
    protected FacetValidator createFacetValidator() throws IOException
    {
      boolean needToValidate = false;
      for (FacetHitCollector facetCollector : _facetCollectors)
      {
        if (facetCollector._postDocIDSetIterator != null) 
        {
          needToValidate = true;
          break;
        }
      }

      if(!needToValidate)
      {
        return new FacetValidator()
        {
          public boolean validate(int docid)
          {
            FacetCountCollector[] countCollectors = _countCollectors;
            for (FacetCountCollector collector : countCollectors)
            {
            	collector.collect(docid);
            }
            return true;
          }
        };
      }
      return new FacetValidator();
    }
    
    @Override
    public void search(Weight weight, Filter filter, HitCollector results)
            throws IOException {
        IndexReader reader=getIndexReader();
        
        Scorer scorer = weight.scorer(reader);
        if (scorer == null)
          return;

        final FacetValidator validator = createFacetValidator();
        int target = 0;
        boolean more;
        
        if (filter == null)
        {
          more = scorer.next();
          while(more)
          {
            target = scorer.doc();
            if(validator.validate(target))
            {
              results.collect(target, scorer.score());
              more = scorer.next();
            }
            else
            {
              target = validator._nextTarget;
              more = scorer.skipTo(target);
            }
          }
          return;
        }

        DocIdSetIterator filterDocIdIterator = filter.getDocIdSet(reader).iterator(); // CHECKME: use ConjunctionScorer here?
        
        if(!filterDocIdIterator.next()) return;
        target = filterDocIdIterator.doc();
        
        int doc = -1;
        while(true)
        {
          if(doc < target)
          {
            if(!scorer.skipTo(target)) break;
            doc = scorer.doc();
          }
          
          if(doc == target) // permitted by filter
          {
            if(validator.validate(doc))
            {
              results.collect(doc, scorer.score());
              
              if(!filterDocIdIterator.next()) break;
              target = filterDocIdIterator.doc();
              continue;
            }
            else
            {
              // skip to the next possible docid
              target = validator._nextTarget;
            }
          }
          else // doc > target
          {
            target = doc;
          }
          
          if(!filterDocIdIterator.skipTo(target)) break;
          target = filterDocIdIterator.doc();
        }
    }
}
