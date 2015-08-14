package org.xmeta.thingManagers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingMetadata;
import org.xmeta.World;
import org.xmeta.util.ThingClassLoader;

/**
 * 数据JDBC数据库存储事物，需要表：
 * tblCategorys(name<String>, parent<String>, simpleName<String>)
 * tblThings(name<String>, label<String>, descriptors<String>, data<byte[]>, path<String>, category<String>)
 * @author Administrator
 *
 */
public class JdbcThingManager extends AbstractThingManager{
	private static Logger logger = LoggerFactory.getLogger(JdbcThingManager.class);
	
	/** JDBC的驱动  */
	String driverClass;
	/** JDBC的URL */
	String url;
	/** JDBC的用户 */
	String dbUser;
	/** JDBC的密码 */
	String dbPassword;
	boolean connectionError = false;
	long lastConnectTime = 0;
	
	public JdbcThingManager(String name, File rootFile) throws IOException{
		super(name);
	}
	
	/**
	 * 返回数据库连接。
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public Connection getConnection(){		
		try {
			if(connectionError){
				//避免重复的连接，当数据库连不上时不要影响其它装载事物的效率
				if(System.currentTimeMillis() - lastConnectTime < 10000){
					return null;
				}
			}
			
			Connection con = DriverManager.getConnection(url, dbUser, dbPassword);
			if(con != null){
				connectionError = false;
			}
			
			return con;
		} catch (SQLException e) {
			logger.error("Get connection error", e);
			
			connectionError = true;
			lastConnectTime = System.currentTimeMillis();
			return null;
		}
	}

	@Override
	public boolean createCategory(String categoryName) {
		Connection con = getConnection();
		if(con == null){
			return false;
		}
		
		PreparedStatement pst = null;
		try{
			String parent = "";
			String simpleName = categoryName;
			int index = categoryName.lastIndexOf(".");
			if(index != -1){
				parent = categoryName.substring(0, index);
				simpleName = categoryName.substring(index + 1, categoryName.length());
			}
			
			pst = con.prepareStatement("insert into tblCategorys(name, parent, simpleName) values(?, ?, ?)");
			pst.setString(1, categoryName);
			pst.setString(2, parent);
			pst.setString(3, simpleName);
			pst.execute();
			
			return true;
		}catch(Exception e){
			logger.error("Create category error", e);
			return false;
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
	public void refresh() {
		rootCategory.refresh();
	}

	@Override
	public boolean remove() {
		return true; //不删除数据库中的数据
	}

	@Override
	public boolean removeCategory(String categoryName) {
		Connection con = getConnection();
		if(con == null){
			return false;
		}
		
		PreparedStatement pst = null;
		try{
			pst = con.prepareStatement("delete from tblCategorys where name=?");
			pst.setString(1, categoryName);
			pst.execute();
			pst.close();
			
			pst = con.prepareStatement("delete from tblThings where category=?");
			pst.setString(1, categoryName);
			pst.execute();
			return true;
		}catch(Exception e){
			logger.error("Delete category error", e);
			return false;
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
	public ThingClassLoader getClassLoader() {
		return World.getInstance().getClassLoader();
	}

	@Override
	public String getClassPath() {
		return World.getInstance().getClassLoader().getClassPath();
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return null;
	}

	@Override
	public URL findResource(String name) {
		return null;
	}

	@Override
	public void init(Properties properties) {
		this.driverClass = properties.getProperty("driverClass");
		this.url = properties.getProperty("url");
		this.dbUser = properties.getProperty("user");
		this.dbPassword = properties.getProperty("password");
				
		try {
			Class.forName(this.driverClass);
		} catch (ClassNotFoundException e) {
			logger.error(this.getName() + " load driver class error", e);
		}
		rootCategory = new JdbcCategory(null, this, null);
	}

	@Override
	public Thing doLoadThing(String thingName) {
		Connection con = getConnection();
		if(con == null){
			return null;
		}
		
		PreparedStatement pst = null;
		ResultSet rs = null;		
		try{
			pst = con.prepareStatement("select * from tblThings where path=?");
			pst.setString(1, thingName);
			rs = pst.executeQuery();
			if(rs.next()){
				Thing thing = new Thing(null, null, null, false);
				
				ThingMetadata metadata = thing.getMetadata();
				metadata.setPath(thingName);
				String category = null;
				int lastDotIndex = thingName.lastIndexOf(".");
				if(lastDotIndex != -1){
					category = thingName.substring(0, lastDotIndex);
				}
				metadata.setCategory(getCategory(category));
				
				ThingCoder coder = World.getInstance().getThingCoder("xer.txt");
				coder.decode(thing, new ByteArrayInputStream(rs.getBytes("data")), 0);
				metadata.setCoderType(coder.getType());
				
				return thing;
			}else{
				return null;
			}
			
		}catch(Exception e){
			logger.error("Load thing error", e);
			return null;
		}finally{
			if(rs != null){
				try{
					rs.close();
				}catch(Exception e){					
				}
			}
			
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
	public boolean doRemoveThing(Thing thing) {
		Connection con = getConnection();
		if(con == null){
			return true;
		}
		
		PreparedStatement pst = null;

		try{
			pst = con.prepareStatement("delete from tblThings where path=?");
			pst.setString(1, thing.getMetadata().getPath());
			pst.execute();
			return true;			
		}catch(Exception e){
			logger.error("Remove thing error", e);
			return false;
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
	public boolean doSaveThing(Thing thing) {
		Connection con = getConnection();
		if(con == null){
			return true;
		}
		
		PreparedStatement pst = null;
		ResultSet rs = null;
		try{
			ThingCoder coder = World.getInstance().getThingCoder("xer.txt");
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			coder.encode(thing, bout);
			
			pst = con.prepareStatement("select * from tblThings where path=?");
			pst.setString(1, thing.getMetadata().getPath());
			rs = pst.executeQuery();
			if(rs.next()){
				//已经存在，更新
				rs.close();
				pst.close();
				
				pst = con.prepareStatement("update tblThings set name=?, label=?, descriptors=?, data=?, lastModified=? where path =?");
				pst.setString(1, thing.getMetadata().getName());
				pst.setString(2, thing.getMetadata().getLabel());
				pst.setString(3, thing.getString("descriptors"));
				pst.setBytes(4, bout.toByteArray());
				pst.setLong(5, thing.getMetadata().getLastModified());
				pst.setString(6, thing.getMetadata().getPath());
				pst.executeUpdate();
			}else{
				//不存在，插入
				//已经存在，更新
				rs.close();
				pst.close();
				
				pst = con.prepareStatement("insert into tblThings(name, label, descriptors, data, lastModified, path, category) values(?, ?, ?, ?, ?,?, ?)");
				pst.setString(1, thing.getMetadata().getName());
				pst.setString(2, thing.getMetadata().getLabel());
				pst.setString(3, thing.getString("descriptors"));
				pst.setBytes(4, bout.toByteArray());
				pst.setLong(5, thing.getMetadata().getLastModified());
				pst.setString(6, thing.getMetadata().getPath());
				Category cat = thing.getMetadata().getCategory();
				if(cat.getName() == null){
					pst.setString(7, "");
				}else{
					pst.setString(7, cat.getName());
				}
				pst.executeUpdate();
			}
			return true;			
		}catch(Exception e){
			logger.error("Save thing error", e);
			return false;
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
}
