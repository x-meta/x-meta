package org.xmeta.thingManagers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xmeta.ActionException;
import org.xmeta.Category;
import org.xmeta.ThingIndex;
import org.xmeta.ThingManager;

public class JdbcCategory  extends CachedCategory{	
	//private static Logger logger = LoggerFactory.getLogger(JdbcCategory.class);
	
	public JdbcCategory(String name, ThingManager thingManager, Category parent){		
		super(thingManager, parent, name);
		
		refresh();
	}
	
	@Override
	public String getFilePath() {
		return null;
	}

	public Connection getConnection(){
		return ((JdbcThingManager) thingManager).getConnection();
	}
	
	@Override
	public void refresh() {
		Connection con = getConnection();
		if(con == null){
			return;
		}
		
		PreparedStatement pst = null;
		ResultSet rs = null;
		try{
			pst = con.prepareStatement("select simpleName from tblCategorys where parent=?");
			pst.setString(1, name == null ? "" : name);
			rs = pst.executeQuery();			
			Set<String> categorys = new HashSet<String>();
			while(rs.next()){
				categorys.add(rs.getString("simpleName"));
			}
			List<String> childNames = new ArrayList<String>();
			for(String path : categorys){
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
						Category category = new JdbcCategory(newCategoryPath, getThingManager(), this);
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
				thingPath = "";
			}else{
				thingPath = name;
			}
			rs.close();
			pst.close();
			
			pst = con.prepareStatement("select * from tblThings where category=?");
			pst.setString(1, thingPath);
			rs = pst.executeQuery();
			while(rs.next()){
				String path = rs.getString("path");
				int index = path.lastIndexOf(".");
				if(index != -1){
					path = path.substring(index + 1, path.length());
				}
			
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
						ThingIndex childIndex = new ThingIndex();
						childIndex.name = path;
						if(name != null && !"".equals(name)){
							childIndex.path = name + "." + path;
						}else{
							childIndex.path = path;
						}
						childIndex.thingManager = thingManager;
						childIndex.lastModified = rs.getLong("lastModified");
						childIndex.descriptors = rs.getString("descriptors");
						childIndex.label = rs.getString("label");
						thingIndexs.add(childIndex);
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
		}catch(Exception e){			
			throw new ActionException("Refresh category error, thingManager=" + name, e);
		}finally{
			if(pst != null){
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(con != null){
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
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
