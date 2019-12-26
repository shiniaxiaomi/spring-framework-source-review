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

	public TestBean() {
		System.out.println("TestBean");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("afterPropertiesSet");
	}

	@PostConstruct
	public void init(){
		System.out.println("PostConstruct");
	}
}
