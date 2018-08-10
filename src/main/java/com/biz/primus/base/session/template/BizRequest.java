package com.biz.primus.base.session.template;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.util.StreamUtils;

import com.biz.primus.base.session.interceptor.SessionInterceptor;
import com.biz.primus.base.session.sessionmanage.ISessionManage;
import com.biz.primus.base.session.sessionmanage.impl.SessionManageImpl;
import com.biz.primus.base.session.util.SessionUtil;

public class BizRequest extends HttpServletRequestWrapper{

	private HttpSession session;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private ISessionManage sessionManage = SessionManageImpl.getInstance();
	private byte[] requestBody = null;
//	private ByteArrayInputStream byteArrayInputStream;
	
	
	public BizRequest(HttpServletRequest request,HttpServletResponse response) throws IOException {
		super(request);
		this.request=request;
		this.response=response;
		requestBody=StreamUtils.copyToByteArray(request.getInputStream());
		if(SessionInterceptor.xssfilter.filter(request)){
			requestBody= SessionUtil.XSSHtmlFilt(new String(requestBody,"UTF-8")).getBytes();
		}
	}


	@Override
	public ServletInputStream getInputStream() throws IOException{
		if(requestBody==null) {
			requestBody=new byte[0];
		}
	
		
		final ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(requestBody);
		return new ServletInputStream() {
			
			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}
			
			@Override
			public void setReadListener(ReadListener listener) {
				// do nothing
			}
			
			@Override
			public boolean isReady() {
				return false;
			}
			
			@Override
			public boolean isFinished() {
				return false;
			}
		};
	}
	@Override//对外提供读取流的方法
	public BufferedReader getReader() throws IOException{
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}
	

	
	
	
	public HttpSession getSession() {
		return super.getSession();
//		TODO 可以进行重写session
//		String sessionId = request.getHeader(SessionUtil.SESSION_ID);
//		return this.getSession(true);
	} 
//	public HttpSession getSession(boolean create) {
//		
//		return session;
//    }
//
//
//	public void login(String sessionID,String createTimestamp,String lastAccessedTimestamp,String maxInactiveInterval,String updateLastAccessedType){
//		sessionManage.login(sessionID, createTimestamp, lastAccessedTimestamp, maxInactiveInterval, updateLastAccessedType);
//	}
//	
//	@Override
//	public void logout(){
//		try {
//			super.logout();
//			sessionManage.logout();
//		} catch (ServletException e) {
//			sessionManage.logout();
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}
//	}
}
