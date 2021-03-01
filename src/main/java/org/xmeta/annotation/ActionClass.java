package org.xmeta.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionClass {
	/**
	 * 必须要是静态的，且参数是ActionContext的方法，否则执行会报错。
	 * 
	 * @return
	 */
	public String creator();
}
