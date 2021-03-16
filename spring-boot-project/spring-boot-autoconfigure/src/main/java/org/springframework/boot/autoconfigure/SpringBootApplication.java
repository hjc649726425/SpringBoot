/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;

/**
 * Indicates a {@link Configuration configuration} class that declares one or more
 * {@link Bean @Bean} methods and also triggers {@link EnableAutoConfiguration
 * auto-configuration} and {@link ComponentScan component scanning}. This is a convenience
 * annotation that is equivalent to declaring {@code @Configuration},
 * {@code @EnableAutoConfiguration} and {@code @ComponentScan}.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 1.2.0
 */

/**
 1.@SpringBootApplication是一个组合注解，包含了@SpringBootConfiguration、@EnableAutoConfiguration、@ComponentScan 三个注解的功能，同时提供了一些属性配置，也是来自于以上3个注解；
 2.@SpringBootConfiguration 包含了Configuration注解的功能；
 3.@EnableAutoConfiguration是开启自动装配的关键注解，其中标记了@AutoConfigurationPackage，会将被@SpringBootApplication标记的类所在的包，包装成BasePackages，然后注册到spring容器中；@EnableAutoConfiguration 还通过@Import注解向容器中引入了AutoConfigurationImportSelector，该类会将当前项目支持的自动配置类添加到spring容器中；
 4.@ComponentScan 定义了包扫描路径，其excludeFilters值可以用来排除类的扫描，springboot指定了TypeExcludeFilter，表明我们可以继承该类来自主定义排除的类 ；同时也指定了 AutoConfigurationExcludeFilter ，该Filter 可以用来排除自动配置类，也就是说，自动配置类不会进行包描述操作。
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
		@Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {

	/**
	 * Exclude specific auto-configuration classes such that they will never be applied.
	 * @return the classes to exclude
	 */
	/**
	 * 自动装配要排除的类，功能来自于 @EnableAutoConfiguration
	 */
	@AliasFor(annotation = EnableAutoConfiguration.class)
	Class<?>[] exclude() default {};

	/**
	 * Exclude specific auto-configuration class names such that they will never be
	 * applied.
	 * @return the class names to exclude
	 * @since 1.3.0
	 */
	/**
	 *  自动装配要排除的类名，功能来自于 @EnableAutoConfiguration
	 */
	@AliasFor(annotation = EnableAutoConfiguration.class)
	String[] excludeName() default {};

	/**
	 * Base packages to scan for annotated components. Use {@link #scanBasePackageClasses}
	 * for a type-safe alternative to String-based package names.
	 * @return base packages to scan
	 * @since 1.3.0
	 */
	/**
	 * 配置扫描的包，功能来自于 @ComponentScan
	 */
	@AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
	String[] scanBasePackages() default {};

	/**
	 * Type-safe alternative to {@link #scanBasePackages} for specifying the packages to
	 * scan for annotated components. The package of each class specified will be scanned.
	 * <p>
	 * Consider creating a special no-op marker class or interface in each package that
	 * serves no purpose other than being referenced by this attribute.
	 * @return base packages to scan
	 * @since 1.3.0
	 */
	/**
	 * 配置扫描的class，该class所在的包都会被扫描，功能来自于 @ComponentScan
	 */
	@AliasFor(annotation = ComponentScan.class, attribute = "basePackageClasses")
	Class<?>[] scanBasePackageClasses() default {};

}
