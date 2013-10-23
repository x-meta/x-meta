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
package org.xmeta.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.ThingManager;
import org.xmeta.World;

public class ThingUtil {
	private static Logger logger = LoggerFactory.getLogger(ThingUtil.class); 
	static World world = World.getInstance();



	public static void replaceCategory(Category category, String replaceFor, String replaceWidth){
		for (Iterator<Thing> iter = category.iterator(true); iter.hasNext();) {
			Thing thing = iter.next();

			logger.info("replace " + thing.getMetadata().getPath());
			replaceThing(thing, replaceFor, replaceWidth);
			thing.getMetadata().setLastModified(System.currentTimeMillis());
			thing.save();
		}
	}
	
	public static void replaceThingManager(ThingManager tm, String replaceFor,
			String replaceWidth) {
		for (Iterator<Thing> iter = tm.iterator("", true); iter.hasNext();) {
			Thing thing = iter.next();

			logger.info("replace " + thing.getMetadata().getPath());
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
	 * @param file
	 * @return
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

}