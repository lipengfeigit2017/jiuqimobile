/**============================================================
 * ��Ȩ�� ������� ��Ȩ���� (c) 2002 - 2016
 * ���� com.jiuqi.sms.intf.task
 * �޸ļ�¼��
 * ����                ����           ����
 * =============================================================
 * 2016��1��4��       qinjunjie        
 * ============================================================*/
package com.jiuqi.gmc.mobile.approval.intf.task;

import com.jiuqi.dna.core.invoke.Task;
import com.jiuqi.gmc.mobile.approval.intf.impl.PushMessageImpl;
/**
 * <p>������Ϣ���ʹ�����</p>
 *
 * <p>Copyright: ��Ȩ���� (c) 2002 - 2016<br>
 * Company: ����</p>
 *
 * @author qinjunjie
 * @version 2017��01��04��22:41:42
 */

public class PushMessageTask extends Task<PushMessageTask.Method>{

	public enum Method{
		ADD
	}

	private PushMessageImpl pushMessageImpl;

	public PushMessageImpl getPushMessageImpl() {
		return pushMessageImpl;
	}

	public void setPushMessageImpl(PushMessageImpl pushMessageImpl) {
		this.pushMessageImpl = pushMessageImpl;
	}

}