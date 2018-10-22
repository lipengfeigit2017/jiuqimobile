package com.jiuqi.gmc.mobile.approval.intf.facade;

public interface ITodoTraceItem {
	
	/**
	 * 审批人
	 * @return
	 */
	public String getApprovalUser();
	
	/**
	 * 审批时间
	 * @return
	 */
	public String getApprovalDate();
	
	/**
	 * 审批意见
	 * @return
	 */
	public String getApprovalSuggest();
	
	/**
	 * 手机号
	 * @return
	 */
	public String getPhone();
	/**
	 * 审批结果
	 * @return  String
	 */
	public String getApprovalResult();
}
