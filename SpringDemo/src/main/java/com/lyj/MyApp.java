package com.lyj;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yingjie.lu
 * @version 1.0
 * @date 2019/12/25 4:06 下午
 */

@ComponentScan("com.lyj.test")
public class MyApp {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MyApp.class);
		Object testBean = context.getBean("testBean");
		System.out.println(testBean);

	}

}
