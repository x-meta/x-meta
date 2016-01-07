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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ognl.Ognl;

import org.xmeta.ActionContext;
import org.xmeta.Thing;
import org.xmeta.World;
import org.xmeta.ui.session.Session;
import org.xmeta.ui.session.SessionManager;

/**
 * 字符串工具类。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class UtilString {
	/**
	 * 解析使用Groovy方式定义的字符串数组，如["cb", "cb"] as String[]。
	 * 
	 * @param str 字符串
	 * @return 数组
	 */
	public static String[] getGroovyStringArray(String str){
		if(str.startsWith("[") && str.endsWith("as String[]")){
			str= str.substring(1, str.lastIndexOf("]", str.length() - 3));
		}
		
		String strs[] = str.split("[,]");
		for(int i=0; i<strs.length; i++){
			strs[i] = strs[i].trim();
			if(strs[i].startsWith("\"") && strs[i].endsWith("\"")){
				strs[i] = strs[i].substring(1, strs[i].length() - 1);
			}
		}
		
		return strs;
	}
	
	/**
	 * 返回字符串是否相等，如果属性值为null也返回false。
	 * 
	 * @param thing 事物
	 * @param attributeName 属性名
	 * @param value 值
	 * @return 是否相等
	 */
	public static boolean eq(Thing thing, String attributeName, String value){
		String attrValue = thing.getString(attributeName);
		if(attrValue != null && attrValue.equals(value)){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 返回指定的事物的属性是否为空，如果是字符串那么null和""都返回true。
	 * 
	 * @param thing 事物
	 * @param attributeName 属性名
	 * @return 是否为null
	 */
	public static boolean isNull(Thing thing, String attributeName){
		Object value = thing.getAttribute(attributeName);
		if(value == null || (value instanceof String && "".equals(value))){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 分割字符串的方法非String实现，稍微快一些。
	 * 
	 * @param str 字符串
	 * @param ch 分给符
	 * @return 结果
	 */
	public static String[] split(String str, char ch){
		char[] chs = str.toCharArray();
		String[] strs = new String[10];
		int strIndex = 0;
		int index1 = 0;
		int index2 = 0;
		while(index2 < chs.length){
			if(chs[index2] == ch){				
				strs[strIndex] = new String(chs, index1, index2 - index1);
				index1 = index2+1;
				strIndex++;
				
				if(strIndex >= strs.length){
					String[] nstrs = new String[strs.length + 10];
					System.arraycopy(strs, 0, nstrs, 0, strs.length);
					strs = nstrs;
				}
			}
			
			index2++;
		}
		if(index1 <= index2){
			strs[strIndex] = new String(chs, index1, index2 - index1);
			strIndex++;
			
			if(strIndex >= strs.length){
				String[] nstrs = new String[strs.length + 10];
				System.arraycopy(strs, 0, nstrs, 0, strs.length);
				strs = nstrs;
			}
		}
		String fstrs[] = new String[strIndex];
		System.arraycopy(strs, 0, fstrs, 0, strIndex);
		return fstrs;
	}
	
	/**
	 * 判断描述者列表字符串中是否包含描述者。
	 * 
	 * @param descriptors 描述者列表
	 * @param descriptor 描述者
	 * @return 是否包含
	 */
	public static boolean haveDescriptor(String descriptors, String descriptor){
		if(descriptors == null){
			return false;
		}else{
			String[] descs = descriptors.split("[,]");
			for(String desc : descs){
				if(desc.equals(descriptor)){
					return true;
				}
			}
			
			return false;
		}
	}
	
	/**
	 * 插入一个指定字符串至源字符串中，源字符串中用,号隔开的。
	 * 
	 * @param source  源字符串
	 * @param forInsert 需要插入的字符串
	 * @param index 位置
	 * @return 新的字符串
	 */
	public static String insert(String source, String forInsert, int index){
		if(forInsert == null || "".equals(forInsert)){
			return source;
		}
		
		if(source != null && !"".equals(source)){
			String srouces[] = source.split("[,]");			
		
			source = "";

			int i = 0;
			boolean added = false;
			for(String src : srouces){
				if(index == i){
					source = source + "," + forInsert;
					added = true;
				}
				
				if(!src.equals(forInsert)){
					source = source + "," + src;
				}
				i++;
			}
			
			if(!added){
				source = source + "," + forInsert;
			}
			
			if(source.startsWith(",")){
				source = source.substring(1, source.length());
			}
			
			return source;
		}else{
			return forInsert;
		}
	}
	
	public static Object createObjectFromParams(String params,  String thingPath, ActionContext actionContext){
		if(params == null || "".equals(params)){
			return null;
		}
		
		Map<String, String> values = UtilString.getParams(params);
		return  createObjectFromParams(values, thingPath, actionContext);
	}
	
	public static Object createObjectFromParams(Map<String, ?> values, String thingPath, ActionContext actionContext){
		Thing thing = World.getInstance().getThing(thingPath);
	    if(thing != null){
	        Thing vthing = new Thing(thingPath);
	        vthing.getAttributes().putAll(values);
	        return vthing.doAction("create", actionContext);
	    }else{
	    	return null;
	    }
	}
	
	public static boolean isNotNull(String str){
		return str != null && !"".equals(str);
	}
	
	public static boolean isNull(String str){
		return str == null || "".equals(str);
	}
	
	public static String capFirst(String str){
    	if(str == null || str.length() == 0){
    		return str;
    	}
    	
    	if(str.length() == 1){
    		return str.toUpperCase();
    	}
    	
    	return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
    }
	
	/**
	 * 让字符串的第一个字母小写。
	 * 
	 * @param str 字符串
	 * @return 出理后的字符串
	 */
    public static String uncapFirst(String str){
    	if(str == null || str.length() == 0){
    		return str;
    	}
    	
    	if(str.length() == 1){
    		return str.toLowerCase();
    	}
    	
    	return str.substring(0, 1).toLowerCase() + str.substring(1, str.length()); 
    }
	
	public static void debug(ActionContext context, Object obj){
		System.out.println(context);
		System.out.println(obj);
		System.out.println(context.get("parent"));
	}
	
	public static String[] getStringArray(String strArray){
		if(strArray == null){
			return null;
		}else{
			String ints[] = strArray.split("[,]");
			String[] is = new String[ints.length];
			for(int i=0; i<ints.length; i++){
				is[i] = ints[i].trim();
			}
			
			return is;
		}
	}
	
	public static int[] toIntArray(String intArray){
		if(intArray == null || "".equals(intArray)){
			return null;
		}else{
			String ints[] = intArray.split("[,]");
			int[] is = new int[ints.length];
			for(int i=0; i<ints.length; i++){
				is[i] = Integer.parseInt(ints[i].trim());
			}
			
			return is;
		}
	}
	
	/**
	 * 解码参数字符串，分隔符默认为&amp;，默认编码utf-8，过滤引号。
	 * 
	* @param str 参数字符串
	 * @return 参数
	 */
	public static Map<String, String> getParams(String str){
		return getParams(str, "&", "utf-8", true);
	}
	
	/**
	 * 解码参数字符串，默认编码utf-8，过滤引号。
	 * 
	* @param str    参数字符串
	 * @param splitStr 分隔符
	 * @return 参数
	 */
	public static Map<String, String> getParams(String str, String splitStr){
		return getParams(str, splitStr, "utf-8", true);
	}
	
	/**
	 * 解码参数字符串，过滤引号。
	 * 
	* @param str    参数字符串
	 * @param splitStr 分隔符
	 * @param encoding 编码
	 * @return 分析后的参数集
	 */
	public static Map<String, String> getParams(String str, String splitStr, String encoding){
		return getParams(str, splitStr, encoding, true);
	}
	
	/**
	 * 解码参数字符串。
	 * 
	 * @param str    参数字符串
	 * @param splitStr 分隔符
	 * @param encoding 编码
	 * @param trimQuotate  是否过滤参数值包围的引号
	 * @return 参数集
	 */
	public static Map<String, String> getParams(String str, String splitStr, String encoding, boolean trimQuotate){
		if(str == null || "".equals(str)){
			return Collections.emptyMap();
		}
		
		Map<String, String> paras = new HashMap<String, String>();
		String[] ps = str.split("[" + splitStr + "]");
		for(int i=0; i<ps.length; i++){
			int index = ps[i].indexOf("=");
			if(index != -1 && index != 0 && index != ps[i].length()){
				String name = ps[i].substring(0, index);
				String value = ps[i].substring(index + 1, ps[i].length());
				try {
					value = URLDecoder.decode(value, encoding);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				
				if(trimQuotate){
					if(value.startsWith("\"") || value.startsWith("'")){
						value = value.substring(1, value.length());
					}
					if(value.endsWith("\"") || value.endsWith("'")){
						value = value.substring(0, value.length() - 1);
					}
				}
				
				paras.put(name.trim(), value);
			}
		}
		
		return paras;
	}
	
	public static int getInt(String str){
		try{
			return Integer.parseInt(str);
		}catch(Exception e){
			// e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 从事物取指定的属性的字符串值，然后从actionContext中取可能的值。
	 * 
	 * @param thing 事物
	 * @param attribute 属性名
	 * @param actionContext 变量上下文
	 * @return 值
	 */
	public static String getString(Thing thing, String attribute, ActionContext actionContext){
		String value = thing.getString(attribute);
		return getString(value, actionContext);
	}
	
	/**
	 * <p>从制定的字符串值中读取字符串。</p>
	 * 
	 * <p>如果字符串值为"号开头，那么返回"号包围的字符串值，如果是以res:开头那么从资源文件中读取，如果以上条件都不符合那么从
	 * actionContext或binding中读取。</p>
	 * 
	 * 如果是读取资源，那么字符串的格式为：res:&lt;resourceName&gt;:&lt;varName&gt;:&lt;defaultValue&gt;。
	 * 
	 * @param value 字符串值
	 * @param actionContext 动作上下文
	 * 
	 * @return 字符串
	 */
	public static String getString(String value, ActionContext actionContext){
		if(value == null) return "";
		value = value.trim();
		
		if(value.startsWith("res:")){
			Session session = SessionManager.getSession(null);
			UtilResource resource = session.getI18nResource();
			
			String resStrs[] = value.split("[:]");
			String defaultValue = "";
			if(resStrs.length >= 4){
				defaultValue = resStrs[3];
			}
				
			String svalue = resource.get(resStrs[1], resStrs[2], defaultValue);
			if(svalue == null){
				return defaultValue;
			}else{
				return svalue;
			}
		}else if(value.startsWith("label:")){
			String thingPath = value.substring(6, value.length());
			Thing labelThing = World.getInstance().getThing(thingPath);
			if(labelThing == null){
				return thingPath;
			}else{
				return World.getInstance().getThing(thingPath).getMetadata().getLabel();
			}
		}else if(value.startsWith("desc:")){
			String thingPath = value.substring(5, value.length());
			Thing labelThing = World.getInstance().getThing(thingPath);
			if(labelThing == null){
				return thingPath;
			}else{
				return World.getInstance().getThing(thingPath).getMetadata().getDescription();
			}
		}else if(value.startsWith("attr:")){
			String thingPath = value.substring(5, value.length());
			Object obj = World.getInstance().get(thingPath);
			if(obj != null){
				return String.valueOf(obj);
			}else{
				return thingPath;
			}
		}else if(value.startsWith("ognl:")){
			String exp = value.substring(5, value.length());
			Object obj = null;
			try {
				obj = (String) Ognl.getValue(exp, actionContext);
			} catch (Exception e) {
				obj = null;
			}				
			
			if(obj != null){
				return String.valueOf(obj);
			}else{
				return null;
			}
		}else if(value.startsWith("xworker:")){
			String path = value.substring(8, value.length());
			return World.getInstance().getPath() + "/" + path;
		}else if(value.startsWith("var:")){
			String path = value.substring(4, value.length());
			return (String) actionContext.get(path);
		}
		
		String v = value;
		//boolean constant = false;
		if(v.startsWith("\"")){
			//按常量处理
			//constant = true;
			//去第一个"
			v = v.substring(1, v.length());
		}
		
		if(v.endsWith("\"")){
			//去最后一个"
			v = v.substring(0, v.length() - 1);
		}
		
		return v;
		/*
		if(constant){
			return v;
		}else{
			String obj = null;
			if(actionContext != null){
				try {
					obj = (String) Ognl.getValue(v, actionContext);
				} catch (Exception e) {
					obj = (String) actionContext.get(v);
				}				
			}
			
			if(obj != null){
				return obj;
			}else{
				return value;
			}
		}
		*/
	}

	public static String toUnicode(String theString, boolean escapeSpace) {
    	if(theString == null) return "";
    	
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\'); outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch(aChar) {
		case ' ':
		    if (x == 0 || escapeSpace) 
			outBuffer.append('\\');
		    outBuffer.append(' ');
		    break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                          break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                          break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                          break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                          break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\'); outBuffer.append(aChar);
                    break;
                default:
                    if ((aChar < 0x0020) || (aChar > 0x007e)) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }
	
	 public static char toHex(int nibble) {
    	return hexDigit[(nibble & 0xF)];
    }

	 public static String toHexString(byte[] bytes) {
			char[] buf = new char[bytes.length * 2];
			int radix = 1 << 4;
			int mask = radix - 1;
			for (int i = 0; i < bytes.length; i++) {
				buf[2 * i] = hexDigit[(bytes[i] >>> 4) & mask];
				buf[2 * i + 1] = hexDigit[bytes[i] & mask];
			}

			return new String(buf);
		}

		/**
		 * 将"00 01 02"形式的字符串转成byte[]
		 * 
		 * @param hex 16进制字符串
		 * @return 字节数组
		 */
		public static byte[] hexStringToByteArray(String hex) {
			if (hex == null) {
				return null;
			}

			int stringLength = hex.length();
			if (stringLength % 2 != 0) {
				throw new IllegalArgumentException("Hex String must have even number of characters!");
			}

			byte[] result = new byte[stringLength / 2];

			int j = 0;
			for (int i = 0; i < result.length; i++) {
				char hi = Character.toLowerCase(hex.charAt(j++));
				char lo = Character.toLowerCase(hex.charAt(j++));
				result[i] = (byte) ((Character.digit(hi, 16) << 4) | Character.digit(lo, 16));
			}

			return result;
		}
		
    /** A table of hex digits */
    private static final char[] hexDigit = {
	'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };
    
	public static String getSizeInfo(double size) {
		DecimalFormat sf = new DecimalFormat("#.##");
		if (size < 1024) {
			return String.valueOf(size) + "B";
		} else if (size < 1024 * 1024) {
			return sf.format(size / 1024) + "KB";
		} else {
			return sf.format(size / 1024 / 1024) + "MB";
		}
	}
}