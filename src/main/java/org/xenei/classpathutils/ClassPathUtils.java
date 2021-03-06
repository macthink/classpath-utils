/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xenei.classpathutils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xenei.classpathutils.filter.PrefixFilter;

/**
 * Package of class path searching utilities
 *
 */
public class ClassPathUtils {

	private static final Logger LOG = LoggerFactory
			.getLogger(ClassPathUtils.class);

	/**
	 * Recursive method used to find all classes in a given directory and
	 * subdirs. Adapted from http://snippets.dzone.com/posts/show/4831 and
	 * extended to support use of JAR files
	 *
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return The classes
	 * @throws IOException
	 *             on error.
	 */
	public static Set<String> findClasses(final String directory,
			final String packageName) throws IOException {
		return findClasses(directory, packageName,
				new PrefixFilter(packageName));
	}

	/**
	 * Find the classes in a directory and sub directory.
	 * 
	 * @param directory
	 *            The directory or jar file to search.
	 * @param packageName
	 *            The list of packages to look for.
	 * @param filter
	 *            The filter to apply to results.
	 * @return list of class names that match the filter.
	 * @throws IOException
	 *             on error.
	 */
	public static Set<String> findClasses(final String directory,
			String packageName, final ClassPathFilter filter)
			throws IOException {
		final Set<String> classes = new HashSet<String>();
		if (directory.startsWith("file:")) {
			if (directory.contains("!")) {
				handleJar(classes, directory, filter);
			} else {
				scanDir(classes, packageName,
						new File(directory.substring("file:".length())), filter);
			}
		} else {
			scanDir(classes, packageName, new File(directory), filter);
		}
		return classes;
	}

	/**
	 * Find all classes accessible from the context class loader which belong to
	 * the given package and sub packages.
	 *
	 * An empty or null packageName = all packages.
	 *
	 * @param packageName
	 *            The base package or class name.
	 * @return A collection of Class objects.
	 */
	public static Collection<Class<?>> getClasses(final String packageName) {
		return getClasses(packageName, new PrefixFilter(packageName));
	}

	/**
	 * Get a collection of classes in the package name that meet the filter.
	 * Classes will be loaded from the current thread class loader.
	 * 
	 * @param packageName
	 *            The package name to locate.
	 * @param filter
	 *            the ClassFilter to filter the results with.
	 * @return A collection of Class objects.
	 */
	public static Collection<Class<?>> getClasses(final String packageName,
			ClassPathFilter filter) {
		final ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		if (classLoader == null) {
			LOG.error("Class loader may not be null.  No class loader for current thread");
			return Collections.emptyList();
		}
		return getClasses(classLoader, packageName, filter);
	}

	/**
	 * Find all classes accessible from the class loader which belong to the
	 * given package and sub packages.
	 *
	 * Adapted from http://snippets.dzone.com/posts/show/4831 and extended to
	 * support use of JAR files
	 *
	 * @param classLoader
	 *            The class loader to load the classes from.
	 * @param packageName
	 *            The base package or class name
	 * @return A collection of Class objects
	 */
	public static Collection<Class<?>> getClasses(
			final ClassLoader classLoader, final String packageName) {
		return getClasses(classLoader, packageName, new PrefixFilter(
				packageName));
	}

