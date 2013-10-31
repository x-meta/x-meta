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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xmeta.ActionException;
import org.xmeta.Thing;
import org.xmeta.ThingMetadata;

public class XerCoder {
	public static final byte NODE_START = 120;

	public static final byte NODE_END = 121;

	public static final byte CHILD_NODE = 122;

	public static final byte ATTRIBUTE = 123;

	public static final byte VERSION = 124;

	public static final byte META = 125;

	public static final byte TYPE_STRING = 1;

	public static final byte TYPE_BIGDECIMAL = 2;

	public static final byte TYPE_BIGINTEGER = 3;

	public static final byte TYPE_BOOLEAN = 4;

	public static final byte TYPE_BYTE = 5;

	public static final byte TYPE_BYTES = 6;

	public static final byte TYPE_INT = 7;

	public static final byte TYPE_DOUBLE = 8;

	public static final byte TYPE_FLOAT = 9;

	public static final byte TYPE_CHAR = 10;

	public static final byte TYPE_SHORT = 11;

	public static final byte TYPE_OBJECT = 12;

	public static final byte TYPE_DATE = 13;

	public static final byte TYPE_LONG = 14;

	private static final String STRING_ENCODING = "UTF-8";

	private static final short version = 1;

	public static void encodeString(String str, OutputStream out)
			throws IOException {
		if (str == null) {
			str = "";
		}
		byte[] strBytes = str.getBytes(STRING_ENCODING);
		out.write(encodeInt32(strBytes.length));
		out.write(strBytes);
	}

	public static byte[] encodeInt16(int value) {
		byte[] bs = new byte[2];
		bs[0] = (byte) (value >>> 8 & 0xFF);
		bs[1] = (byte) (value & 0xFF);

		return bs;
	}
	
