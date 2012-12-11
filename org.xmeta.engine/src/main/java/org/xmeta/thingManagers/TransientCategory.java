/*
 * Copyright 2007-2008 The X-Meta.org.
 * 
 * Licensed to the X-Meta under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The X-Meta licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
