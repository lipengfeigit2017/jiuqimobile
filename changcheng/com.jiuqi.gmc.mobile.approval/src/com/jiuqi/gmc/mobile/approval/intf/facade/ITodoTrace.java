package com.jiuqi.gmc.mobile.approval.intf.facade;

public interface ITodoTrace {
	public static class ApprovalCode {
		public static final int SUBMIT = 1; // 提交
		public static final int AGREE = 2; // 同意
		public static final int REJECT = 3; // 驳回
		public static final int PENDING = 4; // 等待
		public static final int ENDAPPROVER = 5; // 结束
		public static final int ERROR = 0;// 错误
	}

	/**
	 * 节点名称
	 * @return
	 */
	public String getTitle();
	
	/**
	 * 审批信息
	 * @return
	 */
	public ITodoTraceItem[] getItems();

}
