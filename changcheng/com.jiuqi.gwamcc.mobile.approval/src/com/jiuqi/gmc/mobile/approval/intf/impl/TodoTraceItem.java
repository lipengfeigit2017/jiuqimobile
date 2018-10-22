package com.jiuqi.gmc.mobile.approval.intf.impl;

import com.jiuqi.gmc.mobile.approval.intf.facade.ITodoTraceItem;

public class TodoTraceItem implements ITodoTraceItem{

	private String approvalUser ="";
	private String approvalDate ="";
	private String approvalSuggest ="";
	private String phone ="";
	private String approvalResult ="";

	public String getApprovalResult() {
		return approvalResult;
	}

	public void setApprovalResult(String approvalResult) {
		this.approvalResult = approvalResult;
	}

	public String getApprovalUser() {
		return approvalUser;
	}

	public void setApprovalUser(String approvalUser) {
		this.approvalUser = approvalUser;
	}

	public String getApprovalSuggest() {
		return approvalSuggest;
	}

	public void setApprovalSuggest(String approvalSuggest) {
		this.approvalSuggest = approvalSuggest;
	}

	@Override
	public String getApprovalDate() {
		return this.approvalDate;
	}

	@Override
	public String getPhone() {
		return this.phone;
	}


	public void setApprovalDate(String approvalDate) {
		this.approvalDate = approvalDate;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}	
}
