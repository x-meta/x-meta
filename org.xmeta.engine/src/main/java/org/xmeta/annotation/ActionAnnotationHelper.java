package org.xmeta.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.xmeta.ActionContext;

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
	
	/**
	 * 分析方法和方法所述的类有没有定义动作相关的注解，如果有那么返回Helper，如果没有那么返回null。
	 * @param method
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public static ActionAnnotationHelper parse(Method method) throws NoSuchMethodException, SecurityException {
		ActionAnnotationHelper helper = new ActionAnnotationHelper();
		helper.actionMethod = method;
		
		//查找方法的注解
		ActionParams actionParams = method.getAnnotation(ActionParams.class);		
		if(actionParams != null) {
			helper.params = actionParams.names().split("[,]");
		}
		
		//查找类的注解
		Class<?> cls = method.getDeclaringClass();
		helper.actionClass = cls;
		ActionClass actionClass = cls.getAnnotation(ActionClass.class);
		if(actionClass != null) {
			helper.creator = cls.getMethod(actionClass.creator(), ActionContext.class);
		}
		
		//查找字段的注解
		List<Field> fieldList = new ArrayList<Field>();
		List<ActionField> fieldAnnotations_ = new ArrayList<ActionField>();
		for(Field field : cls.getDeclaredFields()) {
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
	 */
	public Object createObject(ActionContext actionContext) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		//创建对象
		Object obj = null;
		if(creator != null) {
			obj = creator.invoke(null, actionContext);
		}else if(fields != null || ((actionMethod.getModifiers() & Modifier.STATIC) == Modifier.STATIC)) {
			obj = actionClass.newInstance();
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
		
}