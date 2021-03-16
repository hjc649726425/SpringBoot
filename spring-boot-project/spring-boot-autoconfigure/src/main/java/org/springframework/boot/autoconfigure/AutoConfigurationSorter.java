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

package org.springframework.boot.autoconfigure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.Assert;

/**
 * Sort {@link EnableAutoConfiguration auto-configuration} classes into priority order by
 * reading {@link AutoConfigureOrder}, {@link AutoConfigureBefore} and
 * {@link AutoConfigureAfter} annotations (without loading classes).
 *
 * @author Phillip Webb
 */
class AutoConfigurationSorter {

	private final MetadataReaderFactory metadataReaderFactory;

	private final AutoConfigurationMetadata autoConfigurationMetadata;

	AutoConfigurationSorter(MetadataReaderFactory metadataReaderFactory,
			AutoConfigurationMetadata autoConfigurationMetadata) {
		Assert.notNull(metadataReaderFactory, "MetadataReaderFactory must not be null");
		this.metadataReaderFactory = metadataReaderFactory;
		this.autoConfigurationMetadata = autoConfigurationMetadata;
	}

	public List<String> getInPriorityOrder(Collection<String> classNames) {
		// 1. 将 classNames 包装成 AutoConfigurationClasses
		AutoConfigurationClasses classes = new AutoConfigurationClasses(
				this.metadataReaderFactory, this.autoConfigurationMetadata, classNames);
		// 2. 按类名排序
		List<String> orderedClassNames = new ArrayList<>(classNames);
		// Initially sort alphabetically
		// 3. 使用 @AutoConfigureOrder 排序
		Collections.sort(orderedClassNames);
		// Then sort by order
		orderedClassNames.sort((o1, o2) -> {
			int i1 = classes.get(o1).getOrder();
			int i2 = classes.get(o2).getOrder();
			return Integer.compare(i1, i2);
		});
		// Then respect @AutoConfigureBefore @AutoConfigureAfter
		// 4. 使用 @AutoConfigureBefore，@AutoConfigureAfter 排序
		orderedClassNames = sortByAnnotation(classes, orderedClassNames);
		return orderedClassNames;
	}

	/**
	 * 进行排序，
	 * 实际上这个方法里只是准备了一些数据，真正干活的是 doSortByAfterAnnotation(...)
	 */
	private List<String> sortByAnnotation(AutoConfigurationClasses classes,
			List<String> classNames) {
		// 需要排序的 className
		List<String> toSort = new ArrayList<>(classNames);
		toSort.addAll(classes.getAllNames());
		// 排序好的 className
		Set<String> sorted = new LinkedHashSet<>();
		// 正在排序中的 className
		Set<String> processing = new LinkedHashSet<>();
		while (!toSort.isEmpty()) {
			// 真正处理排序的方法
			doSortByAfterAnnotation(classes, toSort, sorted, processing, null);
		}
		// 存在于集合 sorted 中，但不存在于 classNames 中的元素将会被移除
		sorted.retainAll(classNames);
		return new ArrayList<>(sorted);
	}

	/**
	 * 具体进行排序的方法
	 * 1.查找当前 className 需要在哪些 className 之后装配，将其保存为afterClasses，也就是说，
	 *  afterClasses中的每一个className都要在当前className之前装配；
	 * 2. 遍历afterClasses，对其中每一个className，继续查找其afterClasses，这样递归下去，
	 *  不考虑循环比较的情况下，最终必然会存在一个className，它的afterClasses为空，
	 *  这里就把className加入到已完成排序的结构中。
	 */
	private void doSortByAfterAnnotation(AutoConfigurationClasses classes,
			List<String> toSort, Set<String> sorted, Set<String> processing,
			String current) {
		if (current == null) {
			current = toSort.remove(0);
		}
		// 使用 processing 来判断是否存在循环比较，比如，类A after 类B，而 类B 又 after 类A
		processing.add(current);
		// classes.getClassesRequestedAfter：当前 className 需要在哪些 className 之后执行
		for (String after : classes.getClassesRequestedAfter(current)) {
			Assert.state(!processing.contains(after),
					"AutoConfigure cycle detected between " + current + " and " + after);
			if (!sorted.contains(after) && toSort.contains(after)) {
				// 递归调用
				doSortByAfterAnnotation(classes, toSort, sorted, processing, after);
			}
		}
		processing.remove(current);
		// 添加到已排序结果中
		sorted.add(current);
	}

	private static class AutoConfigurationClasses {
		// 保存结果
		private final Map<String, AutoConfigurationClass> classes = new HashMap<>();

		/**
		 * 构造方法
		 */
		AutoConfigurationClasses(MetadataReaderFactory metadataReaderFactory,
				AutoConfigurationMetadata autoConfigurationMetadata,
				Collection<String> classNames) {
			// 进行方法调用
			addToClasses(metadataReaderFactory, autoConfigurationMetadata, classNames,
					true);
		}

		public Set<String> getAllNames() {
			return this.classes.keySet();
		}

