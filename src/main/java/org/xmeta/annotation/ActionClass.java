package org.xmeta.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个类是动作类。动作类用于调用非静态的方法，此时需要实例化对象。默认通过不含参数的默认构造方法实例化对象，并把类名作为key，
 * 把生成的对象放到当前的动作上下文的全局变量栈中。如果指定了creator，那么调用creator指定的静态方法获取对象。creator方法必须是静态的，
 * 且参数固定为ActionContext。
 *
 * 当执行动作时，如果字段标记了ActionField注解，那么在每次执行时都会为这些字段赋值，其中值时动作上下文中对应的变量。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionClass {
	/**
	 * 必须要是静态的，且参数是ActionContext的方法，否则执行会报错。
	 * 
	 * @return
	 */
	public String creator() default "";
}
