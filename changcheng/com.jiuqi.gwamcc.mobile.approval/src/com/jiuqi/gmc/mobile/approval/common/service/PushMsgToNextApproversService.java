/**============================================================
 * ��Ȩ�� ������� ��Ȩ���� (c) 2002 - 2016
 * ���� com.jiuqi.gmc.mobile.approval.common.service
 * �޸ļ�¼��
 * ����                ����           ����
 * =============================================================
 * 2016��12��22��       qinjunjie        
 * ============================================================*/

package com.jiuqi.gmc.mobile.approval.common.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jiuqi.dna.bap.authority.intf.facade.FUser;
import com.jiuqi.dna.bap.bill.common.model.BillCentre;
import com.jiuqi.dna.bap.bill.common.model.BillModel;
import com.jiuqi.dna.bap.bill.common.workflow.task.BillApprovalTask;
import com.jiuqi.dna.bap.bill.intf.facade.model.FBillDefine;
import com.jiuqi.dna.bap.bill.intf.model.BillConst;
import com.jiuqi.dna.bap.log.intf.task.AddLogInfoTask;
import com.jiuqi.dna.bap.workflowmanager.common.consts.WorkflowConsts;
import com.jiuqi.dna.bap.workflowmanager.common.event.WorkflowManagerEvent;
import com.jiuqi.dna.bap.workflowmanager.execute.common.BusinessProcessManager;
import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.service.Publish;
import com.jiuqi.dna.core.service.Service;
import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.dna.ui.wt.InfomationException;
import com.jiuqi.dna.workflow.WorkflowException;
import com.jiuqi.dna.workflow.engine.EnumWorkItemState;
import com.jiuqi.dna.workflow.engine.WorkItem;
import com.jiuqi.dna.workflow.engine.object.ParticipantObject;
import com.jiuqi.dna.workflow.frame.common.util.WorkflowUtils;
import com.jiuqi.dna.workflow.intf.facade.IWorkItem;
import com.jiuqi.fo.common.comm.CommandLocal;
import com.jiuqi.fo.common.event.FoAddApprovalEvent;
import com.jiuqi.fo.common.foconst.FieldConst;
import com.jiuqi.fo.workflow.common.util.WorkflowUtil;
import com.jiuqi.fo.workflow.intf.constant.WorkFlowExtendEnum;
import com.jiuqi.fo.workflow.intf.facade.FWorkflowDelegate;
import com.jiuqi.fo.workflow.intf.util.WorkFlowConst;
import com.jiuqi.gmc.mobile.approval.common.command.JPushCommand;
import com.jiuqi.gmc.mobile.approval.common.systemoption.JPushSysOption;
import com.jiuqi.gmc.mobile.approval.intf.impl.PushMessageImpl;
import com.jiuqi.gmc.mobile.approval.intf.task.PushMessageTask;
import com.jiuqi.vacomm.env.EnvCenter;
import com.jiuqi.vacomm.env.FUnitOption;
import com.jiuqi.vacomm.utils.ConfigUtil;
import com.jiuqi.xlib.utils.StringUtil;

/**
 * <p>���ʹ�����Ϣ���ƶ��ͻ���</p>
 *
 * <p>Copyright: ��Ȩ���� (c) 2002 - 2016<br>
 * Company: ����</p>
 *
 * @author qinjunjie
 * @version 2016��12��22��
 */
@SuppressWarnings("restriction")
public class PushMsgToNextApproversService extends Service{

	protected PushMsgToNextApproversService(){
		super("PushMsgToNextApproversService");
	}

	@Publish
	protected class WorkflowListener extends EventListener<WorkflowManagerEvent>{

