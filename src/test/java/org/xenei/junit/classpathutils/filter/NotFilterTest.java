/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xenei.junit.classpathutils.filter;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.xenei.classpathutils.ClassPathFilter;
import org.xenei.classpathutils.filter.NotFilter;
import org.xenei.classpathutils.filter.parser.Parser;

/**
 * Test NotClassFilter.
 *
 */
public class NotFilterTest {
	private Class<?> cls = String.class;
	private String str = cls.getName();

	/**
	 * Test that accept(Class) works
	 */
	@Test
	public void testAcceptClass() {
		ClassPathFilter filter = new NotFilter(ClassPathFilter.FALSE);
		assertTrue(filter.accept(cls));

		filter = new NotFilter(ClassPathFilter.TRUE);
		assertFalse(filter.accept(cls));
	}

	/**
	 * Test that accept(String) works.
	 */
	@Test
	public void testAccceptString() {
		ClassPathFilter filter = new NotFilter(ClassPathFilter.FALSE);
		assertTrue(filter.accept(str));

		filter = new NotFilter(ClassPathFilter.TRUE);
		assertFalse(filter.accept(str));

	}

	/**
	 * Test that accept(String) works.
	 * 
	 * @throws MalformedURLException
	 */
	@Test
	public void testAccceptURL() throws MalformedURLException {
		URL url = new URL("http://example.com");
		ClassPathFilter filter = new NotFilter(ClassPathFilter.FALSE);
		assertTrue(filter.accept(url));

		filter = new NotFilter(ClassPathFilter.TRUE);
		assertFalse(filter.accept(url));
	}

	/**
	 * Test that toString() works.
	 */
	@Test
	public void testToString() {
		ClassPathFilter filter = new NotFilter(ClassPathFilter.FALSE);
		assertEquals("Not( False() )", filter.toString());
	}

	/**
	 * Test that the parser parses string representation correctly.
	 * 
	 * @throws Exception
	 *             on any Exception.
	 */
	@Test
	public void testParse() throws Exception {
		Parser p = new Parser();

		ClassPathFilter cf = p.parse(new NotFilter(ClassPathFilter.FALSE)
				.toString());
		assertTrue("Wrong class", cf instanceof NotFilter);
		String[] args = cf.args();
		assertEquals(ClassPathFilter.FALSE.toString(), args[0]);

	}
}
