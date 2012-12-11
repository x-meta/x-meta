package org.xmeta.util;

import java.util.HashMap;
import java.util.Map;

public class UtilMap {
	/**
	 * 为了便于在代码中书写，把数组转化为Map参数。
	 * 
	 * @param paramArray
	 * @return
	 */
	public static Map<String, Object> toParams(Object[] paramArray){
		Map<String, Object> params = new HashMap<String, Object>();
		for(int i=0; i<paramArray.length / 2; i++){
			params.put((String) paramArray[i * 2], paramArray[i * 2 + 1]);
		}
		return params;
	}
	
	public static Map<String, Object> toMap(Object[] objectArray){
		return toParams(objectArray);
	}
}