		/**
		 * 添加类，就是将类包装成 AutoConfigurationClass，添加到名为 classes 的 Map 中
		 * classNames 就是去除了排除类的所有自动装配类
		 */
		/**
		 * 1.遍历传入的classNames，对其中每一个className，进行下面的操作；
		 * 2.创建 AutoConfigurationClass，传入className；
		 * 3.调用AutoConfigurationSorter.AutoConfigurationClass#isAvailable方法，得到available；
		 * 4.判断available 与 required 的值，如果其一为ture，就将其添加到classes；
		 * 5.如果available为true，递归处理className由@AutoConfigureBefore与@AutoConfigureAfter指定的类。
		 */
		private void addToClasses(MetadataReaderFactory metadataReaderFactory,
				AutoConfigurationMetadata autoConfigurationMetadata,
				Collection<String> classNames, boolean required) {
			for (String className : classNames) {
				if (!this.classes.containsKey(className)) {
					// 将 className 包装成 AutoConfigurationClass
					AutoConfigurationClass autoConfigurationClass = new AutoConfigurationClass(
							className, metadataReaderFactory, autoConfigurationMetadata);
					boolean available = autoConfigurationClass.isAvailable();
					// @AutoConfigureBefore 与 @AutoConfigureAfter 标记的类的 required 为 false
					if (required || available) {
						this.classes.put(className, autoConfigurationClass);
					}
					if (available) {
						// 递归调用
						addToClasses(metadataReaderFactory, autoConfigurationMetadata,
								autoConfigurationClass.getBefore(), false);
						addToClasses(metadataReaderFactory, autoConfigurationMetadata,
								autoConfigurationClass.getAfter(), false);
					}
				}
			}
		}

		public AutoConfigurationClass get(String className) {
			return this.classes.get(className);
		}

		public Set<String> getClassesRequestedAfter(String className) {
			// 当前类：获取在哪些类之后执行，就是获取 @AutoConfigureAfter 注解指定的类
			Set<String> classesRequestedAfter = new LinkedHashSet<>();
			classesRequestedAfter.addAll(get(className).getAfter());
			// 其他类：需要前置执行的类中
			this.classes.forEach((name, autoConfigurationClass) -> {
				if (autoConfigurationClass.getBefore().contains(className)) {
					classesRequestedAfter.add(name);
				}
			});
			return classesRequestedAfter;
		}

	}

	private static class AutoConfigurationClass {

		private final String className;

		private final MetadataReaderFactory metadataReaderFactory;

		private final AutoConfigurationMetadata autoConfigurationMetadata;

		private volatile AnnotationMetadata annotationMetadata;

		private volatile Set<String> before;

		private volatile Set<String> after;

		AutoConfigurationClass(String className,
				MetadataReaderFactory metadataReaderFactory,
				AutoConfigurationMetadata autoConfigurationMetadata) {
			this.className = className;
			this.metadataReaderFactory = metadataReaderFactory;
			this.autoConfigurationMetadata = autoConfigurationMetadata;
		}

		public boolean isAvailable() {
			try {
				if (!wasProcessed()) {
					getAnnotationMetadata();
				}
				return true;
			}
			catch (Exception ex) {
				return false;
			}
		}

		public Set<String> getBefore() {
			if (this.before == null) {
				this.before = (wasProcessed()
						// 如果存在于 `META-INF/spring-autoconfigure-metadata.properties` 文件中，直接获取值
						? this.autoConfigurationMetadata.getSet(this.className,
								"AutoConfigureBefore", Collections.emptySet())
						// 否则从 @AutoConfigureBefore 注解上获取
						: getAnnotationValue(AutoConfigureBefore.class));
			}
			return this.before;
		}

		public Set<String> getAfter() {
			if (this.after == null) {
				this.after = (wasProcessed()
						// 如果存在于 `META-INF/spring-autoconfigure-metadata.properties` 文件中，直接获取值
						? this.autoConfigurationMetadata.getSet(this.className,
								"AutoConfigureAfter", Collections.emptySet())
						// 否则从 @AutoConfigureAfter 注解上获取
						: getAnnotationValue(AutoConfigureAfter.class));
			}
			return this.after;
		}

		private int getOrder() {
			// 判断 META-INF/spring-autoconfigure-metadata.properties 文件中是否存在当前 className
			if (wasProcessed()) {
				// 如果存在，就使用文件中指定的顺序，否则就使用默认顺序
				return this.autoConfigurationMetadata.getInteger(this.className,
						"AutoConfigureOrder", AutoConfigureOrder.DEFAULT_ORDER);
			}
			// 处理不存在的情况：获取 @AutoConfigureOrder 注解指定的顺序
			Map<String, Object> attributes = getAnnotationMetadata()
					.getAnnotationAttributes(AutoConfigureOrder.class.getName());
			// 如果 @AutoConfigureOrder 未配置，就使用默认顺序
			return (attributes != null) ? (Integer) attributes.get("value")
					: AutoConfigureOrder.DEFAULT_ORDER;
		}

		private boolean wasProcessed() {
			return (this.autoConfigurationMetadata != null
					// 判断 META-INF/spring-autoconfigure-metadata.properties 文件中是否存在该配置
					&& this.autoConfigurationMetadata.wasProcessed(this.className));
		}

		private Set<String> getAnnotationValue(Class<?> annotation) {
			Map<String, Object> attributes = getAnnotationMetadata()
					.getAnnotationAttributes(annotation.getName(), true);
			if (attributes == null) {
				return Collections.emptySet();
			}
			Set<String> value = new LinkedHashSet<>();
			Collections.addAll(value, (String[]) attributes.get("value"));
			Collections.addAll(value, (String[]) attributes.get("name"));
			return value;
		}

		private AnnotationMetadata getAnnotationMetadata() {
			if (this.annotationMetadata == null) {
				try {
					// 加载`className`对应的资源，当 className 对应的资源不存在时，会抛出异常
					MetadataReader metadataReader = this.metadataReaderFactory
							.getMetadataReader(this.className);
					this.annotationMetadata = metadataReader.getAnnotationMetadata();
				}
				catch (IOException ex) {
					throw new IllegalStateException(
							"Unable to read meta-data for class " + this.className, ex);
				}
			}
			return this.annotationMetadata;
		}

	}

}
