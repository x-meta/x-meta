package org.xmeta.index;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.xmeta.Index;

public class FileIndex extends Index{
	File file;
	Index parent;
	
	public FileIndex(Index parent, File file) {
		this.parent = parent;
		this.file = file;
	}
	
	@Override
	public Object getIndexObject() {
		return file;
	}

	@Override
	public Index getParent() {
		return parent;
	}

	@Override
	public List<Index> getChilds() {
		List<Index> indexs = new ArrayList<Index>();
		if(file.isDirectory()) {
			for(File child : file.listFiles()) {
				indexs.add(new FileIndex(this, child));
			}
		}
		
		return indexs;
	}

	@Override
	public String getDescription() {
		return file.getAbsolutePath();
	}

	@Override
	public String getLabel() {
		return file.getName();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getPath() {
		return file.getAbsolutePath();
	}

	@Override
	public String getType() {
		return Index.TYPE_FILE;
	}

	@Override
	public boolean refresh() {
		return true;
	}

	public File getFile() {
		return file;
	}

	@Override
	public long getLastModified() {
		return file.lastModified();	
	}
}
