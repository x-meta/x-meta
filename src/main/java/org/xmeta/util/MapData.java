package org.xmeta.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapData {
	protected Map<String, Object> map = new HashMap<String, Object>();
	
	public void put(String key, Object value) {
		map.put(key, value);
	}
	
	public void set(String key, Object value) {
		map.put(key, value);
	}
	
	public Object get(String key) {
		return map.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getObject(String key){
		return (T) map.get(key);
	}
	
	public String getString(String key){
		return UtilData.getString(get(key), null);
	}
	
	public byte getByte(String key){
		return UtilData.getByte(get(key), (byte) 0);
	}
	
	public short getShort(String key){
		return UtilData.getShort(get(key), (short) 0);
	}
	
	public int getInt(String key){
		return UtilData.getInt(get(key), 0);
	}
	
	public long getLong(String key){
		return UtilData.getLong(get(key), 0);
	}
	
	public boolean getBoolean(String key){
		return UtilData.getBoolean(get(key), false);
	}
	
	public byte[] getBytes(String key){
		return UtilData.getBytes(get(key), null);
	}
	
	public Date getDate(String key){
		return UtilData.getDate(get(key), null);
	}
	
	public double getDouble(String key){
		return UtilData.getDouble(get(key), 0);
	}
	
	public float getFloat(String key){
		return UtilData.getFloat(get(key), 0);
	}
	
	
	public BigDecimal getBigDecimal(String key){
		return UtilData.getBigDecimal(get(key), null);
	}
	
	public BigInteger getBigInteger(String key){
		return UtilData.getBigInteger(get(key), null);
	}
}
