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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.classpathutils.ClassPathFilter;

/**
 * A ClassFilter that filters classes that are annotations.
 */
public class AnnotationClassFilter extends _AbstractBaseFilter implements
		Serializable {

	private static final Logger LOG = LoggerFactory
			.getLogger(AnnotationClassFilter.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 7819589374453236969L;

	/** Singleton instance of file filter */
	public static final ClassPathFilter ANNOTATION = new AnnotationClassFilter();

	/**
	 * Restrictive constructor.
	 */
	private AnnotationClassFilter() {
	}

	@Override
	protected Logger getLog() {
		return LOG;
	}

	/**
	 * Checks to see if the class is abstract.
	 *
	 * @param clazz
	 *            the Class to check
	 * @return true if the class is abstract
	 */
	@Override
	public boolean accept(Class<?> clazz) {
		return clazz.isAnnotation();
	}

	/**
	 * Checks to see if the class is abstract.
	 *
	 * @param className
	 *            the Class name to check
	 * @return true if the class is abstract
	 */
	@Override
	public boolean accept(String className) {

		try {
			return accept(Class.forName(className));
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] args() {
		return NO_ARGS;
	}

	/**
	 * Always returns false.
	 */
	@Override
	public boolean accept(URL url) {
		return false;
	}

}
