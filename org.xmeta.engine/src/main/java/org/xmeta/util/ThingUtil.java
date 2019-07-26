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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.ThingManager;
import org.xmeta.World;

public class ThingUtil {
	//private static Logger logger = LoggerFactory.getLogger(ThingUtil.class); 
	static World world = World.getInstance();

	/**
	 * 把目标事物粘贴为子事物。
	 * 
	 * @param thing
	 * @param forPasteThing
	 */
	public static void pasteAsChild(Thing thing, Thing forPasteThing){
		String oldPath = forPasteThing.getStringBlankAsNull(Thing.ORIGIN_THING_PATH);
		
		thing.addChild(forPasteThing);
		
		if(oldPath != null){
			//替换子事物的路径
			String newPath = forPasteThing.getMetadata().getPath();
			ThingUtil.replaceThing(forPasteThing, oldPath, newPath);
		}
	}
	
	/**
	 * 把目标事物粘贴到指定事物上。
	 * 
	 * @param thing
	 * @param forPasteThing
	 */
	public static void paste(Thing thing, Thing forPasteThing){
		String oldPath = forPasteThing.getStringBlankAsNull(Thing.ORIGIN_THING_PATH);
		
		thing.cognize(forPasteThing);
		
		if(oldPath != null){
			//替换子事物的路径
			String newPath = thing.getMetadata().getPath();
			ThingUtil.replaceThing(thing, oldPath, newPath);
		}
	}

	public static void replaceCategory(Category category, String replaceFor, String replaceWidth){
		for (Iterator<Thing> iter = category.iterator(true); iter.hasNext();) {
			Thing thing = iter.next();

			//logger.info("replace " + thing.getMetadata().getPath());
			replaceThing(thing, replaceFor, replaceWidth);
			thing.getMetadata().setLastModified(System.currentTimeMillis());
			thing.save();
		}
	}
	
	public static void replaceThingManager(ThingManager tm, String replaceFor,
			String replaceWidth) {
		for (Iterator<Thing> iter = tm.iterator("", true); iter.hasNext();) {
			Thing thing = iter.next();

			//logger.info("replace " + thing.getMetadata().getPath());
			replaceThing(thing, replaceFor, replaceWidth);
			thing.getMetadata().setLastModified(System.currentTimeMillis());
			thing.save();
		}
	}

	public static void replaceThing(Thing thing, String replaceFor,
			String replaceWith) {
		Map<String, Object> attrs = thing.getAttributes();
		for (String key : attrs.keySet()) {
			Object value = attrs.get(key);
			if (value != null && value instanceof String) {
				String str = (String) value;
				attrs.put(key, str.replaceAll("(" + replaceFor + ")",
						replaceWith));
			}
		}

		for (Thing child : thing.getChilds()) {
			replaceThing(child, replaceFor, replaceWith);
		}
	}

	/**
	 * 如果文件是事物，那么返回事物。
	 * 
	 * @param file 文件
	 * @return 事物
	 */
	public static Thing getThing(File file) {
		File wfile = new File(world.getPath());
		Stack<String> stack = new Stack<String>();
		while (!file.getParentFile().equals(wfile)) {
			stack.push(file.getName());

			file = file.getParentFile();
		}

		String thingPath = "";
		int count = 0;
		while (stack.size() > 0) {
			count++;
			if (count == 1) {
				thingPath = stack.pop();
			} else if (count == 2 || count == 3) {
				thingPath = thingPath + ":" + stack.pop();
			} else {
				thingPath = thingPath + "." + stack.pop();
			}
		}

		return world.getThing(thingPath);
	}
	
	public static List<Thing> getAllDescriptorsAndExtends(Thing thing){
		List<Thing> list = new ArrayList<Thing>();
		Map<String, String> context = new HashMap<String, String>();
		for(Thing des : thing.getAllDescriptors()){
			String path = des.getMetadata().getPath();
			if(context.get(path) == null){
				list.add(des);
				context.put(path, path);
			}
		}
		
		for(Thing ext : thing.getExtends()){
			String path = ext.getMetadata().getPath();
			if(context.get(path) == null){
				list.add(ext);
				context.put(path, path);
			}
		}
		
		return list;
	}
	
}