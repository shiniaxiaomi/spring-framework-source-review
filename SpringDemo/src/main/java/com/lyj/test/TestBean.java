package com.lyj.test;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author yingjie.lu
 * @version 1.0
 * @date 2019/12/25 4:12 下午
 */


@Component
public class TestBean implements InitializingBean {

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
}
