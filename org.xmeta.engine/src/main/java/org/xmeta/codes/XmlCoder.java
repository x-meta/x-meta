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
package org.xmeta.codes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xmeta.Thing;
import org.xmeta.ThingCoderException;
import org.xmeta.util.UtilData;
import org.xml.sax.SAXException;

/**
 * <p>XML格式的事物编码，XML格式的事物不保存修改日期等。</p>
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class XmlCoder {
	public static final String NODE_NAME = "__node__name__";
	public static final String NEW_LINE = "\r\n";
	/**
	 * 把事物编码成XML字符串。
	 * 
	 * @param thing 事物
	 * @return XML代码
	 */
	public static String encodeToString(Thing thing){
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try{
			encode(thing, bout);
			return new String(bout.toByteArray());
		}catch(Exception e){
			System.out.println(bout.toString());
			throw new ThingCoderException(e);
		}finally{
			try {
				bout.close();
			} catch (IOException e) {
				throw new ThingCoderException(e);
			}
		}
	}
	
	/**
	 * 把指定的事物以XML编码到输出流中。
	 * 
	 * @param thing 事物
	 * @param out 输出流
	 * @throws XMLStreamException XML异常
	 * @throws IOException  IO异常
	 */
	public static void encode(Thing thing, OutputStream out) throws XMLStreamException, IOException{
		//element.a
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(out, "utf-8");
		//OutputKeys.
		try{			
			writer.writeStartDocument("utf-8", "1.0");
			writer.writeCharacters(NEW_LINE);
			encode(thing, null, writer, "", false, out);
			writer.writeEndDocument();
		}finally{
			writer.close();
		}
	}
	
	/**
	 * 把事物编码成XML字符串。
	 * 
	 * @param thing 事物
	 * @param includeDefaultValue 是否包含默认值
	 * @return XML字符串
	 */
	public static String encodeToString(Thing thing,  boolean includeDefaultValue){
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try{
			encode(thing, bout, includeDefaultValue);
			return new String(bout.toByteArray());
		}catch(Exception e){
			throw new ThingCoderException(e);
		}finally{
			try {
				bout.close();
			} catch (IOException e) {
				throw new ThingCoderException(e);
			}
		}
	}
	
	/**
	 * 把指定的事物以XML编码到输出流中。
	 * 
	 * @param thing 事物
	 * @param out 输出流
	 * @param includeDefaultValue 是否包含默认值
	 * @throws XMLStreamException XML异常
	 * @throws IOException  IO异常
	 */
	public static void encode(Thing thing, OutputStream out,  boolean includeDefaultValue) throws XMLStreamException, IOException{
		//element.a
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(out, "utf-8");
		try{			
			writer.writeStartDocument("utf-8", "1.0");
			writer.writeCharacters(NEW_LINE);
			encode(thing, null, writer, "", includeDefaultValue, out);
			writer.writeEndDocument();
		}finally{
			writer.close();
		}
	}
	
	/**
	 * <p>编码事物到XML。</p>
	 * 
	 * 由于每个节点都有descriptors属性，使用XML编写比较麻烦，因此简化子节点的描述者：<br></br>
	 * 1. 如果描述者只有一个，且描述者是父节点的第一个描述者的事物子节点，那么节点名是描述者的名字。
	 * 
	 * @param thing 事物
	 * @param descriptor 描述者
	 * @param writer XML写入
	 * @param ident 偏移
	 * @param includeDefaultValue 是否包含默认值，现在改属性无效，总是不保存默认值
	 * @throws XMLStreamException XML异常
	 * @throws IOException IO异常
	 */
	private static void encode(Thing thing, Thing parentDescriptors, XMLStreamWriter writer, String ident, boolean includeDefaultValue, OutputStream out) throws XMLStreamException, IOException{
		writer.writeCharacters(NEW_LINE + ident);
				
		//属性缓存，避免写入重复的属性
		Map<String, String> attrContext = new HashMap<String, String>();
		//节点名
		String thingName = thing.getThingName();
		writer.writeStartElement(thingName);				
		
		int lineStartIndex = ident.length() + 4;
		int lineWidth = lineStartIndex;
				
		//name和id
		String name = thing.getMetadata().getName(); //总是先写入name属性
		if(!name.equals(thingName) || thing.getChilds().size() == 0){ // 叶子节点必须要有一个name属性，否则会被解析为上一个节点的属性
			writer.writeAttribute("name", name);
			lineWidth = lineWidth + name.length() + 8;
		}
		attrContext.put("name", name);
		String id = thing.getMetadata().getId(); //写入meta中的id属性 
		if(includeDefaultValue== false && id != null && !"".equals(id) && !id.equals(name)){ //如果id和name不同，写入
			writer.writeAttribute("_xmeta_id_", id);
			lineWidth = lineWidth + id.length() + 15;
		}
		
		//描述者，descriptors
		boolean writeDescriptor = true;
		String descriptors = thing.getString("descriptors");
		Thing descriptor = thing.getDescriptor();
		//如果描述者只有一个且是父描述者的子节点，那么不用写入描述者
		if(parentDescriptors != null && descriptors != null && descriptors.split("[,]").length == 1){
			for(Thing parentDescriptor : parentDescriptors.getAllChilds("thing")){
				if(parentDescriptor.getMetadata().getName().equals(thingName)){
					if(descriptor.getMetadata().getPath().equals(parentDescriptor.getMetadata().getPath())){
						writeDescriptor = false;
						break;
					}else{
						break;
					}
				}
			}
		}
		
		if(writeDescriptor && descriptors != null){
			writer.writeAttribute("descriptors", descriptors);		
			lineWidth = lineWidth + descriptors.length() + 15;
		}
		attrContext.put("descriptors", descriptors);
		
		//其他属性
		List<Thing> attributes = thing.getAllAttributesDescriptors();
		List<Thing> cDataAttributes = new ArrayList<Thing>();
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		
		for(Thing attribute : attributes){
			String attrname = attribute.getMetadata().getName();
			if(attrContext.get(attrname) != null){
				//过滤重复的属性
				continue;
			}
			attrContext.put(attrname, attrname);			
			
			Object value = thing.getAttribute(attrname);
			String defaultValue = attribute.getStringBlankAsNull("default");
			
			//如果没有默认值，且值为空或null，可以不保存
			if(defaultValue == null && (value == null || "".equals(value))){
				continue;
			}
			
			/*
			if(attrname.equals("inheritDescription")){
				System.out.println();
			}*/
			
			//如果默认为不为null，且值和默认值不一样，可以不保存
			if(defaultValue != null && CoderUtils.isDefault(attribute, value == null ? null : value.toString())){
				continue;
			}
			
			if(value == null){
				value = "";
			}
			if(includeDefaultValue){
				defaultValue = null;  //现在XmlCode不再保存默认值了
			}
			
			if("label".equals(attrname) && name.equals(value)){
				//如果标签和label相同，也可以省略
				continue;
			}
			
			boolean isCdata = false;
			String type = attribute.getString("type");
			String strValue = null;
			if("__PCDATA__".equals(attrname) || "__CDATA__".equals(attrname)){
				cDataAttributes.add(attribute);
				isCdata = true;
			}else{
				if ("int".equals(type)) {
					strValue = String.valueOf(value);
				} else if ("long".equals(type)) {
					strValue = String.valueOf(value);
				} else if ("double".equals(type)) {
					strValue = String.valueOf(value);
				} else if ("float".equals(type)) {
					strValue = String.valueOf(value);
				} else if ("bigDecimal".equals(type)) {
					strValue = String.valueOf(value);
				} else if ("bigInteger".equals(type)) {
					strValue = String.valueOf(value);
				} else if ("boolean".equals(type)) {
					strValue = String.valueOf(value);
				} else if ("byte".equals(type)) {
					strValue = String.valueOf(value);
				} else if ("bytes".equals(type)) {
					byte[] bytes = thing.getBytes(attrname);
					strValue = UtilData.bytesToHexString(bytes);
				} else if ("char".equals(type)) {
					strValue = String.valueOf(value);
				} else if ("short".equals(type)) {
					strValue = String.valueOf(value);
				} else if ("date".equals(type)) {
					Date date = thing.getDate(attrname);
					strValue = dateFormater.format(date);					
				} else if ("object".equals(type)) {
					if (value instanceof Serializable) {
										ByteArrayOutputStream bout = new ByteArrayOutputStream();
						ObjectOutputStream oout = new ObjectOutputStream(bout);
						oout.writeObject(value);
						oout.flush();
						byte[] bs = bout.toByteArray();
						strValue = UtilData.bytesToHexString(bs);
					}
				} else {
					//默认都当作字符串来保存
					strValue = String.valueOf(value);
					//String str = thing.getString(attrname);
					if(strValue.indexOf("\n") != -1){
						cDataAttributes.add(attribute);
						isCdata = true;
					}			
				}
			}
			
			if(!isCdata){//cdata另外保存	
				if(strValue != null){				
					if(includeDefaultValue || !CoderUtils.isDefault(attribute, strValue)){
						//处理行的最大宽度
						if(lineWidth > 80){
							writer.flush();
							out.write(NEW_LINE.getBytes());
							out.write((ident + "    ").getBytes());
							//writer.writeCharacters("\n" + "    ");
							lineWidth = lineStartIndex;
						}
						
						//XML不保存和默认值相同的值
						//System.out.println(attrname + "=" + strValue);
						writer.writeAttribute(attrname, strValue);
						lineWidth = lineWidth + attrname.length() + strValue.length() + 3;
					}
				}else if(defaultValue != null && !"".equals(defaultValue)){
					//处理行的最大宽度
					if(lineWidth > 80){
						writer.flush();
						out.write(NEW_LINE.getBytes());
						out.write((ident + "    ").getBytes());
						//writer.writeCharacters("\n" + "    ");
						lineWidth = lineStartIndex;
					}
					
					//如果默认值不为空但是属性值为空，写入一个空字符串，避免读取xml时重赋值为默认值
					writer.writeAttribute(attrname, "");
					lineWidth = lineWidth + attrname.length() + 3;
				}
			}
		}
		
		//写入schema的引用，如果存在话
		String xmlns = thing.getStringBlankAsNull("xmlns");
		String xmlns_xsi = thing.getStringBlankAsNull("xmlns:xsi");
		String schemaLocation = thing.getStringBlankAsNull("xsi:schemaLocation");
		if(xmlns != null){
			writer.writeAttribute("xmlns", xmlns);
		}
		if(xmlns_xsi != null){
			writer.writeAttribute("xmlns:xsi", xmlns_xsi);
		}
		if(schemaLocation != null){
			writer.writeAttribute("xsi:schemaLocation", schemaLocation);
		}
		
		for(Thing attribute : cDataAttributes){
			String attrname = attribute.getMetadata().getName();
			String str = thing.getString(attrname);
			writer.writeCharacters(NEW_LINE + ident + "    ");
			writer.writeStartElement(attrname);
			writer.writeCData(str);
			writer.writeEndElement();
		}
		
		//子节点
		for(Thing child : thing.getChilds()){
			encode(child, descriptor, writer, ident + "    ", includeDefaultValue, out);
		}
		
		//节点结束
		if(cDataAttributes.size() > 0 || thing.getChilds().size() > 0){
			writer.writeCharacters(NEW_LINE + ident);
		}
		
		writer.writeEndElement();
	}

	
	/**
	 * 分析XML字符串并返回事物。
	 * 
	 * @param thing 事物
	 * @param content 内容 
	 * @throws ParserConfigurationException  分析异常
	 * @throws IOException  IO异常
	 * @throws SAXException  XML异常
	 */
	public static void parse(Thing thing, String content) throws ParserConfigurationException, SAXException, IOException{
		if(content == null){
			return;
		}
		
		ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes("UTF-8"));
		Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();     
        document = builder.parse(bis);
        
        Element root = document.getDocumentElement();
        parse(thing, null, root, System.currentTimeMillis());
        
        setLastModified(thing, System.currentTimeMillis());
	}
	
	/**
	 * 从输入流中读取事物。
	 * 
	 * @param thing 事物
	 * @param input 输入流
	 * @throws SAXException SAX异常
	 * @throws IOException IO异常
	 * @throws ParserConfigurationException 分析异常
	 */
	public static void parse(Thing thing, InputStream input) throws SAXException, IOException, ParserConfigurationException{
		Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();     
        document = builder.parse(input);
        
        Element root = document.getDocumentElement();
        parse(thing, null, root, System.currentTimeMillis());
        
        setLastModified(thing, System.currentTimeMillis());
	}
	
	/**
	 * 分析XML元素数据到事物中。
	 * 
	 * @param thing 事物
	 * @param element 元素
	 * @param parentDescriptor 父描述者
	 * @param lastModifyed 最后修改日期
	 */
	public static void parse(Thing thing, Thing parentDescriptor, Element element, long lastModifyed){
		Map<String, Object> attributes = thing.getAttributes();
		
		//先设置描述
		String thingName = element.getNodeName();
		String descriptors = element.getAttribute("descriptors");
		if(descriptors != null && !"".equals(descriptors.trim())){
			attributes.put("descriptors", descriptors);
			thing.initDefaultValue();
		}else if(parentDescriptor != null){
			//通过节点名确定描述者，父描述不为空的情况下			
			for(Thing descriptor : parentDescriptor.getAllChilds("thing")){
				if(thingName.equals(descriptor.getMetadata().getName())){
					descriptors = descriptor.getMetadata().getPath();
					attributes.put("descriptors", descriptors);
					thing.initDefaultValue();					
					break;
				}
			}
		}
		//如果descriptors为空，默认设置为元事物
		descriptors = (String) attributes.get("descriptors");
		if(descriptors == null || descriptors.equals("")){
			if("actions".equals(thingName)){
				attributes.put("descriptors", "MetaThing/@actions");
			}else if("JavaAction".equals(thingName)){
				attributes.put("descriptors", "MetaThing/@actions/@JavaAction");
			}else{
				attributes.put("descriptors", "MetaThing");
			}
		}
		
		NamedNodeMap nodes = element.getAttributes();
		for(int i=0; i<nodes.getLength(); i++){
			Node attributeNode = nodes.item(i);
			if("descriptors".equals(attributeNode.getNodeName())){
				continue;
			}
			attributes.put(attributeNode.getNodeName(), attributeNode.getNodeValue());
			attributes.put(NODE_NAME, attributeNode.getNodeValue());
		}
		
		//分析子节点
		Node node = element.getFirstChild();
		if(node != null){			
			do{
				if (node.getNodeType() == Node.ELEMENT_NODE ) {					
					if(isAttributeNode(node)){
						//子事物必有name属性，没有的当作是属性
						
						Node childNode = node.getFirstChild();
						if(childNode.getNodeType() == Node.TEXT_NODE || childNode.getNodeType() == Node.CDATA_SECTION_NODE){
							attributes.put(node.getNodeName().intern(), childNode.getNodeValue());
						}
					}else{				
						Thing child = new Thing(null, null, null, false);
						parse(child, thing.getDescriptor(), (Element) node, lastModifyed);
						if(child != null){
							thing.addChild(child);
						}
					}
				}
			}while((node = node.getNextSibling()) != null);
		}
		
		//如果name属性为空，使用thingName
		if(attributes.get("name") == null){
			attributes.put("name", thingName);
		}
		//如果id和name相同，编码会忽略，需要恢复
		if(attributes.get("_xmeta_id_") != null){
			thing.getMetadata().setId(attributes.get("_xmeta_id_").toString());
		}else{
			thing.getMetadata().setId((String) attributes.get("name")); 
		}
		
		//最后修改时间
		thing.getMetadata().setLastModified(lastModifyed);
	}		
	
	/**
	 * 具有换行的字符串可以保存到cdata下，如果节点不包含任何属性，并且子节点只有一个并且是CDATA，那么认为是属性：
	 * &lt;xxx&gt;&lt;![CDATA[]]&gt;&lt;/xxx&gt;
	 * 
	 * @param node 节点
	 * @return 是否是属性
	 */
	public static boolean isAttributeNode(Node node){
		if (node.getNodeType() == Node.ELEMENT_NODE ) {		
			if(((Element) node).getAttributes().getLength() == 0 && node.getChildNodes().getLength() == 1){				
				Node childNode = node.getFirstChild();
				if(childNode.getNodeType() == Node.CDATA_SECTION_NODE || childNode.getNodeType() == Node.TEXT_NODE){					
					return true;
				}
			}
			
		}
		
		return false;		
	}
	

	public static void setLastModified(Thing thing, long lastModified){
		for(Thing child : thing.getChilds()){
			setLastModified(child, lastModified);
		}
		
		thing.getMetadata().setLastModified(lastModified);
		//System.out.println(lastModified + ":" + thing.getMetadata().getPath());
	}
}