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
package org.xmeta.thingManagers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.ThingIndex;
import org.xmeta.ThingManager;
import org.xmeta.ThingManagerListener;
import org.xmeta.World;
import org.xmeta.util.ThingClassLoader;

public class ClassThingManager implements ThingManager{
	Map<String, ClassCategory> categorys = new HashMap<String, ClassCategory>();
	World world = World.getInstance();
	
	@Override
	public void addThingManagerListener(ThingManagerListener listener) {
	}

	@Override
	public void clearCache() {
	}

	@Override
	public boolean createCategory(String categoryName) {
		return false;
	}

	@Override
	public URL findResource(String name) {
		return ClassThingManager.class.getResource(name);
	}

	@Override
	public Category getCategory(String name) {
		ClassCategory category = categorys.get(name);
		if(category == null){
			category = new ClassCategory(this, name);
			categorys.put(name, category);
		}
		return category;
	}

	@Override
	public List<Category> getCategorys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ThingClassLoader getClassLoader() {
		return World.getInstance().getClassLoader();
	}

	@Override
	public String getClassPath() {
		return null;
	}

	@Override
	public String getName() {
		return "classThingManager";
	}

	@Override
	public InputStream getResourceAsStream(String name)  {
		try {
			return world.getResourceAsStream(name);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public Thing getThing(String thingName) {
		return null;
	}

	@Override
	public List<ThingIndex> getThingIndexs(String categoryName) {
		return null;
	}

	@Override
	public List<ThingIndex> getThingIndexs(String categoryName, String descriptorPath) {
		return null;
	}

	@Override
	public List<Thing> getThings(String categoryName) {
		return null;
	}

	@Override
	public List<Thing> getThings(String categoryName, String descriptorPath) {
		return null;
	}

	@Override
	public Iterator<Thing> iterator(String categoryName, boolean includeChildCategory) {
		return null;
	}

	@Override
	public Iterator<Thing> iterator(String categoryName, String descriptorPath, boolean includeChildCategory) {
		return null;
	}

	@Override
	public void refresh() {
	}

	@Override
	public void refresh(String categoryName, boolean includeChildCategory) {
	}

	@Override
	public boolean remove() {
		return false;
	}

	@Override
	public boolean remove(Thing thing) {
		return false;
	}

	@Override
	public boolean removeCategory(String categoryName) {
		return false;
	}

	@Override
	public boolean removeThingManagerListener(ThingManagerListener listener) {
		return false;
	}

	@Override
	public boolean save(Thing athing) {
		return false;
	}

	@Override
	public void init(Properties properties) {
	}

}