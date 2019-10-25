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

import java.util.Iterator;
import java.util.List;

import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.ThingIndex;
import org.xmeta.ThingManager;

public class ClassCategory implements Category{
	String name = null;
	ClassThingManager thingManager = null;
	
	public ClassCategory(ClassThingManager thingManager, String name){		
		this.name = name;
		this.thingManager = thingManager;
	}
	
	@Override
	public Category getCategory(String name) {
		return null;
	}

	@Override
	public List<Category> getCategorys() {
		return null;
	}

	@Override
	public String getFilePath() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Category getParent() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return null;
	}

	@Override
	public Thing getThing(String name) {
		return null;
	}

	@Override
	public List<ThingIndex> getThingIndexs() {
		return null;
	}

	@Override
	public List<ThingIndex> getThingIndexs(String descriptor) {
		return null;
	}

	@Override
	public ThingManager getThingManager() {
		return thingManager;
	}

	@Override
	public List<Thing> getThings() {
		return null;
	}

	@Override
	public List<Thing> getThings(String descriptor) {
		return null;
	}

	@Override
	public Iterator<Thing> iterator(boolean includeChildCategory) {
		return null;
	}

	@Override
	public Iterator<Thing> iterator(String descriptorPath, boolean includeChildCategory) {
		return null;
	}

	@Override
	public void refresh() {
	}

	@Override
	public void refresh(boolean includeChild) {
	}

}