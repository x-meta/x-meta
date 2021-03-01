package org.xmeta.thingManagers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingMetadata;
import org.xmeta.World;
import org.xmeta.util.ThingClassLoader;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 使用Redis内存数据库保存的事物的事物管理器。
 * 
 * @author Administrator
 *
 */
public class RedisThingManager extends AbstractThingManager{
	@Override
	public String toString() {
		return "RedisThingManager [host=" + host + ", port=" + port + ", jedisPool=" + jedisPool + "]";
	}

	/** Redist服务器的地址 */
	String host;
	/** Redist服务器的端口 */
	int port;
	/** Redist服务器的密码 */
	String password;
	
	JedisPool jedisPool;
	
	public RedisThingManager(String name, File rootFile) throws IOException{
		super(name);
	}
	
	/**
	 * 返回Jedis示例。
	 * 
	 * @return Jedis实例
	 */
	public Jedis getJedis(){
		return jedisPool.getResource();
	}
	
	/**
	 * 释放连接。
	 * 
	 * @param jedis jedis
	 */
	public void releaseJedis(Jedis jedis){
		jedis.close();
	}
	
	@Override
	public boolean createCategory(String categoryName) {
		Jedis jedis = jedisPool.getResource();
		try{
			jedis.set("category|" + categoryName, categoryName);
			
			this.refreshParentCategory(categoryName);
		}finally{
			jedis.close();
		}
		return true;
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
		Jedis jedis = jedisPool.getResource();
		try{
			//删除目录和子目录
			Set<String> categorys = jedis.keys("category|" + categoryName + "*");
			String[] keys = new String[categorys.size()];
			int index = 0;
			for(String name : categorys){
				keys[index] = name;
				index++;
			}			
			jedis.del(keys);
			
			//删除目录下的文件
			Set<String> things = jedis.keys("thing|" + categoryName + ".*");
			keys = new String[things.size()];
			index = 0;
			for(String name : things){
				keys[index] = name;
				index++;
			}			
			jedis.del(keys);
			
			this.refreshParentCategory(categoryName);
		}finally{
			jedis.close();
		}
		return true;
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
		super.init(properties);
		
		String host = properties.getProperty("host");
		int port = Integer.parseInt(properties.getProperty("port"));
		String password = properties.getProperty("password");
		
		//创建并测试Jedis连接池
		jedisPool = new JedisPool(new JedisPoolConfig(), host, port, 5000, password);
		Jedis jedis = jedisPool.getResource();
		jedis.close();
		
		rootCategory = new RedisCategory(null, this, null);
	}

	@Override
	public Thing doLoadThing(String thingName) {
		Jedis jedis = jedisPool.getResource();
		try{
			String thingCode = jedis.get("thing|" + thingName);
			if(thingCode != null){
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
				coder.decode(thing, new ByteArrayInputStream(thingCode.getBytes()), 0);
				metadata.setCoderType(coder.getType());
				
				return thing;
			}else{
				return null;
			}
		}finally{
			jedis.close();			
		}
	}

	@Override
	public boolean doRemoveThing(Thing thing) {
		Jedis jedis = jedisPool.getResource();
		try{
			jedis.del("thing|" + thing.getMetadata().getPath());
		}finally{
			jedis.close();
		}
		return true;
	}

	@Override
	public boolean doSaveThing(Thing thing) {
		Jedis jedis = jedisPool.getResource();
		try{
			ThingCoder coder = World.getInstance().getThingCoder("xer.txt");
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			coder.encode(thing, bout);
			jedis.set("thing|" + thing.getMetadata().getPath(), new String(bout.toByteArray()));
			try {
				bout.close();
			} catch (IOException e) {
			}
		}finally{
			jedis.close();
		}
		return true;
	}

	@Override
	public boolean isSaveable() {
		return true;
	}

}
