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
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.xenei.classpathutils.ClassPathFilter;

/**
 * A ClassFilter providing conditional OR logic across a list of class filters.
 * This filter returns {@code true} if any filters in the list return
 * {@code true}. Otherwise, it returns {@code false}. Checking of the class
 * filter list stops when the first filter returns {@code true}.
 *
 */
public class OrFilter extends _AbstractConditionalFilter implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4219706007335645398L;

	/**
	 * Constructs a new instance of <code>OrClassFilter</code> with the
	 * specified filters.
	 * 
	 * @param filters
	 *            a collection of filters
	 * @throws IllegalArgumentException
	 *             if any filter is null.
	 */
	public OrFilter(final Collection<ClassPathFilter> filters) {
		super(filters);
	}

	/**
	 * Constructs a new file filter that ORs the result of an array of filters.
	 * 
	 * @param filters
	 *            an array of filters
	 * @throws IllegalArgumentException
	 *             if any filter is null.
	 */
	public OrFilter(ClassPathFilter... filters) {
		super(filters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String funcName() {
		return "Or";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(final URL url) {
		List<ClassPathFilter> filters = this.getFilters();
		if (filters.isEmpty()) {
			return false;
		}
		for (ClassPathFilter filter : filters) {
			if (filter.accept(url)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(final String className) {
		List<ClassPathFilter> filters = this.getFilters();
		if (filters.isEmpty()) {
			return false;
		}
		for (ClassPathFilter filter : filters) {
			if (filter.accept(className)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean accept(Class<?> clazz) {
		List<ClassPathFilter> filters = this.getFilters();
		if (filters.isEmpty()) {
			return false;
		}
		for (ClassPathFilter filter : filters) {
			if (filter.accept(clazz)) {
				return true;
			}
		}
		return false;
	}

}
