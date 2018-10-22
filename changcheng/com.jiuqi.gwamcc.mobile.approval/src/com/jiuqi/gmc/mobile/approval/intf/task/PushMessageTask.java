/**============================================================
 * 版权： 久其软件 版权所有 (c) 2002 - 2016
 * 包： com.jiuqi.sms.intf.task
 * 修改记录：
 * 日期                作者           内容
 * =============================================================
 * 2016年1月4日       qinjunjie        
 * ============================================================*/
package com.jiuqi.gmc.mobile.approval.intf.task;

import com.jiuqi.dna.core.invoke.Task;
import com.jiuqi.gmc.mobile.approval.intf.impl.PushMessageImpl;
/**
 * <p>待办消息推送处理类</p>
 *
 * <p>Copyright: 版权所有 (c) 2002 - 2016<br>
 * Company: 久其</p>
 *
 * @author qinjunjie
 * @version 2017年01月04日22:41:42
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