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
}
