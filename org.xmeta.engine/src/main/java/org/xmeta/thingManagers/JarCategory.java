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
