package com.biz.primus.base.session.servletfilter;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.biz.primus.base.session.template.BizRequest;

/**
 * @author yellowcong
 * 创建日期:2018/01/31
 * 编码拦截器
 */
public class BizReqFilter implements Filter {


    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse resposne, FilterChain filter)
            throws IOException, ServletException {
    	 HttpServletRequest req = (HttpServletRequest) request;
		 HttpServletResponse res=(HttpServletResponse)resposne;
		 
		//V2 内容
		  req = new BizRequest(req, res);
//        System.out.println("sessionId\t"+req.getSession().getId());
//        System.out.println("用户ip\t"+req.getRemoteAddr());
//        System.out.println("用户名称\t"+req.getRemoteUser());
//        System.out.println("请求编码\t"+req.getCharacterEncoding());
//        System.out.println("请求访问地址\t"+req.getRequestURI());
//        Enumeration<String>  e = req.getHeaderNames();
//        while (e.hasMoreElements()) {
//			String string = (String) e.nextElement();
//			System.out.println(string+"="+req.getHeader(string));
//		}
        System.out.println("Authorization="+req.getHeader("Authorization"));
        filter.doFilter(req, resposne);
        
        
        
        
    }

    @Override
    public void init(FilterConfig filter) throws ServletException {


    }
}