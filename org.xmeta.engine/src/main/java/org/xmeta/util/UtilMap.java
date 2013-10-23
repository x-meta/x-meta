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