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
package org.xmeta;

/**
 * 事物的路径，把路径整字符串分解成小单元，用作缓存，不用每次都执行分解的方法。<p/>
 * 
 * 路径的规则
 * <pre>
 *     <package>.<thingName>/<childPath1>/<childPath2>.../<childPathn>
 * 包的路径，比如
 *     com
 *     xmeta.core
 * 事物的路径，比如
 *     xmeta.core.things.MetaThing
 * 子节点的路径规则：
 *     /@thingId              //取标识为thingId的子事物
 *     /#attributeId          //取属性名为attributeId的事物属性
 *     /thingName$index       //取事物名为thingName的第index个子事物
 *     /thingName@thingId     //取事物名为thingName且标识为thingId的子事物
 *     /thingName@            //取事物名为thingName的所有子事物
 * </pre>
 * <p/>
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zhangyuxiang</a>
 *
 */
public class Path {
	public final static byte TYPE_CATEGORY = 1;
	public final static byte TYPE_THING = 2;
	public final static byte TYPE_ATTRIBUTE = 3;
	public final  static byte TYPE_CHILD_THINGS = 4;
	public final static byte TYPE_CHILD_THING_AT_INDEX = 5;
	public final static byte TYPE_CATEGORY_OR_THING = 6;
	public final static byte TYPE_CHILD_THING = 7;
	public final static byte TYPE_CHILD_THING_OR_INDEX = 8;
	
	/** 路径 */
	private String path = null;
	
	/** 类型 */
	private byte type;
	
	/** 事物名称 */
	private String thingName = null;
	
	/** 属性名称 */
	private String attributeName = null;
	
	/** 子事物的标识 */
	private String thingId = null;
	
	/** 索引 */
	private short index = -1;
	
	/** 子路径 */
	private Path childPath = null;

	private Path(){	
	}
	
	public Path(String pathStr){
		if(pathStr == null || "".equals(pathStr)){
			throw new java.lang.NullPointerException("path can not be null");
		}
		
		if(pathStr.indexOf(":") != -1){
			//旧的路径规则，兼容旧系统
			String paths[] = pathStr.split(":");
			if(paths.length == 1){
				this.path = paths[0];
				this.type = Path.TYPE_CATEGORY_OR_THING;
			}else if(paths.length == 2){
				this.path = paths[0] + "." + paths[1];
				this.type = Path.TYPE_CATEGORY_OR_THING;
			}else if(paths.length == 3){
				this.path = paths[0] + "." + paths[1] + "." + paths[2];
				this.type = Path.TYPE_CATEGORY_OR_THING;
			}else{
				this.path = paths[0] + "." + paths[1] + "." + paths[2];
				this.type = Path.TYPE_THING;
				Path parent = this;
				for(String childPath : paths[3].split("[/]")){
					if("".equals(childPath) || "@".equals(childPath)){
						continue;
					}
					
					Path child = parseOldChildPath(path, childPath);					
					if(child != null){
						parent.childPath = child;
						parent = child;
					}
				}
			}
		}else{
			//新的路径规则
			String paths[] = pathStr.split("[/]");
			this.path = paths[0];
			this.type = Path.TYPE_CATEGORY_OR_THING;
			
			//子节点
			Path parent = this;			
			for(int i=1; i<paths.length; i++){				
				String childPath = paths[i];
				Path child = parseChildPath(path, childPath);
				
				if(child != null){
					parent.childPath = child;
					parent = child;
				}
			}
		}
	}
	
	/**
	 * 分析子节点。
	 * 
	 * @param path
	 * @param childPath
	 * @return
	 */
	public static Path parseChildPath(String path, String childPath){
		Path child = null;
		if(childPath == null || childPath.equals("")){
			return null;
		}
		if(childPath.startsWith("@")){
			child = new Path();
			child.type = Path.TYPE_CHILD_THING;
			child.thingId = childPath.substring(1, childPath.length());
		}else if(childPath.startsWith("#")){
			child = new Path();
			child.type = Path.TYPE_ATTRIBUTE;
			child.attributeName = childPath.substring(1, childPath.length());					
		}else if(childPath.startsWith("$")){
			child = new Path();
			child.type = Path.TYPE_CHILD_THING_AT_INDEX;
			try{
				child.index = Short.parseShort(childPath.substring(1, childPath.length()));
			}catch(Exception e){
				return null;
			}
		}else if(childPath.endsWith("$") || childPath.endsWith("@")){
			child = new Path();
			child.type = Path.TYPE_CHILD_THINGS;
			child.thingName = childPath.substring(0, childPath.length() - 1);
		}else if(childPath.indexOf("$") != -1){
			child = new Path();					
			child.type = Path.TYPE_CHILD_THING_AT_INDEX;
			int index = childPath.indexOf("$");
			child.thingName = childPath.substring(0, index);
			try{
				child.index = Short.parseShort(childPath.substring(index + 1, childPath.length()));
			}catch(Exception e){
				return null;
			}
		}else{
			return null;
		}
		
		if(child != null){
			child.path = childPath;
		}
		return child;
	}
	
	/**
	 * 分析旧规则的子路径。
	 * 
	 * @param path
	 * @param childPath
	 * @return
	 */
	public static Path parseOldChildPath(String path, String childPath){
		Path child = null;
		if(childPath.startsWith("@")){
			//子事物或第n个子事物
			child = new Path();
			child.thingId = childPath.substring(1, childPath.length());
			child.type = Path.TYPE_CHILD_THING_OR_INDEX;
			try{
				child.index = Short.parseShort(child.thingId);
			}catch(Exception e){
				child.index = -1;
			}
		}else if(childPath.endsWith("@")){
			//子事物列表
			child = new Path();
			child.thingName = childPath.substring(0, childPath.length() - 1);
			child.type = Path.TYPE_CHILD_THINGS;
		}else if(childPath.indexOf("@") != -1){
			//指定事物名的子事物
			child = new Path();
			int index = childPath.indexOf("@");
			child.thingName = childPath.substring(0, index);
			child.thingId = childPath.substring(index + 1, childPath.length());
			child.type = Path.TYPE_CHILD_THING_OR_INDEX;
			try{
				child.index = Short.parseShort(child.thingId);
			}catch(Exception e){
				child.index = -1;
			}
		}else{
			throw new XMetaException("Invailid path, path=" + path);
		}
		
		if(child != null){
			child.path = childPath;
		}
		return child;
	}
	
	public String getPath() {
		return path;
	}

	public byte getType() {
		return type;
	}

	public short getIndex() {
		return index;
	}

	public Path getChildPath() {
		return childPath;
	}

	@Override
	public String toString() {
		return "Path [childPath=" + childPath + ", index=" + index + ", path=" + path + ", type=" + type + "]";
	}

	public String getThingName() {
		return thingName;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public String getThingId() {
		return thingId;
	}

	public void setType(byte type) {
		this.type = type;
	}
}