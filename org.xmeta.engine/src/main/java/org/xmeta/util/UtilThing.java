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

import org.xmeta.Thing;
import org.xmeta.World;

public class UtilThing {
	/**
	 * 通过事物属性获取事物定义的事物，或者通过指定的子事物的第一个子节点获取定义的事物。
	 * 
	 * @param thing
	 * @param attributeName
	 * @param childThingPath
	 * @return
	 */
	public static Thing getThingFromAttributeOrChilds(Thing thing, String attributeName, String childThingPath){
		Thing t = null;
		
		String attrValue = thing.getString(attributeName);
		if(attrValue != null && !"".equals(attrValue)){
			t = World.getInstance().getThing(attrValue);
		}
		
		if(t == null){
			t = thing.getThing(childThingPath);
			if(t != null && t.getChilds().size() > 0){
				t = t.getChilds().get(0);
			}else{
				t = null;
			}
		}
		
		return t;
	}
	
	/**
	 * 判断一个事物作为描述者时是否是给定的类型，即判断这个事物的路径以及所有继承事物的路径是否是指定的descritpor。
	 * 
	 * @param descriptorThing
	 * @param descriptor
	 * @return
	 */
	public static boolean isDescriptorEquals(Thing descriptorThing, String descriptor){
		if(descriptorThing == null){
	        return false;
	    }
	    
	    if(descriptorThing.getMetadata().getPath().equals(descriptor)){
	        return true;
	    }
	    
	    for(Thing ext : descriptorThing.getAllExtends()){
	        if(ext.getMetadata().getPath().equals(descriptor)){
	            return true;
	        }
	    }
	    
	    return false;
	}
	
	public static boolean isDescriptorEquals1(String descriptorFor, String descriptor){
		return isDescriptorEquals(World.getInstance().getThing(descriptorFor), descriptor);
	}
}