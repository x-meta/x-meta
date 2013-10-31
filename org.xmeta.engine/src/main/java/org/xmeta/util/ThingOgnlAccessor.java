/*******************************************************************************
* Copyright 2007-2013 See AUTHORS file.
 * 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
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