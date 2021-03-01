package org.xmeta.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.xmeta.ActionException;
import org.xmeta.Thing;
import org.xmeta.util.UtilString;

public class ThingAnnotationUtils {
	public static Thing parse(Class<?> cls) {
		AThing athing = cls.getAnnotation(AThing.class);
		if(athing == null) {
			//必须要设置athing标识
			return null;
		}
		
		//创建新的事物
		Thing thing = new Thing(athing.descriptors());
		thing.set("extends", athing.extends_());
		String attrs = athing.attributes();
		if(attrs != null) {
			Map<String, String> as = UtilString.getParams(attrs);
			for(String key : as.keySet()) {
				thing.set(key, as.get(key));
			} 
		}
		
		//注解AThingAttribute
		for(AThingAttribute attr : cls.getAnnotationsByType(AThingAttribute.class)) {
			thing.set(attr.name(), attr.value());
		}

		//注解AThingChild
		for(AThingChild achild : cls.getAnnotationsByType(AThingChild.class)) {
			Thing child = new Thing();
			child.set("name", achild.name());
			child.set("extends", achild.descriptors());
			thing.addChild(child);
		}
		
		//字段的注解
		for(Field field : cls.getDeclaredFields()) {
			//属性
			AAttribute aatri = field.getAnnotation(AAttribute.class);
			if(aatri != null) {
				try {
					if((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
						thing.set(field.getName(), field.get(null));
					}
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
				continue;
			}
			
			//子节点
			AChild achild = field.getAnnotation(AChild.class);
			if(achild != null) {
				if((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
					try {
						Object obj = field.get(null);
						if(obj != null && obj instanceof Thing) {
							thing.addChild((Thing) obj);
						}else {
							Thing child = parse(field.getType());
							if(child != null) {
								thing.addChild(child);
							}
						}
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					}
				}
				continue;
			}	
			
			//XML表示的子节点
			AChildXml achildxml = field.getAnnotation(AChildXml.class);
			if(achildxml != null) {
				if((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
					try {
						String xml = (String)field.get(null);
						if(xml != null) {
							thing.addChild(xml);
						}
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					} catch (Exception e) {
						throw new ActionException("Parse xml error, field=" + field, e);
					}
				}
				continue;
			}	
		}
		
		//方法的注解，设置模型的行为
		Thing actions = null;
		for(Method method : cls.getDeclaredMethods()) {
			AAction aaction = method.getAnnotation(AAction.class);
			if(aaction != null) {
				if(actions == null) {
					actions = new Thing("actions");
					thing.addChild(actions);
				}
				Thing javaAction = new Thing("JavaAction");
				javaAction.set("name", method.getName());
				javaAction.set("useOuterJava", "true");
				javaAction.set("outerClassName", cls.getName());
				javaAction.set("methodName", method.getName());
				actions.addChild(javaAction);
			}
		}
		
		return thing;
	}
}
