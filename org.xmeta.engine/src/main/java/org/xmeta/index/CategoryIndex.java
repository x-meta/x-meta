package org.xmeta.index;

import java.util.ArrayList;
import java.util.List;

import org.xmeta.Category;
import org.xmeta.Index;
import org.xmeta.ThingManager;
import org.xmeta.World;

public class CategoryIndex extends Index{
	List<Index> childs = null;
	Index parent = null;
	Category category = null;
	
	public CategoryIndex(Index parent, Category category){
		this.parent = parent;
		this.category = category;
		
		refresh();
	}
	
	@Override
	public Object getIndexObject() {
		return category;
	}

	@Override
	public Index getParent() {
		return parent;
	}

	@Override
	public List<Index> getChilds() {
		if(childs ==  null){
			refresh();
		}
		
		return childs;
	}

	@Override
	public String getDescription() {
		return category.getName();
	}

	@Override
	public String getLabel() {
		return category.getSimpleName();
	}

	@Override
	public String getName() {
		return category.getName();
	}

	@Override
	public String getPath() {
		return category.getName() ;
	}

	@Override
	public String getType() {
		return Index.TYPE_CATEGORY;
	}

	@Override
	public boolean refresh() {
		//先判断Category是否还有效
		ThingManager thingManager = World.getInstance().getThingManager(category.getThingManager().getName());
		if(thingManager == null){
			return false;
		}
		
		if(thingManager.getCategory(category.getName()) == null){
			return false;
		}
		
		if(childs == null){
			childs = new ArrayList<Index>();
		}
		//刷新下级节点
		List<Category> categorys = new ArrayList<Category>();
		for(Category cat : category.getCategorys()){
			if(cat.getName() != null && !"".equals(cat.getName())){
				categorys.add(cat);
			}
		}
		
		IndexFactory.addOrRemoveChilds(this, childs, categorys,	IndexFactory.categoryIndexFactory, Index.TYPE_CATEGORY);
		
		//事物索引
		IndexFactory.addOrRemoveChilds(this, childs, category.getThingIndexs(),	IndexFactory.thingIndexFactory, Index.TYPE_THING);		
		
		WorldIndex.sort(childs);
		
		return true;
	}

}