	/**
	 * Find all classes accessible from the class loader which belong to the
	 * given package and sub packages.
	 *
	 * Adapted from http://snippets.dzone.com/posts/show/4831 and extended to
	 * support use of JAR files
	 *
	 * @param classLoader
	 *            The class loader to load the classes from.
	 * @param packageName
	 *            The package name to locate the classes in.
	 * @param filter
	 *            The filter for the classes.
	 * @return A collection of Class objects
	 */
	public static Collection<Class<?>> getClasses(
			final ClassLoader classLoader, final String packageName,
			final ClassPathFilter filter) {
		if (classLoader == null) {
			LOG.error("Class loader may not be null.");
			return Collections.emptyList();
		}
		if (packageName == null) {
			LOG.error("Package name may not be null.");
			return Collections.emptyList();
		}

		String dirName = packageName.replace('.', '/');
		Enumeration<URL> resources;
		try {
			resources = classLoader.getResources(dirName);
		} catch (final IOException e1) {
			LOG.error(e1.toString());
			return Collections.emptyList();
		}

		final Set<Class<?>> classes = new HashSet<Class<?>>();
		final Set<String> directories = new HashSet<String>();
		if (resources.hasMoreElements()) {
			while (resources.hasMoreElements()) {
				final URL resource = resources.nextElement();
				String dir = resource.getPath();
				if (!directories.contains(dir)) {
					directories.add(dir);

					try {
						for (final String clazz : findClasses(dir, packageName,
								filter)) {
							try {
								LOG.debug("Adding class {}", clazz);
								classes.add(Class.forName(clazz, false,
										classLoader));
							} catch (final ClassNotFoundException e) {
								LOG.warn(e.toString());
							}
						}
					} catch (final IOException e) {
						LOG.warn(e.toString());
					}
				}
			}
		} else {
			if (packageName.length() > 0) {
				// there are no resources at that path so see if it is a class
				try {
					classes.add(Class.forName(packageName, false, classLoader));
				} catch (final ClassNotFoundException e) {
					LOG.warn(String.format(
							"'%s' was neither a package name nor a class name",
							packageName));
				}
			}
		}
		return classes;
	}

	/**
	 * Return the set of classes from the collection that pass the filter.
	 * 
	 * @param classes
	 *            The collection of classes to filter.
	 * @param filter
	 *            The filter to use.
	 * @return the set of Class objects that pass the filter.
	 */
	public static Set<Class<?>> filterClasses(Collection<Class<?>> classes,
			ClassPathFilter filter) {
		Set<Class<?>> retval = new HashSet<Class<?>>();
		for (Class<?> clazz : classes) {
			if (filter.accept(clazz)) {
				retval.add(clazz);
			}
		}
		return retval;
	}

	/**
	 * Return the set of classes from the collection that pass the filter.
	 * 
	 * @param classNames
	 *            the collection of class names.
	 * @param filter
	 *            The filter to apply.
	 * @return the set of class names that pass the filter.
	 */
	public static Set<String> filterClassNames(Collection<String> classNames,
			ClassPathFilter filter) {
		Set<String> retval = new HashSet<String>();
		for (String className : classNames) {
			if (filter.accept(className)) {
				retval.add(className);
			}
		}
		return retval;
	}

	/**
	 * Get the array of class path elements.
	 *
	 * These are strings separated by java.class.path property
	 *
	 * @return Array of class path elements
	 */
	public static String[] getClassPathElements() {
		final String splitter = String.format("\\%s",
				System.getProperty("path.separator"));
		final String[] classPath = System.getProperty("java.class.path").split(
				splitter);
		return classPath;
	}

	/**
	 * Get all the interfaces for the class.
	 *
	 * @param clazz
	 *            The class to find interfaces for.
	 * @return set of interfaces implemented by clazz.
	 */
	public static Set<Class<?>> getAllInterfaces(final Class<?> clazz) {
		// set of implementation classes
		final Set<Class<?>> implClasses = new LinkedHashSet<Class<?>>();
		// populate the set of implementation classes
		ClassPathUtils.getAllInterfaces(implClasses, clazz);
		return implClasses;
	}

	/**
	 * Get all the interfaces for the class that meet the filter.
	 *
	 * @param clazz
	 *            The class to find interfaces for.
	 * @param filter
	 *            The filter to apply.
	 * @return set of interfaces implemented by clazz.
	 */
	public static Set<Class<?>> getAllInterfaces(final Class<?> clazz,
			ClassPathFilter filter) {
		return filterClasses(getAllInterfaces(clazz), filter);
	}

	/**
	 * Get all the interfaces that the class implements. Adds the interfaces to
	 * the set of classes.
	 *
	 * This method calls recursively to find all parent interfaces.
	 *
	 * @param set
	 *            The set off classes to add the interface classes to.
	 * @param c
	 *            The class to check.
	 */
	public static void getAllInterfaces(final Set<Class<?>> set,
			final Class<?> c) {
		if ((c == null) || (c == Object.class)) {
			return;
		}
		for (final Class<?> i : c.getClasses()) {
			if (i.isInterface()) {
				if (!set.contains(i)) {
					set.add(i);
					getAllInterfaces(set, i);
				}
			}
		}
		for (final Class<?> i : c.getInterfaces()) {
			if (!set.contains(i)) {
				set.add(i);
				getAllInterfaces(set, i);
			}
		}
		getAllInterfaces(set, c.getSuperclass());
	}

