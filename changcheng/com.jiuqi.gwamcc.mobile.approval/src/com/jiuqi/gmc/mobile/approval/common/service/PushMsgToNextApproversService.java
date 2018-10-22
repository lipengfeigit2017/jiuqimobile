/**============================================================
 * 版权： 久其软件 版权所有 (c) 2002 - 2016
 * 包： com.jiuqi.gmc.mobile.approval.common.service
 * 修改记录：
 * 日期                作者           内容
 * =============================================================
 * 2016年12月22日       qinjunjie        
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
 * <p>推送待办消息到移动客户端</p>
 *
 * <p>Copyright: 版权所有 (c) 2002 - 2016<br>
 * Company: 久其</p>
 *
 * @author qinjunjie
 * @version 2016年12月22日
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
				//如果不启用消息推送则直接退出
				if(!ConfigUtil.getSysBoolValue(context, JPushSysOption.IS_PUSH_MSG)){
					return;
				}
				if(event.eventType == WorkflowManagerEvent.EventType.RefreshParticipant) {
					return;
				}
				//WorkItemStartFinish使event中的context置为空
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
				if(businessObjectType != null && !businessObjectType.endsWith("单据")) {
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
				{//同意（包括流程结束时的同意）
					model = (BillModel)billDefine.createModel(context, event.getBOdataID());
					FUser createUser = model.getContext().find(FUser.class,
					        model.getModelData().getMaster().getValueAsGUID("CREATEUSERID"));
					IWorkItem item = WorkflowUtils.getAdaptor(event.item);//当前工作项
					List<ParticipantObject> list;
					FUser user;
					FUnitOption op = EnvCenter.getUnitOption(model.getContext(),
					        model.getModelData().getMaster().getValueAsGUID(FieldConst.f_unitID), "FO067");//系统选项配是否移除委托人
					boolean bap_DelegateDone =
					        Boolean.valueOf(event.item.getActiveNode().getProperty(WorkflowConsts.BAP_DELEGATENODE));//工作流节点启用委托是否勾选
					boolean removeDelegater = op.getBoolValue();
					String title = model.getDefine().getBillInfo().getTitle();
					String name = createUser.getName();
					String billCode = model.getModelData().getMaster().getValueAsString(BillConst.f_billCode);
					if(event.item.getActionId() == 1) {//同意
						//工作项完成发送待办
						if(event.item.getState() == EnumWorkItemState.COMPLETE) {
							//同意增加待办
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
								msg.append("您收到一条\"");
								msg.append(title);
								msg.append("\"待办，单据编号：");
								msg.append(billCode);
								msg.append(",提单人：");
								msg.append(name);
								impl.setMessage(msg.toString());
								task.setPushMessageImpl(impl);
								JPushCommand cmd = new JPushCommand(task); 
								CommandLocal.getDefault().add(cmd);
							}
						}
					}
					else if(event.item.getActionId() == 3) {//提交
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
									//审批人和提单人相同自动同意
									if(!sameWithCreaterAutoAgree(model, nextItem)) {
										if(!bap_DelegateDone && removeDelegater) {
											receiverUserNameList.add(user);
										}
										receiverUserNameList.addAll(getDelegater(context,
										        nextItem.getAdaptor(WorkItem.class), model, createUser));
									}
									else{
										if(nextItem.getActiveNode().getMultiSubscriptAction() == 1) {//自动同意，会签节点
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
							msg.append("您收到一条\"");
							msg.append(title);
							msg.append("\"待办，单据编号：");
							msg.append(billCode);
							msg.append(",提单人：");
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
				throw new InfomationException("推送待办给移动端的监听工作流发生异常");
			}
		}

	}

	/**
	 * 
	 * sameWithCreaterAutoAgree:(审批人和制单人相同是否自动同意). <br/>
	 *
	 * @param model
	 * @param event
	 * @return
	 */
	private boolean sameWithCreaterAutoAgree(BillModel model, IWorkItem item){
		Context context = model.getContext();
		boolean isAutoAgree = false;
		//系统选项是否配置 “制单人和审批人相同，自动同意该节点”
		if(model != null && model.getDefine().getMasterTable().findField("UNITID") != null
		        && model.getModelData().getMaster().getValueAsGUID("UNITID") != null)
		    isAutoAgree =
		            EnvCenter.getUnitOption(context, model.getModelData().getMaster().getValueAsGUID("UNITID"), "FO042")
		                    .getBoolValue();
		if(!isAutoAgree) {
			return false;
		}
		if(item == null) return false;
		//节点是否勾选“制单人和审批人相同时不自动同意”
		String result = item.getActiveNode().getProperty(WorkFlowExtendEnum.AUTO_AGREE.getKey());
		if("true".equals(result)) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * getReceivesList:(获取增加待办的USERNAME). <br/>
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
		IWorkItem item = WorkflowUtils.getAdaptor(event.item);//当前工作项
		Set<FUser> receiverList = new HashSet<FUser>();
		List<ParticipantObject> list = nextItem.getParticipants();
		if(list == null || list.size() == 0) {
			return receiverList;
		}
		Boolean flag = false;
		int multiSubscript = 0;//非会签
		if(item.getActiveNode().getMultiSubscriptAction() == 1) {
			multiSubscript = 1;//会签同意
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
								if(sameWithCreaterAutoAgree(model, nextItem)) {//提单人和审批人相同自动同意
									if(multiSubscript == 0 || list.size() == 1) {//非会签或只有一个用户
										receiverList.clear();
										flag = true;
										break;
									}
									else if(multiSubscript == 1) {//会签
										continue;
									}
								}
							}
							if(par.getUserguid() != null && par.getUserguid().equals(list.get(i).getUserguid())
							        && par.getAction() == 1)
							{
								if(multiSubscript == 0 || list.size() == 1) {//非会签，或只有一个用户,审批人在历史记录中已经存在同意记录，则自动同意
									flag = true;
									receiverList.clear();
									break;
								}
								else if(multiSubscript == 1) {//会签，审批人在历史记录中已经存在同意记录,自动同意
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
	 * 加签待办
	 */
	@Publish
	protected class WorkflowAddApproverEvent extends EventListener<FoAddApprovalEvent>{

		@Override
		protected void occur(Context context, FoAddApprovalEvent event) throws Throwable{
			Map<GUID, Map<String, String>> userDatas = event.getUserDatas();
			if(userDatas != null) {
				for(GUID recUserID : userDatas.keySet()){//被加签人
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
						msg.append("您收到一条\"");
						msg.append(title);
						msg.append("\"待办，单据编号：");
						msg.append(billCode);
						msg.append(",提单人：");
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
	 * 获取当前节点当前用户的代理人
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

				List<GUID> participantGUIDList = new ArrayList<GUID>();//存放节点参与者的guid
				for(ParticipantObject participant : participantList){
					participantGUIDList.add(GUID.valueOf(participant.getUserguid()));
				}
				String uid = user.getGuid().toString();
				if(uid.equals(delegateGuid.toString())) {
					GUID agentUserGuid = WorkflowUtil.getUserIdByStaff(context, wd.getAgentStaff());
					//如果被委托人不存在与节点中，则执行修改方法
					if(!participantGUIDList.contains(agentUserGuid)) {
						fUsers.add(context.find(FUser.class, agentUserGuid));
					}
				}
			}
		}
		catch(Exception e){
			context.handle(new AddLogInfoTask(AddLogInfoTask.Constant.INFOMATION, "工作流委托",
			        "用户：" + context.getLogin().getUser().getID() + "委托ID：" + recid, "getCommission委托异常"));
		}
		if(fUsers.size() < 1) {
			fUsers.add(user);
		}
		return fUsers;
	}

}