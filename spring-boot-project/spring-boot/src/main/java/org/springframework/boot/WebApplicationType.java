/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot;

import org.springframework.util.ClassUtils;

/**
 * An enumeration of possible types of web application.
 *
 * @author Andy Wilkinson
 * @author Brian Clozel
 * @since 2.0.0
 */
public enum WebApplicationType {

	/**
	 * The application should not run as a web application and should not start an
	 * embedded web server.
	 */
	// 不是 web 应用
	NONE,

	/**
	 * The application should run as a servlet-based web application and should start an
	 * embedded servlet web server.
	 */
	// servlet 类型的 web 应用
	SERVLET,

	/**
	 * The application should run as a reactive web application and should start an
	 * embedded reactive web server.
	 */
	// reactive 类型的 web 应用
	REACTIVE;

	private static final String[] SERVLET_INDICATOR_CLASSES = { "javax.servlet.Servlet",
			"org.springframework.web.context.ConfigurableWebApplicationContext" };

	private static final String WEBMVC_INDICATOR_CLASS = "org.springframework."
			+ "web.servlet.DispatcherServlet";

	private static final String WEBFLUX_INDICATOR_CLASS = "org."
			+ "springframework.web.reactive.DispatcherHandler";

	private static final String JERSEY_INDICATOR_CLASS = "org.glassfish.jersey.servlet.ServletContainer";

	private static final String SERVLET_APPLICATION_CONTEXT_CLASS = "org.springframework.web.context.WebApplicationContext";

	private static final String REACTIVE_APPLICATION_CONTEXT_CLASS = "org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext";

	//1.如果classpath 中仅存在 WEBFLUX 相关类，则表明当前项目是reactive类型的web应用，返回；
	//2.如果classpath 中不存在 SERVLET 相关类，则表明当前项目不是web应用，返回；
	//3.如果以上条件都不满足，则表明当前项目是servlet类型的web应用。
	static WebApplicationType deduceFromClasspath() {
		// classpath 中仅存在 WEBFLUX 相关类
		if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null)
				&& !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
				&& !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
			return WebApplicationType.REACTIVE;
		}
		// classpath 不存在 SERVLET 相关类
		for (String className : SERVLET_INDICATOR_CLASSES) {
			if (!ClassUtils.isPresent(className, null)) {
				return WebApplicationType.NONE;
			}
		}
		// 默认 web 类型为 SERVLET
		// 也就是说，同时存在 WEBFLUX 与 SERVLET 相关类，最终返回的是 SERVLET
		return WebApplicationType.SERVLET;
	}

	static WebApplicationType deduceFromApplicationContext(
			Class<?> applicationContextClass) {
		if (isAssignable(SERVLET_APPLICATION_CONTEXT_CLASS, applicationContextClass)) {
			return WebApplicationType.SERVLET;
		}
		if (isAssignable(REACTIVE_APPLICATION_CONTEXT_CLASS, applicationContextClass)) {
			return WebApplicationType.REACTIVE;
		}
		return WebApplicationType.NONE;
	}

	private static boolean isAssignable(String target, Class<?> type) {
		try {
			return ClassUtils.resolveClassName(target, null).isAssignableFrom(type);
		}
		catch (Throwable ex) {
			return false;
		}
	}

}
