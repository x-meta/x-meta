package org.xmeta.codes;

import org.xmeta.Thing;

public class CoderUtils {
	/**
	 * 判断给定的值是否是默认值。
	 * 
	 * @param attribute 属性描述者
	 * @param value 要比较的属性值
	 * @return
	 */
	public static boolean isDefault(Thing attribute, String value){
		String defaultValue = attribute.getStringBlankAsNull("default");
		String type = attribute.getString("type");
		String inputtype = attribute.getString("inputtype");
		
		if(defaultValue == null){
			//布尔值的默认值都是false
			if("boolean".equals(type) || "truefalse".equals(inputtype) || "truefalseselect".equals(inputtype)){
				if("false".equals(value)){
					return true;
				}
			}
			
			return false;
		}else{
			return defaultValue.equals(value);
		}
	}
}
