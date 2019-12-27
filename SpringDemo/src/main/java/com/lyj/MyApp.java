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

		//指定启动类
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MyApp.class);
		Object testBean = context.getBean("testBean");
		System.out.println(testBean);

		//直接指定扫描哪个包
//		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.lyj.test");
//		Object testBean = context.getBean("testBean");
//		System.out.println(testBean);
	}

	public MyApp() {
		System.out.println("构造函数:MyApp");
	}
}
