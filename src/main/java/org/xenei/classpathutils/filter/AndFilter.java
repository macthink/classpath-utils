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
 * A ClassFilter providing conditional AND logic across a list of file filters.
 * This filter returns {@code true} if all filters in the list return
 * {@code true}. Otherwise, it returns {@code false}. Checking of the file
 * filter list stops when the first filter returns {@code false}.
 */
public class AndFilter extends _AbstractConditionalFilter implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7607072170374969854L;

	/**
	 * Constructs a new instance of <code>AndClassFilter</code> with the
	 * specified list of filters.
	 *
	 * @param classFilters
	 *            a Collection of ClassFilter instances, copied, null ignored
	 */
	public AndFilter(final Collection<ClassPathFilter> classFilters) {
		super(classFilters);
	}

	/**
	 * Constructs a new instance of <code>AndClassFilter</code> with the
	 * specified list of filters.
	 *
	 * @param classFilters
	 *            a List of ClassFilter instances, copied, null ignored
	 */
	public AndFilter(final ClassPathFilter... classFilters) {
		super(classFilters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String funcName() {
		return "And";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(String className) {
		List<ClassPathFilter> filters = this.getFilters();

		if (filters.isEmpty()) {
			return false;
		}
		for (ClassPathFilter classFilter : filters) {
			if (!classFilter.accept(className)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(URL url) {
		List<ClassPathFilter> filters = this.getFilters();

		if (filters.isEmpty()) {
			return false;
		}
		for (ClassPathFilter classFilter : filters) {
			if (!classFilter.accept(url)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(Class<?> clazz) {
		List<ClassPathFilter> filters = this.getFilters();

		if (filters.isEmpty()) {
			return false;
		}
		for (ClassPathFilter classFilter : filters) {
			if (!classFilter.accept(clazz)) {
				return false;
			}
		}
		return true;
	}
}
