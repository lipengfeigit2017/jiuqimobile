/**============================================================
 * 版权： 久其软件 版权所有 (c) 2002 - 2016
 * 包： com.jiuqi.sms.intf.impl
 * 修改记录：
 * 日期                作者           内容
 * =============================================================
 * 2016年9月12日       qinjunjie        
 * ============================================================*/
package com.jiuqi.gmc.mobile.approval.intf.impl;

import java.util.Date;

import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.gmc.mobile.approval.intf.facade.FPushMessage;

/**
 * <p>待办消息推送实现类</p>
 *
 * <p>Copyright: 版权所有 (c) 2002 - 2016<br>
 * Company: 久其</p>
 *
 * @author qinjunjie
 * @version 2017年01月04日22:41:25
 */

public class PushMessageImpl implements FPushMessage{

	/**
	 * 主键recid
	 * */
	private GUID recid;

	/**
	 * 版本号recver
	 * */
	private long recver;
	
	/**
	 * 单据定义
	 * */

	private GUID billDefine;
	
	/**
	 * 单据id
	 * */
	private GUID billDataID;
	
	/**
	 * 单据编号
	 * */
	private String billCode;
	
	/**
	 * 工作项ID
	 * */
	private GUID workItemID;
	
	/**
	 * 所有接受者；接受待办提醒的人，极光推送最大支持一次给1000个alias（费控系统对应登录用户的用户名）推送，如果超出1000人需要程序控制为推送多次
	 * */
	private String[] receivers;
	
	/**
	 * 推送的消息内容
	 * */
	private String message;

	/**
	 * 推送时间
	 * */
	private Date sendTime;

	/**
	 * 推送结果信息
	 * */
	private String resultType;
	
	/**
	 * 推送成功标志
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