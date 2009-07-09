package com.browseengine.bobo.util;

public interface Collector<T>
{
	/** return true if the collection grows
		@param bskip causes the collector to make a "dry run"
	 */
	public boolean collect(T obj, boolean bskip);
}
