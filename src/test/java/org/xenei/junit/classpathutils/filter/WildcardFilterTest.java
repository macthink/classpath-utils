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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.xenei.classpathutils.Case;
import org.xenei.classpathutils.ClassPathFilter;
import org.xenei.classpathutils.filter.RegexFilter;
import org.xenei.classpathutils.filter.WildcardFilter;
import org.xenei.classpathutils.filter.parser.Parser;

/**
 * Test WildcardClassFilter
 *
 */
public class WildcardFilterTest {

	private final ClassPathFilter filter_sens;
	private final ClassPathFilter filter_insens;

	private Class<?> t = ClassPathFilter.class;
	private Class<?> f = String.class;

	/**
	 * Constructor.
	 */
	public WildcardFilterTest() {
		filter_sens = new WildcardFilter(Case.SENSITIVE,
				"*xene?.*ClassPathFilter");
		filter_insens = new WildcardFilter(Case.INSENSITIVE,
				"*Xene?.*ClassPathFilter");
	}

	/**
	 * Test that accept(Class) works
	 */
	@Test
	public void testAcceptClass() {
		assertTrue(filter_sens.accept(t));
		assertTrue(filter_insens.accept(t));

		assertFalse(filter_sens.accept(f));
		assertFalse(filter_insens.accept(f));
	}

	/**
	 * Test that accept(String) works.
	 */
	@Test
	public void testAccceptString() {

		assertTrue(filter_sens.accept(t.getName()));
		assertTrue(filter_insens.accept(t.getName()));

		assertFalse(filter_sens.accept(t.getName().toUpperCase()));
		assertTrue(filter_insens.accept(t.getName().toUpperCase()));

		assertFalse(filter_sens.accept(f.getName()));
		assertFalse(filter_insens.accept(f.getName()));
	}

	/**
	 * Test that accept(String) works.
	 * 
	 * @throws MalformedURLException
	 */
	@Test
	public void testAccceptURL() throws MalformedURLException {

		URL url = new URL("http://example.com");
		RegexFilter sens = new RegexFilter(Case.SENSITIVE, ".*example.c.*");
		RegexFilter insens = new RegexFilter(Case.INSENSITIVE, ".*Example.c.*");

		assertTrue(sens.accept(url));
		assertTrue(insens.accept(url));

		url = new URL("http://Example.com");
		assertFalse(sens.accept(url));
		assertTrue(insens.accept(url));

		url = new URL("http://example.net");
		assertFalse(sens.accept(url));
		assertFalse(insens.accept(url));
	}

	/**
	 * Test that toString() works.
	 */
	@Test
	public void testToString() {
		assertEquals("Wildcard( Sensitive, *xene?.*ClassPathFilter )",
				filter_sens.toString());
		assertEquals("Wildcard( Insensitive, *Xene?.*ClassPathFilter )",
				filter_insens.toString());
	}

	/**
	 * Test that the dot does not get expanded to regex expression.
	 */
	@Test
	public void testDotPosition() {
		assertEquals("^\\Q.org.xenei.\\E$",
				WildcardFilter.makeRegex(".org.xenei."));
	}

	/**
	 * Test that the asterisk is expanded to .* whereever it is found in the
	 * string.
	 */
	@Test
	public void testAsteriskPosition() {
		assertEquals("^.*\\Qorg\\E.*\\Qxenei\\E.*$",
				WildcardFilter.makeRegex("*org*xenei*"));
		assertEquals("^.*\\Q.bad.\\E.*$", WildcardFilter.makeRegex("*.bad.*"));
	}

	/**
	 * Test that the asterisk is expanded to .* whereever it is found in the
	 * string.
	 */
	@Test
	public void testSingleWildcards() {
		assertEquals("^.*$", WildcardFilter.makeRegex("*"));
		assertEquals("^.$", WildcardFilter.makeRegex("?"));
	}

	/**
	 * Test that the question mark is expanded to . (dot) where ever it is found
	 * in the string.
	 */
	@Test
	public void testQuestionPosition() {
		assertEquals("^.\\Qorg\\E.\\Qxenei\\E.$",
				WildcardFilter.makeRegex("?org?xenei?"));
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

		ClassPathFilter cf = p.parse(filter_sens.toString());
		assertTrue("Wrong class", cf instanceof WildcardFilter);
		String[] args = cf.args();
		assertEquals(Case.SENSITIVE.toString(), args[0]);
		assertEquals("*xene?.*ClassPathFilter", args[1]);

		cf = p.parse(filter_insens.toString());
		assertTrue("Wrong class", cf instanceof WildcardFilter);
		args = cf.args();
		assertEquals(Case.INSENSITIVE.toString(), args[0]);
		assertEquals("*Xene?.*ClassPathFilter", args[1]);

	}
}
