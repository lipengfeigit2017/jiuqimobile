/**============================================================
 * ��Ȩ�� ������� ��Ȩ���� (c) 2002 - 2016
 * ���� com.jiuqi.sms.intf.facade
 * �޸ļ�¼��
 * ����                ����           ����
 * =============================================================
 * 2017��1��4��       qinjunjie        
 * ============================================================*/
package com.jiuqi.gmc.mobile.approval.intf.facade;

import java.util.Date;

import com.jiuqi.dna.core.type.GUID;

/**
 * <p>�ѿش�����Ϣ���������</p>
 *
 * <p>Copyright: ��Ȩ���� (c) 2002 - 2016<br>
 * Company: ����</p>
 *
 * @author qinjunjie
 * @version 2017-01-04 21:28:33
 */

public interface FPushMessage{

	/**
	 * ��ȡ����recid
	 * */
	public GUID getRecid();

	/**
	 * ��ȡ�汾��recver
	 * */
	public long getRecver();
	
	/**
	 * ��ȡ���ݶ���
	 * */
	public GUID getBillDefine();
	
	/**
	 * ��ȡ����id
	 * */
	public GUID getBillDataID();
	
	/**
	 * ������ID
	 * */
	public GUID getWorkItemID();
	
	/**
	 * ��ȡ���ݱ��
	 * */
	public String getBillCode();
	
	/**
	 * ��ȡ���н�����;
	 * */
	public String[] getReceivers();
	
	/**
	 * ��ȡ���͵���Ϣ����
	 * */
	public String getMessage();

	/**
	 * ��ȡ����ʱ��
	 * */
	public Date getSendTime();

	/**
	 * ��ȡ���ͽ��
	 * */
	public String getResultType();
	
	/**
	 * �Ƿ�ɹ�����
	 * */
	public boolean isPushed();

}