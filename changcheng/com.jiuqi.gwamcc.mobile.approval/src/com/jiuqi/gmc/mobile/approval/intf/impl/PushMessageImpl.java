/**============================================================
 * ��Ȩ�� ������� ��Ȩ���� (c) 2002 - 2016
 * ���� com.jiuqi.sms.intf.impl
 * �޸ļ�¼��
 * ����                ����           ����
 * =============================================================
 * 2016��9��12��       qinjunjie        
 * ============================================================*/
package com.jiuqi.gmc.mobile.approval.intf.impl;

import java.util.Date;

import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.gmc.mobile.approval.intf.facade.FPushMessage;

/**
 * <p>������Ϣ����ʵ����</p>
 *
 * <p>Copyright: ��Ȩ���� (c) 2002 - 2016<br>
 * Company: ����</p>
 *
 * @author qinjunjie
 * @version 2017��01��04��22:41:25
 */

public class PushMessageImpl implements FPushMessage{

	/**
	 * ����recid
	 * */
	private GUID recid;

	/**
	 * �汾��recver
	 * */
	private long recver;
	
	/**
	 * ���ݶ���
	 * */

	private GUID billDefine;
	
	/**
	 * ����id
	 * */
	private GUID billDataID;
	
	/**
	 * ���ݱ��
	 * */
	private String billCode;
	
	/**
	 * ������ID
	 * */
	private GUID workItemID;
	
	/**
	 * ���н����ߣ����ܴ������ѵ��ˣ������������֧��һ�θ�1000��alias���ѿ�ϵͳ��Ӧ��¼�û����û��������ͣ��������1000����Ҫ�������Ϊ���Ͷ��
	 * */
	private String[] receivers;
	
	/**
	 * ���͵���Ϣ����
	 * */
	private String message;

	/**
	 * ����ʱ��
	 * */
	private Date sendTime;

	/**
	 * ���ͽ����Ϣ
	 * */
	private String resultType;
	
	/**
	 * ���ͳɹ���־
	 * */
	private boolean isPushed;

	public GUID getRecid() {
		return recid;
	}

	public void setRecid(GUID recid) {
		this.recid = recid;
	}

	public long getRecver() {
		return recver;
	}

	public void setRecver(long recver) {
		this.recver = recver;
	}

	public GUID getBillDefine() {
		return billDefine;
	}

	public void setBillDefine(GUID billDefine) {
		this.billDefine = billDefine;
	}

	public GUID getBillDataID() {
		return billDataID;
	}

	public void setBillDataID(GUID billDataID) {
		this.billDataID = billDataID;
	}

	public String getBillCode() {
		return billCode;
	}

	public void setBillCode(String billCode) {
		this.billCode = billCode;
	}

	public GUID getWorkItemID() {
		return workItemID;
	}

	public void setWorkItemID(GUID workItemID) {
		this.workItemID = workItemID;
	}

	public String[] getReceivers() {
		return receivers;
	}

	public void setReceivers(String[] receivers) {
		this.receivers = receivers;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public String getResultType() {
		return resultType;
	}

	public void setResultType(String resultType) {
		this.resultType = resultType;
	}

	public boolean isPushed() {
		return isPushed;
	}

	public void setPushed(boolean isPushed) {
		this.isPushed = isPushed;
	}

}