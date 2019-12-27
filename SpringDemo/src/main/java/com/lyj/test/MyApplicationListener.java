package com.lyj.test;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author yingjie.lu
 * @version 1.0
 * @date 2019/12/26 6:33 下午
 */
@Component
public class MyApplicationListener implements ApplicationListener<ApplicationEvent> {
	/**
	 * 在应用最后加载完成之后回调
	 * @param event the event to respond to
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		System.out.println("onApplicationEvent");
	}

	public MyApplicationListener() {
		System.out.println("构造方法:MyApplicationListener");
	}
}
