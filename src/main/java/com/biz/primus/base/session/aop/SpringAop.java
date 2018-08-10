package com.biz.primus.base.session.aop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.biz.primus.base.session.util.ModleParase;
import com.biz.primus.base.session.util.SessionUtil;

public class SpringAop {
	
	private final static ThreadLocal<Boolean> local = new ThreadLocal<>();
	
//	@SuppressWarnings("null")
//	public static void before(ProceedingJoinPoint poin){
//		 Boolean isbody = false;
//		 Class<?> class1 = poin.getTarget().getClass();
//		 isbody = (Boolean) isBodyMap.get(class1);
//		 if(isbody==null){
//	        if(!(isbody = class1.isAnnotationPresent(org.springframework.web.bind.annotation.RestController.class))){
//	        	 Method[] methods = class1.getMethods();
//	             for (Method method : methods) {
//	     			if(method.getName().equals(poin.getSignature().getName())){
//	     				isbody = method.isAnnotationPresent(RequestBody.class);
//	     				isBodyMap.put(class1.hashCode()+method.getName(),isbody);
//	     				break;
//	     			}
//	     		}
//	        }else{
//	        	//该class 所有响应写入body
//	        	isBodyMap.put(class1, isbody);
//	        }
//		 }
//	        local.set(isbody);
//	}
//	public static Object after(Object object) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
//		  
//			if(local.get()){
//			  object = ModleParase.paraseObject(object);
//			  local.set(false);
//			}
//			  return object;
//	}
//	
	public static Object dataHandle(ProceedingJoinPoint poin,Object object) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException{
		//返回data
		Class<?> class1 = poin.getTarget().getClass();
	        if(class1.isAnnotationPresent(org.springframework.web.bind.annotation.RestController.class)){
	        	//body
	        	return ModleParase.paraseObject(object);
	        }
	        Method[] methods = class1.getMethods();
            for (Method method : methods) {
    			if(method.getName().equals(poin.getSignature().getName())){
    				 if(method.isAnnotationPresent(RequestBody.class)){
    					 //body
    					 return ModleParase.paraseObject(object);
    				 }
    				 break;
    			}
    		}
            
            // 返回view
            if(object instanceof ModelAndView){
            	//解析过滤
            	ModelAndView modelAndView = (ModelAndView)object;
            	for (Map.Entry<String, Object> entry : modelAndView.getModelMap().entrySet()) {
					entry.setValue(ModleParase.paraseObject(entry.getValue()));
				}
            }
            ServletRequestAttributes servletRequestAttributes =  ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes());
            if(servletRequestAttributes == null) return object;
            HttpServletRequest request = servletRequestAttributes.getRequest();
            Enumeration<String>  enumeration = request.getAttributeNames();
            //过滤request域对象
            while (enumeration.hasMoreElements()) {
				String string = (String) enumeration.nextElement();
				Object value  = request.getAttribute(string);
				if(value instanceof String){
					request.setAttribute(string, SessionUtil.XSSHtmlFilt((String) value));
				}else{
					request.setAttribute(string, ModleParase.paraseObject(value));
				}
			}
            //过滤session域对象
            HttpSession session = request.getSession(false);
            if(session!=null){
            	Map<String,String> map = SessionUtil.getSessionMap(session);
            	for (Map.Entry<String, String> entry : map.entrySet()) {
            		entry.setValue((String)ModleParase.paraseObject(object));
				}
            }
		return object;
	}
}