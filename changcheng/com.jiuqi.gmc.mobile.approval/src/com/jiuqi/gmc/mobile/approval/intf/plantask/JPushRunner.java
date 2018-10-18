/**============================================================
 * ��Ȩ�� ������� ��Ȩ���� (c) 2002 - 2016
 * ���� com.jiuqi.fo.youse.ui.plantask
 * �޸ļ�¼��
 * ����                ����           ����
 * =============================================================
 * 2017��1��6��       qinjunjie        
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
 * �������ͼƻ�����ִ��
 * </p>
 *
 * <p>
 * Copyright: ��Ȩ���� (c) 2002 - 2016<br>
 * Company: ����
 * </p>
 *
 * @author qinjunjie
 * @version 2017-01-05 22:09:34
 */
@SuppressWarnings("restriction")
public class JPushRunner extends Runner {
	@Override
	public boolean excute(RunnerParameter param, Context context) {
		// �����������Ϣ������ֱ���˳�
		if (!ConfigUtil.getSysBoolValue(context, JPushSysOption.IS_PUSH_MSG)) {
			LogManageUtils.printInformation(context, "�ƶ�app", "��������", new StringBuffer("���ѿ�ϵͳ����������ϵͳѡ�û�����ü�������"));
			return false;
		}
		List<FPushMessage> needPush = context.getList(FPushMessage.class);
		if (needPush != null && needPush.size() > 0) {//���������Ϣδ������Ҫ��������
			PushMessageTask task = null;
			PushMessageImpl pushMessageImpl = null;
			for (FPushMessage fPushMsg : needPush) {
				GUID itemID = fPushMsg.getWorkItemID();
				IWorkItem item = WorkflowRunUtil.loadWorkItem(context, itemID.toString());//����ÿ����¼��ȡ����Ӧ�Ĺ�����
				if(item!= null && item.getState() == EnumWorkItemState.ACTIVE){//�����Active״̬�Ľڵ�Ż����������Ϣ
					List<String> receivers = new ArrayList<String>(Arrays.asList(fPushMsg.getReceivers()));//�������͵���
					List<String> approveders = getApprovedUsers(context, item);//���˵��Ѿ������˵��û�
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
	 * ��ȡ�Ѿ������˵���.<br>
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
			//���˵�����������������(������״̬Ϊ1Ϊ����������������ǩ��㣩)
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
