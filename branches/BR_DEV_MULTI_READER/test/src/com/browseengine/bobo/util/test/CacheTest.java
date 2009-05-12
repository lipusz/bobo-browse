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
 * please go to https://sourceforge.net/projects/bobo-browse/.
 * <or other contact info for bobo-browse; snail mail/email/both>
 */

package com.browseengine.bobo.util.test;

import junit.framework.TestCase;

import com.browseengine.bobo.util.SimpleHardCache;

/**
 * @author spackle
 *
 */
public class CacheTest extends TestCase {
	public void testCaching() throws Throwable {
		try {
			SimpleHardCache<Integer,String> cache = new SimpleHardCache<Integer,String>(10);
			for (int i = 0; i < 11; i++) {
				Integer key = new Integer(i);
				String value = key.toString();
				assertTrue("cache entry wasn't null for key="+i, null == cache.getObject(key));
				cache.cacheObject(key, value);
			}
			// 0 should not be there, but all the others should
			assertTrue("zero should not be there, but was", null == cache.getObject(new Integer(0)));
			for (int i = 1; i < 11; i++) {
				Integer key = new Integer(i);
				String value = key.toString();
				assertTrue("cache entry should have been in the cache for key: "+key, null != cache.getObject(key));
			}
			// [1,10] is in there, with order preserved
			for (int i = 11; i < 16; i++) {
				Integer key = new Integer(i);
				String value = key.toString();
				cache.cacheObject(key, value);
			}
			// [15,14,13,12,11,10,9,8,7,6] is in there
			for (int i = 6; i < 16; i++) {
				Integer key = new Integer(i);
				assertTrue("cache entry should have been in the cache for key: "+key, null != cache.getObject(key));
			}
			// order the same
			for (int i = 0; i < 6; i++) {
				assertTrue("cache entry should not have been there for: "+i, null == cache.getObject(new Integer(i)));
			}
			System.out.println("caching success");
		} catch (Throwable t) {
			System.err.println("fail: "+t.getMessage());
			t.printStackTrace();
			throw t;
		}
	}
}
