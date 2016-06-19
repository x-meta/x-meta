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

import org.xmeta.ActionException;
import org.xmeta.Thing;
import org.xmeta.World;

public class UtilThing {
	/**
	 * 通过事物属性获取事物定义的事物，或者通过指定的子事物的第一个子节点获取定义的事物。
	 * 
	 * @param thing 事物
	 * @param attributeName 属性名
	 * @param childThingPath 子事物路径
	 * @return 事物
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
	 * 通过事物属性获取事物定义的事物，或者通过指定的子事物的第一个子节点获取定义的事物。
	 * 
	 * @param thing 事物
	 * @param attributeName 属性名
	 * @param childThingPath 子事物路径
	 * @return 事物
	 */
	public static Thing getThingFromAttributeOrChild(Thing thing, String attributeName, String childThingPath){
		Thing t = null;
		
		String attrValue = thing.getString(attributeName);
		if(attrValue != null && !"".equals(attrValue)){
			t = World.getInstance().getThing(attrValue);
		}
		
		if(t == null){
			return thing.getThing(childThingPath);
		}
		
		return t;
	}
	
	/**
	 * 判断一个事物作为描述者时是否是给定的类型，即判断这个事物的路径以及所有继承事物的路径是否是指定的descritpor。
	 * 
	 * @param descriptorThing 描述者事物
	 * @param descriptor 描述者
	 * @return 是否
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
	
	/**
	 * 获取事物，如果不存在就创建一个。
	 * 
	 * @param path 路径
	 * @param thingManager 事物管理器
	 * @param descriptorForCreate 描述者
	 * @return 事物
	 */
	public static Thing getThingIfNotExistsCreate(String path, String thingManager, String descriptorForCreate){
		Thing thing = World.getInstance().getThing(path);
		if(thing == null){
			thing = new Thing(descriptorForCreate);
			thing.saveAs(thingManager, path);
		}
		
		return thing;
	}
	
	/**
	 * 获取事物，如果不存在就用已有的事物创建一个。
	 * 
	 * @param path 路径
	 * @param thingManager 事物管理器
	 * @param forReplace 是否覆盖
	 * @return 事物
	 */
	public static Thing getThingIfNotExistsCreate(String path, String thingManager, Thing forReplace){
		Thing thing = World.getInstance().getThing(path);
		if(thing == null){
			thing = forReplace.detach();
			thing.saveAs(thingManager, path);
		}
		
		return thing;
	}
	
	/**
	 * 返回引用的事物，如果安路就不存在，那么就找根事物下的子事物。如果是子事物，那么替换路径。
	 * 
	 * @param parent
	 * @param attribute
	 * @return
	 */
	public static Thing getQuoteThing(Thing thing, String attribute){
		String path = thing.getStringBlankAsNull(attribute);
		if(path == null){
			return null;
		}
		
		World world = World.getInstance();
		Thing qthing = world.getThing(path);
		if(qthing == null){			
			int index = path.indexOf("/@");
			if(index != -1){
				Thing root = thing.getRoot();
				path = root + path.substring(index, path.length());
				qthing = world.getThing(path);
				if(qthing != null){
					thing.set(attribute, path);
				}
			}
		}
		
		return qthing;
	}
	
	/**
	 * 改变一个事物的编码格式。
	 * 
	 * @param thing
	 * @param coder
	 */
	public static void changeCoder(Thing thing, String coder){
		if(coder != null && !coder.equals(thing.getMetadata().getCoderType())){
			if(World.getInstance().getThingCoder(coder) == null){
				throw new ActionException("Thing coder not exists, type=" + coder);
			}
			
			thing = thing.getRoot();
			
			//先删除原有事物，这样可以从文件中删除，然后再保存
			thing.remove();
			
			thing.getMetadata().setCoderType(coder);
			thing.save();
		}
	}
}