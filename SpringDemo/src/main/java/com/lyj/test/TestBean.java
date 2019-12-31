package com.lyj.test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author yingjie.lu
 * @version 1.0
 * @date 2019/12/25 4:12 下午
 */


@Component
public class TestBean implements InitializingBean, BeanFactoryAware, ApplicationContextAware, DisposableBean {

	/**
	 * 顺序1:调用构造方法
	 */
	public TestBean() {
		System.out.println("构造方法:TestBean");
	}

	/**
	 * 顺序3:调用实现InitializingBean接口的afterPropertiesSet方法
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("afterPropertiesSet:TestBean");
	}

	/**
	 * 顺序2:调用@PostConstruct标注的方法
	 */
	@PostConstruct
	public void init(){
		System.out.println("PostConstruct:TestBean");
	}

	@PreDestroy
	public void preDestroy(){
		System.out.println("preDestroy:TestBean");
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
//		DefaultListableBeanFactory beanFactory1 = (DefaultListableBeanFactory) beanFactory;
//		beanFactory1.getBeanDefinition()
		System.out.println("setBeanFactory");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//		AnnotationConfigApplicationContext applicationContext1 = (AnnotationConfigApplicationContext) applicationContext;
//		applicationContext1.registerBeanDefinition();
		System.out.println("setApplicationContext");
	}

	@Override
	public void destroy() throws Exception {
		System.out.println("destroy");
	}



}
