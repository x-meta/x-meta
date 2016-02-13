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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ognl.Ognl;
import ognl.OgnlException;

import org.xmeta.Action;
import org.xmeta.ActionContext;
import org.xmeta.ActionException;
import org.xmeta.Thing;
import org.xmeta.World;

/**
 * 数据工具类。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class UtilData {
	public static final String VALUE_BLANK = "";
	public static final String VALUE_ONE = "1";
	public static final String VALUE_ZERO = "0";
	public static final String VALUE_FALSE = "false";
	public static final String VALUE_TRUE = "true";
	public static final String VALUE_CURRENT_DATE = "current_date";
	public static final String VALUE_NOW = "now";
	
	public static final String TYPE_STRING = "string";
	public static final String TYPE_INT = "int";
	public static final String TYPE_INTEGER = "integer";
	public static final String TYPE_LONG = "long";
	public static final String TYPE_FLOAT = "float";
	public static final String TYPE_DOUBLE = "double";
	public static final String TYPE_SHORT = "short";
	public static final String TYPE_BYTE = "byte";
	public static final String TYPE_BIGDECIMAL = "bigdecimal";
	public static final String TYPE_BIGINTEGER = "biginteger";
	public static final String TYPE_BOOLEAN = "boolean";
	public static final String TYPE_DATE = "date";
	public static final String TYPE_TIME = "time";
	public static final String TYPE_DATETIME = "datetime";
	public static final String TYPE_BYTES = "bytes";
	public static final String TYPE_OBJECT = "object";
	
	/**
	 * 如果str和matchs中任何一个匹配返回true，如果str=null或者matchs=null，返回false。
	 * 
	 * @param str 字符串
	 * @param matchs 匹配正则表达式
	 * @return 是否匹配
	 */
	public static boolean equalsOne(String str, String[] matchs){
		if(str == null || matchs == null){
			return false;
		}
		
		for(String match : matchs){
			if(match.equals(str)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 把数据对象转换为字符串类型。
	 * 
	 * @param value 值
	 * @param pattern 格式
	 * @return 结果
	 */
	public static String format(Object value, String pattern){
		if(value == null) return VALUE_BLANK;
		
		if(value instanceof Number){
			DecimalFormat df = null;
			if(pattern == null || VALUE_BLANK.equals(pattern)){
				df = new DecimalFormat("#.####################");
			}else{
				df = new DecimalFormat(pattern);
			}
			
			return df.format(value, new StringBuffer(), new FieldPosition(0)).toString();
		}else if(value instanceof Date){
			SimpleDateFormat sf = null;
			if(pattern == null || VALUE_BLANK.equals(pattern)){
				sf = new SimpleDateFormat("yyyy-MM-dd");
			}else{
				sf = new SimpleDateFormat(pattern);
			}
			
			return sf.format(value);			
		}else{
			return value.toString();
		}
	}
	
	public static Object parse(String value, String valueType, String pattern) throws ParseException{
		if(value == null) return null;
		
		String type = valueType;
		if(type != null){
			type = type.toLowerCase();
		}else{
			type = TYPE_STRING;
		}
		
		if(type == null || VALUE_BLANK.equals(type) || TYPE_STRING.equals(type)){
			return value;
		}
		
		if(VALUE_BLANK.equals(value)) return null;
		
		if(TYPE_INTEGER.equals(type) || TYPE_LONG.equals(type) || TYPE_FLOAT.equals(type) 
				|| TYPE_DOUBLE.equals(type) || TYPE_SHORT.equals(type)
	            || TYPE_BYTE.equals(type)){
			DecimalFormat df = null;
			if(pattern == null || VALUE_BLANK.equals(pattern)){
				df = new DecimalFormat("#.####################");
			}else{
				df = new DecimalFormat(pattern);
			}
			
			Number n = df.parse(value);
			
			if(TYPE_INTEGER.equals(type)){
				return new Integer(n.intValue());
			}
			
			if(TYPE_LONG.equals(type)){
				return new Long(n.longValue());
			}
			
			if(TYPE_FLOAT.equals(type)){
				return new Float(n.floatValue());
			}
			
			if(TYPE_DOUBLE.equals(type)){
				return new Double(n.doubleValue());
			}
			
			if(TYPE_SHORT.equals(type)){
				return n.shortValue();
			}
			
			if(TYPE_BYTE.equals(type)){
				return n.byteValue();
			}
			
			return null;
		}
		
		if(TYPE_BIGDECIMAL.equals(type)){
			return new BigDecimal(value);
		}
		
		if(TYPE_BIGINTEGER.equals(type)){
			return new BigInteger(value);
		}
		
		if(TYPE_BOOLEAN.equals(type)){
			if(VALUE_FALSE.equals(value.toLowerCase()) || VALUE_ZERO.equals(value)){
				return false;
			}else{
				return true;
			}
		}
		
		if(TYPE_DATE.equals(type) || TYPE_TIME.equals(type) || TYPE_DATETIME.equals(type)){
			if(VALUE_CURRENT_DATE.equals(value) || VALUE_NOW.equals(value)) return new Date();
			
			DateFormat sf = null;
			if(pattern == null || VALUE_BLANK.equals(pattern)){
				if(value.length() == 10){
					sf = new SimpleDateFormat("yyyy-MM-dd");
				}else if(TYPE_DATE.equals(type)){
					sf = SimpleDateFormat.getDateInstance();
				}else if(TYPE_TIME.equals(type)){
					sf = SimpleDateFormat.getTimeInstance();
				}else{
					sf = SimpleDateFormat.getDateTimeInstance();
				}
			}else{				
				sf = new SimpleDateFormat(pattern);
			}
			
			return sf.parse(value);
		}
		
		return value;
	}
	
	/**
	 * 添加指定的事物列表到已有的事物列表中，添加时如果存在第一个的描述者相同且名字相同的则不添加。
	 * 
	 * @param source 源事物列表
	 * @param forAdd 要添加的事物列表
	 * @param strict 是否严格判断，如果是严格判断那么校验两个事物必须相等，否则校验事物的描述者和名
	 * 
	 * @return 结果
	 */
	public static List<Thing> addToSource(List<Thing> source, List<Thing> forAdd, boolean strict){
		if(forAdd == null){
			return source;
		}
		
		if(source == null){
			source = new ArrayList<Thing>();
		}
		
		for(Thing fadd : forAdd){
			addToSource(source, fadd, strict);
		}
		
		return source;
	}
	
	/**
	 * 添加指定的事物到源事物列表中。
	 * 
	 * @param source 源事物列表
	 * @param forAdd 需要添加的事物
	 * @param strict 是否严格判断，如果是严格判断那么校验两个事物必须相等，否则校验事物的描述者和名
	 * @return 事物列表
	 */
	public static List<Thing> addToSource(List<Thing> source, Thing forAdd, boolean strict){
		if(forAdd == null){
			return source;
		}
		
		if(source == null){
			source = new ArrayList<Thing>();
		}
		
		//Thing forAddDescriptor = forAdd.getDescriptors().get(0);
		boolean have = false;
		for(Thing src : source){
			if(src == forAdd){
				have = true;
				break;
			}//else if(//forAddDescriptor == src.getDescriptors().get(0) && 
			//		forAdd.getMetadata().getName().equals(src.getMetadata().getName())){
			//	have = true;
			//	break;				
			//}
		}
		
		if(!have){
			source.add(forAdd);
		}
		
		return source;
	}
	
	public static BigDecimal getBigDecimal(Object v, BigDecimal defaultValue){		
    	if(v == null || VALUE_BLANK.equals(v)){
    		return null;
    	}else if(v instanceof BigDecimal){
    		return (BigDecimal) v;
    	}else if(v instanceof String){
    		return new BigDecimal((String) v);
    	}else if(v instanceof Integer){
    		return new BigDecimal((Integer) v);
    	}else if(v instanceof Boolean){
    		return new BigDecimal(((Boolean) v) ? 1 : 0);
    	}else if(v instanceof Byte){
    		return new BigDecimal((Byte) v);
    	}else if(v instanceof BigInteger){
    		return new BigDecimal((BigInteger) v);
    	}else if(v instanceof Double){
    		return new BigDecimal((Double) v);
    	}else if(v instanceof Float){
    		return new BigDecimal((Float) v);
    	}else if(v instanceof Short){
    		return new BigDecimal((Short) v);
    	}
    	
    	return defaultValue;
	}
	
	public static BigInteger getBigInteger(Object v, BigInteger defaultValue){    	
    	if(v == null || VALUE_BLANK.equals(v)){
    		return null;
    	}else if(v instanceof BigInteger){
    		return (BigInteger) v;
    	}else if(v instanceof String){
    		return new BigInteger((String) v);
    	}else if(v instanceof Integer){
    		return new BigInteger(v.toString());
    	}else if(v instanceof Boolean){
    		return new BigInteger(((Boolean) v) ? VALUE_ONE : VALUE_ZERO);
    	}else if(v instanceof Byte){
    		return new BigInteger(v.toString());
    	}else if(v instanceof BigDecimal){
    		return new BigInteger(v.toString());
    	}else if(v instanceof Double){
    		return new BigInteger(v.toString());
    	}else if(v instanceof Float){
    		return new BigInteger(v.toString());
    	}else if(v instanceof Short){
    		return new BigInteger(v.toString());
    	}
    	
    	return defaultValue;
    }
	
	public static byte getByte(Object v, byte defaultValue){    	
    	if(v == null || VALUE_BLANK.equals(v)){
    		return defaultValue;
    	}else if(v instanceof Number){
    		return ((Number) v).byteValue();
    	}else if(v instanceof String){
    		try{
    			return Byte.parseByte((String) v);
    		}catch(Exception e){
    			if("true".equals(v) || "TRUE".equals(v)){
    				return 1;
    			}else{
    				return 0;
    			}
    		}
    	}else if(v instanceof Boolean){
    		return (byte) (((Boolean) v) ? 1 : 0);
    	}else if(v instanceof BigInteger){
    		return (byte) ((BigInteger) v).byteValue();
    	}else if(v instanceof BigDecimal){
    		return ((BigDecimal) v).byteValue();
    	}
    	
    	return defaultValue;
    }
	
	public static byte[] getBytes(Object v, byte[] defaultValue){
    	if(v == null || VALUE_BLANK.equals(v)){
    		return null;
    	}else if (v instanceof byte[]){
    		return (byte[]) v;
    	}else if(v instanceof java.io.Serializable){
    		ByteArrayOutputStream bout = new ByteArrayOutputStream();
    		ObjectOutputStream oout;
			try {
				oout = new ObjectOutputStream(bout);
				oout.writeObject(v);
			} catch (IOException e) {
			}
			
    		return bout.toByteArray();
    	}
    	
    	return defaultValue;
    }
	
	public static char getChar(Object v, char defaultValue){
    	if(v == null || VALUE_BLANK.equals(v)){
    		return defaultValue;
    	}else if(v instanceof Number){
    		return (char) ((Number) v).intValue();
    	}else if(v instanceof BigInteger){
    		return (char) ((BigInteger) v).intValue();
    	}else if(v instanceof String){
    		return (char) new Integer((String) v).intValue();
    	}else if(v instanceof Boolean){
    		return (char) (((Boolean) v) ? 1 : 0);
    	}else if(v instanceof BigDecimal){
    		return (char) ((BigDecimal) v).intValue();
    	}
    	
    	return defaultValue;
    }
	
	public static Date getDate(Object v, Date defaultValue){    	
    	if(v == null || VALUE_BLANK.equals(v)){
    		return null;
    	}else if(v instanceof Date){
    		return (Date) v;
    	}else if(v instanceof String){
    		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");    		
    		try {
				return sf.parse((String) v);
			} catch (ParseException e) {
				Date date = getDateDefault((String) v);
	    		if(date != null){
	    			return date;
	    		}
			}
    	}
    	
    	return defaultValue;
    }
	
	public static Date getDateDefault(String defaultValue){
		  //日期的默认值
        defaultValue = defaultValue.toLowerCase();
        String dateStr = defaultValue;
        String numberStr = "";
        int index = defaultValue.indexOf("+");
        if(index != -1){
            dateStr = defaultValue.substring(0, index).trim();
            numberStr = defaultValue.substring(index, defaultValue.length()).trim();
        }else{
            index = defaultValue.indexOf("-");
            if(index != -1){
                dateStr = defaultValue.substring(0, index).trim();
                numberStr = defaultValue.substring(index, defaultValue.length()).trim();
            }
        }
 
        Date date = null;
        if("now".equals(dateStr) || "sysdate".equals(dateStr)){
            date = new Date();
        }else if("tomorrow".equals(dateStr)){
            date = UtilDate.getTomorrow();
        }else if("yesterday".equals(dateStr)){
            date = UtilDate.getYesterday();
        }else if("weekstart".equals(dateStr)){
            date = UtilDate.getWeekStart();
        }else if("weekend".equals(dateStr)){
            date = UtilDate.getWeekEnd();
        }else if("monthstart".equals(dateStr)){
            date = UtilDate.getMonthStart();
        }else if("monthend".equals(dateStr)){
            date = UtilDate.getMonthEnd();
        }else if("yearstart".equals(dateStr)){
            date = UtilDate.getYearStart();
        }else if("yearend".equals(dateStr)){
            date = UtilDate.getYearEnd();
        }else{
            date = new Date();
            try{
                double d = Double.parseDouble(dateStr);
                date = UtilDate.getDate(date, d);
            }catch(Exception e){
            }
        }
        if(numberStr != null && numberStr != ""){
            try{
                double d = (Double) Ognl.getValue(numberStr, null);
                //log.info("d=" + d);
                date = UtilDate.getDate(date, d);
            }catch(Exception e){
                //log.info("error", e);
            }
        }
        
        return date;
	}
	
	public static Date getDate(Object v, Date defaultValue, String pattern){    	
    	if(v == null || VALUE_BLANK.equals(v)){
    		return null;
    	}else if(v instanceof Date){
    		return (Date) v;
    	}else if(v instanceof String){
    		if(pattern == null || pattern.equals(VALUE_BLANK)){
    			pattern = "yyyy-MM-dd";
    		}
    		SimpleDateFormat sf = new SimpleDateFormat(pattern);    		
    		try {
				return sf.parse((String) v.toString().replace("T"," "));
			} catch (ParseException e) {
			}
    	}
    	
    	return defaultValue;
    }
	
	public static double getDouble(Object v, double defaultValue){    	
    	if(v == null || VALUE_BLANK.equals(v)){
    		return defaultValue;
    	}else if(v instanceof Number){
    		return ((Number) v).doubleValue();
    	}else if(v instanceof BigInteger){
    		return ((BigInteger) v).doubleValue();
    	}else if(v instanceof String){
    		return Double.parseDouble((String) v);
    	}else if(v instanceof Boolean){
    		return (byte) (((Boolean) v) ? 1 : 0);
    	}else if(v instanceof BigDecimal){
    		return ((BigDecimal) v).doubleValue();
    	}
    	
    	return defaultValue;
    }
	
	public static float getFloat(Object v, float defaultValue){    	
    	if(v == null || VALUE_BLANK.equals(v)){
    		return defaultValue;
    	}else if(v instanceof Number){
    		return ((Number) v).floatValue();
    	}else if(v instanceof BigInteger){
    		return ((BigInteger) v).floatValue();
    	}else if(v instanceof String){
    		return Float.parseFloat((String) v);
    	}else if(v instanceof Boolean){
    		return (byte) (((Boolean) v) ? 1 : 0);
    	}else if(v instanceof BigDecimal){
    		return ((BigDecimal) v).floatValue();
    	}
    	
    	return defaultValue;
    }
	
	public static long getLong(Object v, long defaultValue){    	
    	if(v == null || VALUE_BLANK.equals(v)){
    		return defaultValue;
    	}else if(v instanceof Long){
    		return (Long) v;
    	}else if(v instanceof Number){
    		return  ((Number) v).longValue();
    	}else if(v instanceof BigInteger){
    		return ((BigInteger) v).longValue();
    	}else if(v instanceof String){ 
    		return Long.parseLong((String) v);
    	}else if(v instanceof Integer){
    		return ((Integer) v).longValue();
    	}else if(v instanceof Boolean){
    		return (byte) (((Boolean) v) ? 1 : 0);
    	}else if(v instanceof BigDecimal){
    		return ((BigDecimal) v).longValue();
    	}
    	
    	return defaultValue;
    }
	
	public static int getInt(Object v, int defaultValue){    	
    	if(v == null || VALUE_BLANK.equals(v)){
    		return defaultValue;
    	}else if(v instanceof Integer){
    		return (Integer) v;
    	}else if(v instanceof Number){
    		return  ((Number) v).intValue();
    	}else if(v instanceof BigInteger){
    		return ((BigInteger) v).intValue();
    	}else if(v instanceof String){
    		try{
    			return Integer.parseInt((String) v);
    		}catch(Exception e){
    			String vs = ((String) v).toLowerCase();
    			if("false".equals(vs)){
    				return 0;
    			}else if("true".equals(vs)){
    				return 1;
    			}else{
    				throw new ActionException("Get int error", e);
    			}
    		}
    	}else if(v instanceof Boolean){
    		return (int) (((Boolean) v) ? 1 : 0);
    	}else if(v instanceof BigDecimal){
    		return ((BigDecimal) v).intValue();
    	}
    	
    	return defaultValue;
    }
	
	public static short getShort(Object v, short defaultValue){    	
    	if(v == null || VALUE_BLANK.equals(v)){
    		return defaultValue;
    	}else if(v instanceof Short){
    		return (Short) v;
    	}else if(v instanceof Number){
    		return ((Number) v).shortValue();
    	}else if(v instanceof BigInteger){
    		return (short) ((BigInteger) v).intValue();
    	}else if(v instanceof String){
    		return Short.parseShort((String) v);
    	}else if(v instanceof Boolean){
    		return (short) (((Boolean) v) ? 1 : 0);
    	}else if(v instanceof BigDecimal){
    		return (short) ((BigDecimal) v).shortValue();
    	}    	
    	return defaultValue;
    }
	
	public static boolean getBoolean(Object v, boolean defaultValue){
    	if(v instanceof Boolean){
    		return ((Boolean) v).booleanValue();
    	}else if(v instanceof String){
    		String value = (String) v;
	    	if(VALUE_TRUE.equals(value)){
	    		return true;
	    	}else if(VALUE_ONE.equals(value)){
	    		return true;
	    	}else{
	    		return false;
	    	}
    	}else if(v instanceof Number){
    		if(((Number) v).doubleValue() == 0){
    			return false;
    		}else{
    			return true;
    		}
    	}else if(v != null){
    		return true;
    	}else{
    		return defaultValue;
    	}
    }
	
	public static String getString(Object v, String defaultValue){    	
    	if(v != null){
    		if(v instanceof String){
    			return (String) v;
    		}else{
    			return v.toString();
    		}
    	}
    	
    	return defaultValue;
    }
	
	/** 
	 * 按照字节、千字节和兆返回大小的值。
	 * 
	 * @param size 大小
	 * @return 字符串
	 */
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
	
	/**
	 * 二进制转字符串。
	 * 
	 * @param bytes 字节数组
	 * @return 16进制字符串
	 */
	 public static String bytesToHexString(byte[] bytes) {
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
		 * 转换数据类型。
		 * 
		 * @param sourceValue 原值
		 * @param targetType 目标值
		 * @param pattern 格式
		 * @param patternType 格式类型
		 * @param patternAction 动作
		 * @return 转换后的结果
		 * @throws ParseException 异常
		 */
	public static Object transfer(Object sourceValue, String targetType, String pattern, String patternType, String patternAction) throws ParseException{
		if (pattern != null  && !"".equals(pattern) && sourceValue != null && !"".equals(sourceValue)) {
			if ("parse".equals(patternAction)) {
				return UtilData.parse(sourceValue.toString(), targetType, pattern);
			} else {
				sourceValue = UtilData.format(sourceValue, pattern);
			}
		}

		// 数据转换
		Object targetValue = sourceValue;
		if (targetType != null && !"".equals(targetType)) {
			if ("byte".equals(targetType)) {
				targetValue = UtilData.getByte(sourceValue, (byte) 0);
			} else if ("short".equals(targetType)) {
				targetValue = UtilData.getShort(sourceValue, (short) 0);
			} else if ("int".equals(targetType)) {
				targetValue = (int) UtilData.getLong(sourceValue, 0);
			} else if ("long".equals(targetType)) {
				targetValue = UtilData.getLong(sourceValue, 0);
			} else if ("float".equals(targetType)) {
				targetValue = UtilData.getFloat(sourceValue, 0);
			} else if ("double".equals(targetType)) {
				targetValue = UtilData.getDouble(sourceValue, 0);
			} else if ("boolean".equals(targetType)) {
				targetValue = UtilData.getBoolean(sourceValue, false);
			} else if ("byte[]".equals(targetType)) {
				if (sourceValue instanceof String) {
					targetValue = UtilString
							.hexStringToByteArray((String) sourceValue);
				} else if (sourceValue instanceof byte[]) {
					targetValue = sourceValue;
				} else {
					targetValue = null;
				}
			} else if ("hex_byte[]".equals(targetType)) {
				if (sourceValue instanceof byte[]) {
					targetValue = UtilString.toHexString((byte[]) sourceValue);
				} else if (sourceValue instanceof String) {
					targetValue = sourceValue;
				} else {
					targetValue = null;
				}
			} else
				targetValue = sourceValue;
		}
		
		return targetValue;
	}
	
	/**
	 * 返回通过属性定义的对象，首先使用UtilData获取，如果没有从actionContext中获取。
	 * 
	 * @param thing 事物
	 * @param attributeName 属性名
	 * @param actionContext 变量上下文
	 * @return 对象
	 * @throws OgnlException 异常
	 */
	public static Object getObject(Thing thing, String attributeName, ActionContext actionContext) throws OgnlException{
		Object obj = UtilData.getData(thing, attributeName, actionContext);
		if(obj instanceof String){
			obj = actionContext.get((String) obj);
		}
		
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(Thing thing, String attributeName, ActionContext actionContext) throws OgnlException{
		Object obj = UtilData.getData(thing, attributeName, actionContext);
		if(obj instanceof String){
			obj = actionContext.get((String) obj);
		}
		
		return (T) obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getObjectByType(Thing thing, String attributeName, Class<T> t, ActionContext actionContext) throws OgnlException{
		Object obj = getObject(thing, attributeName, actionContext);
		if(t.isInstance(obj)){
			return (T) obj;
		}else{
			return null;
		}
	}
		
	/**
	 * 根据事物的属性返回指定的事物。
	 * 
	 * @param thing 事物
	 * @param attributeName 属性名
	 * @param actionContext 变量上下文
	 * @return 事物，如果不存在返回null
	 * @throws OgnlException
	 */
	public static Thing getThing(Thing thing, String attributeName, ActionContext actionContext) throws OgnlException{
		Object obj = UtilData.getData(thing, attributeName, actionContext);
		if(obj instanceof Thing){
			return (Thing) obj;
		}else if(obj instanceof String){
			return World.getInstance().getThing((String) obj);
		}else if(obj != null){
			return World.getInstance().getThing(obj.toString());
		}else{
			return null;
		}
	}
	
	/**
	 * 先从事物指定的属性上获取事物，如果不存在从子节点的路径上获取。
	 * 
	 * @param thing 事物
	 * @param attributeName 属性名
	 * @param childThingPath 子事物节点
	 * @param actionContext 变量上下文
	 * @return 要返回的额事物，如果不存在返回null
	 * @throws OgnlException
	 */
	public static Thing getThing(Thing thing, String attributeName, String childThingPath, ActionContext actionContext) throws OgnlException{
		Thing th = getThing(thing, attributeName, actionContext);
		if(th == null && childThingPath != null){
			return thing.getThing(childThingPath);
		}else{
			return th;
		}
	}
	
	/**
	 * 通过事物的属性获取数据。
	 * 
	 * @param thing 事物
	 * @param attributeName 属性名
	 * @param actionContext 变量上下文
	 * @return 结果
	 * @throws OgnlException 异常
	 */
	public static Object getData(Thing thing, String attributeName, ActionContext actionContext) throws OgnlException{
		Object value = thing.get(attributeName);
		if(value != null && value instanceof String){
			String str = (String) value;
			if(str.startsWith("var:")){
				return actionContext.get(str.substring(4, str.length()));
			}else if(str.startsWith("ognl:")){
				return OgnlUtil.getValue(thing, attributeName, actionContext);
			}else if(str.startsWith("thing:")){
				String thingPath = str.substring(6, str.length());
				return World.getInstance().getThing(thingPath);
			}else if(str.startsWith("action:")){
				String thingPath = str.substring(7, str.length());
				Action action =  World.getInstance().getAction(thingPath);
				if(action != null){
					return action.run(actionContext);
				}else{
					return null;
				}
			}else{
				if("".equals(str)){
					return null;
				}
				
				return str;
			}
		}
		
		return value;
	}
	
	public static Object getData( String value, ActionContext actionContext) throws OgnlException{		
		String str = (String) value;
		if(str.startsWith("var:")){
			return actionContext.get(str.substring(4, str.length()));
		}else if(str.startsWith("ognl:")){
			return Ognl.getValue(str.substring(5, str.length()), actionContext);
		}else if(str.startsWith("thing:")){
			String thingPath = str.substring(6, str.length());
			return World.getInstance().getThing(thingPath);
		}else{
			if("".equals(str)){
				return null;
			}
			
			return str;
		}
	}
	
	public static boolean isTrue(Object condition){
		boolean ok = false;
		if(condition != null){
			if(condition instanceof Boolean){
				ok = (Boolean) condition;
			}else if(condition instanceof String){
				String str = ((String) condition).toLowerCase().trim();
				ok = "true".equals(str) | "1".equals(str);
			}else if(condition instanceof Number){
				Number n = (Number) condition;
				ok = n.intValue() == 1;
			}else{
				ok = true;
			}
		}else{
			ok = false;
		}
		
		return ok;
	}
	
	public static String getString(Thing thing, String attributeName, ActionContext actionContext) {
		return UtilString.getString(thing, attributeName, actionContext);
	}
	 
	public static void resetAttributeByType(Thing thing, String name, String type){
		if ("int".equals(type)) {
			try {
				thing.getAttributes().put(name, thing.getInt(name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("long".equals(type)) {
			try {
				thing.getAttributes().put(name, thing.getLong(name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("double".equals(type)) {
			try {
				thing.getAttributes().put(name, thing.getDouble(name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("float".equals(type)) {
			try {
				thing.getAttributes().put(name, thing.getFloat(name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("bigDecimal".equals(type)) {
			try {
				thing.getAttributes().put(name, thing.getBigDecimal(name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("bigInteger".equals(type)) {
			try {
				thing.getAttributes().put(name, thing.getBigInteger(name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("boolean".equals(type)) {
			try {
				thing.getAttributes().put(name, thing.getBoolean(name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("byte".equals(type)) {
			try {
				thing.getAttributes().put(name, thing.getByte(name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("bytes".equals(type)) {
			try {
				thing.getAttributes().put(name, thing.getBytes(name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("char".equals(type)) {
			try {
				thing.getAttributes().put(name, thing.getChar(name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("short".equals(type)) {
			try {
				thing.getAttributes().put(name, thing.getShort(name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("date".equals(type) || "datetime".equals(type) || "time".equals(type)) {
			try {
				thing.getAttributes().put(name, thing.getDate(name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
    /** A table of hex digits */
    private static final char[] hexDigit = {
	'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };
}
