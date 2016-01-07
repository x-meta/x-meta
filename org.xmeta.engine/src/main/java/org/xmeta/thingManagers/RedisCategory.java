package org.xmeta.thingManagers;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.xmeta.Category;
import org.xmeta.ThingCoder;
import org.xmeta.ThingIndex;
import org.xmeta.ThingManager;
import org.xmeta.World;

import redis.clients.jedis.Jedis;

public class RedisCategory extends CachedCategory{	
	public RedisCategory(String name, ThingManager thingManager, Category parent){		
		super(thingManager, parent, name);
		
		refresh();
	}
	
	@Override
	public String getFilePath() {
		return null;
	}

	@Override
	public void refresh() {
		Jedis jedis = getJedis();
		try{
			//刷新子目录
			String categoryPath = null;
			if(name == null || "".equals(name)){
				categoryPath = "category|";
			}else{
				categoryPath = "category|" + name + ".";
			}
			Set<String> categorys = jedis.keys(categoryPath + "*");
			int index = categoryPath.length();
			List<String> childNames = new ArrayList<String>();
			for(String path : categorys){
				path = path.substring(index, path.length());
				if(path.indexOf(".") == -1){
					childNames.add(path);
					boolean have = false;
					for(Category category : childCategorys){
						if(category.getSimpleName().equals(path)){
							have = true;
							break;
						}
					}
					
					if(!have){
						String newCategoryPath = path;
						if(this.name != null && !"".equals(this.name)){
							newCategoryPath = this.name + "." + path;
						}
						Category category = new RedisCategory(newCategoryPath, getThingManager(), this);
						childCategorys.add(category);
					}
				}
			}
			
			//删除已经不存在的目录
			for(int i=0; i<childCategorys.size(); i++){
				Category category = childCategorys.get(i);
				boolean have = false;
				for(String path : childNames){
					if(path.equals(category.getSimpleName())){
						have = true;
						break;
					}
				}
				
				if(!have){
					childCategorys.remove(i);
					i--;
				}
			}
			
			//刷新事物的索引
			String thingPath = null;
			if(name == null || "".equals(name)){
				thingPath = "thing|";
			}else{
				thingPath = "thing|" + name + ".";
			}
			index = thingPath.length();
			Set<String> things = jedis.keys(thingPath + "*");
			childNames = new ArrayList<String>();
			for(String path : things){
				path = path.substring(index, path.length());
				if(path.indexOf(".") == -1){
					childNames.add(path);
					boolean have = false;
					for(ThingIndex thingIndex : thingIndexs){
						if(thingIndex.getName().equals(path)){
							have = true;
							break;
						}
					}
					
					if(!have){
						thingPath = "thing|" + name + "." + path;
						String thingCode = jedis.get(thingPath);
						if(thingCode != null){
							ThingCoder coder = World.getInstance().getThingCoder("xer.txt");
							ThingIndex childIndex = new ThingIndex();
							childIndex.name = path;
							if(name != null && !"".equals(name)){
								childIndex.path = name + "." + path;
							}else{
								childIndex.path = path;
							}
							childIndex.thingManager = thingManager;
							childIndex.lastModified = 0;
							coder.decodeIndex(childIndex, new ByteArrayInputStream(thingCode.getBytes()), 0);
							thingIndexs.add(childIndex);
						}						
					}
				}
			}
			
			//删除已经不存在的目录
			for(int i=0; i<thingIndexs.size(); i++){
				ThingIndex thingIndex = thingIndexs.get(i);
				boolean have = false;
				for(String path : childNames){
					if(path.equals(thingIndex.getName())){
						have = true;
						break;
					}
				}
				
				if(!have){
					thingIndexs.remove(i);
					i--;
				}
			}
		}finally{
			releaseJedis(jedis);
		}
	}
	
	public Jedis getJedis(){
		RedisThingManager th = (RedisThingManager) this.getThingManager();
		return th.getJedis();
	}
	
	public void releaseJedis(Jedis jedis){
		RedisThingManager th = (RedisThingManager) this.getThingManager();
		th.releaseJedis(jedis);
	}

	@Override
	public void refresh(boolean includeChild) {
		this.refresh();
		
		if(includeChild){
			for(Category child : childCategorys){
				child.refresh(includeChild);
			}
		}
	}

}
