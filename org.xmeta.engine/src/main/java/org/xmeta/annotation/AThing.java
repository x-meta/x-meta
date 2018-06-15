package org.xmeta.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来标记一个类是事物。
 * 
 * @author zyx
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AThing {
	/** 
	 * 描述者列表。
	 * @return
	 */
	public String descriptors();
	
	/**
	 * 继承者列表。
	 * @return
	 */
	public String extends_() default "";
	
	/**
	 * 属性设置，格式key1=value1&amp;key2=value2&amp;...&amp;keyn=valuen。
	 * 
	 * @return
	 */
	public String attributes() default "";
}
