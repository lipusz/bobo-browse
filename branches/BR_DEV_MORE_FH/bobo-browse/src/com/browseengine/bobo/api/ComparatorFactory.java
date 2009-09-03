package com.browseengine.bobo.api;

import java.util.Comparator;

import com.browseengine.bobo.facets.data.TermValueList;

/**
 * Comparator for custom sorting a facet value
 * @author jwang
 */
public interface ComparatorFactory{
	Comparator<Integer> newComparator(TermValueList valueList,int[] counts);
	Comparator<BrowseFacet> newComparator();
}
