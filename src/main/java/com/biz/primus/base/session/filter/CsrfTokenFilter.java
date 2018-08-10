package com.biz.primus.base.session.filter;

import javax.servlet.http.HttpServletRequest;
@FunctionalInterface
public interface CsrfTokenFilter {
	/**
	 * 判断哪些请求需要Csrf令牌校验
	 * @param request
	 * @return
	 */
	public boolean filter(HttpServletRequest request);

}
