package com.browseengine.bobo.api;

import java.util.Comparator;

/**
 * Comparator for custom sorting a facet value
 * @author jwang
 */
public interface ComparatorFactory{
	/**
	 * Providers a {@link Comparator<Integer>} from field values and counts. This is called within a browse.
	 * @param fieldValueAccessor accessor for field values
	 * @param counts hit counts
	 * @return {@link Comparator<Integer} instance
	 */
	Comparator<Integer> newComparator(FieldValueAccessor fieldValueAccessor,int[] counts);
	
	/**
	 * Providers a {@link Comparator<BrowseFacet>}. This is called when doing a merge across browses.
	 * @return {@link Comparator<BrowseFacet} instance
	 */
	Comparator<BrowseFacet> newComparator();
}
