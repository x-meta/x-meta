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

import java.util.HashMap;
import java.util.Map;

public class UtilMap {
	/**
	 * 为了便于在代码中书写，把数组转化为Map参数。
	 * 
	 * @param paramArray 参数列表
	 * @return 参数集
	 */
	public static Map<String, Object> toParams(Object[] paramArray){
		Map<String, Object> params = new HashMap<String, Object>();
		if(paramArray != null) {
			for(int i=0; i<paramArray.length / 2; i++){
				params.put((String) paramArray[i * 2], paramArray[i * 2 + 1]);
			}
		}
		return params;
	}
	
	//public static Map<String, Object> toMap(Object[] objectArray){
	//	return toParams(objectArray);
	//}
	
	public static Map<String, Object> toMap(Object ... objectArray){
		return toParams(objectArray);
	}
}