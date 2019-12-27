/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}


	//执行BeanFactory的后置处理器
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		Set<String> processedBeans = new HashSet<>();

		//+++++++++++++++++++++++++先执行默认已经存在的BeanDefinitionRegistry的回调+++++++++++++++++++++++++
		//如果beanFactory属于BeanDefinitionRegistry类型
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			//进行beanFactoryPostProcessors的postProcessBeanDefinitionRegistry回调
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}

			//接下来会执行所有的beanPostProcessor相关的回调,用于在bean实例化进行人为的干预
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			//===========首先,先执行实现了PriorityOrdered接口的回调=========
			//拿到所有的BeanDefinitionRegistryPostProcessor类型的beanName
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			//遍历所有beanName,并将实现了PriorityOrdered接口的BeanDefinitionRegistryPostProcessor先加到RegistryProcessors中
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					//通过bean的名称和类型注册并实例化相关的BeanDefinitionRegistry后置处理器
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					//并将已经添加的名称保存,用于接下来不重复执行回调
					processedBeans.add(ppName);
				}
			}
			//将实现PriorityOrdered接口的postProcessBeanDefinitionRegistry进行排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			//将所有的排好序并且优先级较高的先全部添加到registryProcessors中
			registryProcessors.addAll(currentRegistryProcessors);
			//执行优先级较高的postProcessBeanDefinitionRegistry回调
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			//执行完之后,将已经执行的postProcessor全部清空
			currentRegistryProcessors.clear();

			//=========接下来,执行实现Ordered接口的postProcessBeanDefinitionRegistry的回调========
			//拿到实现了Ordered接口的所有beanName,进行遍历回调
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					//并将已经添加的名称保存,用于接下来不重复执行回调
					processedBeans.add(ppName);
				}
			}
			//这里和之前上面的逻辑是一样的,就是先排序,再回调,最后清空
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			//===========最后,执行剩下的postProcessBeanDefinitionRegistry回调===========
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					//如果之前都没有执行过该对应的回调,那么就添加
					if (!processedBeans.contains(ppName)) {
						//通过bean的名称和类型注册并实例化相关的BeanDefinitionRegistry后置处理器
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				//这里和之前上面的逻辑是一样的,就是先排序,再回调,最后清空
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			//执行所有的BeanFactoryPostProcessors的postProcessBeanFactory方法
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
		}
		//如果不属于BeanDefinitionRegistry类型
		else {
			//执行所有默认的BeanFactoryPostProcessors的postProcessBeanFactory方法
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		//+++++++++++++++++++++++++在执行自己实现的BeanDefinitionRegistry的回调+++++++++++++++++++++++++
		//获取到所有类型为BeanFactoryPostProcessor的beanName
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		//===========下面的套路和上面是一样的,就是先排序,再执行,再清空==========
		//下面将不同的还未执行过的BeanFactoryPostProcessor分发到不同的集合中
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			//如果之前已经回调过了,那么这里就跳过
			if (processedBeans.contains(ppName)) {

			}
			//如果实现了PriorityOrdered接口,则添加到对应的集合中
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			//如果实现了Ordered接口,则添加到对应的集合中
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			//剩下的都添加到nonOrdered的集合中
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		//===========首先,将实现PriorityOrdered接口的进行在priorityOrderedPostProcessors集合中排序===========
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		//再挨个执行排序后的集合中的postProcessBeanFactory回调
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		//===========接着,将实现Ordered接口并且是BeanFactoryPostProcessor类型的进行在orderedPostProcessors集合中排序==========
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		//再挨个执行排序后的集合中的postProcessBeanFactory回调
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		//===========最后,将BeanFactoryPostProcessor类型的保存到nonOrderedPostProcessors集合中==========
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		//再挨个执行nonOrderedPostProcessors集合中的postProcessBeanFactory回调
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		//清除缓存
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		//获取到所有的postProcessor的name
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);//获取所有类型为BeanPostProcessor的后置处理器

		//注册BeanPostProcessorChecker,用于打印bean创建的日志,并校验bean的数量等
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		//比较BeanPostProcessors的优先级并进行排序(总的顺序是按照PriorityOrdered>Ordered>其余的)
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		//将实现了不同接口的类进行分发,分发到不同的集合中,然后再按照每个集合中每个类的优先级去执行回调
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {//如果实现了PriorityOrdered接口
				//如果属于BeanPostProcessor类型
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				//将该PostProcessor添加到priorityOrderedPostProcessors集合中
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					//如果该PostProcessor又属于MergedBeanDefinitionPostProcessor类型,则再添加到internalPostProcessors集合中
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {//如果实现了Ordered接口
				//则添加到orderedPostProcessorNames集合中
				orderedPostProcessorNames.add(ppName);
			}
			else {
				//将剩下的全部添加到nonOrderedPostProcessorNames集合中
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		//========首先,先注册实现了PriorityOrdered接口的BeanPostProcessors===========
		//将全都实现了PriorityOrdered接口的BeanPostProcessors再进行排序
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		//将排好序的BeanPostProcessors全部注册到beanFactory中
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		//接下来,再注册实现了Ordered接口的BeanPostProcessors,套路和上面的相同,先排序,然后再全部注册到beanFactory中===========
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);//添加
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);//排序
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);//注册

		//注册全部普通BeanPostProcessors(剩下的所有BeanPostProcessors),套路和上面略微不同,直接全部注册,因为没有顺序要求
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);//注册

		//internalPostProcessors保存着用于合并bean定义的后处理器回调接口
		//BeanPostProcessor实现可以实现这个子接口,用于在创建bean实例时的合并beanDefinition的回调(原始bean定义的已处理副本)
		//最后,重新注册所有的属于MergedBeanDefinitionPostProcessor类型的后置处理器
		sortPostProcessors(internalPostProcessors, beanFactory);//排序
		registerBeanPostProcessors(beanFactory, internalPostProcessors);//注册

		//将ApplicationListenerDetector注册到最后面
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		//执行所有的postProcessBeanDefinitionRegistry回调
		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
