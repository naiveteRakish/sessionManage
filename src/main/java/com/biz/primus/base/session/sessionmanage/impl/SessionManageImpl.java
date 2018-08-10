package com.biz.primus.base.session.sessionmanage.impl;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.session.StandardSessionFacade;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.biz.primus.base.session.cache.ICacheAccessto;
import com.biz.primus.base.session.cache.impl.CacheAccesstoImpl;
import com.biz.primus.base.session.filter.CsrfTokenFilter;
import com.biz.primus.base.session.filter.XssFilter;
import com.biz.primus.base.session.sessionmanage.ISessionManage;
import com.biz.primus.base.session.util.CipherHelper;
import com.biz.primus.base.session.util.SessionUtil;
import com.biz.primus.common.exception.BizSilentException;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

import redis.clients.jedis.JedisPoolConfig;
import sun.misc.BASE64Encoder;

@SuppressWarnings("deprecation")
public class SessionManageImpl implements ISessionManage{
	/**
	 * 时间更新回调
	 * @author 胡君琦
	 *
	 */
	@FunctionalInterface
	interface Callback{
		void run ();
	}
	private final ThreadLocal<Set<String>> params = new ThreadLocal<>();
	private static ISessionManage sessionManageImpl;
	/**
	 * session有效时间 单位秒 默认30分钟
	 */
	private int maxInactiveInterval = 1800;
	/**
	 * 缓存访问api
	 */
	private ICacheAccessto cacheAccessto;
	/**
	 * 是否禁用CRSF令牌验证 令牌结构 : sessionId + . + 时间戳  最后MD5摘要
	 */
	private boolean isDisable = false;
	
	/**
	 * 是否让需要令牌验证的请求具有防重复提交功能
	 */
	private boolean unrepeatable = true;
	/**
	 * 令牌失效时间默认30分钟
	 */
	private long csrf_disableTime = 1800000L;
	
	private Callback callback;
	

	public void setDisable(boolean isDisable) {
		this.isDisable = isDisable;
	}
	/**
	 * 禁用可重复提交 默认开启
	 * @param unrepeatable
	 */
	public void setUnrepeatable(boolean unrepeatable) {
		this.unrepeatable = unrepeatable;
	}
	/**
	 * 会话时间重置回调
	 * @param callback
	 */
	public void setCallback(Callback callback) {
		this.callback = callback;
	}
	
	public void setCacheAccessto(ICacheAccessto cacheAccessto) {
		this.cacheAccessto = cacheAccessto;
	}
	/**
	 * 单位秒  session有效时长
	 * @param maxInactiveInterval
	 */
	public void setMaxInactiveInterval(int maxInactiveInterval){
		this.maxInactiveInterval = maxInactiveInterval;
	}
	/**
	 * csrf令牌有效时长默认30分钟 单位毫秒
	 * @param csrf_disableTime
	 */
	public void setCsrf_disableTime(long csrf_disableTime) {
		this.csrf_disableTime = csrf_disableTime;
	}
	public static ISessionManage getInstance(){
		return sessionManageImpl;
	}
	@Override
	public Map<String, String> getSessionParameter(String sessionId) {
		if(sessionId == null) return null;
		Map<String, String> map = cacheAccessto.getSession(sessionId);
		params.set(map.keySet());
		return map;
	}


