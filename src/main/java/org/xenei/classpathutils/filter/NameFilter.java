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

package org.xenei.classpathutils.filter;

import java.io.Serializable;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.classpathutils.Case;

/**
 * filters classes by name.
 * 
 */
public class NameFilter extends _AbstractStringFilter implements Serializable {

	private static final Logger LOG = LoggerFactory.getLogger(NameFilter.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 2314511406134237664L;

	/**
	 * Constructs a new case-sensitive name class filter for a single name.
	 * 
	 * @param name
	 *            the name to allow, must not be null
	 * @throws IllegalArgumentException
	 *             if the name is null
	 */
	public NameFilter(String name) {
		super(name);
	}

	@Override
	protected Logger getLog() {
		return LOG;
	}

	/**
	 * Construct a new name class filter specifying case-sensitivity.
	 *
	 * @param caseSensitivity
	 *            how to handle case sensitivity, null means case-sensitive
	 * @param name
	 *            the name to allow, must not be null
	 * @throws IllegalArgumentException
	 *             if the name is null
	 */
	public NameFilter(Case caseSensitivity, String name) {
		super(caseSensitivity, name);
	}

	/**
	 * Constructs a new case-sensitive name class filter for an array of names.
	 * <p>
	 * The array is not cloned, so could be changed after constructing the
	 * instance. This would be inadvisable however.
	 * </p>
	 * 
	 * @param names
	 *            the names to allow, must not be null
	 * @throws IllegalArgumentException
	 *             if the names array is null
	 */
	public NameFilter(String... names) {
		super(names);
	}

	/**
	 * Constructs a new name class filter for an array of names specifying
	 * case-sensitivity.
	 * <p>
	 * The array is not cloned, so could be changed after constructing the
	 * instance. This would be inadvisable however.
	 * 
	 * @param caseSensitivity
	 *            how to handle case sensitivity, null means case-sensitive
	 * @param names
	 *            the names to allow, must not be null
	 * @throws IllegalArgumentException
	 *             if the names array is null
	 */
	public NameFilter(Case caseSensitivity, String... names) {
		super(caseSensitivity, names);
	}

	/**
	 * Constructs a new case-sensitive name class filter for a collection of
	 * names.
	 * 
	 * @param names
	 *            the names to allow, must not be null
	 * @throws IllegalArgumentException
	 *             if the name list is null
	 * @throws ClassCastException
	 *             if the list does not contain Strings
	 */
	public NameFilter(Collection<String> names) {
		super(names);
	}

	/**
	 * Constructs a new name class filter for a collection of names specifying
	 * case-sensitivity.
	 * 
	 * @param caseSensitivity
	 *            how to handle case sensitivity, null means case-sensitive
	 * @param names
	 *            the names to allow, must not be null
	 * @throws IllegalArgumentException
	 *             if the name list is null
	 * @throws ClassCastException
	 *             if the list does not contain Strings
	 */
	public NameFilter(Case caseSensitivity, Collection<String> names) {
		super(caseSensitivity, names);
	}

	/**
	 * Checks to see if the name matches.
	 * 
	 * @param className
	 *            the class name to check
	 * @return true if the filename matches
	 */
	@Override
	public boolean accept(String className) {
		for (String name2 : getStrings()) {
			if (caseSensitivity.checkEquals(className, name2)) {
				return true;
			}
		}
		return false;
	}

}
