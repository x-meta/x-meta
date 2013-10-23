/*
    X-Meta Engine。
    Copyright (C) 2013  zhangyuxiang

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    For alternative license options, contact the copyright holder.

    Emil zhangyuxiang@tom.com
 */
package org.xmeta;

import java.util.HashMap;
import java.util.Map;

/**
 * 动作上下文中栈点保存的就是Bindings，Bindings是一个Map，用于保存变量和一些函数调用相关的数据。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class Bindings extends HashMap<String, Object>{
	private static final long serialVersionUID = 1L;
	
	/** 调用动作的对象，一般是actionContext压入栈时的对象 */
	public Object caller = null;
	
	/** 事物动作上下文的动作上下文 */
	public Map<Thing, ActionContext> contexts = new HashMap<Thing, ActionContext>();
		
	public World world = World.getInstance();
	
	/** 是否关闭全局事物动作上下文，如果关闭子函数也都关闭 */
	public boolean disableGloableContext = false;
	
	/** 
	 * 是否是变量范围的标志，通常在函数调用（动作执行）、条件、循环等处设置为true，以便划分一个
	 * 变量范围，一般新的变量都保存到此变量范围中。
	 * 
	 * 为了实现类似其他语言如java的变量范围的设置。
	 */
	public boolean isVarScopeFlag = false;
	
	/**
	 * 构造一个空的StackMap。
	 *
	 */
	public Bindings(){
		super();	
	}

	public Object getCaller() {
		return caller;
	}

	public void setCaller(Object caller) {
		this.caller = caller;
	}	
	
	public Map<Thing, ActionContext> getContexts(){
		return contexts;
	}
}