	/**
	 * Recursive method used to find all classes in a given directory and
	 * subdirs. Adapted from http://snippets.dzone.com/posts/show/4831 and
	 * extended to support use of JAR files
	 *
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return The classes
	 * @throws IOException
	 *             on error.
	 */
	public static Set<String> findResources(final String directory,
			final String packageName) throws IOException {
		return findResources(directory, packageName, new PrefixFilter(
				packageName));
	}

	/**
	 * Find the classes in a directory and sub directory.
	 * 
	 * @param directory
	 *            The directory or jar file to search.
	 * @param packageName
	 *            The list of packages to look for.
	 * @param filter
	 *            The filter to apply to results.
	 * @return list of class names that match the filter.
	 * @throws IOException
	 *             on error.
	 */
	public static Set<String> findResources(final String directory,
			String packageName, final ClassPathFilter filter)
			throws IOException {
		final Set<String> classes = new HashSet<String>();
		if (directory.startsWith("file:")) {
			if (directory.contains("!")) {
				handleJar(classes, directory, filter);
			} else {
				scanDir(classes, packageName,
						new File(directory.substring("file:".length())), filter);
			}
		} else {
			scanDir(classes, packageName, new File(directory), filter);
		}
		return classes;
	}

	/**
	 * Find all classes accessible from the context class loader which belong to
	 * the given package and sub packages.
	 *
	 * An empty or null packageName = all packages.
	 *
	 * @param packageName
	 *            The base package or class name.
	 * @return A collection of Class objects.
	 */
	public static Collection<URL> getResources(final String packageName) {
		return getResources(packageName, new PrefixFilter(packageName));
	}

	/**
	 * Get a collection of classes in the package name that meet the filter.
	 * Classes will be loaded from the current thread class loader.
	 * 
	 * @param packageName
	 *            The package name to locate.
	 * @param filter
	 *            the ClassFilter to filter the results with.
	 * @return A collection of Class objects.
	 */
	public static Collection<URL> getResources(final String packageName,
			ClassPathFilter filter) {
		final ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		if (classLoader == null) {
			LOG.error("Class loader may not be null.  No class loader for current thread");
			return Collections.emptyList();
		}
		
		return getResources(classLoader, packageName,  filter);
	}

	/**
	 * Find all classes accessible from the class loader which belong to the
	 * given package and sub packages.
	 *
	 * Adapted from http://snippets.dzone.com/posts/show/4831 and extended to
	 * support use of JAR files
	 *
	 * @param classLoader
	 *            The class loader to load the classes from.
	 * @param packageName
	 *            The base package or class name
	 * @return A collection of Class objects
	 */
	public static Collection<URL> getResources(final ClassLoader classLoader,
			final String packageName) {
		return getResources(classLoader, packageName, new PrefixFilter(
				packageName));
	}

	/**
	 * Find all classes accessible from the class loader which belong to the
	 * given package and sub packages.
	 *
	 * Adapted from http://snippets.dzone.com/posts/show/4831 and extended to
	 * support use of JAR files
	 *
	 * @param classLoader
	 *            The class loader to load the classes from.
	 * @param packageName
	 *            The package name to locate the classes in.
	 * @param filter
	 *            The filter for the classes.
	 * @return A collection of URL objects
	 */
	public static Collection<URL> getResources(final ClassLoader classLoader,
			final String packageName, final ClassPathFilter filter) {
		if (classLoader == null) {
			LOG.error("Class loader may not be null.");
			return Collections.emptyList();
		}
		if (packageName == null) {
			LOG.error("Package name may not be null.");
			return Collections.emptyList();
		}

		String dirName = packageName.replace('.', '/');
		Enumeration<URL> resources;
		try {
			resources = classLoader.getResources(dirName);
		} catch (final IOException e1) {
			LOG.error(e1.toString());
			return Collections.emptyList();
		}

		final Set<URL> classes = new HashSet<URL>();
		final Set<String> directories = new HashSet<String>();
		if (resources.hasMoreElements()) {
			while (resources.hasMoreElements()) {
				final URL resource = resources.nextElement();
				String dir = resource.getPath();
				if (!directories.contains(dir)) {
					directories.add(dir);

					try {
						for (final String clazz : findResources(dir,
								packageName, filter)) {
							LOG.debug("Adding class {}", clazz);
							URL url = classLoader.getResource(clazz);
							if (url != null) {
								classes.add(url);
							} else {
								LOG.warn(String.format("Unable to locate: %s",
										clazz));
							}
						}
					} catch (final IOException e) {
						LOG.warn(e.toString());
					}
				}
			}
		} else {
			if (packageName.length() > 0) {
				// there are no resources at that path so see if it is a class
				LOG.debug("Adding class {}", packageName);
				URL url = classLoader.getResource(packageName);
				if (url != null) {
					classes.add(url);
				} else {
					LOG.warn(String.format("Unable to locate: %s", packageName));
					classes.add(classLoader.getResource(packageName));
				}

			}
		}
		return classes;
	}

