package com.jiuqi.gmc.mobile.approval.intf.facade;

public interface ITodoTraceItem {
	
	/**
	 * ������
	 * @return
	 */
	public String getApprovalUser();
	
	/**
	 * ����ʱ��
	 * @return
	 */
	public String getApprovalDate();
	
	/**
	 * �������
	 * @return
	 */
	public String getApprovalSuggest();
	
	/**
	 * �ֻ���
	 * @return
	 */
	public String getPhone();
	/**
	 * �������
	 * @return  String
	 */
	public String getApprovalResult();
}
