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