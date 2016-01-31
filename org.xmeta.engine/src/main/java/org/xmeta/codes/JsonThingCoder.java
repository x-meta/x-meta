package org.xmeta.codes;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmeta.ActionException;
import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingIndex;
import org.xmeta.XMetaException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonThingCoder implements ThingCoder{
	private static  ObjectMapper mapper = new ObjectMapper();
	private static String codeType = "xer.js";

	@Override
	public void encode(Thing thing, OutputStream out) {
		throw new ActionException("JsonThingCoder doese not supper encode method, code is edit by hand");
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void decode(Thing thing, InputStream in, long lastModifyed) {
		try{
			Map<String, Object> values = mapper.readValue(in, Map.class);
			
			Map<String, String> importContext = new HashMap<String, String>();
			Map<String, Object> imports = (Map<String, Object>) values.get("imports");
		
			if(imports != null){
				for(String key : imports.keySet()){
					importContext.put(key, String.valueOf(imports.get(key)));
				}
			}
			
			for(String key : values.keySet()){
				if(!"imports".equals(key)){
					decode(thing, key, values.get(key), importContext, lastModifyed, false);
					break;
				}
			}
		}catch(Exception e){
			throw new XMetaException("decode json thing error", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void decode(Thing thing, String thingName, Object value, Map<String, String> imports, long lastModifyed, boolean decodeIndex){
		//解析描述者
		Map<String, Object> attributes = thing.getAttributes();
		if(!(value instanceof Map)){
			thing.put(thingName, value);
			return;
		}
		
		String descriptors = null;
		Map<String, Object> values = (Map<String, Object>) value;
		String descs = (String) values.get("descriptors");
		if(descs == null){
			descriptors = importToNormalString(thingName, imports);
		}else{
			descriptors = importToNormalString(thingName + "," + descs, imports);
		}
		attributes.put("descriptors", descriptors);
		
		String exts = (String) values.get("extends");
		if(exts != null){
			attributes.put("extends", importToNormalString(exts, imports));
		}
		
		//解析其他属性和子事物
		for(String key : values.keySet()){
			if(!"descriptors".equals(key) && !"extends".equals(key)){
				if("xid".equals(key)){
					thing.getMetadata().setId((String) values.get(key));
				}else{
					Object v = values.get(key);
					if(v instanceof Map){
						if(!decodeIndex){
							Thing child = new Thing();
							decode(child, key, v, imports, lastModifyed, decodeIndex);
							thing.addChild(child);
						}
					}else{
						attributes.put(key, v);
					}
				}
			}
		}
		
		thing.initDefaultValue();
		thing.getMetadata().setLastModified(lastModifyed);
	}

	public String importToNormalString(String str, Map<String, String> imports){
		String strs = null;
		if(str == null){
			return strs;
		}
		
		for(String s : str.split("[,]")){
			int index = s.indexOf(":");
			if(index != -1){
				String key = s.substring(0, index);
				String subPath = s.substring(index, s.length());
				String imp = imports.get(key);
				if(imp == null){
					if("thing".equals(imp)){
						imp ="MetaThing";
					}
				}
					
				s = imp + subPath;
			}else{
				String imp = imports.get(s);
				if(imp != null){
					s = imp;
				}
			}
			
			if(strs == null){
				strs = s;
			}else{
				strs = strs + "," + s;
			}
		}
		
		return strs;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void decodeIndex(ThingIndex thingIndex, InputStream in,
			long lastModifyed) {
		try{
			Map<String, Object> values = mapper.readValue(in, Map.class);
			
			Map<String, String> importContext = new HashMap<String, String>();
			Map<String, Object> imports = (Map<String, Object>) values.get("imports");
		
			if(imports != null){
				for(String key : imports.keySet()){
					importContext.put(key, String.valueOf(imports.get(key)));
				}
			}
			
			Thing thing = new Thing();
			for(String key : values.keySet()){
				if(!"imports".equals(key)){
					decode(thing, key, values.get(key), importContext, lastModifyed, false);
					break;
				}
			}
			
			thingIndex.name = thing.getMetadata().getName();
			thingIndex.description = thing.getString("description");
			thingIndex.descriptors = thing.getString("descriptors");
			thingIndex.extendsStr = thing.getString("extends");
			thingIndex.label = thing.getString("label");
			thingIndex.lastModified = lastModifyed;
		}catch(Exception e){
			throw new XMetaException("decode json thing error", e);
		}
	}

	@Override
	public String getType() {
		return codeType;
	}

	@Override
	public boolean acceptType(String type) {
		return codeType.equals(type);
	}

}