	/**
	 * 编码事物到输出流中。
	 * 
	 * @param thing
	 * @param out
	 * @param context
	 * @throws IOException
	 */
	public static void encode1(Thing thing, OutputStream out,
			Map<Thing, String> context) throws IOException {
		if (context == null) {
			context = new HashMap<Thing, String>();
		}

		if (context.get(thing) != null) {
			return;
		} else {
			context.put(thing, "aaa");
		}

		//编码Meta
		ThingMetadata meta = thing.getMetadata();
		out.write(META);
		//为保持不同版本也能够读取，Metadata信息有长度，事物本身的信息不保存长度
		ByteArrayOutputStream metaout = new ByteArrayOutputStream();
		//文件版本	
		metaout.write(encodeInt32(version));
		//标识
		encodeString(meta.getId(), metaout);
		//最后修改日期
		metaout.write(encodeLong64(meta.getLastModified()));
		//事物的描述者列表
		encodeString(thing.getString("descriptors", ""), metaout);
		//事物的继承者列表
		encodeString(thing.getString("extends", ""), metaout);
		//Meta信息写入
		byte[] metabytes = metaout.toByteArray();
		out.write(encodeInt32(metabytes.length));
		out.write(metabytes);

		//输出事物
		out.write(new byte[] { NODE_START });
		//输出事物的属性
		List<Thing> fields = thing.getAllAttributesDescriptors();
		for (Iterator<Thing> iter = fields.iterator(); iter.hasNext();) {
			Thing field = iter.next();
			String name = field.getMetadata().getName();
			String type = field.getString("type", "string").toLowerCase();

			if ("int".equals(type)) {
				int value = thing.getInt(name);
				encodeName(out, name);
				out.write(TYPE_INT);				
				out.write(encodeInt32(value));
			} else if ("long".equals(type)) {
				long value = thing.getLong(name);
				encodeName(out, name);
				out.write(TYPE_LONG);				
				out.write(encodeLong64(value));
			} else if ("double".equals(type)) {
				double value = thing.getDouble(name);
				encodeName(out, name);
				out.write(TYPE_DOUBLE);				
				long dvalue = Double.doubleToLongBits(value);
				out.write(encodeLong64(dvalue));
			} else if ("float".equals(type)) {
				float value = thing.getFloat(name);
				encodeName(out, name);
				out.write(TYPE_FLOAT);				
				long dvalue = Double.doubleToLongBits(value);
				out.write(encodeLong64(dvalue));
			} else if ("bigDecimal".equals(type)) {
				BigDecimal value = thing.getBigDecimal(name);
				if (value == null)
					continue;

				encodeName(out, name);
				out.write(TYPE_BIGDECIMAL);				
				String str = value.toString();
				out.write(str.length());
				out.write(str.getBytes());
			} else if ("bigInteger".equals(type)) {
				BigInteger value = thing.getBigInteger(name);
				if (value == null)
					continue;

				encodeName(out, name);
				out.write(TYPE_BIGINTEGER);				
				byte[] bvalue = value.toByteArray();
				out.write(bvalue.length);
				out.write(bvalue);
			} else if ("boolean".equals(type)) {
				boolean value = thing.getBoolean(name);
				encodeName(out, name);
				out.write(TYPE_BOOLEAN);				
				out.write((byte) (value ? 1 : 0));
			} else if ("byte".equals(type)) {
				byte value = thing.getByte(name);
				encodeName(out, name);
				out.write(TYPE_BYTE);				
				out.write(value);
			} else if ("bytes".equals(type)) {
				byte[] value = thing.getBytes(name);
				if (value == null)
					continue;

				encodeName(out, name);
				out.write(TYPE_BYTES);				
				out.write(encodeInt32(value.length));
				out.write(value);
			} else if ("char".equals(type)) {
				char value = thing.getChar(name);
				encodeName(out, name);
				out.write(TYPE_CHAR);				
				out.write(encodeInt16(value));
			} else if ("short".equals(type)) {
				short value = thing.getShort(name);
				encodeName(out, name);
				out.write(TYPE_SHORT);				
				out.write(encodeInt16(value));
				out.write(new byte[] { (byte) value });
			} else if ("date".equals(type)) {
				Date value = thing.getDate(name);
				if (value == null)
					continue;

				encodeName(out, name);
				out.write(TYPE_DATE);				
				long dvalue = value.getTime();
				out.write(encodeLong64(dvalue));
			} else if ("object".equals(type)) {
				Object value = thing.getAttribute(name);
				if (value == null)
					continue;

				if (value instanceof Serializable) {
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					ObjectOutputStream oout;
					oout = new ObjectOutputStream(bout);
					oout.writeObject(value);
					oout.flush();

					byte[] bs = bout.toByteArray();
					encodeName(out, name);
					out.write(TYPE_OBJECT);					
					out.write(encodeInt32(bs.length));
					out.write(bs);
				}
			} else {
				//默认都当作字符串来保存
				String value = thing.getString(name);
				if (value != null) {// && (meta.isIncludeDefaultValue() || !value.equals(defaultValue))){
					encodeName(out, name);
					out.write(TYPE_STRING);					
					encodeString(value, out);
				}
			}
		}

		//输出子节点
		List<Thing> childs = thing.getChilds();
		for (Iterator<Thing> iter = childs.iterator(); iter.hasNext();) {
			Thing child = iter.next();
			out.write(CHILD_NODE);
			encode1(child, out, context);
		}

		//输入数据对象结束标识
		out.write(NODE_END);
	}

