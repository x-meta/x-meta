package org.xmeta.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xmeta.Action;
import org.xmeta.ActionContext;
import org.xmeta.ActionException;

public class ActionAnnotationHelper {
	/** 带有注解的字段列表 */
	private Field[] fields;
	private ActionField[] fieldAnnotations;
	
	/** 带有注解的参数列表 */
	private String[] params;
	
	/** 创建类的方法 */
	private Method creator;
	
	private Class<?> actionClass;
	
	private Method actionMethod;

	private boolean isActionClass = false;
	
	/**
	 * 分析方法和方法所述的类有没有定义动作相关的注解，如果有那么返回Helper，如果没有那么返回null。
	 * @param method
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public static ActionAnnotationHelper parse(Class<?> cls, Method method) throws NoSuchMethodException, SecurityException {
		ActionAnnotationHelper helper = new ActionAnnotationHelper();
		helper.actionMethod = method;
		
		//查找方法的注解
		ActionParams actionParams = method.getAnnotation(ActionParams.class);		
		if(actionParams != null) {
			helper.params = actionParams.names().split("[,]");
			for(int i=0; i<helper.params.length; i++) {
				if(helper.params[i] != null) {
					helper.params[i] = helper.params[i].trim();
				}
			}
		}
		
		//查找类的注解
		helper.actionClass = cls;
		ActionClass actionClass = cls.getAnnotation(ActionClass.class);
		//静态方法不需要创建实例化的对象
		if(actionClass != null && (method.getModifiers() & Modifier.STATIC) != Modifier.STATIC) {
			helper.isActionClass = true;
			if(!actionClass.creator().isEmpty()) {
				helper.creator = cls.getMethod(actionClass.creator(), ActionContext.class);
			}
		}
		
		//查找字段的注解
		List<Field> allFieldList = new ArrayList<Field>() ;
		Class<?> tempClass = cls;
		while (tempClass != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
			allFieldList.addAll(Arrays.asList(tempClass .getDeclaredFields()));
		    tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
		}
		
		List<Field> fieldList = new ArrayList<Field>();
		List<ActionField> fieldAnnotations_ = new ArrayList<ActionField>();
		for(Field field : allFieldList) {
			ActionField actionField = field.getAnnotation(ActionField.class);
			if(actionField != null) {
				fieldList.add(field);
				fieldAnnotations_.add(actionField);
			}
		}
		if(fieldList.size() > 0) {
			Field[] fields = new Field[fieldList.size()];
			fieldList.toArray(fields);
			helper.fields = fields;
			helper.fieldAnnotations = new ActionField[fieldList.size()];
			fieldAnnotations_.toArray(helper.fieldAnnotations);
		}
		
		if(helper.params != null || helper.creator != null || helper.fields != null) {
			return helper;
		}else {
			return null;
		}
	}
	
	/**
	 * 创建动作的java方法所属于的类的实例化对象。如果
	 * 
	 * @param actionContext
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public Object createObject(ActionContext actionContext) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
		//创建对象
		Object obj = null;
		if(creator != null) {
			obj = creator.invoke(null, actionContext);
		}else if(fields != null || ((actionMethod.getModifiers() & Modifier.STATIC) != Modifier.STATIC)) {
			if(isActionClass){
				obj = actionContext.get(actionClass.getName());
			}
			if(obj == null) {
				obj = actionClass.getConstructor(new Class<?>[0]).newInstance();
			}
			if(isActionClass) {
				actionContext.g().put(actionClass.getName(), obj);
			}
		}
		
		//设置字段的值
		if(obj != null && fields != null) {
			for(int i=0; i<fields.length; i++) {
				Field field  = fields[i];
				String name = field.getName();
				String vname = fieldAnnotations[i].name();
				if(vname == null || "".equals(vname)) {
					vname = name;
				}
				
				boolean accessChanged = false;
				try {					
					if(field.isAccessible() == false) {
						field.setAccessible(true);
						accessChanged = true;
					}
					field.set(obj, actionContext.get(vname));
				}finally {
					if(accessChanged) {
						field.setAccessible(false);
					}
				}
			}
		}
		
		return obj;
	}
	
	public Object[] getParamValues(ActionContext actionContext){
		Object[] values = new Object[actionMethod.getParameterCount()];
		if(params != null) {
			for(int i=0; i<params.length; i++) {
				if(i > values.length - 1) {
					break;
				}
				
				values[i] = actionContext.get(params[i]);
			}
		}
		
		//如果最后多一个参数，那么设置为actionContext
		if(values.length > 0 && (params == null || params.length < values.length)){
			values[values.length - 1] = actionContext;
		}
		
		return values;
	}

	public Object invoke(Object object, ActionContext actionContext){
		if(actionMethod == null){
			return null;
		}

		Object[] params = getParamValues(actionContext);
		try {
			return actionMethod.invoke(object, params);
		} catch (Exception e) {
			return  new ActionException(e);
		}
	}
		
}
