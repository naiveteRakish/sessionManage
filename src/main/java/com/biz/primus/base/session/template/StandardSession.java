package com.biz.primus.base.session.template;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class StandardSession implements HttpSession{
	
	private String id;
	private boolean isNew;
	private long creationTime;
	private long lastAccessedTime;
	private int maxInactiveInterval;
	private HttpSession session;
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	
	
	public StandardSession() {
		super();
	}

	
	


	public StandardSession(String id, boolean isNew, long creationTime, long lastAccessedTime, int maxInactiveInterval,
			HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		super();
		this.id = id;
		this.isNew = isNew;
		this.creationTime = creationTime;
		this.lastAccessedTime = lastAccessedTime;
		this.maxInactiveInterval = maxInactiveInterval;
		this.session = session;
		this.setRequest(request);
		this.setResponse(response);
	}





	public StandardSession(String id, boolean isNew, long creationTime, long lastAccessedTime, int maxInactiveInterval,
			HttpSession session) {
		super();
		this.id = id;
		this.isNew = isNew;
		this.creationTime = creationTime;
		this.lastAccessedTime = lastAccessedTime;
		this.maxInactiveInterval = maxInactiveInterval;
		this.session = session;
	}



	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	@Override
	@Deprecated
	public void invalidate() {
		//TODO 是否需要具有立刻销毁功能？  好好思考
//		super.invalidate();
//		throw new RuntimeErrorException(e)
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		maxInactiveInterval = interval;
	}

	@Override
	public Object getAttribute(String name) {
		return session.getAttribute(name);
	}

	@Override
	@Deprecated
	public Object getValue(String name) {
		return session.getAttribute(name);
	}

	@Override
	@Deprecated
	public Enumeration<String> getAttributeNames() {
		return session.getAttributeNames();
	}

	@Override
	public void setAttribute(String name, Object value) {
		session.setAttribute(name, value);
	}

	

	@Override
	public void removeAttribute(String name) {
		session.removeAttribute(name);
	}




	@Override
	public ServletContext getServletContext() {
		return session.getServletContext();
	}




	@Override
	@Deprecated
	public HttpSessionContext getSessionContext() {
		return session.getSessionContext();
	}




	@Override
	@Deprecated
	public String[] getValueNames() {
		return session.getValueNames();
	}



	@Override
	@Deprecated
	public void putValue(String name, Object value) {
		session.setAttribute(name, value);
	}




	@Override
	public void removeValue(String name) {
		session.removeAttribute(name);
	}

	public HttpSession getHttpSession(){
		return session;
	}





	public HttpServletResponse getResponse() {
		return response;
	}





	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}





	public HttpServletRequest getRequest() {
		return request;
	}





	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	

}
