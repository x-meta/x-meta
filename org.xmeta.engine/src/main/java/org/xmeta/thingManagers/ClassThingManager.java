package org.xmeta.thingManagers;

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
	public InputStream getResourceAsStream(String name) {
		return ClassThingManager.class.getResourceAsStream(name);
	}

	@Override
	public Thing getThing(String thingName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ThingIndex> getThingIndexs(String categoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ThingIndex> getThingIndexs(String categoryName, String descriptorPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Thing> getThings(String categoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Thing> getThings(String categoryName, String descriptorPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Thing> iterator(String categoryName, boolean includeChildCategory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Thing> iterator(String categoryName, String descriptorPath, boolean includeChildCategory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh(String categoryName, boolean includeChildCategory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean remove() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(Thing thing) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeCategory(String categoryName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeThingManagerListener(ThingManagerListener listener) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean save(Thing athing) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void init(Properties properties) {
		// TODO Auto-generated method stub
		
	}

}