	public SessionManageImpl() {
		super();
	}
	

	
	@Override
	public void createSession(HttpServletRequest request, Map<String, String> sessionParams){
		if(sessionParams.containsKey(SessionUtil.SESSION_ID)){
			if(!sessionParams.containsKey(SessionUtil.CREATION_TIME)){
				sessionParams.put(SessionUtil.CREATION_TIME, String.valueOf(System.currentTimeMillis()));
			}
			if(!sessionParams.containsKey(SessionUtil.LAST_ACCESSED_TIME)){
				sessionParams.put(SessionUtil.LAST_ACCESSED_TIME, String.valueOf(System.currentTimeMillis()));
			}
			if(!sessionParams.containsKey(SessionUtil.MAX_INACTIVE_INTERVAL)){
				sessionParams.put(SessionUtil.MAX_INACTIVE_INTERVAL, String.valueOf(maxInactiveInterval));
			}
//			if(!sessionParams.containsKey(SessionUtil.IS_NEW)){
//				sessionParams.put(SessionUtil.IS_NEW, true);
//			}
			if(!sessionParams.containsKey(SessionUtil.UPDATE_LAST_ACCESSED_TYPE)){
				sessionParams.put(SessionUtil.UPDATE_LAST_ACCESSED_TYPE, "0");
			}
			if(Long.valueOf(sessionParams.get(SessionUtil.UPDATE_LAST_ACCESSED_TYPE).toString())<=0){
				HttpSession session = request.getSession();
				if(!session.isNew()){
					 //由于登录会创建会话 而销毁会话是在拦截器结束的时候， 但是登录接口通常在拦截器之外，就造成登录的本地会话是存活在本地的
					 //由于是分布式的，可能会造成某一次访问到该站点，却意外得到旧的会话信息，造成数据不一致。
					 session.invalidate();
					 session = request.getSession();
				}
				for (Map.Entry<String, String> itme : sessionParams.entrySet()) {
					session.setAttribute(itme.getKey(), itme.getValue());
				}
			}else{
				StringBuilder sb = new StringBuilder();
				//新建session
				HttpSession session = request.getSession();
				for (Map.Entry<String, String> itme : sessionParams.entrySet()) {
					session.setAttribute(itme.getKey(), itme.getValue());
					sb.append(itme.getKey()+itme.getValue());
				}
				session.setAttribute(SessionUtil.PARAMS_MD5, SessionUtil.MD5(sb.toString()));
			}
		}else{
			throw new BizSilentException("缺少'"+SessionUtil.SESSION_ID+"'属性");
		}
//		Field field = null;
//		try {
//			field = session.getClass().getDeclaredField("session");
//		} catch (NoSuchFieldException | SecurityException e) {
//			//  Auto-generated catch block
//			e.printStackTrace();
//		}
//		Field[] field1 = field.getType().getDeclaredFields();
//		for (Field field2 : field1) {
//			System.out.println(field2.getName());
//			System.out.println(field2.getType());
//			System.out.println(field2.getModifiers());
//			System.out.println(field2.getDeclaringClass());
//			System.out.println("-----------------");
//		}
	}
	/**
	 * token失效校验 
	 * 1.判断是否需要令牌校验
	 * 2.判断前台令牌是否可信（将后台令牌MD5进行比对）
	 * 3.判断令牌是否失效
	 * 4.判断令牌是否超时
	 */
	@Override
	public boolean tokenVerify(HttpServletRequest request, String serToken, CsrfTokenFilter csrfTokenFilter) {
		if(!isDisable && csrfTokenFilter.filter(request)){
			//进行CRSF校验
			String cliTonken = request.getHeader(SessionUtil.CSRF_TOKEN);
			if(cliTonken != null && serToken != null && SessionUtil.MD5(serToken).equals(cliTonken)){
				Long oriTime = Long.valueOf(serToken.split(SessionUtil.CSRF_TOKEN_SEPARATOR)[1]);
				if(System.currentTimeMillis()-oriTime < csrf_disableTime){
				//使令牌验证的请求具有防重复提交功能
					if(unrepeatable){
						HttpSession  session = request.getSession(false);
						session.removeAttribute(SessionUtil.CSRF_TOKEN);
					}
				return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * 根据请求头，判断是否申请token令牌。
	 * 申请则生成token
	 * 没有生成则不生成token
	 * 生成则MD5摘要后存入响应头中
	 */
	@Override
	public void generateToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String t = httpServletRequest.getHeader(SessionUtil.WANT_TOKEN);
		if(t!= null && t.equals("1")){
			HttpSession session = httpServletRequest.getSession(false);
			if(session!=null){
			String token = session.getAttribute(SessionUtil.SESSION_ID)+SessionUtil.CSRF_TOKEN_SEPARATOR+System.currentTimeMillis();
			session.setAttribute(SessionUtil.CSRF_TOKEN, token);
			httpServletResponse.setHeader(SessionUtil.CSRF_TOKEN, SessionUtil.MD5(token));
			}
		}
	}


	



	@Override
	public void saveSession(HttpServletRequest httpServletRequest) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		HttpSession session = httpServletRequest.getSession(false);
		//判断是否存在会话
		if(session == null){
			System.out.println("session已销毁");
		}
		if(session!=null&&session.getAttribute(SessionUtil.SESSION_ID)!=null){
			long timeStamp = Long.valueOf(session.getAttribute(SessionUtil.UPDATE_LAST_ACCESSED_TYPE).toString());
//			session.setAttribute(SessionUtil.IS_NEW, false);
			if(timeStamp <= 0){
				save(session);
			}else{
				//按设定的时间间隔更新
				if((System.currentTimeMillis()-Long.valueOf(session.getAttribute(SessionUtil.LAST_ACCESSED_TIME).toString()))/1000D > Long.valueOf(session.getAttribute(SessionUtil.UPDATE_LAST_ACCESSED_TYPE).toString())){
					//执行更新
					session.removeAttribute(SessionUtil.PARAMS_MD5);
					save(session);
					
				}else{
					String sessionId = session.getAttribute(SessionUtil.SESSION_ID).toString();
					Map<String,String> map = new HashMap<String, String>();
					Enumeration<String> es= session.getAttributeNames();
					String paramsMd5 = session.getAttribute(SessionUtil.PARAMS_MD5).toString();
					session.removeAttribute(SessionUtil.PARAMS_MD5);
					StringBuilder sb = new StringBuilder();
					String key = null;
					String value = null;
					while(es.hasMoreElements()){
						key = es.nextElement();
						value = session.getAttribute(key).toString();
						map.put(key,value);
						sb.append(key+value);
					}
					if(!paramsMd5.equals(SessionUtil.MD5(sb.toString()))){
						
						paramRemoveUpdate(sessionId, map);
						//session参数改变 执行更新
						map.put(SessionUtil.LAST_ACCESSED_TIME, String.valueOf(System.currentTimeMillis()));
						cacheAccessto.save(sessionId, map);
					}
				}
			}
		}
	}
	
	/**
	 * 对于移除的参数的更新 因为redis putAll 散列表只有添加和update功能 缺少remove
	 * @param sessionId
	 * @param map
	 */
	private void paramRemoveUpdate(String sessionId, Map<String, String> map) {
		Set<String> params = this.params.get();
		if(params!=null){
			List<String> list = new ArrayList<>(params.size());
			this.params.remove();
			for (String string : params) {
				if(!map.containsKey(string)){
					list.add(string);
				}
			}
			if(!list.isEmpty()){
				//更新
				cacheAccessto.delete(sessionId, list.toArray(new String[list.size()]));
			}
		}
	}
	private void save(HttpSession session) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String sessionId = session.getAttribute(SessionUtil.SESSION_ID).toString();
		//每次更新
		Map<String,String> map = SessionUtil.getSessionMap(session);
		map.put(SessionUtil.LAST_ACCESSED_TIME, String.valueOf(System.currentTimeMillis()));
		//
		paramRemoveUpdate(sessionId, map);
		cacheAccessto.save(sessionId, map);
		if(!cacheAccessto.expire(session.getAttribute(SessionUtil.SESSION_ID).toString(), Long.valueOf(session.getAttribute(SessionUtil.MAX_INACTIVE_INTERVAL).toString()))){
			throw new BizSilentException("会话生命重置失败");
		}else{
			if(callback!=null){
				//生命重置更新回调
				callback.run();
			}
		}
	}



	
	@Override
	public void XSS(HttpServletRequest request, XssFilter xssfilter) {
		if(xssfilter.filter(request)){
			//处理parameterMap中的参数
			Map<String, String[]> map = request.getParameterMap();
			Field feild = null;
			try {
				feild = map.getClass().getDeclaredField("locked");
				feild.setAccessible(true);
				feild.set(map, false);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			
			Map<String, String[]> copyMap = new HashMap<>(map.size()+1,1);
			copyMap.putAll(map);
			
			String key = null;
			String[] values= null;
			for (Map.Entry<String, String[]> item : copyMap.entrySet()) {
				key = item.getKey();
				values = item.getValue();
				if(values!=null && values.length >0){
					for (int i = 0; i < values.length; i++) {
						values[i] = SessionUtil.XSSHtmlFilt(values[i]);
					}
				}
				map.put(key, values);
			}
			try {
				feild.set(map, true);
				feild.setAccessible(false);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	
	
	@SuppressWarnings({ "restriction" })
	@Override
	public void  login(String sessionID,String createTimestamp,String lastAccessedTimestamp,String maxInactiveInterval,String updateLastAccessedType,Map<String,String> params) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		
		Map<String, String> sessionParams = null;
		
		if(params != null){
			sessionParams = new HashMap<>(params.size()+6,1);
			sessionParams.putAll(params);
		}else{
			sessionParams = new HashMap<>(6,1);
		}
		
		if(Strings.isNullOrEmpty(sessionID)){
			throw new BizSilentException("缺少SESSIONID");
		}
		
		sessionParams.put(SessionUtil.SESSION_ID, sessionID);
		if(!Strings.isNullOrEmpty(createTimestamp)){
			sessionParams.put(SessionUtil.CREATION_TIME, createTimestamp);
		}
		if(!Strings.isNullOrEmpty(lastAccessedTimestamp)){
			sessionParams.put(SessionUtil.LAST_ACCESSED_TIME, lastAccessedTimestamp);
		}
		if(!Strings.isNullOrEmpty(maxInactiveInterval)){
			sessionParams.put(SessionUtil.MAX_INACTIVE_INTERVAL, maxInactiveInterval);
		}
		if(!Strings.isNullOrEmpty(updateLastAccessedType)){
			sessionParams.put(SessionUtil.UPDATE_LAST_ACCESSED_TYPE, updateLastAccessedType);
		}
		
		HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		
		Map<String, String> map= cacheAccessto.getSession(sessionID);
		if(map == null || map.isEmpty()){
			//服务器存储公钥
			KeyPair kPair= CipherHelper.KeyPairGenerator();
			sessionParams.put(SessionUtil.PUBLIC_KEY,  new BASE64Encoder().encode(kPair.getPublic().getEncoded()).replaceAll(System.getProperty("line.separator"),""));
			System.out.println("公钥:"+new BASE64Encoder().encode(kPair.getPublic().getEncoded()));
			createSession(request, sessionParams);
			saveSession(request);
			//返回客户端私钥
			sessionParams.put(SessionUtil.PRIVATE_KEY, new BASE64Encoder().encode(kPair.getPrivate().getEncoded()).replaceAll(System.getProperty("line.separator"),""));
			response.addHeader(SessionUtil.PRIVATE_KEY, new BASE64Encoder().encode(kPair.getPrivate().getEncoded()).replaceAll(System.getProperty("line.separator"),""));
			System.out.println("私钥:"+new BASE64Encoder().encode(kPair.getPrivate().getEncoded()));
		
		}else{
			Optional.ofNullable(params).ifPresent(consumer->{
				map.putAll(consumer);
			});
			cacheAccessto.save(sessionID, map);
			//存在则用旧的session , 但会重置生命周期
			cacheAccessto.expire(sessionID,Long.valueOf(map.get(SessionUtil.MAX_INACTIVE_INTERVAL)));
		}
		//写入cookie
		Optional.ofNullable(getCookie(sessionID,map)).ifPresent(consumer->response.addCookie(consumer));
		response.addHeader(SessionUtil.SESSION_ID, sessionID);
	}
	
	private Cookie getCookie(String sessionID, Map<String, String>  map) {
		Cookie cookie = new Cookie(SessionUtil.SESSION_ID, sessionID);
		cookie.setHttpOnly(true);
		cookie.setMaxAge(SessionUtil.COOKIE_MAXAGE);
		Optional.ofNullable(SessionUtil.DOMAIN_PATTERN).ifPresent(consumer->cookie.setDomain(consumer));
		return cookie;
	}
	@Override
	public void logout() {
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		HttpSession session = request.getSession(false);
		Object object = null;
		if(session!=null&& (object = session.getAttribute(SessionUtil.SESSION_ID))!=null){
			String sessionId = object.toString();
			if(!cacheAccessto.remove(sessionId)){
				throw new BizSilentException("会话销毁失败");
			}
			removeSeesionIdByCookie(request);
			
			
		}
		Optional.ofNullable(session).ifPresent(consumer->{
			consumer.invalidate();
		});
	}
	
	@Override
	public void invalidate(HttpServletRequest httpServletRequest) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		saveSession(httpServletRequest);
		HttpSession session =  httpServletRequest.getSession(false);
		if(session==null){
			System.out.println("session已销毁");
		}
		if(session!=null) 
			try {
			session.invalidate();
		} catch (Exception e) {
		}
	}
	
	
	@Override
	public boolean isValid(String sessionId) {
		return cacheAccessto.expire(sessionId,60l);
	}
	/**
	 * 从cookie删除会话id
	 * @param request
	 */
	private void removeSeesionIdByCookie(HttpServletRequest request) {
		HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
		Cookie[]  cookies = request.getCookies();
		if(cookies!=null){
			for (Cookie cokie : cookies) {
				if(Objects.equal(cokie.getName(), SessionUtil.SESSION_ID)){
					cokie.setValue(null);
					cokie.setMaxAge(0);
					response.addCookie(cokie);
					break;
				}
			}
		}
	}
	
}
