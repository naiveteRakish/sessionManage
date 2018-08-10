package com.biz.primus.base.session.filter;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
@FunctionalInterface
public interface RquestVerifyFilter {
	/**
	 * 验签成功返回true
	 * @param request
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public boolean filter(HttpServletRequest request) throws InvalidKeyException, NoSuchAlgorithmException;
}
