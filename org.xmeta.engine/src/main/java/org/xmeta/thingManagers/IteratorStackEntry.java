package org.xmeta.thingManagers;

import java.util.List;

import org.xmeta.Category;
import org.xmeta.ThingIndex;

public class IteratorStackEntry {
	Category category;
	int categoryIndex;
	int thingIndex;
	List<ThingIndex> thingIndexs;
	List<Category> categorys;
}
