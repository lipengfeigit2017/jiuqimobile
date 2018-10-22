/**============================================================
 * ��Ȩ�� ������� ��Ȩ���� (c) 2002 - 2016
 * ���� com.jiuqi.gmc.mobile.approval.common.command
 * �޸ļ�¼��
 * ����                ����           ����
 * =============================================================
 * 2017��1��5��       qinjunjie        
 * ============================================================*/
package com.jiuqi.gmc.mobile.approval.common;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jiuqi.dna.bap.workflowmanager.execute.intf.entity.TaskListDefine;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.facade.FTaskListDefine;
import com.jiuqi.dna.core.Context;
import com.jiuqi.fo.logmanage.common.LogManageUtils;
import com.jiuqi.gmc.mobile.approval.common.systemoption.JPushSysOption;
import com.jiuqi.gmc.mobile.approval.intf.impl.PushMessageImpl;
import com.jiuqi.mt2.dna.mobile.todo.facade.FApprovalDefine;
import com.jiuqi.vacomm.utils.ConfigUtil;
import com.jiuqi.vacomm.utils.ConfigUtil.SaveType;
import com.jiuqi.xlib.utils.StringUtil;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

/**
 * <p>��������</p>
 * ���ɼ������ͷ��񹤾��࣬�������ƶ������ʹ������ѡ�</br>
 * <a href="https://docs.jiguang.cn/jpush/guideline/intro/" >�ٷ��ĵ�</a>
 * @author qinjunjie
 * @version 2017��01��05��00:16:39
 * */
public class JPushUtil {

	public boolean pushMessage(Context ctx, PushMessageImpl pushMessage) {
		StringBuffer log = null;
		String appKey = ConfigUtil.getString(ctx,JPushSysOption.APP_KEY, SaveType.SYS);
		if (StringUtil.isEmpty(appKey)) {
			log = new StringBuffer("���ѿ�ϵͳ����������ϵͳѡ�� APP_KEYû������");
			LogManageUtils.printInformation(ctx, "�ƶ�app", "��������", log);
			pushMessage.setSendTime(new Date());
			pushMessage.setResultType(log.toString());
			pushMessage.setPushed(false);
			return false;
		}
		String masterSecret = ConfigUtil.getString(ctx,JPushSysOption.MASTER_SECRET, SaveType.SYS);
		if (masterSecret.isEmpty()) {
			log = new StringBuffer("���ѿ�ϵͳ����������ϵͳѡ�� MASTER_SECRETû������");
			LogManageUtils.printInformation(ctx, "�ƶ�app", "��������", log);
			pushMessage.setSendTime(new Date());
			pushMessage.setResultType(log.toString());
			pushMessage.setPushed(false);
			return false;
		}
		ClientConfig config = ClientConfig.getInstance();
		JPushClient jpushClient = new JPushClient(masterSecret, appKey, null, config);
		String msg = pushMessage.getMessage();
		Map<String,String> extras = new HashMap<String, String>();
		extras.put("billDefineID", pushMessage.getBillDefine().toString());
		extras.put("billDataID", pushMessage.getBillDataID().toString());
		extras.put("workItemID", pushMessage.getWorkItemID().toString());
		extras.put("flowtype", "0");//ǰ̨�Զ����ʾ 0:���죻1�Ѱ�
		extras.put("fApprovalDefine", getFTaskDefine(ctx, pushMessage.getBillDefine().toString()));
		PushPayload payload = PushPayload.newBuilder().setPlatform(Platform.android_ios())
				.setAudience(Audience.alias(pushMessage.getReceivers()))
				.setNotification(Notification.newBuilder().setAlert(pushMessage.getMessage())
						.addPlatformNotification(AndroidNotification.newBuilder().addExtras(extras).build())
						.addPlatformNotification(IosNotification.newBuilder().incrBadge(1).addExtras(extras).build())
						.build())
				.setOptions(Options.newBuilder()
                        .setApnsProduction(true)
                        .build())
				.build();
		try {
			PushResult result = jpushClient.sendPush(payload);
			pushMessage.setSendTime(new Date());
			pushMessage.setResultType(result.toString());
			pushMessage.setPushed(true);
			return true;
		} catch (APIConnectionException e) {
			log = new StringBuffer("������Ϣ��" + msg + "\n����ʧ�ܣ�ʧ��ԭ�����Ӽ������ͷ�����ʧ�ܣ��ȱ�֤�������ɷ��ʼ�����������Ժ�����");
			LogManageUtils.printExeception(ctx, "�ƶ�app", "��������", log, e);
			pushMessage.setSendTime(new Date());
			pushMessage.setResultType("���ӷ�������ʱ");
			pushMessage.setPushed(false);
			return false;
		} catch (APIRequestException e) {
			log = new StringBuffer("����ʧ�ܵ���Ϣ��" + msg
					+ "\n����ʧ�ܣ�ʧ��ԭ��Error Code: " + e.getErrorCode() 
					+ "\nMsg ID: " + e.getMsgId()
					+ "\nError Message: " + e.getErrorMessage()
					+ "\nHTTP Status: " + e.getStatus());
			LogManageUtils.printInformation(ctx, "�ƶ�app", "��������", log);
			pushMessage.setSendTime(new Date());
			pushMessage.setResultType(String.valueOf(e.getErrorCode()));
			pushMessage.setPushed(false);
			return false;
		}
	}

	private String getFTaskDefine(Context context, String billDefineID) {
		List<FTaskListDefine> list = context.getList(FTaskListDefine.class);
		for (int i = 0; i < list.size(); i++) {
			TaskListDefine taskListDefine = list.get(i).getTaskListDefine();
			if (taskListDefine != null) {
				if (taskListDefine.businessObjectList.size() > 0) {
					if (taskListDefine.businessObjectList.get(0).getID().toString().equals(billDefineID)){
						List<FApprovalDefine> defines = context.getList(FApprovalDefine.class);
						for (FApprovalDefine define : defines) {
							if (define.getTaskDefineID().toString().equals(taskListDefine.getID().toString())) {
								return define.getRECID().toString();
							}
						}
					}	
				}
			}
		}
		return "";
	}
	
}
