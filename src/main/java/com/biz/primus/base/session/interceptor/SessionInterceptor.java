package com.biz.primus.base.session.interceptor;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codelogger.plugin.log.util.WebUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.biz.primus.base.session.exception.InvalidCsrfTokenException;
import com.biz.primus.base.session.exception.NotFindRequiredFieldException;
import com.biz.primus.base.session.exception.NotLoginException;
import com.biz.primus.base.session.exception.VerifyFailException;
import com.biz.primus.base.session.filter.CsrfTokenFilter;
import com.biz.primus.base.session.filter.PublicResourceFilter;
import com.biz.primus.base.session.filter.RquestVerifyFilter;
import com.biz.primus.base.session.filter.XssFilter;
import com.biz.primus.base.session.sessionmanage.ISessionManage;
import com.biz.primus.base.session.util.JsonValidator;
import com.biz.primus.base.session.util.CipherHelper;
import com.biz.primus.base.session.util.SessionUtil;
import com.biz.primus.common.exception.BizSilentException;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.UserAgent;

/**
 * 请求拦截
 * @author 胡君琦
 *
 */
public class SessionInterceptor implements HandlerInterceptor {
	private ISessionManage sessionManage;
	@SuppressWarnings("unused")
	private SessionUtil sessionUtil = new SessionUtil();
	private Set<String> urlSet = new HashSet<>();
	
