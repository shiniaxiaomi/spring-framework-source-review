package com.lyj.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yingjie.lu
 * @version 1.0
 * @date 2020/1/6 3:57 下午
 */

@Component
public class UserBean {

	@Autowired
	IndexBean indexBean;

}
