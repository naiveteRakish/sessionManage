package com.biz.primus.base.session.filter;

import javax.servlet.http.HttpServletRequest;
@FunctionalInterface
public interface PublicResourceFilter {
	/**
	 * 判断当前请求是否属于公共资源
	 * @param url
	 * @param method
	 * @return 属于 返回true
	 */
	boolean filter(HttpServletRequest request);
}
