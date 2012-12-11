package org.xmeta;

/**
 * 在运行中可以通过world添加的帮助。
 * 
 * @author zhangyuxiang
 *
 */
public class Help {
	/**
	 * 帮助的标题。
	 */
	String title;
	
	/** 源事物路径，通常是发生需要帮助的事物的路径 */
	String sourcePath;
	
	/** 帮助路径，提供帮助事物的路径 */
	String helpPath;

	public Help(String title, String sourcePath, String helPath) {
		super();
		this.title = title;
		this.sourcePath = sourcePath;
		this.helpPath = helPath;
	}

	public String getTitle() {
		return title;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public String getHelpPath() {
		return helpPath;
	}
	
	
}
