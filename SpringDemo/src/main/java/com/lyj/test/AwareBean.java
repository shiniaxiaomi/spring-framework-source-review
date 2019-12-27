package com.lyj.test;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author yingjie.lu
 * @version 1.0
 * @date 2019/12/25 4:13 下午
 */

@Component
public class AwareBean implements InstantiationAwareBeanPostProcessor {

	/**
	 * 在bean实例化之前回调
	 */
	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		System.out.println("postProcessBeforeInstantiation:"+beanName);
		return null;
	}

	/**
	 * 在bean实例化之后回调
	 */
	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		System.out.println("postProcessAfterInstantiation:"+beanName);
		return true;
	}

	/**
	 * 在bean初始化之前回调
	 */
	@Override
	public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
		System.out.println("postProcessProperties:"+beanName);
		return null;
	}

	/**
	 * 在bean执行初始化方法之前回调
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("postProcessBeforeInitialization:"+beanName);
		return bean;
	}

	/**
	 * 在bean执行初始化方法之后回调
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("postProcessAfterInitialization:"+beanName);
		return bean;
	}

	public AwareBean() {
		System.out.println("构造方法:AwareBean");
	}
}