	private CsrfTokenFilter csrfTokenFilter = new CsrfTokenFilter() {
		
		@Override
		public boolean filter(HttpServletRequest request) {
			if(request.getMethod().equals("POST")){
				return true;
			}
			return false;
		}
	};
	private RquestVerifyFilter rquestVerifyFilter = new RquestVerifyFilter() {
		
		@Override
		public boolean filter(HttpServletRequest request) throws InvalidKeyException, NoSuchAlgorithmException {
			String json = null;
			Map<String, String[]> parameterMap = request.getParameterMap();
			Map<String, String> map = new HashMap<>();
			
			for (Map.Entry<String, String[]> iterable_element : parameterMap.entrySet()) {
				map.put(iterable_element.getKey(), iterable_element.getValue()[0]);
			}
			
			
			String transfer = request.getHeader("Transfer-Encoding");
			String contentType = request.getHeader("Content-Type");
			if(!request.getMethod().equals("GET")){
			//对文件传输做参数处理
				if(SessionUtil.FILE_VERIFY_SWITCH){
					Map<String,String> fileNames = SessionUtil.getRequestFileNames(request);
					if(contentType!= null && (contentType.equals("application/javascript")||contentType.equals("application/xml")||contentType.equals("text/html")||contentType.equals("text/xml"))){
						throw new BizSilentException(SessionUtil.NOT_SUPPORT_OPERATE, "不支持的传输类型>>>"+contentType);
					}
					
					if(fileNames!=null && !fileNames.isEmpty()){
						map.putAll(fileNames);
					}										
				}
			if((transfer == null || transfer.indexOf("chunked")==-1) && contentType!=null && !contentType.contains("multipart/form-data") && !contentType.contains("application/x-www-form-urlencoded")/** && (contentType.equals("application/json") || contentType.equals("text/plain") || contentType.equals("application/javascript") || contentType.equals("application/xml") || contentType.equals("text/html")||contentType.equals("text/xml"))**/){
			 	//兼容 RAW/JSON   body体json数据
				try {
//					json = SessionUtil.getRequestBodyParam(request);
					json = WebUtil.getBody(request, SessionUtil.CHARSET_NAME);
				} catch (SecurityException | IllegalArgumentException | IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			else{
				//兼容RAW/text body体json数据
				json = parseMapParams(map);
			}
		}
		
			//能获取到的类型 get  Content-Type:multipart/form-data; boundary=    能获取到混合参数=getParameterMap  但是无法获取到fileName参数
			//Content-Type:application/x-www-form-urlencoded 能按照get方式处理
			//raw text类型 Content-Type:text/plain 只能获取到url上的参数 body体无法获取
			//Content-Type:application/json 只能获取到url上的参数 body体无法获取
			//raw text类型 Content-Type:text/plain && Transfer-Encoding:chunked 只能获取到url头信息
			
//			System.out.println("Content-Type:"+request.getHeader("Content-Type"));
//			System.out.println("Transfer-Encoding:"+request.getHeader("Transfer-Encoding"));
//			
//		
//			System.out.println();
//			
//			System.out.println("参数 :<<<<<<<<<<<<<<");
			
			for (Map.Entry<String,String> iterable_element : map.entrySet()) {
				System.out.println(iterable_element.getKey()+"="+iterable_element.getValue());
			}
			System.out.println(Thread.currentThread().getName()+"bodyData="+json);
//			System.out.println(">>>>>>>>>>>>>");
			//携带参数才需要签名
			if((map==null || map.isEmpty()) && json == null) return true;
			
			
			//没有进行签名
			if(Strings.isNullOrEmpty(request.getHeader(SessionUtil.SIGN))){
				throw new NotFindRequiredFieldException(SessionUtil.MISSING_FIELD, "header未携带'"+SessionUtil.SIGN+"'标识");
			}
			String content = getContent(map,json,request);
			String sign = getSign(request);
			//没有携带参数 无需验签
			if(content == null) return true;
//			System.out.println("content="+content);
//			System.out.println("SIGN="+sign);
//			return true;
			//验签
			return SessionUtil.verify(content,sign,request);
//			return RsaCipherHelper.verify(content, sign,request.getSession(false).getAttribute(SessionUtil.PUBLIC_KEY).toString());
			
		}
		private String parseMapParams(Map<String, String> map) {
			for (Map.Entry<String, String> iterable_element : map.entrySet()) {
				if(Strings.isNullOrEmpty(iterable_element.getValue())){
					if(new JsonValidator().validate(iterable_element.getKey())){
						map.remove(iterable_element.getKey());
						return iterable_element.getKey();
					}
				}
			}
			return null;
		}
		/**
		 * 获取签名
		 * @param request
		 * @return
		 */
		private String getSign(HttpServletRequest request) {
			String string = request.getHeader(SessionUtil.SIGN);
			if(string.indexOf(" ")!= -1){
				string=string.replaceAll(" ", "+");
			}
			return string;
		}

		
		//按照字典排序 ，并按name=value&name=value 格式拼接 返回 
		private String getContent(Map<String, String> map,String json,HttpServletRequest  request) {
			String content = null;
			if(map!=null){
				List<Map.Entry<String, String>> infoIds = new ArrayList<Map.Entry<String, String>>(map.entrySet());
				// 对HashMap中的key 进行排序
				Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {
					public int compare(Map.Entry<String, String> o1,
							Map.Entry<String, String> o2) {
						return (o1.getKey()).toString().compareTo(o2.getKey().toString());
					}
				});
				
				// 对HashMap中的key 进行排序后  显示排序结果
				StringBuilder sBuilder = new StringBuilder();
				String key = null; 
				for (int i = 0; i < infoIds.size(); i++) {
					key = infoIds.get(i).getKey();
					if(!key.equals(SessionUtil.SIGN))
					sBuilder.append(key+"="+map.get(key)+"&");
				}
				if(json!=null){
					sBuilder.append(json);
					content = sBuilder.toString();
				}else{
					content = sBuilder.substring(0,sBuilder.length()-1);
				}
			}else if(json!=null){
				content = json;
			}
			//如果是手机端签名需要先排序再转换为大写， 然后根据规则 : [A-->Z	B-->C	T-->I	P-->Q	U-->R	M-->E] 进行替换，然后进行签名 
			if(SessionUtil.APP_REPLACE_SWITCH){
				boolean switchV = false;
				Browser browser = UserAgent.parseUserAgentString(request.getHeader("User-Agent")).getBrowser();
				switchV  = java.util.Objects.equals(browser.getName(), "Unknown");
				if(switchV){
					//1.转换大写
					
					content = content.toUpperCase();
					char[] cs = content.toCharArray();
					char c ;
//					A-->Z
//					B-->C
//					T-->I
//					P-->Q
//					U-->R
//					M-->E
					
					for (int i = 0; i < cs.length; i++) {
						c=cs[i];
						switch (c) {
						case 'A':
							cs[i] = 'Z';
							break;
						case 'B':
							cs[i] = 'C';
							break;
						case 'T':
							cs[i] = 'I';
							break;
						case 'P':
							cs[i] = 'Q';
							break;
						case 'U':
							cs[i] = 'R';
							break;
						case 'M':
							cs[i] = 'E';
							break;
						default:
							break;
						}
					}
					content = new String(cs);
				}
			}
			
			
			return content;
		}
	};
	public static XssFilter xssfilter = new XssFilter() {
		
		@Override
		public boolean filter(HttpServletRequest request) {
//			if(request.getRequestURL().indexOf("xss")!=-1){
//				return true;
//			}
			return false;
		}
	};
	private PublicResourceFilter publicResourceFilter = new PublicResourceFilter() {
		private final String  ignoreUrls = "/|/+.*(html)$|/+heartbeat.*|/+webjars.*|/+swagger.*|/+v\\d/api-docs.*|/+css.*|/+js.*|/+favicon.ico";
		@Override
		public boolean filter(HttpServletRequest request) {
			Pattern p=Pattern.compile(ignoreUrls);  
			Matcher m=p.matcher(request.getRequestURI()); 
			if(m.matches()) return true;
			return  urlSet.contains(request.getRequestURI());
		}
	};
	
	
	
    public SessionInterceptor() {
		super();
	}

	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
    	System.out.println(Thread.currentThread().getName()+"请求进入");
    	//    	application/json app发送格式	
    	
		//1.获取sessionId
		String sessionId = request.getHeader(SessionUtil.SESSION_ID);
		
		if(sessionId==null){
			String url = request.getRequestURL().toString();
			sessionId = SessionUtil.getUrlSessionId(url);
			if(sessionId == null){
				Cookie[]  cookies = request.getCookies();
				if(cookies!=null){
					for (Cookie cokie : cookies) {
						if(Objects.equal(cokie.getName(), SessionUtil.SESSION_ID)){
							sessionId = cokie.getValue();
							break;
						}
					}
				}
				
			}
		}
		
		Map<String,String>  sessionParams = null;
		//未登录 也必须携带默认标识值
		if(sessionId==null){
			//请求头必须携带SessionUtil.SESSION_ID ，如果没有登录携带默认标识
			throw new NotLoginException(SessionUtil.MISSING_FIELD,"header未携带'"+SessionUtil.SESSION_ID+"'标识");
		}
		if(!SessionUtil.DEFAULT_SESSIONID_VALUE.equals(sessionId)){
			sessionParams = sessionManage.getSessionParameter(sessionId);
		}
	
		//2.session是否存在
		if(sessionParams == null || sessionParams.isEmpty()){
			//3.临时用户 ，判断是否公共资源
			if(!publicResourceFilter.filter(request)){
				//公共资源: 否
				throw new NotLoginException(SessionUtil.UNVERIFIED, "用户未认证");
			}
			//公共资源: 是
//			根据条件生成令牌
			return true ; 
		}
		
		//4.数据同步到Session 并创建本地session
		sessionManage.createSession(request,sessionParams);
		
		if(!publicResourceFilter.filter(request)){
			//不是公共资源需要进行签名
			//5.验签
			boolean switchV = false;
			Browser browser = UserAgent.parseUserAgentString(request.getHeader("User-Agent")).getBrowser();
			switchV  = java.util.Objects.equals(browser.getName(), "Unknown");
			if(switchV&&!rquestVerifyFilter.filter(request)){
				 //验签失败
				 throw new VerifyFailException(SessionUtil.VERIFY_FAIL,"验签失败");
			}
			
			//6.令牌校验  
			if(!sessionManage.tokenVerify(request,(String)sessionParams.get(SessionUtil.CSRF_TOKEN),csrfTokenFilter)){
				throw new  InvalidCsrfTokenException(SessionUtil.INVALID_CSRF, "CSRF令牌无效");
			}
		}
		
		//7.XSS攻击防御
		sessionManage.XSS(request,xssfilter);
		//根据条件生成令牌
		sessionManage.generateToken(request,response);
		
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o,
            ModelAndView modelAndView) throws Exception {
//    	System.out.println(Thread.currentThread().getName()+"视图渲染之前");
//    	sessionManage.generateToken(httpServletRequest,httpServletResponse);

    }


  

	@Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            Object o, Exception e) throws Exception {
//		System.out.println(Thread.currentThread().getName()+"请求结束");
		//更新session
		sessionManage.invalidate(httpServletRequest);

    }


	public void setSessionManage(ISessionManage sessionManage) {
		this.sessionManage = sessionManage;
	}

	public void setXssfilter(XssFilter xssfilter) {
		this.xssfilter = xssfilter;
	}

	public void setPublicResourceFilter(PublicResourceFilter publicResourceFilter) {
		this.publicResourceFilter = publicResourceFilter;
	}

	public void setRquestVerifyFilter(RquestVerifyFilter rquestVerifyFilter) {
		this.rquestVerifyFilter = rquestVerifyFilter;
	}
	
	
	public void setCsrfTokenFilter(CsrfTokenFilter csrfTokenFilter) {
		this.csrfTokenFilter = csrfTokenFilter;
	}

	public Set<String> getUrlSet() {
		return urlSet;
	}

	public void setUrlSet(Set<String> urlSet) {
		this.urlSet = urlSet;
	}
	
	
	
}