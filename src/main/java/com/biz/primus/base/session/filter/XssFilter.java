package com.biz.primus.base.session.filter;

import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface XssFilter {
	/**
	 * 判断当前请求是否属于写数据请求
	 * @param url
	 * @param method
	 * @return 返回true 则进行xss替换字符策略 
	 */
	boolean filter(HttpServletRequest request);

}
