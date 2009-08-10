/**
 * Bobo Browse Engine - High performance faceted/parametric search implementation 
 * that handles various types of semi-structured data.  Written in Java.
 * 
 * Copyright (C) 2005-2006  spackle
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * To contact the project administrators for the bobo-browse project, 
 * please go to https://sourceforge.net/projects/bobo-browse/, or 
 * contact owner@browseengine.com.
 */

package com.browseengine.bobo.util.test;

import junit.framework.TestCase;

import com.browseengine.bobo.util.CachingScheme;
import com.browseengine.bobo.util.TimeoutHardCache;

/**
 * @author spackle
 *
 */
public class TimeoutCacheTest extends TestCase {
	public void testCaching() throws Throwable {
		try {
			long timeout = 1000;
			long half = timeout/2;
			// 5 objects, for 1 second before timeout
			CachingScheme<String,Integer> cache = new TimeoutHardCache<String,Integer>(5, timeout);
			for (int i = 0; i < 6; i++) {
				cache.cacheObject(""+i, i);
			}
			for (int i = 0; i < 6; i++) {
				assertTrue("should have been out of LRU cache "+i, cache.getObject(""+i) == null);
				cache.cacheObject(""+i, i);
			}
			for (int i = 1; i < 4; i++) {
				assertTrue("should have been in the cache "+i, cache.getObject(""+i) != null);
			}
			Thread.sleep(half);
			for (int i = 4; i < 6; i++) {
				assertTrue("should have been in the cache after sleep "+half+" "+i, cache.getObject(""+i) != null);
				cache.cacheObject(""+i, i);
			}
			Thread.sleep(half+1);
			for (int i = 1; i < 4; i++) {
				assertTrue("should have timed out of cache after 2 sleeps "+half+" "+i, cache.getObject(""+i) == null);
			}
			for (int i = 4; i < 6; i++) {
				assertTrue("should have still been in cache after 2 sleeps for "+half+" "+i, cache.getObject(""+i) != null);
			}
			Thread.sleep(half);
			for (int i = 0; i < 6; i++) {
				assertTrue("should have been out of cache after another "+half+" "+i, cache.getObject(""+i) == null);
			}
		} catch (Throwable t) {
			System.err.println("fail: "+t.getMessage());
			t.printStackTrace();
			throw t;
		}
	}

}
