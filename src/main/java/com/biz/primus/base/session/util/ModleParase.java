package com.biz.primus.base.session.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Lists;

public class ModleParase {
	private static final Map<Class<?>, Method[]> methods = new ConcurrentHashMap<>();
	/**
	 * 
	 * @param  判断 object map list String
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object paraseObject(Object object) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		if(object == null) return null;
		Class<?> classez = object.getClass();
		if(object instanceof Map){
			Map<Object, Object> map = (Map)object;
			if(!map.isEmpty()){
				for (Map.Entry<Object, Object> entry : map.entrySet()) {
					if(entry.getValue() instanceof String)
					entry.setValue(paraseObject(entry.getValue()));
					paraseObject(entry.getValue());
				}
			}
		}else if (object instanceof List) {
			List<Object> list = (List)object;
			if(!list.isEmpty()){
				Object object2 = null;
				for (int i = 0; i < list.size(); i++) {
					object2 = list.get(i);
					if(object2 instanceof String)
					list.set(i, paraseObject(object2));
					paraseObject(object2);
				}
				
			}
			
		}else if(classez.isArray()){
			//是否是数组
			Object[] objects = (Object[]) object;
			Object object2 = null;
			for (int i = 0; i < objects.length; i++) {
				object2 = objects[i];
				if(object2 instanceof String)
				objects[i] = paraseObject(object2);
				paraseObject(object2);
			}
			
		}else if (object instanceof String){
//			return "hello";
			return SessionUtil.XSSHtmlFilt((String)object);
		}else if(!classez.isEnum() && !classez.isPrimitive() && !(object instanceof Byte || object instanceof Short || object instanceof Integer || object instanceof Long || object instanceof Float || object instanceof Double || object instanceof Character || object instanceof Boolean )){
			//复合类型 加入反射
			return paraseSimpleObject(object,classez);
		}
		return object;
	}
	private static Object paraseSimpleObject(Object object,Class<?> classes) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		Method[] methdos = null;
		if(!methods.containsKey(classes)){
			methdos =  classes.getMethods();
			methods.put(classes, methdos);
		}else{
			methdos = methods.get(classes);
		}
		if(methdos==null||methdos.length==0){
			throw new RuntimeException("object lack  'get' 'set' method. ");
		}
		String methodName = null;
		Method setMethod = null;
		Object methodValue = null;
		for (Method method : methdos) {
			methodName = method.getName();
			if(methodName.contains("get")){
				 methodValue = method.invoke(object, null);
				 if(methodValue!=null){
					 try {
						 setMethod = classes.getMethod(methodName.replaceAll("get", "set"),methodValue instanceof Map ?Map.class : methodValue instanceof List? List.class: methodValue.getClass().isArray()? methodValue.getClass():methodValue.getClass());
					 } catch (NoSuchMethodException e) {
						 break;
					 }
					 methodValue = paraseObject(methodValue);
					 setMethod.invoke(object, methodValue);
				 }
			}
		}
		
		//判断是否具有父类
		Class<?> superClass = classes.getSuperclass();
		if(!superClass.isInterface() && !superClass.equals(Object.class)){
			paraseSimpleObject(object, superClass);
		}
		return object;
	}
		
}