		@Override
		protected void occur(Context context, WorkflowManagerEvent event) throws Throwable{
			try{
				//�����������Ϣ������ֱ���˳�
				if(!ConfigUtil.getSysBoolValue(context, JPushSysOption.IS_PUSH_MSG)){
					return;
				}
				if(event.eventType == WorkflowManagerEvent.EventType.RefreshParticipant) {
					return;
				}
				//WorkItemStartFinishʹevent�е�context��Ϊ��
				if(event.eventType == WorkflowManagerEvent.EventType.WorkItemStartFinish) {
					return;
				}
				if(event.itemID == null) {
					return;
				}
				if(event.getFBobject() == null) {
					return;
				}
				String businessObjectType = event.getFBobject().getWFBusinessObjectType();
				if(businessObjectType != null && !businessObjectType.endsWith("����")) {
					return;
				}
				if(!event.instance.getEngine().getName().equals("bill")) {
					return;
				}
				if(event.instance.getParam(WorkflowConsts.procommit, true) != null) {
					return;
				}

				FBillDefine billDefine = BillCentre.findBillDefine(context, event.getBusinessObjectID());
				if(billDefine == null) {
					return;
				}
				BillModel model = null;
				if(event.eventType == WorkflowManagerEvent.EventType.WorkItemFinish
				        || event.eventType == WorkflowManagerEvent.EventType.WorkItemAutoFinish)
				{//ͬ�⣨�������̽���ʱ��ͬ�⣩
					model = (BillModel)billDefine.createModel(context, event.getBOdataID());
					FUser createUser = model.getContext().find(FUser.class,
					        model.getModelData().getMaster().getValueAsGUID("CREATEUSERID"));
					IWorkItem item = WorkflowUtils.getAdaptor(event.item);//��ǰ������
					List<ParticipantObject> list;
					FUser user;
					FUnitOption op = EnvCenter.getUnitOption(model.getContext(),
					        model.getModelData().getMaster().getValueAsGUID(FieldConst.f_unitID), "FO067");//ϵͳѡ�����Ƿ��Ƴ�ί����
					boolean bap_DelegateDone =
					        Boolean.valueOf(event.item.getActiveNode().getProperty(WorkflowConsts.BAP_DELEGATENODE));//�������ڵ�����ί���Ƿ�ѡ
					boolean removeDelegater = op.getBoolValue();
					String title = model.getDefine().getBillInfo().getTitle();
					String name = createUser.getName();
					String billCode = model.getModelData().getMaster().getValueAsString(BillConst.f_billCode);
					if(event.item.getActionId() == 1) {//ͬ��
						//��������ɷ��ʹ���
						if(event.item.getState() == EnumWorkItemState.COMPLETE) {
							//ͬ�����Ӵ���
							IWorkItem nextItem =
							        BusinessProcessManager.getNextManaulItem(event.context, item.getGuid());
							if(nextItem != null) {
								Set<FUser> receiverList = getReceivesList(context, event, nextItem, model,
								        bap_DelegateDone, removeDelegater);
								String[] receivers = new String[receiverList.size()];
								int i=0;
								for(FUser receiverUser : receiverList){
									receivers[i] = receiverUser.getName();
									i++;
								}
								GUID billDefineID = event.getBusinessObjectID();
								GUID billDataID = model.getModelData().getMaster().getRECID();
								GUID workItemID = nextItem.getGuid();
								PushMessageTask task = new PushMessageTask();
								PushMessageImpl impl = new PushMessageImpl();
								impl.setBillDefine(billDefineID);
								impl.setBillDataID(billDataID);
								impl.setWorkItemID(workItemID);
								impl.setReceivers(receivers);
								impl.setBillCode(billCode);
								StringBuilder msg = new StringBuilder();
								msg.append("���յ�һ��\"");
								msg.append(title);
								msg.append("\"���죬���ݱ�ţ�");
								msg.append(billCode);
								msg.append(",�ᵥ�ˣ�");
								msg.append(name);
								impl.setMessage(msg.toString());
								task.setPushMessageImpl(impl);
								JPushCommand cmd = new JPushCommand(task); 
								CommandLocal.getDefault().add(cmd);
							}
						}
					}
					else if(event.item.getActionId() == 3) {//�ύ
						IWorkItem nextItem = BusinessProcessManager.getNextManaulItem(context, item.getGuid());
						String receiver = "";
						if(nextItem != null) {
							list = nextItem.getParticipants();
							if(list == null || list.size() == 0) {
								return;
							}
							List<FUser> receiverUserNameList = new ArrayList<FUser>();
							for(int i = 0; i < list.size(); i++){
								user = context.find(FUser.class, GUID.valueOf(list.get(i).getUserguid()));
								receiver = user.getName();
								if(receiver != null && receiver.equalsIgnoreCase(createUser.getName())) {
									//�����˺��ᵥ����ͬ�Զ�ͬ��
									if(!sameWithCreaterAutoAgree(model, nextItem)) {
										if(!bap_DelegateDone && removeDelegater) {
											receiverUserNameList.add(user);
										}
										receiverUserNameList.addAll(getDelegater(context,
										        nextItem.getAdaptor(WorkItem.class), model, createUser));
									}
									else{
										if(nextItem.getActiveNode().getMultiSubscriptAction() == 1) {//�Զ�ͬ�⣬��ǩ�ڵ�
											receiverUserNameList.clear();
											break;
										}
										else{
											continue;
										}
									}
								}
								else{
									receiverUserNameList.add(user);
								}
							}
							String[] receivers = new String[receiverUserNameList.size()];
							int i=0;
							for(FUser receiverUser : receiverUserNameList){
								receivers[i] = receiverUser.getName();
								i++;
							}
							GUID billDefineID = event.getBusinessObjectID();
							GUID billDataID = model.getModelData().getMaster().getRECID();
							GUID workItemID = nextItem.getGuid();
							PushMessageTask task = new PushMessageTask();
							PushMessageImpl impl = new PushMessageImpl();
							impl.setBillDefine(billDefineID);
							impl.setBillDataID(billDataID);
							impl.setWorkItemID(workItemID);
							impl.setReceivers(receivers);
							impl.setBillCode(billCode);
							StringBuilder msg = new StringBuilder();
							msg.append("���յ�һ��\"");
							msg.append(title);
							msg.append("\"���죬���ݱ�ţ�");
							msg.append(billCode);
							msg.append(",�ᵥ�ˣ�");
							msg.append(name);
							impl.setMessage(msg.toString());
							task.setPushMessageImpl(impl);
							JPushCommand cmd = new JPushCommand(task); 
							CommandLocal.getDefault().add(cmd);
						}
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
				throw new InfomationException("���ʹ�����ƶ��˵ļ��������������쳣");
			}
		}

	}

	/**
	 * 
	 * sameWithCreaterAutoAgree:(�����˺��Ƶ�����ͬ�Ƿ��Զ�ͬ��). <br/>
	 *
	 * @param model
	 * @param event
	 * @return
	 */
	private boolean sameWithCreaterAutoAgree(BillModel model, IWorkItem item){
		Context context = model.getContext();
		boolean isAutoAgree = false;
		//ϵͳѡ���Ƿ����� ���Ƶ��˺���������ͬ���Զ�ͬ��ýڵ㡱
		if(model != null && model.getDefine().getMasterTable().findField("UNITID") != null
		        && model.getModelData().getMaster().getValueAsGUID("UNITID") != null)
		    isAutoAgree =
		            EnvCenter.getUnitOption(context, model.getModelData().getMaster().getValueAsGUID("UNITID"), "FO042")
		                    .getBoolValue();
		if(!isAutoAgree) {
			return false;
		}
		if(item == null) return false;
		//�ڵ��Ƿ�ѡ���Ƶ��˺���������ͬʱ���Զ�ͬ�⡱
		String result = item.getActiveNode().getProperty(WorkFlowExtendEnum.AUTO_AGREE.getKey());
		if("true".equals(result)) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * getReceivesList:(��ȡ���Ӵ����USERNAME). <br/>
	 *
	 * @param context
	 * @param event
	 * @param nextItem
	 * @param model
	 * @return
	 * @throws WorkflowException
	 */
	private Set<FUser> getReceivesList(Context context, WorkflowManagerEvent event, IWorkItem nextItem, BillModel model,
	        boolean bap_DelegateDone, boolean removeDelegater) throws WorkflowException
	{
		IWorkItem item = WorkflowUtils.getAdaptor(event.item);//��ǰ������
		Set<FUser> receiverList = new HashSet<FUser>();
		List<ParticipantObject> list = nextItem.getParticipants();
		if(list == null || list.size() == 0) {
			return receiverList;
		}
		Boolean flag = false;
		int multiSubscript = 0;//�ǻ�ǩ
		if(item.getActiveNode().getMultiSubscriptAction() == 1) {
			multiSubscript = 1;//��ǩͬ��
		}
		FUser user;
		String receiver = "";
		String currUserName = EnvCenter.getCurrUser(event.context).getLoginName();
		for(int i = 0; i < list.size(); i++){
			user = context.find(FUser.class, GUID.valueOf(list.get(i).getUserguid()));
			receiver = user.getName();
			if(receiver.equalsIgnoreCase(currUserName) && (multiSubscript == 0 || list.size() == 1)) {
				flag = true;
				break;
			}
		}
		if(!flag) {
			for(int i = 0; i < list.size(); i++){
				user = context.find(FUser.class, GUID.valueOf(list.get(i).getUserguid()));
				receiver = user.getName();
				BillApprovalTask billTask = new BillApprovalTask(
				        ((FBillDefine)model.getDefine()).getBillInfo().getRecID(), (BillModel)model);
				billTask.businessInstanceID = ((BillModel)model).getData().getMaster().getRECID();
				List<WorkItem> history = null;
				history = item.getProcessInstance().getHistories();
				if(history != null && history.size() > 0) {
					for(int j = 0; j < history.size(); j++){
						if(flag) {
							break;
						}
						List<ParticipantObject> parList = history.get(j).getParticipant();
						for(ParticipantObject par : parList){
							if(par.getUserguid() != null && par.getUserguid().equals(list.get(i).getGuid().toString())
							        && par.getAction() == 3)
							{
								if(sameWithCreaterAutoAgree(model, nextItem)) {//�ᵥ�˺���������ͬ�Զ�ͬ��
									if(multiSubscript == 0 || list.size() == 1) {//�ǻ�ǩ��ֻ��һ���û�
										receiverList.clear();
										flag = true;
										break;
									}
									else if(multiSubscript == 1) {//��ǩ
										continue;
									}
								}
							}
							if(par.getUserguid() != null && par.getUserguid().equals(list.get(i).getUserguid())
							        && par.getAction() == 1)
							{
								if(multiSubscript == 0 || list.size() == 1) {//�ǻ�ǩ����ֻ��һ���û�,����������ʷ��¼���Ѿ�����ͬ���¼�����Զ�ͬ��
									flag = true;
									receiverList.clear();
									break;
								}
								else if(multiSubscript == 1) {//��ǩ������������ʷ��¼���Ѿ�����ͬ���¼,�Զ�ͬ��
									continue;
								}
							}
						}
					}
				}
				if(flag) {
					break;
				}
				else{
					FUser tmpUser = context.find(FUser.class, GUID.valueOf((list.get(i).getUserguid())));
					if(!bap_DelegateDone || !removeDelegater) {
						receiverList.add(tmpUser);
					}
					if(bap_DelegateDone && removeDelegater) {
						receiverList.addAll(getDelegater(context, item.getAdaptor(WorkItem.class), model, tmpUser));
					}
				}
			}
		}
		return receiverList;
	}

	/**
	 * ��ǩ����
	 */
	@Publish
	protected class WorkflowAddApproverEvent extends EventListener<FoAddApprovalEvent>{

		@Override
		protected void occur(Context context, FoAddApprovalEvent event) throws Throwable{
			Map<GUID, Map<String, String>> userDatas = event.getUserDatas();
			if(userDatas != null) {
				for(GUID recUserID : userDatas.keySet()){//����ǩ��
					Map<String, String> sendUserMap = userDatas.get(recUserID);
					String sendUserID = sendUserMap.get(WorkFlowConst.AddApproverStaff);
					if(!StringUtil.isEmpty(sendUserID)) {
						FUser receiverUser = context.find(FUser.class, recUserID);
						GUID billDefineID = event.getModel().getDefine().getBillInfo().getRecID();
						GUID billDataID = event.getModel().getModelData().getMaster().getRECID();
						GUID workItemID = event.getWorkItem().getGuid();
						String title = event.getModel().getDefine().getBillInfo().getTitle();
						FUser createUser = event.getModel().getContext().find(FUser.class,
								event.getModel().getModelData().getMaster().getValueAsGUID("CREATEUSERID"));
						String name = createUser.getName();
						String billCode = event.getModel().getModelData().getMaster().getValueAsString(BillConst.f_billCode);
						PushMessageTask task = new PushMessageTask();
						PushMessageImpl impl = new PushMessageImpl();
						impl.setBillDefine(billDefineID);
						impl.setBillDataID(billDataID);
						impl.setWorkItemID(workItemID);
						impl.setReceivers(new String[]{receiverUser.getName()});
						impl.setBillCode(billCode);
						StringBuilder msg = new StringBuilder();
						msg.append("���յ�һ��\"");
						msg.append(title);
						msg.append("\"���죬���ݱ�ţ�");
						msg.append(billCode);
						msg.append(",�ᵥ�ˣ�");
						msg.append(name);
						impl.setMessage(msg.toString());
						task.setPushMessageImpl(impl);
						JPushCommand cmd = new JPushCommand(task); 
						CommandLocal.getDefault().add(cmd);
					}
				}
			}
		}

	}

	/**
	 * ��ȡ��ǰ�ڵ㵱ǰ�û��Ĵ�����
	 * @param context
	 * @param item
	 * @param model
	 * @param user
	 * @return
	 */
	private List<FUser> getDelegater(Context context, WorkItem item, BillModel model, FUser user){
		List<FUser> fUsers = new ArrayList<FUser>();
		if(item == null || item.getWorkCategory() == null) {
			return fUsers;
		}
		List<FWorkflowDelegate> list = context.getList(FWorkflowDelegate.class, Boolean.TRUE);
		if(list == null || list.isEmpty()) {
			return fUsers;
		}
		List<ParticipantObject> participantList = item.getParticipant();
		if(participantList == null || participantList.isEmpty()) {
			return fUsers;
		}
		GUID recid = GUID.emptyID;
		try{
			for(FWorkflowDelegate wd : list){
				recid = wd.getRECID();
				if(!WorkflowUtil.isAccept(wd, item, model)) {
					continue;
				}
				GUID delegateGuid = WorkflowUtil.getUserIdByStaff(context, wd.getDelegateStaff());
				if(!user.getGuid().toString().equalsIgnoreCase(delegateGuid.toString())) {
					continue;
				}

				List<GUID> participantGUIDList = new ArrayList<GUID>();//��Žڵ�����ߵ�guid
				for(ParticipantObject participant : participantList){
					participantGUIDList.add(GUID.valueOf(participant.getUserguid()));
				}
				String uid = user.getGuid().toString();
				if(uid.equals(delegateGuid.toString())) {
					GUID agentUserGuid = WorkflowUtil.getUserIdByStaff(context, wd.getAgentStaff());
					//�����ί���˲�������ڵ��У���ִ���޸ķ���
					if(!participantGUIDList.contains(agentUserGuid)) {
						fUsers.add(context.find(FUser.class, agentUserGuid));
					}
				}
			}
		}
		catch(Exception e){
			context.handle(new AddLogInfoTask(AddLogInfoTask.Constant.INFOMATION, "������ί��",
			        "�û���" + context.getLogin().getUser().getID() + "ί��ID��" + recid, "getCommissionί���쳣"));
		}
		if(fUsers.size() < 1) {
			fUsers.add(user);
		}
		return fUsers;
	}

}