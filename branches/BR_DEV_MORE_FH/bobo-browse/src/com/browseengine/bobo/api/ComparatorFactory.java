package com.browseengine.bobo.api;

import java.util.Comparator;

/**
 * Comparator for custom sorting a facet value
 * @author jwang
 */
public interface ComparatorFactory{
	Comparator<Integer> newComparator(FieldValueAccessor fieldValueAccessor,int[] counts);
	Comparator<BrowseFacet> newComparator();
}
