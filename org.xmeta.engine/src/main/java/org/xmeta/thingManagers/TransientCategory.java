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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.ThingManager;
import org.xmeta.ThingIndex;

/**
 * 瞬态事物管理者下的目录。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class TransientCategory implements Category{
	/** 目录所属的事物管理者 */
	TransientThingManager thingFactory;
	
	public TransientCategory(TransientThingManager thingFactory){
		this.thingFactory = thingFactory;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#getCategory(java.lang.String)
	 */
	public Category getCategory(String name) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#getCategorys()
	 */
	public List<Category> getCategorys() {
		return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#getParent()
	 */
	public Category getParent() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#getThing(java.lang.String)
	 */
	public Thing getThing(String name) {
		return thingFactory.getThing(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#getThingFactory()
	 */
	public ThingManager getThingManager() {
		return thingFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#getName()
	 */
	public String getName() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#getSimpleName()
	 */
	public String getSimpleName(){
		return getName();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#getThingIndexs(java.lang.String, boolean)
	 */
	public List<ThingIndex> getThingIndexs(String descriptorPath) {
		return thingFactory.getThingIndexs(null, descriptorPath);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#getThings(java.lang.String, boolean)
	 */
	public List<Thing> getThings(String descriptorPath) {
		return thingFactory.getThings(null, descriptorPath);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#iterator(boolean)
	 */
	public Iterator<Thing> iterator(boolean includeChildCategory) {
		return thingFactory.iterator(null, includeChildCategory);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#iterator(java.lang.String, boolean)
	 */
	public Iterator<Thing> iterator(String descriptorPath, boolean includeChildCategory) {
		return thingFactory.iterator(null, descriptorPath, includeChildCategory);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#getThingIndexs(boolean)
	 */
	public List<ThingIndex> getThingIndexs() {
		return thingFactory.getThingIndexs(null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#getThings(boolean)
	 */
	public List<Thing> getThings() {
		return thingFactory.getThings(null);
	}

	@Override
	public void refresh() {
		
	}

	@Override
	public void refresh(boolean includeChild) {
		
	}

	@Override
	public String getFilePath() {
		return null;
	}

}