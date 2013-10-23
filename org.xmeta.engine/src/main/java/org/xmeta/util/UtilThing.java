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