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
	 * 编码。
	 * 
	 * @param thing
	 * @param out
	 * @param context
	 * @throws IOException
	 */
	public static void encode(Thing thing, PrintWriter out,
			Map<Thing, String> context) throws IOException {
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
		//路径
		out.println(TYPE_NODE + meta.getPath());
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

					encodeName(out, name, TYPE_INT);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("long".equals(type)) {
				try {
					long value = thing.getLong(name);
					//if(value != null && !("" + value).equals(anObject)

					encodeName(out, name, TYPE_LONG);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("double".equals(type)) {
				try {
					double value = thing.getDouble(name);

					encodeName(out, name, TYPE_DOUBLE);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("float".equals(type)) {
				try {
					float value = thing.getFloat(name);

					encodeName(out, name, TYPE_FLOAT);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("bigDecimal".equals(type)) {
				try {
					BigDecimal value = thing.getBigDecimal(name);
					if (value == null)
						continue;

					encodeName(out, name, TYPE_BIGDECIMAL);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("bigInteger".equals(type)) {
				try {
					BigInteger value = thing.getBigInteger(name);
					if (value == null)
						continue;

					encodeName(out, name, TYPE_BIGINTEGER);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("boolean".equals(type)) {
				try {
					boolean value = thing.getBoolean(name);

					encodeName(out, name, TYPE_BOOLEAN);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("byte".equals(type)) {
				try {
					byte value = thing.getByte(name);

					encodeName(out, name, TYPE_BYTE);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("bytes".equals(type)) {
				try {
					byte[] value = thing.getBytes(name);

					encodeName(out, name, TYPE_BYTES);
					if(value != null){
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

					encodeName(out, name, TYPE_CHAR);
					out.println(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("short".equals(type)) {
				try {
					short value = thing.getShort(name);

					encodeName(out, name, TYPE_SHORT);
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
					encodeName(out, name, TYPE_DATE);
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
						encodeName(out, name, TYPE_OBJECT);
						out.println(UtilString.toHexString(bs));						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				//默认都当作字符串来保存
				String value = thing.getString(name);
				if (value != null) {// && (meta.isIncludeDefaultValue() || !value.equals(defaultValue))){
					encodeString(name, value, out);
				}
			}
		}

		if (!idSetted) {
			encodeString("id", thing.getMetadata().getId(), out);
		}
		if (!descriptorsSetted) {
			encodeString("descriptors", thing.getString("descriptors"), out);
		}
		if (!nameSetted) {
			encodeString("name", thing.getMetadata().getName(), out);
		}
		if (!labelSetted) {
			encodeString("label", thing.getMetadata().getLabel(), out);
		}		
		
		//编码子节点
		for(Thing child : thing.getChilds()){
			TxtCoder.encode(child, out, context);
		}
	}


	public static void encodeName(PrintWriter out, String name, char type)
			throws IOException {
		out.print(type);
		out.println(name);
	}

	public static void encodeString(String name, String value, PrintWriter out) throws IOException {
		if (value == null || "".equals(value)) {
			return;
		}
		
		if(value.indexOf("\n") != -1){
			//多行
			encodeName(out, name, TYPE_STRINGS);
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
			encodeName(out, name, TYPE_STRING);
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
	 * @param thing
	 * @param input
	 * @param full
	 * @return
	 * @throws IOException
	 */
	public static Thing decode(Thing thing, InputStream input, boolean full)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(input, STRING_ENCODING));
		
		String line = null;
		String path = br.readLine();
		if(path != null){
			Thing current = thing;
			Map<String, Object> attributes = current.getAttributes();
			//第一个是路径
			path = path.substring(1, path.length());
			thing.getMetadata().setPath(path);
			//最后修改日期
			//current.getMetadata().setLastModified(Long.parseLong(br.readLine()));
			//标识
			current.getMetadata().setId(getId(path));
			
			while((line = br.readLine()) != null){
				//属性或子节点
				char type = line.charAt(0);
				String name = line.substring(1, line.length());
				if(type == TxtCoder.TYPE_NODE){
					if(!full){
						break;
					}
					current = getPathParent(current, name);
					if(current != null){
						Thing childThing = new Thing(null, null, null, false);
						childThing.getMetadata().setPath(name);
						//最后修改日期
						//childThing.getMetadata().setLastModified(Long.parseLong(br.readLine()));
						//标识
						childThing.getMetadata().setId(getId(name));
						current.addChild(childThing);
						
						current = childThing;
						attributes = current.getAttributes();
					}
				}else{
					//其他都是属性
					if (type == TYPE_INT) {						
						try {
							attributes.put(name, Integer.parseInt(br.readLine()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (type == TYPE_LONG) {
						try {
							attributes.put(name, Long.parseLong(br.readLine()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (type == TYPE_DOUBLE) {
						try {
							attributes.put(name, Double.parseDouble(br.readLine()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (type == TYPE_FLOAT) {
						try {
							attributes.put(name, Float.parseFloat(br.readLine()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (type == TxtCoder.TYPE_BIGDECIMAL) {
						try {
							attributes.put(name, new BigDecimal(br.readLine()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (type == TxtCoder.TYPE_BIGINTEGER) {
						try {
							attributes.put(name, new BigInteger(br.readLine()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (type == TxtCoder.TYPE_BOOLEAN) {
						try {
							attributes.put(name, Boolean.parseBoolean(br.readLine()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (type == TYPE_BYTE) {
						try {
							attributes.put(name, Byte.parseByte(br.readLine()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (type == TYPE_BYTES) {
						try {
							byte[] value = UtilString.hexStringToByteArray(br.readLine());
							attributes.put(name, value);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (type == TYPE_CHAR) {
						try {
							attributes.put(name, (char) Integer.parseInt(br.readLine()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (type == TYPE_SHORT) {
						try {
							attributes.put(name, Short.parseShort(br.readLine()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (type == TYPE_DATE) {
						try {
							Date value = new Date(Long.parseLong(br.readLine()));
							attributes.put(name, value);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (type == TYPE_OBJECT) {
						String hexstring = br.readLine();
						if(hexstring == null || "".equals(hexstring.trim())){
							
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
						attributes.put(name, br.readLine());
					}
				}
			}
		}
		return thing;
	}
	
	private static Thing getPathParent(Thing current, String path){
		String currentPath = current.getMetadata().getPath();
		if(path.startsWith(currentPath + "/")){
			String subpath = path.substring(currentPath.length(), path.length());
			//父应该是直接的父，所以最后的/一定是第一个字母
			if(subpath.lastIndexOf("/") == 0){
				return current;
			}
		}
		
		current = current.getParent();
		return getPathParent(current, path);
	}
}