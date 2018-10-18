package com.jiuqi.gmc.mobile.approval.intf.impl;

import com.jiuqi.gmc.mobile.approval.intf.facade.ITodoTrace;
import com.jiuqi.gmc.mobile.approval.intf.facade.ITodoTraceItem;

public class TodoTrace implements ITodoTrace{

	private String title;
	private ITodoTraceItem[] items;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ITodoTraceItem[] getItems() {
		return items;
	}
	public void setItems(ITodoTraceItem[] items) {
		this.items = items;
	}


		
}
