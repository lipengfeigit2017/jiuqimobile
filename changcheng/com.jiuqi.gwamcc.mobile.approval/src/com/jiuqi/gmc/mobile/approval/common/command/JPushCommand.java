/**============================================================
 * 版权： 久其软件 版权所有 (c) 2002 - 2016
 * 包： com.jiuqi.gmc.mobile.approval.common.command
 * 修改记录：
 * 日期                作者           内容
 * =============================================================
 * 2016年12月22日       qinjunjie        
 * ============================================================*/

package com.jiuqi.gmc.mobile.approval.common.command;

import com.jiuqi.dna.core.service.AsyncInfo;
import com.jiuqi.dna.core.service.AsyncInfo.SessionMode;
import com.jiuqi.fo.common.comm.FoCommand;
import com.jiuqi.gmc.mobile.approval.intf.task.PushMessageTask;

/**
 * <p>
 * 激光推送Command，使用异步方式推送
 * </p>
 * <p>Copyright: 版权所有 (c) 2002 - 2016<br>
 * Company: 久其</p>
 *
 * @author qinjunjie
 * @version 2016年12月22日
 */

public class JPushCommand extends FoCommand{
	
	private PushMessageTask task;
	
	public JPushCommand(PushMessageTask task){
		this.task = task;
	}
	
	@Override
	public void execute(){
		AsyncInfo async = new AsyncInfo();
		async.setSessionMode(SessionMode.INDIVIDUAL_ANONYMOUS);
		getCtx().asyncHandle(task, PushMessageTask.Method.ADD, async);
	}

}