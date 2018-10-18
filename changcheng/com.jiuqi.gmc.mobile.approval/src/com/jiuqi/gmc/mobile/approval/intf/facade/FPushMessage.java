/**============================================================
 * 版权： 久其软件 版权所有 (c) 2002 - 2016
 * 包： com.jiuqi.sms.intf.facade
 * 修改记录：
 * 日期                作者           内容
 * =============================================================
 * 2017年1月4日       qinjunjie        
 * ============================================================*/
package com.jiuqi.gmc.mobile.approval.intf.facade;

import java.util.Date;

import com.jiuqi.dna.core.type.GUID;

/**
 * <p>费控待办消息推送外观类</p>
 *
 * <p>Copyright: 版权所有 (c) 2002 - 2016<br>
 * Company: 久其</p>
 *
 * @author qinjunjie
 * @version 2017-01-04 21:28:33
 */

public interface FPushMessage{

	/**
	 * 获取主键recid
	 * */
	public GUID getRecid();

	/**
	 * 获取版本号recver
	 * */
	public long getRecver();
	
	/**
	 * 获取单据定义
	 * */
	public GUID getBillDefine();
	
	/**
	 * 获取单据id
	 * */
	public GUID getBillDataID();
	
	/**
	 * 工作项ID
	 * */
	public GUID getWorkItemID();
	
	/**
	 * 获取单据编号
	 * */
	public String getBillCode();
	
	/**
	 * 获取所有接受者;
	 * */
	public String[] getReceivers();
	
	/**
	 * 获取推送的消息内容
	 * */
	public String getMessage();

	/**
	 * 获取推送时间
	 * */
	public Date getSendTime();

	/**
	 * 获取推送结果
	 * */
	public String getResultType();
	
	/**
	 * 是否成功推送
	 * */
	public boolean isPushed();

}