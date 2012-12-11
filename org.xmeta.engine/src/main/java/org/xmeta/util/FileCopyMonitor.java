package org.xmeta.util;

import java.io.File;

/**
 * 文件拷贝监控者。
 * 
 * @author zhangyuxiang
 *
 */
public interface FileCopyMonitor {
	/**
	 * 如果目标文件存在，返回要被覆盖的文件。
	 * 如果要覆盖目标文件，可直接返回目标文件，否则可以返回
	 * 一个可以覆盖的文件，如果取消返回null。
	 * 
	 * @param src
	 * @param dest
	 * @return
	 */
	public File onOverWrite(File src, File dest);
	
	/**
	 * 当文件拷贝成功后触发的事件。
	 * 
	 * @param src
	 * @param dest
	 */
	public void onCopyed(File src, File dest);
	
	/**
	 * 当不覆盖一个已存在文件后，会调用此方法决定是否
	 * 取消整个拷贝任务。
	 * 
	 * @return
	 */
	public boolean cancel();
}
