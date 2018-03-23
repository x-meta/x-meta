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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xmeta.Thing;
import org.xmeta.ThingMetadata;
import org.xmeta.util.UtilString;

public class TxtCoder {
	public static final char TYPE_LASTMODIFIED = '^';
	
	public static final char TYPE_NODE = '@';
	
	public static final char TYPE_STRING = 's';
	
	public static final char TYPE_STRINGS = 'S';

	public static final char TYPE_BIGDECIMAL = 'D';

	public static final char TYPE_BIGINTEGER = 'I';

	public static final char TYPE_BOOLEAN = 'L';

	public static final char TYPE_BYTE = 'b';

	public static final char TYPE_BYTES = 'B';

	public static final char TYPE_INT = 'i';

	public static final char TYPE_DOUBLE = 'd';

	public static final char TYPE_FLOAT = 'f';

	public static final char TYPE_CHAR = 'c';

	public static final char TYPE_SHORT = 'h';

	public static final char TYPE_OBJECT = 'o';

	public static final char TYPE_DATE = 't';

	public static final char TYPE_LONG = 'l';

	public static final String STRING_ENCODING = "UTF-8";
	
	private static final String STRING_TAG = "#$@text#$@";

	/**
	 * 编码事物。
	 * 
	 * @param thing
	 * @param out
	 * @param context
	 * @throws IOException
	 */
	public static void encode(Thing thing, PrintWriter out,	Map<Thing, String> context) throws IOException {
		encode(thing, out, context, "");
	}
	/**
	 * 编码。
	 * 
	 * @param thing 事物
	 * @param out 输出流writer
	 * @param context 上下文
	 * @param ident 索引
	 * @throws IOException 异常
	 */
	private static void encode(Thing thing, PrintWriter out, Map<Thing, String> context, String ident) throws IOException {
		if (context == null) {
			context = new HashMap<Thing, String>();
		}

		if (context.get(thing) != null) {
			return;
		} else {
			context.put(thing, "aaa");
		}

		//版本和数据头
		ThingMetadata meta = thing.getMetadata();
		
		if(thing.getParent() == null){
			//最后修改时间
			out.print(ident);
			out.println(TxtCoder.TYPE_LASTMODIFIED + String.valueOf(meta.getLastModified()));
		}
		
		//路径
		String rootPath = thing.getRoot().getMetadata().getPath();
		String path = meta.getPath();
		out.print(ident);
		out.println(TYPE_NODE + path.substring(rootPath.length(), path.length()));
		//最后修改时间
		//out.println(meta.getLastModified());
		//标识
		//out.println(meta.getId());
		
		//输出属性
		List<Thing> fields = thing.getAllAttributesDescriptors();
		if (thing.getString("name") == null
				|| "".equals(thing.getString("name"))) {
			thing.put("name", thing.getMetadata().getName());
		}

		//标识和描述者是必须要保存的
		boolean idSetted = false;
		boolean descriptorsSetted = false;
		boolean nameSetted = false;
		boolean labelSetted = false;
		//避免属性重复
		Map<String, String> atrContext = new HashMap<String, String>();
		for (Iterator<Thing> iter = fields.iterator(); iter.hasNext();) {
			Thing field = iter.next();
			//String defaultValue = field.getString("default");
			String name = field.getMetadata().getName();
			if(atrContext.get(name) != null){
				continue;
			}else{
				atrContext.put(name, name);
			}
			String type = field.getString("type");
			/** 去掉了过滤空值的代码，文件尺寸减少的不过，并可能造成异常 */
			if (name.equals("url")) {
				//System.out.println("dd");
			}
			if (!idSetted && "id".equals(name)) {
				idSetted = true;
			}
			if (!descriptorsSetted && "descriptors".equals(name)) {
				descriptorsSetted = true;
			}
			if (!nameSetted && "name".equals(name)) {
				nameSetted = true;
			}
			if (!labelSetted && "label".equals(name)) {
				labelSetted = true;
			}

			if ("int".equals(type)) {
				try {
					int value = thing.getInt(name);
					//if(value != null && !("" + value).equals(anObject)

					encodeName(out, name, TYPE_INT, ident);
					out.print(ident);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("long".equals(type)) {
				try {
					long value = thing.getLong(name);
					//if(value != null && !("" + value).equals(anObject)

					encodeName(out, name, TYPE_LONG, ident);
					out.print(ident);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("double".equals(type)) {
				try {
					double value = thing.getDouble(name);

					encodeName(out, name, TYPE_DOUBLE, ident);
					out.print(ident);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("float".equals(type)) {
				try {
					float value = thing.getFloat(name);

					encodeName(out, name, TYPE_FLOAT, ident);
					out.print(ident);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("bigDecimal".equals(type)) {
				try {
					BigDecimal value = thing.getBigDecimal(name);
					if (value == null)
						continue;

					encodeName(out, name, TYPE_BIGDECIMAL, ident);
					out.print(ident);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("bigInteger".equals(type)) {
				try {
					BigInteger value = thing.getBigInteger(name);
					if (value == null)
						continue;

					encodeName(out, name, TYPE_BIGINTEGER, ident);
					out.print(ident);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("boolean".equals(type)) {
				try {
					boolean value = thing.getBoolean(name);

					encodeName(out, name, TYPE_BOOLEAN, ident);
					out.print(ident);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("byte".equals(type)) {
				try {
					byte value = thing.getByte(name);

					encodeName(out, name, TYPE_BYTE, ident);
					out.print(ident);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("bytes".equals(type)) {
				try {
					byte[] value = thing.getBytes(name);

					encodeName(out, name, TYPE_BYTES, ident);
					if(value != null){
						out.print(ident);
						out.println(UtilString.toHexString(value));
					}else{
						out.println();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("char".equals(type)) {
				try {
					char value = thing.getChar(name);

					encodeName(out, name, TYPE_CHAR, ident);
					out.print(ident);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("short".equals(type)) {
				try {
					short value = thing.getShort(name);

					encodeName(out, name, TYPE_SHORT, ident);
					out.print(ident);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("date".equals(type) || "datetime".equals(type) || "time".equals(type)) {
				try {
					Date value = thing.getDate(name);
					if (value == null)
						continue;

					long dvalue = value.getTime();
					encodeName(out, name, TYPE_DATE, ident);
					out.print(ident);
					out.println(dvalue);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("object".equals(type)) {
				Object value = thing.getAttribute(name);
				if (value == null)
					continue;

				if (value instanceof Serializable) {
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					ObjectOutputStream oout;
					try {
						oout = new ObjectOutputStream(bout);
						oout.writeObject(value);
						oout.flush();

						byte[] bs = bout.toByteArray();
						encodeName(out, name, TYPE_OBJECT, ident);
						out.print(ident);
						out.println(UtilString.toHexString(bs));						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				//默认都当作字符串来保存
				String value = thing.getString(name);
				if (value != null) {// && (meta.isIncludeDefaultValue() || !value.equals(defaultValue))){
					encodeString(name, value, out, ident);
				}
			}
		}

		if (!idSetted) {
			encodeString("id", thing.getMetadata().getId(), out, ident);
		}
		if (!descriptorsSetted) {
			encodeString("descriptors", thing.getString("descriptors"), out, ident);
		}
		if (!nameSetted) {
			encodeString("name", thing.getMetadata().getName(), out, ident);
		}
		if (!labelSetted) {
			encodeString("label", thing.getMetadata().getLabel(), out, ident);
		}		
		
		//编码子节点
		for(Thing child : thing.getChilds()){
			if(child.getParent() != thing){
				//如果是在程序里软添加的，不保存
				continue;
			}
			
			TxtCoder.encode(child, out, context, ident + "  ");
		}
		
		out.flush();
	}


	protected static void encodeName(PrintWriter out, String name, char type, String ident)	
			throws IOException {
		out.print(ident);
		out.print(type);
		out.println(name);
	}

	protected static void encodeString(String name, String value, PrintWriter out, String ident) throws IOException {
		if (value == null || "".equals(value)) {
			return;
		}
		
		if(value.indexOf("\n") != -1){
			//多行
			encodeName(out, name, TYPE_STRINGS, ident);
			out.println(STRING_TAG); //文本起始符
			ByteArrayInputStream bin = new ByteArrayInputStream(value.getBytes());
			BufferedReader br = new BufferedReader(new InputStreamReader(bin));
			String line = null;
			while((line = br.readLine()) != null){
				if(line.length() == STRING_TAG.length() && line.equals(STRING_TAG)){
					line = "\\" + line;
				}
				out.println(line);
			}
			br.close();
			bin.close();
			out.println(STRING_TAG); //文本结束符
		}else{
			//单行
			encodeName(out, name, TYPE_STRING, ident);
			out.print(ident);
			out.println(value);
		}
	}
	
	private static String getId(String path){
		int index = path.lastIndexOf("@");
		if(index != -1){
			return path.substring(index + 1, path.length());
		}
		
		index = path.lastIndexOf(".");
		if(index != -1){
			return path.substring(index + 1, path.length());
		}
		
		return path;
	}

	/**
	 * 解码。
	 * 
	 * @param thing 事物
	 * @param input 输入流
	 * @param full 是否全部导入
	 * @param lastModified 最后修改日期
	 * @return 事物
	 * @throws IOException 异常如果发生
	 */
	public static Thing decode(Thing thing, InputStream input, boolean full, long lastModified)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(input, STRING_ENCODING));
		
		String line = null;
		String path = br.readLine().trim();
		if(path.charAt(0) == 65279){ //去掉FF FE两个字节，如果存在
			path = path.substring(1, path.length());
		}
		if(TYPE_LASTMODIFIED == path.charAt(0)){
			//修改时间, 2012-03-12日加入
			lastModified = Long.parseLong(path.substring(1, path.length()));
			
			//其次才是路径
			path = br.readLine().trim();
		}
		
		if(path != null){
			Thing current = thing;
			Map<String, Object> attributes = current.getAttributes();
			//第一个是路径
			path = path.substring(1, path.length());
			if(path != null && !"".equals(path)){
				thing.getMetadata().setPath(path);
			}
			thing.getMetadata().setLastModified(lastModified);
			
			//最后修改日期
			//current.getMetadata().setLastModified(Long.parseLong(br.readLine()));
			//标识
			current.getMetadata().setId(getId(path));
			
			while((line = br.readLine()) != null){
				line = line.trim();
				//属性或子节点
				if(line.length() == 0){
					continue;
				}
				
				char type = line.charAt(0);
				String name = line.substring(1, line.length()).intern();
				if(type == TxtCoder.TYPE_NODE){
					if(!full){
						break;
					}
					current = getPathParent(current, name);
					if(current != null){
						Thing childThing = new Thing(null, null, null, false);
						ThingMetadata childMetadata = childThing.getMetadata();
						childMetadata.setPath(name);
						childMetadata.setLastModified(lastModified);
						//最后修改日期
						//childThing.getMetadata().setLastModified(Long.parseLong(br.readLine()));
						//标识
						childMetadata.setId(getId(name));
						current.addChild(childThing);
						
						current = childThing;
						attributes = current.getAttributes();
					}
				}else{
					//其他都是属性
					if (type == TYPE_INT) {						
						//try {
							attributes.put(name, Integer.parseInt(br.readLine().trim()));
						//} catch (Exception e) {
						//	e.printStackTrace();
						//}
					} else if (type == TYPE_LONG) {
						//try {
							attributes.put(name, Long.parseLong(br.readLine().trim()));
						//} catch (Exception e) {
						//	e.printStackTrace();
						//}
					} else if (type == TYPE_DOUBLE) {
						//try {
							attributes.put(name, Double.parseDouble(br.readLine().trim()));
						//} catch (Exception e) {
						//	e.printStackTrace();
						//}
					} else if (type == TYPE_FLOAT) {
						//try {
							attributes.put(name, Float.parseFloat(br.readLine().trim()));
						//} catch (Exception e) {
						//	e.printStackTrace();
						//}
					} else if (type == TxtCoder.TYPE_BIGDECIMAL) {
						//try {
							attributes.put(name, new BigDecimal(br.readLine().trim()));
						//} catch (Exception e) {
						//	e.printStackTrace();
						//}
					} else if (type == TxtCoder.TYPE_BIGINTEGER) {
						//try {
							attributes.put(name, new BigInteger(br.readLine().trim()));
						//} catch (Exception e) {
						//	e.printStackTrace();
						//}
					} else if (type == TxtCoder.TYPE_BOOLEAN) {
						//try {
							attributes.put(name, Boolean.parseBoolean(br.readLine().trim()));
						//} catch (Exception e) {
						//	e.printStackTrace();
						//}
					} else if (type == TYPE_BYTE) {
						//try {
							attributes.put(name, Byte.parseByte(br.readLine().trim()));
						//} catch (Exception e) {
						//	e.printStackTrace();
						//}
					} else if (type == TYPE_BYTES) {
						//try {
							byte[] value = UtilString.hexStringToByteArray(br.readLine().trim());
							attributes.put(name, value);
						//} catch (Exception e) {
						//	e.printStackTrace();
						//}
					} else if (type == TYPE_CHAR) {
						//try {
							attributes.put(name, (char) Integer.parseInt(br.readLine().trim()));
						//} catch (Exception e) {
						//	e.printStackTrace();
						//}
					} else if (type == TYPE_SHORT) {
						//try {
							attributes.put(name, Short.parseShort(br.readLine().trim()));
						//} catch (Exception e) {
						//	e.printStackTrace();
						//}
					} else if (type == TYPE_DATE) {
						//try {
							Date value = new Date(Long.parseLong(br.readLine().trim()));
							attributes.put(name, value);
						//} catch (Exception e) {
						//	e.printStackTrace();
						//}
					} else if (type == TYPE_OBJECT) {
						String hexstring = br.readLine();
						if(hexstring == null || "".equals(hexstring.trim().trim())){
							
						}else{
							try {
								byte[] bytes = UtilString.hexStringToByteArray(hexstring);
								ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
								ObjectInputStream oin = new ObjectInputStream(bin);
								Object value = oin.readObject();
								attributes.put(name, value);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}						
					} else if (type == TYPE_STRINGS){
						//tag
						br.readLine();
						String l = null;
						String value = null;
						while((l = br.readLine()) != null){
							if(l.length() == 1 && '\r' == l.charAt(0)){
								continue;
							}
							
							if(l.length() == STRING_TAG.length() && l.equals(STRING_TAG)){
								//字符串结束
								break;
							}else if(l.length() == STRING_TAG.length() + 1 && l.equals("\\" + STRING_TAG)){
								//转义
								if(value != null){
									value = value + "\n" + l.substring(1, l.length());
								}else{
									value = l.substring(1, l.length());
								}
							}else{
								if(value != null){
									value = value + "\n" + l;
								}else{
									value = l;
								}
							}
						}
						attributes.put(name, value);
					} else if(type == TYPE_STRING){
						attributes.put(name, br.readLine().trim());
					}
				}
			}
		}
		return thing;
	}
	
	private static Thing getPathParent(Thing current, String path){
		String currentPath = current.getMetadata().getPath();
		if(path == null || "".equals(path)){
			path = current.getRoot().getMetadata().getPath();
		}else if(path.startsWith("/@")){
			path = current.getRoot().getMetadata().getPath() + path; 
		}
		if(path.startsWith(currentPath + "/")){
			String subpath = path.substring(currentPath.length(), path.length());
			//父应该是直接的父，所以最后的/一定是第一个字母
			if(subpath.lastIndexOf("/") == 0){
				return current;
			}
		}
		
		current = current.getParent();
		if(current == null){
			return null;
		}
		return getPathParent(current, path);
	}
}