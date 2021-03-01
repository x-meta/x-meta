package org.xmeta.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 应用在类上，用于定义一个子节点，使用继承的方法。
 * 如：{name="xxx" extends="xxxx"}
 * @author zyx
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AThingChild {
	public String name();
	
	public String descriptors();
}