	public static void encode(Thing thing, OutputStream out,
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
		int version = 3;//meta.getFileVersion();
		boolean includeDefault = meta.isIncludeDefaultValue();

		//总是按照最新版本保存，当前最新版本2		
		out.write(new byte[] { VERSION });
		out.write(encodeInt32(version));

		if (version >= 1) {
			//版本1，增加是否包含默认值的属性
			if (includeDefault) {
				out.write(new byte[] { 1 });
			} else {
				out.write(new byte[] { 0 });
			}
		}
		if (version >= 2) {
			//版本2，增加最后更新时间
			long lastTime = meta.getLastModified();
			if (lastTime == 0) {
				lastTime = System.currentTimeMillis();
			}
			out.write(encodeLong64(lastTime));
		}

		if (version >= 3) {
			//版本3增加保存事物的标识
			String value = thing.getMetadata().getId();

			if (value == null) {
				value = thing.getMetadata().getName();
			}

			//输入值
			String str = value.toString();
			byte[] bs = str.getBytes("UTF-8");
			out.write(encodeInt32(bs.length));
			out.write(bs);
		}

		//输出本数据对象
		out.write(new byte[] { NODE_START });

		//输出属性
		List<Thing> fields = thing.getAllAttributesDescriptors();
		if (thing.getString("name") == null
				|| "".equals(thing.getString("name"))) {
			thing.put("name", thing.getMetadata().getName());
		}

		/*
		 if(thing.getMetadata().getPath().equals("core:things:lang.MetaDescriptor3:/@attribute/@SwtObject/@inputEditor/@actions/@create")){
		 for(Thing f : fields){
		 System.out.println(f.getString("name"));
		 }
		 
		 }*/

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

					encodeName(out, name);
					out.write(new byte[] { TYPE_INT });
					out.write(encodeInt32(4));
					out.write(encodeInt32(value));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("long".equals(type)) {
				try {
					long value = thing.getLong(name);
					//if(value != null && !("" + value).equals(anObject)

					encodeName(out, name);
					out.write(new byte[] { TYPE_LONG });
					byte[] vbytes = BigInteger.valueOf(value).toByteArray();
					out.write(encodeInt32(vbytes.length));
					out.write(vbytes);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("double".equals(type)) {
				try {
					double value = thing.getDouble(name);

					encodeName(out, name);
					out.write(new byte[] { TYPE_DOUBLE });
					long dvalue = Double.doubleToLongBits(value);
					byte[] vbytes = BigInteger.valueOf(dvalue).toByteArray();
					out.write(encodeInt32(vbytes.length));
					out.write(vbytes);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("float".equals(type)) {
				try {
					float value = thing.getFloat(name);

					encodeName(out, name);
					out.write(new byte[] { TYPE_FLOAT });
					long dvalue = Double.doubleToLongBits(value);
					byte[] vbytes = BigInteger.valueOf(dvalue).toByteArray();
					out.write(encodeInt32(vbytes.length));
					out.write(vbytes);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("bigDecimal".equals(type)) {
				try {
					BigDecimal value = thing.getBigDecimal(name);
					if (value == null)
						continue;

					encodeName(out, name);
					out.write(new byte[] { TYPE_BIGDECIMAL });
					String str = value.toString();
					out.write(encodeInt32(str.length()));
					out.write(str.getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("bigInteger".equals(type)) {
				try {
					BigInteger value = thing.getBigInteger(name);
					if (value == null)
						continue;

					encodeName(out, name);
					out.write(new byte[] { TYPE_BIGINTEGER });
					byte[] bvalue = value.toByteArray();
					out.write(encodeInt32(bvalue.length));
					out.write(bvalue);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("boolean".equals(type)) {
				try {
					boolean value = thing.getBoolean(name);

					encodeName(out, name);
					out.write(new byte[] { TYPE_BOOLEAN });
					out.write(encodeInt32(1));
					out.write(new byte[] { (byte) (value ? 1 : 0) });
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("byte".equals(type)) {
				try {
					byte value = thing.getByte(name);

					encodeName(out, name);
					out.write(new byte[] { TYPE_BYTE });
					out.write(encodeInt32(1));
					out.write(new byte[] { value });
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("bytes".equals(type)) {
				try {
					byte[] value = thing.getBytes(name);

					encodeName(out, name);
					out.write(new byte[] { TYPE_BYTES });
					out.write(encodeInt32(value.length));
					out.write(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("char".equals(type)) {
				try {
					char value = thing.getChar(name);

					encodeName(out, name);
					out.write(new byte[] { TYPE_CHAR });
					out.write(encodeInt32(1));
					out.write(new byte[] { (byte) value });
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("short".equals(type)) {
				try {
					short value = thing.getShort(name);

					encodeName(out, name);
					out.write(new byte[] { TYPE_SHORT });
					out.write(encodeInt32(1));
					out.write(new byte[] { (byte) value });
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("date".equals(type) || "datetime".equals(type) || "time".equals(type)) {
				try {
					Date value = thing.getDate(name);
					if (value == null)
						continue;

					long dvalue = value.getTime();
					encodeName(out, name);
					out.write(new byte[] { TYPE_DATE });
					byte[] vbytes = BigInteger.valueOf(dvalue).toByteArray();
					out.write(encodeInt32(vbytes.length));
					out.write(vbytes);
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
						encodeName(out, name);
						out.write(new byte[] { TYPE_OBJECT });
						out.write(encodeInt32(bs.length));
						out.write(bs);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				//默认都当作字符串来保存
				String value = thing.getString(name);
				if (value != null) {// && (meta.isIncludeDefaultValue() || !value.equals(defaultValue))){
					encodeName(out, name);
					out.write(new byte[] { TYPE_STRING });

					//输入值
					String str = value.toString();
					byte[] bs = str.getBytes("UTF-8");
					out.write(encodeInt32(bs.length));
					out.write(bs);
				}
			}
		}

		if (!idSetted) {
			encode("id", thing.getMetadata().getId(), "string", out);
		}
		if (!descriptorsSetted) {
			encode("descriptors", thing.getString("descriptors"), "string", out);
		}
		if (!nameSetted) {
			encode("name", thing.getMetadata().getName(), "string", out);
		}
		if (!labelSetted) {
			encode("label", thing.getMetadata().getLabel(), "string", out);
		}
		//输出子节点
		List<Thing> childs = thing.getChilds();
		for (Iterator<Thing> iter = childs.iterator(); iter.hasNext();) {
			Thing child = iter.next();
			out.write(new byte[] { CHILD_NODE });
			encode(child, out, context);
		}

		//输入数据对象结束标识
		out.write(new byte[] { NODE_END });
	}

	/**
	 * 编码所有。
	 * 
	 * @param thing
	 * @param out
	 * @param context
	 * @throws IOException
	 */
	public static void encodeAllx(Thing thing, OutputStream out,
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
		int version = thing.getMetadata().getFileVersion();
		//Integer version = (Integer) thing.getAttribute("_xer_version");
		String includeDefault = "false";//(String) thing.getAttribute("_xer_includeDefault");
		if (version != 0) {
			out.write(new byte[] { VERSION });
			out.write(encodeInt32(version));

			//版本不为0
			switch (version) {
			case 1:
				//版本1，增加是否包含默认值的属性
				if (includeDefault != null && includeDefault.equals("true")) {
					out.write(new byte[] { 1 });
				} else {
					out.write(new byte[] { 0 });
				}
			}
		}
		//输出本数据对象
		out.write(new byte[] { NODE_START });

		//thing.put("name", thing.getMetadata().getName());

		//标识和描述者是必须要保存的
		boolean idSetted = false;
		boolean descriptorsSetted = false;
		for (String key : thing.getAttributes().keySet()) {
			Object value = thing.getAttribute(key);
			if (value instanceof String) {
				encode(key, value, "string", out);
			}
		}

		if (!idSetted) {
			encode("id", thing.getMetadata().getId(), "string", out);
		}
		if (!descriptorsSetted) {
			encode("descriptors", thing.getString("descriptors"), "string", out);
		}
		//输出子节点
		List<Thing> childs = thing.getChilds();
		for (Iterator<Thing> iter = childs.iterator(); iter.hasNext();) {
			Thing child = iter.next();
			out.write(new byte[] { CHILD_NODE });
			encodeAllx(child, out, context);
		}

		//输入数据对象结束标识
		out.write(new byte[] { NODE_END });
	}

	public static void encodeName(OutputStream out, String name)
			throws IOException {
		byte[] ns = name.getBytes("UTF-8");
		out.write(new byte[] { ATTRIBUTE });
		out.write(new byte[] { (byte) ns.length });
		out.write(ns);
	}

	public static void encode(String name, Object value, String type,
			OutputStream out) throws IOException {
		if (value == null) {
			return;
		}

		byte[] ns = name.getBytes("UTF-8");
		out.write(new byte[] { ATTRIBUTE });
		out.write(new byte[] { (byte) ns.length });
		out.write(ns);

		if ("string".equals(type) || type == null || "".equals(type)) {
			//字符串类型
			out.write(new byte[] { TYPE_STRING });

			//输入值
			String str = (String) value;
			byte[] bs = str.getBytes("UTF-8");
			out.write(encodeInt32(bs.length));
			out.write(bs);
		} else if ("bigDecimal".equals(type)) {
			//BigDecimal类型
			out.write(new byte[] { TYPE_BIGDECIMAL });

		}
	}

	public static Thing decode(Thing thing, InputStream input)
			throws IOException {
		byte bytes[] = new byte[input.available()];
		
		byte[] buf = new byte[1024];
		int index = 0;
		int size = 0;
		while((size = input.read(buf)) > 0){
			System.arraycopy(buf, 0, bytes, index, size);
			index = index + size;
		}
		
		if (thing == null) {
			thing = new Thing(null, null, null, false);
		}
		decode(thing, bytes, 0);
		return thing;
	}

	/**
	 * 从字节流里构建数据对象。
	 * 
	 * @param thing
	 * @param bytes
	 * @param offset
	 * @return
	 * @throws IOException
	 */
	public static int decode(Thing thing, byte[] bytes, int offset)
			throws IOException {
		if (thing == null) {
			thing = new Thing(null, null, null, false);
		}

		ThingMetadata meta = thing.getMetadata();
		meta.setFileVersion(0);
		//定位到开始的位置		
		while (true) {
			if (bytes[offset] == VERSION) {
				offset++;
				byte[] valueLS = new byte[4];
				System.arraycopy(bytes, offset, valueLS, 0, 4);
				offset += 4;
				int version = decodeInt32(valueLS);

				meta.setFileVersion(version);
				if (version >= 1) {
					//版本1，增加是否包含默认值s
					offset++;
					if (bytes[offset] == 1) {
						meta.setIncludeDefaultValue(true);
					} else {
						meta.setIncludeDefaultValue(false);
					}
				}
				if (version >= 2) {
					//版本2，增加最后更新日期
					byte[] timeBytes = new byte[8];
					System.arraycopy(bytes, offset, timeBytes, 0, 8);
					offset += 8;
					long lastModified = decodeLong64(timeBytes);
					meta.setLastModified(lastModified);
				}

				if (version >= 3) {
					//事物的标识
					valueLS = new byte[4];
					System.arraycopy(bytes, offset, valueLS, 0, 4);
					offset += 4;
					int valueLength = decodeInt32(valueLS);
					try {
						//System.out.println(name + ":" + valueLength);
						byte[] valueBytes = new byte[valueLength];
						System.arraycopy(bytes, offset, valueBytes, 0,
								valueLength);
						offset += valueLength;

						meta.setId(new String(valueBytes, "UTF-8"));
					} catch (Exception e) {
						offset = offset - 4;
					} catch (Error e) {
						offset = offset - 4;
					}
				}
			}

			if (bytes[offset] == NODE_START) {
				offset++;
				break;
			} else {
				offset++;
			}
		}

		//开始读取属性和子节点
		while (true) {
			if (offset >= bytes.length) {
				break;
			}

			if (bytes[offset] == ATTRIBUTE) {
				offset++;
				//取名称的长度				
				int nameLength = bytes[offset];
				byte[] nameBytes = new byte[nameLength];

				offset++;
				System.arraycopy(bytes, offset, nameBytes, 0, nameLength);
				offset += nameLength;

				String name = new String(nameBytes, "UTF-8");
				//System.out.println(name);

				//取类型
				byte type = bytes[offset];
				offset++;

				//取属性
				byte[] valueLS = new byte[4];
				System.arraycopy(bytes, offset, valueLS, 0, 4);
				offset += 4;
				int valueLength = decodeInt32(valueLS);
				//System.out.println(name + ":" + valueLength);
				byte[] valueBytes = new byte[valueLength];
				System.arraycopy(bytes, offset, valueBytes, 0, valueLength);
				offset += valueLength;

				Object value = null;
				switch (type) {
				case TYPE_STRING:
					value = new String(valueBytes, "UTF-8");
					break;
				case TYPE_INT:
					value = decodeInt32(valueBytes);
					break;
				case TYPE_LONG:
					value = new BigInteger(valueBytes).longValue();
					break;
				case TYPE_DOUBLE:
					long dv = new BigInteger(valueBytes).longValue();
					value = Double.longBitsToDouble(dv);
					break;
				case TYPE_FLOAT:
					dv = new BigInteger(valueBytes).longValue();
					value = (float) Double.longBitsToDouble(dv);
					break;
				case TYPE_BIGDECIMAL:
					value = new BigDecimal(new String(valueBytes));
					break;
				case TYPE_BIGINTEGER:
					value = new BigInteger(valueBytes);
					break;
				case TYPE_BOOLEAN:
					if (valueBytes[0] == 1) {
						value = true;
					} else {
						value = false;
					}
					break;
				case TYPE_BYTE:
					value = valueBytes[0];
					break;
				case TYPE_BYTES:
					value = valueBytes;
					break;
				case TYPE_CHAR:
					value = (char) valueBytes[0];
					break;
				case TYPE_DATE:
					value = new Date(new BigInteger(valueBytes).longValue());
					break;
				case TYPE_SHORT:
					value = (short) valueBytes[0];
					break;
				case TYPE_OBJECT:
					ByteArrayInputStream bin = new ByteArrayInputStream(
							valueBytes);
					ObjectInputStream oin = new ObjectInputStream(bin);
					try {
						value = oin.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					break;
				}

				//value = new String(value.getBytes(), "BIG5");
				//value = new String(value.getBytes("UTF-8"), "UTF-8");
				//System.out.println(value);
				thing.getAttributes().put(name, value);
				if (thing.getMetadata().getId() == null && "id".equals(name)
						&& value != null) {
					thing.getMetadata().setId(value.toString());
				}
				//thing.put(name, value);
			} else if (bytes[offset] == CHILD_NODE) {
				offset++;
				Thing childObj = new Thing(null, null, null, false);
				offset = decode(childObj, bytes, offset);
				thing.addChild(childObj);
			} else if (bytes[offset] == NODE_END) {
				offset++;
				break;
			} else {
				throw new IOException("Unexception code");
			}
		}

		return offset;
	}

	/**
	 * 仅仅读取一个数据对象的属性。
	 * 
	 * @param bytes
	 * @param offset
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static Thing decodeAttributeOnly(byte[] bytes, int offset)
			throws UnsupportedEncodingException {
		Thing xer = new Thing(null, null, null, false);

		//定位到开始的位置		
		while (true) {
			if (bytes[offset] == NODE_START) {
				offset++;
				break;
			} else {
				offset++;
			}
		}

		//开始读取属性和子节点
		while (true) {
			if (bytes[offset] == ATTRIBUTE) {
				offset++;
				//取名称的长度				
				int nameLength = bytes[offset];
				byte[] nameBytes = new byte[nameLength];

				offset++;
				System.arraycopy(bytes, offset, nameBytes, 0, nameLength);
				offset += nameLength;

				String name = new String(nameBytes, "UTF-8");
				//System.out.println(name);

				//取类型
				//byte type = bytes[offset];
				offset++;

				//取属性
				byte[] valueLS = new byte[4];
				System.arraycopy(bytes, offset, valueLS, 0, 4);
				offset += 4;
				int valueLength = decodeInt32(valueLS);
				//System.out.println(name + ":" + valueLength);
				byte[] valueBytes = new byte[valueLength];
				try {
					System.arraycopy(bytes, offset, valueBytes, 0, valueLength);
				} catch (Exception e) {
					return xer;
				}
				offset += valueLength;
				String value = new String(valueBytes, "UTF-8");
				//System.out.println(value);

				xer.getAttributes().put(name, value);
			} else if (bytes[offset] == CHILD_NODE) {
				offset++;
				break;
			} else if (bytes[offset] == NODE_END) {
				offset++;
				break;
			} else {
				break;
			}
		}

		return xer;
	}

	public static byte[] encodeInt32(int value) {
		byte[] bs = new byte[4];
		bs[0] = (byte) (value >>> 24 & 0xFF);
		bs[1] = (byte) (value >>> 16 & 0xFF);
		bs[2] = (byte) (value >>> 8 & 0xFF);
		bs[3] = (byte) (value & 0xFF);

		return bs;
	}

	public static byte[] encodeLong64(long value) {
		byte[] bs = new byte[8];
		bs[0] = (byte) (value >>> 56 & 0xFF);
		bs[1] = (byte) (value >>> 48 & 0xFF);
		bs[2] = (byte) (value >>> 40 & 0xFF);
		bs[3] = (byte) (value >>> 32 & 0xFF);
		bs[4] = (byte) (value >>> 24 & 0xFF);
		bs[5] = (byte) (value >>> 16 & 0xFF);
		bs[6] = (byte) (value >>> 8 & 0xFF);
		bs[7] = (byte) (value & 0xFF);

		return bs;
	}

	public static int decodeInt32(byte[] bytes) {
		int v = (bytes[0] & 0xff) << 24 | (bytes[1] & 0xff) << 16
				| (bytes[2] & 0xff) << 8 | bytes[3] & 0xff;
		return v;
	}

	public static long decodeLong64(byte[] bytes) {
		long v = (((long) (bytes[0] & 0xff) << 56 | (bytes[1] & 0xff) << 48
				| (bytes[2] & 0xff) << 40 | (bytes[3] & 0xff) << 32) << 32)
				| ((long) (bytes[4] & 0xff) << 24 | (bytes[5] & 0xff) << 16
						| (bytes[6] & 0xff) << 8 | bytes[7] & 0xff);
		return v;
	}

	/**
	 * 编码名字字符串。
	 * 
	 * @param aname 一个名字
	 * @param buffer 字节缓存
	 * @throws UnsupportedEncodingException 
	 */
	private static void encodeName(String aname, ByteBuffer buffer)
			throws UnsupportedEncodingException {
		byte[] bytes = aname.getBytes(STRING_ENCODING);
		buffer.put((byte) (bytes.length & 0xFF));
		buffer.put(bytes);
	}

	/**
	 * 编码事物到字节缓存中。
	 * 
	 * @param thing
	 * @param buffer
	 * @param context
	 * @throws IOException
	 */
	public static void encode(Thing thing, ByteBuffer buffer,
			Map<Thing, String> context) throws IOException {
		//使用上下文避免重复编码事物，尤其是当子事物继承了父事物之后子事物的子事物可能是父事物
		if (context == null) {
			context = new HashMap<Thing, String>();
		}
		if (context.get(thing) != null) {
			return;
		} else {
			context.put(thing, "abcd");
		}

		//首先编码Metdata的数据
		ThingMetadata meta = thing.getMetadata();
		buffer.put(META);
		//文件格式版本
		buffer.putShort(version);
		//标识
		byte[] bytes = meta.getId().getBytes(STRING_ENCODING);
		buffer.put((byte) (bytes.length & 0xFF));
		buffer.put(bytes);
		//最后修改日期
		buffer.putLong(meta.getLastModified());
		//事物描述列表
		bytes = thing.getString("descriptors", "").getBytes(STRING_ENCODING);
		buffer.putShort((short) (bytes.length & 0xFFFF));
		buffer.put(bytes);
		//事物继承列表
		bytes = thing.getString("extends", "").getBytes(STRING_ENCODING);
		buffer.putShort((short) (bytes.length & 0xFFFF));
		buffer.put(bytes);

		//其次是事物本身的内容
		//一个节点的开始
		buffer.put(NODE_START);

		//输出事物的属性，首先获得事物的所有属性定义，对于没有定义的属性不做保存
		List<Thing> fields = thing.getAllAttributesDescriptors();
		//循环保存事物的属性        
		for (Iterator<Thing> iter = fields.iterator(); iter.hasNext();) {
			Thing field = iter.next();
			String name = field.getMetadata().getName();
			String type = field.getString("type").toLowerCase();

			try {
				if ("int".equals(type)) {
					encodeName(name, buffer);

					buffer.put(TYPE_INT);
					int value = thing.getInt(name);
					buffer.putInt(value);
				} else if ("long".equals(type)) {
					encodeName(name, buffer);

					buffer.put(TYPE_LONG);
					long value = thing.getLong(name);
					buffer.putLong(value);
				} else if ("double".equals(type)) {
					encodeName(name, buffer);

					buffer.put(TYPE_DOUBLE);
					buffer.putDouble(thing.getDouble(name));
				} else if ("float".equals(type)) {
					encodeName(name, buffer);

					buffer.put(TYPE_FLOAT);
					buffer.putFloat(thing.getFloat(name));
				} else if ("bigDecimal".equals(type)) {
					BigDecimal value = thing.getBigDecimal(name);
					if (value == null)
						continue;

					encodeName(name, buffer);

					buffer.put(TYPE_BIGDECIMAL);
					bytes = value.toString().getBytes();
					buffer.put((byte) (bytes.length & 0xFF));
				} else if ("bigInteger".equals(type)) {
					BigInteger value = thing.getBigInteger(name);
					if (value == null)
						continue;

					encodeName(name, buffer);

					buffer.put(TYPE_BIGINTEGER);
					bytes = value.toString().getBytes();
					buffer.put((byte) (bytes.length & 0xFF));
				} else if ("boolean".equals(type)) {
					encodeName(name, buffer);

					buffer.put(TYPE_BOOLEAN);
					buffer.put((byte) (thing.getBoolean(name) ? 1 : 0));
				} else if ("byte".equals(type)) {
					encodeName(name, buffer);

					buffer.put(TYPE_BYTE);
					buffer.put(thing.getByte(name));
				} else if ("bytes".equals(type)) {
					byte[] value = thing.getBytes(name);
					if (value == null)
						continue;

					encodeName(name, buffer);
					buffer.put(TYPE_BYTES);
					buffer.putInt(value.length);
					buffer.put(value);
				} else if ("char".equals(type)) {
					char value = thing.getChar(name);

					encodeName(name, buffer);
					buffer.put(TYPE_CHAR);
					buffer.putChar(value);
				} else if ("short".equals(type)) {
					encodeName(name, buffer);

					buffer.put(TYPE_SHORT);
					buffer.putShort(thing.getShort(name));
				} else if ("date".equals(type)) {
					Date value = thing.getDate(name);
					if (value == null)
						continue;

					encodeName(name, buffer);
					buffer.put(TYPE_DATE);
					buffer.putLong(value.getTime());
				} else if ("object".equals(type)) {
					Object value = thing.getAttribute(name);
					if (value == null)
						continue;

					if (value instanceof Serializable) {
						encodeName(name, buffer);

						buffer.put(TYPE_OBJECT);

						ByteArrayOutputStream bout = new ByteArrayOutputStream();
						ObjectOutputStream oout = new ObjectOutputStream(bout);
						oout.writeObject(value);
						oout.flush();
						byte[] bs = bout.toByteArray();
						buffer.putInt(bs.length);
						buffer.put(bs);
					}
				} else {
					//默认都当作字符串来保存
					String value = thing.getString(name);
					if (value != null) {// && (meta.isIncludeDefaultValue() || !value.equals(defaultValue))){
						encodeName(name, buffer);

						buffer.put(TYPE_STRING);
						bytes = value.getBytes(STRING_ENCODING);
						buffer.putInt(bytes.length);
						buffer.put(bytes);
					}
				}
			} catch (Exception e) {
				throw new ActionException("encoding attribute " + name
						+ " error", e);
			}
		}

		//输出子节点
		List<Thing> childs = thing.getChilds();
		for (Iterator<Thing> iter = childs.iterator(); iter.hasNext();) {
			Thing child = iter.next();
			buffer.put(CHILD_NODE);
			encode(child, buffer, context);
		}

		//输入数据对象结束标识
		buffer.put(NODE_END);
	}
}