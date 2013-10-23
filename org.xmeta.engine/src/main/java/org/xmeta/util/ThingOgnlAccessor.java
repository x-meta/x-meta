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
package org.xmeta.util;

import java.util.Map;

import org.xmeta.Thing;

import ognl.ObjectPropertyAccessor;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

/**
 * Ognl取Thing的属性的方法。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class ThingOgnlAccessor  extends ObjectPropertyAccessor implements PropertyAccessor{
	static{
		OgnlRuntime.setPropertyAccessor(Thing.class, new ThingOgnlAccessor());	
		OgnlClassResolver.getInstance();
	}
	
	public static void init(){		
	}
	
	@SuppressWarnings("rawtypes")
	public Object getProperty(Map context, Object target, Object name) throws OgnlException {
		Thing thing = (Thing) target;

		if(name instanceof String){
			return thing.getAttribute((String) name);
		}else{
			throw new OgnlException("没有发现属性：" + name);			
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
		Thing thing = (Thing) target;
		
		thing.set((String) name, value);
	}	
}