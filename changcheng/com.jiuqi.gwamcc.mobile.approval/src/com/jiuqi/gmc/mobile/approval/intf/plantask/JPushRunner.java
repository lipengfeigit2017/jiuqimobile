/**============================================================
 * 版权： 久其软件 版权所有 (c) 2002 - 2016
 * 包： com.jiuqi.fo.youse.ui.plantask
 * 修改记录：
 * 日期                作者           内容
 * =============================================================
 * 2017年1月6日       qinjunjie        
 * ============================================================*/

package com.jiuqi.gmc.mobile.approval.intf.plantask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jiuqi.dna.bap.authority.intf.facade.FUser;
import com.jiuqi.dna.bap.plantask.intf.runner.Runner;
import com.jiuqi.dna.bap.plantask.intf.runner.RunnerParameter;
import com.jiuqi.dna.bap.workflowmanager.execute.common.util.WorkflowRunUtil;
import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.dna.workflow.engine.EnumWorkItemState;
import com.jiuqi.dna.workflow.engine.object.ParticipantObject;
import com.jiuqi.dna.workflow.intf.facade.IWorkItem;
import com.jiuqi.fo.logmanage.common.LogManageUtils;
import com.jiuqi.gmc.mobile.approval.common.systemoption.JPushSysOption;
import com.jiuqi.gmc.mobile.approval.intf.facade.FPushMessage;
import com.jiuqi.gmc.mobile.approval.intf.impl.PushMessageImpl;
import com.jiuqi.gmc.mobile.approval.intf.task.PushMessageTask;
import com.jiuqi.vacomm.utils.ConfigUtil;

/**
 * <p>
 * 极光推送计划任务执行
 * </p>
 *
 * <p>
 * Copyright: 版权所有 (c) 2002 - 2016<br>
 * Company: 久其
 * </p>
 *
 * @author qinjunjie
 * @version 2017-01-05 22:09:34
 */
@SuppressWarnings("restriction")
public class JPushRunner extends Runner {
	@Override
	public boolean excute(RunnerParameter param, Context context) {
		// 如果不启用消息推送则直接退出
		if (!ConfigUtil.getSysBoolValue(context, JPushSysOption.IS_PUSH_MSG)) {
			LogManageUtils.printInformation(context, "移动app", "极光推送", new StringBuffer("【费控系统】极光推送系统选项，没有启用极光推送"));
			return false;
		}
		List<FPushMessage> needPush = context.getList(FPushMessage.class);
		if (needPush != null && needPush.size() > 0) {//如果存在消息未推送需要逐条推送
			PushMessageTask task = null;
			PushMessageImpl pushMessageImpl = null;
			for (FPushMessage fPushMsg : needPush) {
				GUID itemID = fPushMsg.getWorkItemID();
				IWorkItem item = WorkflowRunUtil.loadWorkItem(context, itemID.toString());//根据每条记录，取出对应的工作项
				if(item!= null && item.getState() == EnumWorkItemState.ACTIVE){//如果是Active状态的节点才会继续发送消息
					List<String> receivers = new ArrayList<String>(Arrays.asList(fPushMsg.getReceivers()));//接受推送的人
					List<String> approveders = getApprovedUsers(context, item);//过滤掉已经审批了的用户
					for(String name: approveders){
						receivers.remove(name);
					}
					task = new PushMessageTask();
					pushMessageImpl = (PushMessageImpl) fPushMsg;
					pushMessageImpl.setReceivers(receivers.toArray(new String[receivers.size()]));
					task.setPushMessageImpl(pushMessageImpl);
					context.asyncHandle(task, PushMessageTask.Method.ADD);
				}
			}
		}
		return true;
	}

	/**
	 * 获取已经审批了的人.<br>
	 * 
	 * @param model
	 * @return List<FUser>
	 */
	private static List<String> getApprovedUsers(Context context, IWorkItem item) {
		if (item == null)
			return null;
		List<String> result = new ArrayList<String>();
		List<ParticipantObject> participantList = item.getParticipants();
		if (participantList == null || participantList.isEmpty())
			return null;
		String uid = null;
		FUser user = null;
		for (int i = 0, s = participantList.size(); i < s; i++) {
			ParticipantObject po = participantList.get(i);
			//过滤掉：参与了审批的人(如果结点状态为1为已有人审批过（会签结点）)
			if (po.getAction() == 1) {
				uid = po.getUserguid();
				user = context.find(FUser.class, GUID.valueOf(uid));
				if (null != user) {
					result.add(user.getName());
				}
			}
		}
		return result;
	}
}
