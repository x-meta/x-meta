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

import org.xmeta.Category;
import org.xmeta.ThingManager;

/**
 * Jar包分类。
 * 
 * @author zhangyuxiang
 *
 */
public class JarCategory extends CachedCategory{
	public JarCategory(ThingManager thingManager, Category parentPackage, String name){
		super(thingManager, parentPackage, name);
	}
	
	@Override
	public String getFilePath() {
		return null;
	}

	@Override
	public void refresh() {
	}

	@Override
	public void refresh(boolean includeChild) {
	}
}