	/**
	 * Handle the files in a given package. The directory is already known to be
	 * at or under the directory specified by the package name. So we just have
	 * to find matches.
	 * 
	 * @param classes
	 * @param packageName
	 * @param dir
	 * @param cFilter
	 */
	private static void handleDir(Set<String> classes, String packageName,
			File dir, ClassPathFilter cFilter) {
		if (!dir.exists()) {
			return;
		}
		if (dir.isDirectory()) {
			// handle all the classes in the directory
			for (File file : dir.listFiles()) {
				handleDir(classes, packageName, file, cFilter);
			}
			// handle all the sub-directories
			for (File file : dir.listFiles((FileFilter) new AndFileFilter(
					DirectoryFileFilter.DIRECTORY, new NotFileFilter(
							new PrefixFileFilter("."))))) {
				final String newPkgName = String.format("%s%s%s", packageName,
						(packageName.length() > 0 ? "/" : ""), file.getName());
				handleDir(classes, newPkgName, file, cFilter);
			}
		} else {
			// just in case
			// if (dir.getName().endsWith(".class")) {
			// process the file name.
			String className = String.format("%s%s%s", packageName,
					(packageName.length() > 0 ? "/" : ""), dir.getName());
			// create class name
			// className = className.substring(0, className.length()
			// - ".class".length());
			if (cFilter.accept(className)) {
				classes.add(className);
			}
			// }
		}
	}

	/**
	 * Scan a directory for packages that match. This method is used prior to
	 * finding a matching directory. Once the package names is matched
	 * handleDir() is used.
	 * 
	 * @param classes
	 *            The classes that have been found.
	 * @param packageName
	 *            The package name for classes to find.
	 * @param dir
	 *            The directory to scan.
	 * @param cFilter
	 *            The class acceptance filter.
	 */
	private static void scanDir(Set<String> classes, String packageName,
			File dir, ClassPathFilter cFilter) {
		if (!dir.exists()) {
			return;
		}
		if (dir.isDirectory()) {
			if (dir.getPath().endsWith(packageName.replace('.', '/'))) {
				// we have a match
				handleDir(classes, packageName, dir, cFilter);
			} else {
				// no match check next level
				for (File file : dir.listFiles((FileFilter) new AndFileFilter(
						DirectoryFileFilter.DIRECTORY, new NotFileFilter(
								new PrefixFileFilter("."))))) {
					scanDir(classes, packageName, file, cFilter);
				}
			}
		}
		// if it is not a directory we don't process it here as we are looking
		// for directories that start with the packageName.
	}

	/**
	 * handle finding classes in a jar.
	 * 
	 * @param classes
	 *            the classes that have been found.
	 * @param directory
	 *            The directory path to a file in a jar or the jar itself.
	 * @param filter
	 *            The classes to accept.
	 * @throws IOException
	 */
	private static void handleJar(Set<String> classes, String directory,
			ClassPathFilter filter) throws IOException {
		final String[] split = directory.split("!");
		final URL jar = new URL(split[0]);
		final String prefix = split[1].substring(1);
		final ZipInputStream zip = new ZipInputStream(jar.openStream());
		ZipEntry entry = null;
		while ((entry = zip.getNextEntry()) != null) {
			if (entry.getName().startsWith( prefix) && filter.accept(entry.getName())) {
				classes.add(entry.getName());
			}
		}
	}

}
