package com.lyj.test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author yingjie.lu
 * @version 1.0
 * @date 2019/12/26 2:58 下午
 */

@Component
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

	/**
	 * 执行顺序:1
	 * 可以添加或修改BeanDefinition
	 * 在执行该方法时,所有常规的bean都已经加载并添加到BeanDefinitionMap中,但是此时的bean还未被实例化,
	 * 所以,在该方法中,允许修改已经添加到BeanDefinitionMap中的bean的定义,或者是添加我们自己的bean的定义,
	 * 然后在bean的实例化时,添加或修改的bean都会生效,并被spring所管理
	 * @param registry 上下文的beanDefinitionMap(bean定义的注册表)
	 * @throws BeansException
	 */
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		System.out.println("postProcessBeanDefinitionRegistry");
	}

	/**
	 * 执行顺序:2
	 * 只可以修改BeanDefinition,而不能添加BeanDefinition
	 * 在执行该方法时,所有bean定义都已加载，但还没有实例化bean;
	 * @param beanFactory 上下文的beanFactory
	 * @throws BeansException
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("postProcessBeanFactory");

//		DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
//		factory.registerBeanDefinition("mybean",null);
	}

	public MyBeanDefinitionRegistryPostProcessor() {
		System.out.println("构造方法:MyBeanDefinitionRegistryPostProcessor");
	}
}
