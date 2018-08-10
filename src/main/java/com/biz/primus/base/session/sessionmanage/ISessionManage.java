package com.biz.primus.base.session.sessionmanage;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.biz.primus.base.session.filter.CsrfTokenFilter;
import com.biz.primus.base.session.filter.XssFilter;

public interface ISessionManage {
	
	/**
	 * 获取session参数数据 返回值只能用于读  禁止对map进行写操作
	 * @param sessionId
	 * @return
	 */
	Map<String ,String> getSessionParameter(String sessionId);
	/**
	 * 创建session 并将数据同步到requestsession中
	 * 注意: 后续使用session中的 id 创建时间 最后访问时间 最大有效时间 是否新建session 都统一通过getAttribute()
	 * 
	 * 后续再对会话管理进行升级 才可以像以前一样使用session
	 * @param request
	 * @param sessionParams 创建session所需数据
	 * long SessionUtil.CREATION_TIME;  默认当前时间戳
	 * String	SessionUtil.SESSION_ID;		注意:分布式中保证唯一  （必填）
	 * long	SessionUtil.LAST_ACCESSED_TIME;   默认当前时间戳
	 * boolean	SessionUtil.IS_NEW;			 这个参数判断session是否新建 默认true 
	 * int SessionUtil.MAX_INACTIVE_INTERVAL; 默认30分钟 可以通过配置指定， 也可以显式传入 优先级从低到高  单位秒
	 * long SessionUtil.UPDATE_LAST_ACCESSED_TYPE;(默认为0 )  SessionUtil.UPDATE_LAST_ACCESSED_TYPE <=0:无间隔，每次访问都更新一次session  其它数值都代表间隔x秒 之后用户第一次访问更新
	 */
	void createSession(HttpServletRequest request, Map<String, String> sessionParams);
	/**
	 * 创建session 并将数据同步到requestsession中
	 * 注意: 后续使用session中的 id 创建时间 最后访问时间 最大有效时间 是否新建session 都统一通过getAttribute()   选填都可以传入null
	 * 
	 * 后续再对会话管理进行升级 才可以像以前一样使用session  
	 * @param request
	 * @param sessionParams 创建session所需数据
	 * createTimestamp;  默认当前时间戳
	 * sessionId;		注意:分布式中保证唯一  （必填）
	 * lastAccessedTimestamp; 最后一次访问时间，  默认当前时间戳
	 * maxInactiveInterval; 默认30分钟 可以通过配置指定， 也可以显式传入 优先级从低到高  单位秒
	 * updateLastAccessedType (默认为0 )  updateLastAccessedType <=0:无间隔，每次访问都更新一次session  其它数值都代表间隔x秒 之后用户第一次访问更新
	 * params : 需要存储在会话attribute中的数据
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	void login(String sessionId,String createTimestamp,String lastAccessedTimestamp,String maxInactiveInterval,String updateLastAccessedType,Map<String,String> params) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException; 
	
	/**
	 * 退出登录 session销毁， 立刻清空request中的session和redis中的session
	 * @param request
	 */
	void logout();
	
	boolean tokenVerify(HttpServletRequest request, String object, CsrfTokenFilter csrfTokenFilter);
	
	void generateToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
	/**
	 * 请求响应时候进行更新session
	 * @param httpServletRequest
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	void saveSession(HttpServletRequest httpServletRequest) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException;
	
	/**
	 * XSS攻击防御 策略替换敏感字符
	 * @param request
	 * @param xssfilter 
	 */
	void XSS(HttpServletRequest request, XssFilter xssfilter);
	
	/**
	 * 销毁session
	 * @param httpServletRequest
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	void invalidate(HttpServletRequest httpServletRequest) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException;
	
	/**
	 * 判断该会话sessionId是否有效,并默认生命周期延长60秒。
	 * @return
	 */
	boolean isValid(String sessionId);
}
