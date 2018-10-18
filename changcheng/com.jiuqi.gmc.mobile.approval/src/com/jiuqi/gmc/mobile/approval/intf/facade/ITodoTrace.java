package com.jiuqi.gmc.mobile.approval.intf.facade;

public interface ITodoTrace {
	public static class ApprovalCode {
		public static final int SUBMIT = 1; // �ύ
		public static final int AGREE = 2; // ͬ��
		public static final int REJECT = 3; // ����
		public static final int PENDING = 4; // �ȴ�
		public static final int ENDAPPROVER = 5; // ����
		public static final int ERROR = 0;// ����
	}

	/**
	 * �ڵ�����
	 * @return
	 */
	public String getTitle();
	
	/**
	 * ������Ϣ
	 * @return
	 */
	public ITodoTraceItem[] getItems();

}
