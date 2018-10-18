package com.jiuqi.gmc.mobile.approval.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jiuqi.dna.bap.authority.intf.facade.FUser;
import com.jiuqi.dna.bap.basedata.intf.facade.FBaseDataObject;
import com.jiuqi.dna.bap.bill.common.action.AgreeAction;
import com.jiuqi.dna.bap.bill.common.action.IWorkFlowAction;
import com.jiuqi.dna.bap.bill.common.model.BillCentre;
import com.jiuqi.dna.bap.bill.common.model.BillModel;
import com.jiuqi.dna.bap.bill.common.workflow.task.BillApprovalTask;
import com.jiuqi.dna.bap.bill.intf.facade.model.FBillDefine;
import com.jiuqi.dna.bap.bill.intf.model.BillConst;
import com.jiuqi.dna.bap.model.common.define.intf.IAction;
import com.jiuqi.dna.bap.workflowmanager.common.consts.WorkflowConsts;
import com.jiuqi.dna.bap.workflowmanager.define.intf.facade.FBusinessInstanceAndWorkItem;
import com.jiuqi.dna.bap.workflowmanager.execute.common.ApprovalTaskManager;
import com.jiuqi.dna.bap.workflowmanager.execute.common.BusinessProcessManager;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.facade.FApproveHistoryItem;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.task.BaseApprovalTask;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.util.WorkflowRunUtil;
import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.User;
import com.jiuqi.dna.core.da.RecordSet;
import com.jiuqi.dna.core.def.query.QueryStatementDeclare;
import com.jiuqi.dna.core.def.query.QueryStatementDefine;
import com.jiuqi.dna.core.spi.application.ContextSPI;
import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.dna.ui.wt.InfomationException;
import com.jiuqi.dna.workflow.define.DefaultAction;
import com.jiuqi.dna.workflow.engine.EnumWorkItemState;
import com.jiuqi.dna.workflow.engine.ProcessInstance;
import com.jiuqi.dna.workflow.engine.object.ParticipantObject;
import com.jiuqi.dna.workflow.intf.facade.IWorkItem;
import com.jiuqi.fo.common.comm.FoCommon;
import com.jiuqi.fo.common.foconst.FoFieldStateConst;
import com.jiuqi.fo.common.foconst.WorkflowConst;
import com.jiuqi.fo.intf.utilis.FoSumTodoApprovalUtil;
import com.jiuqi.fo.intf.utilis.WorkFlowLogUtil;
import com.jiuqi.fo.logmanage.common.LogManageUtils;
import com.jiuqi.fo.model.FoBaseBillModel;
import com.jiuqi.fo.workflow.common.util.WorkflowCenter;
import com.jiuqi.fo.workflow.intf.impl.WorkflowApproveImpl;
import com.jiuqi.fo.workflow.intf.task.WorkflowApproveTask;
import com.jiuqi.fo.workflow.intf.util.WorkFlowConst;
import com.jiuqi.fo.workflow.intf.util.WorkflowUtils;
import com.jiuqi.gmc.mobile.approval.common.AppUtil;
import com.jiuqi.gmc.mobile.approval.common.FUnitOptionUtils;
import com.jiuqi.mt2.dna.mobile.todo.facade.FApprovalDefine;
import com.jiuqi.mt2.dna.service.todo.internal.ListenerGatherer;
import com.jiuqi.mt2.dna.service.todo.listener.ITodoResult;
import com.jiuqi.mt2.spi.common2.command.intf.CommandQueue;
import com.jiuqi.mt2.spi.common2.command.intf.WebCommand;
import com.jiuqi.sm.imagecenter.common.ImageConst.ViewType;
import com.jiuqi.sm.imagecenter.common.util.ImageCenterUtil;
import com.jiuqi.vacomm.biz.common.intf.FUserLinkStaff;
import com.jiuqi.vacomm.env.EnvCenter;
import com.jiuqi.vacomm.env.FUnit;
import com.jiuqi.vacomm.env.FUnitOption;
import com.jiuqi.vacomm.model.common.BillDataModel;
import com.jiuqi.vacomm.utils.sys.BizHttpServlet;

public class FlowActionServlet extends BizHttpServlet {

	private static final long serialVersionUID = -1577938235346141251L;
	@Override
	protected void doService(Context context, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		response.setHeader("pragma", "no-cache");
		response.setHeader("cache-control", "no-no-cache");
		response.setHeader("expires", "0");
		if ("agree".equals(action)) {
			doAccept(context, request, response);
		}		
		if ("softagree".equals(action)) {
			doSoftAccept(context, request, response);
		}
		if ("batchagree".equals(action)) {
			doBatchAccept(context, request, response);
		}
		if ("reject".equals(action)) {
			doReject(context, request, response);
		}
		if ("softreject".equals(action)) {
			doSoftReject(context, request, response);
		}
		if ("getback".equals(action)) {
			doGetback(context, request, response);
		}
		if ("adduser".equals(action)) {
			addApprovalUser(context, request, response);
		}
		if ("route".equals(action)) {
			getApprovalinfo(context, request, response);
		}
		// 获取加签意见
		if ("addapprovalsugggest".equals(action)) {
			getAddApprovalSuggest(context, request, response);
		}
		// 加签审批
		if ("doaddapproval".equals(action)) {
			doAddApproval(context, request, response);
		}
		// 检查是否加签完成
		if ("checkaddapproval".equals(action)) {
			checkAddApproval(context, request, response);
		}
		// 获取委托意见
		if ("getdelegateoption".equals(action)) {
			getDelegateOption(context, request, response);
		}
		if ("getimage".equals(action)) {
			getImage(context, request, response);
		}
		if ("getaddapprpvalnote".equals(action)) {
			getAddApprovalNote(context, request, response);
		}
		if ("showimages".equals(action)) {
//			getShowImages(context, request, response);
			getImage(context, request, response);
		}
		if("deleteapprove".equals(action)){
			deleteApproveUser(context, request, response);
		}
	}

	private void getShowImages(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		FBillDefine billDefine = BillCentre.findBillDefine(context, GUID.valueOf(billDefineID));
        BillModel model = BillCentre.createBillModel(context, billDefine);
        model.load(GUID.valueOf(billDataID));
        String url = ImageCenterUtil.getImageUrl(model,"0",ViewType.MOBIL);
		WebCommand com = new WebCommand(url.toString(), 0);
		com.setLinkName("影像");
		CommandQueue comm = new CommandQueue();
		comm.add(com);
	}
	/**
	 * 同意前加签生效时删除加签人操作
	 * @param context
	 * @param request
	 * @param response  void
	 */
	private void deleteApproveUser(Context context, HttpServletRequest request,
			HttpServletResponse response){
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		if(userstr == null) userstr = (String)request.getParameter("user");
		User currUser = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(currUser);
		String workitemid = (String) request.getParameter("workitemid");	
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		String user = request.getParameter("user");
		PrintWriter writer = null;
		JSONObject approvalJson = new JSONObject();
		String message = "";
		try {
			writer = response.getWriter();
			IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context,workitemid);
			FBillDefine define = BillCentre.findBillDefine(context, GUID.tryValueOf(billDefineID));
			BillModel model = BillCentre.createBillModel(context, define);
		    model.loadData(GUID.tryValueOf(billDataID));
		    
			LinkedHashMap<GUID, Integer> curStaffMap = new LinkedHashMap<GUID, Integer>();
			FUnitOption Fo049 = EnvCenter.getUnitOption(context,model.getModelData().getMaster().getValueAsGUID("UNITID"),"FO049");
			getAllApproveUser(context, curStaffMap, workItem, model, Fo049);
			List<GUID> userList  = new ArrayList<GUID>();
			GUID userID = GUID.tryValueOf(user);
			//判断要删除的user是不是在这里
			if(curStaffMap != null && curStaffMap.get(userID)!= null && String.valueOf(curStaffMap.get(userID)).equals("1")){
				message = "该用户已经对单据做处理,不能移除";
				approvalJson.put("responsecode", "0");
				approvalJson.put("responsemessage", message);
				writer.println(approvalJson);
				writer.flush();
				writer.close();
				return;
			}
			if(userID.equals(currUser.getID())){
				message = "不能移除自己的审批";
				approvalJson.put("responsecode", "0");
				approvalJson.put("responsemessage", message);
				writer.println(approvalJson);
				writer.flush();
				writer.close();
				return;
			}
			userList.add(userID);
			//处理workItem addCustomApproverKey参数
			String addPerson = "";
			if(workItem.getProcessInstance().getParam(WorkflowConst.addCustomApproverKey, true) != null){
				addPerson = workItem.getProcessInstance().getParam(WorkflowConst.addCustomApproverKey, true).toString();
				String[] userGuids = addPerson.split(",");
				String addUser = "";
				for (int i = 0; i < userGuids.length; i++) {
					if(user.equals(userGuids[i])){
						continue;					
					}					
					addUser = addUser + userGuids[i] + ",";
				}
				if(!"".equals(addUser)){
					BusinessProcessManager.setProcessParaByIWorkItem(workItem,WorkflowConst.addCustomApproverKey, addUser.substring(0, addUser.length() - 1));						
				}else{
					BusinessProcessManager.setProcessParaByIWorkItem(workItem,WorkflowConst.addCustomApproverKey, null);
				}
			}
			//删除此人
			if(Fo049 != null && !Fo049.getBoolValue()){
				WorkflowUtils.deleteWorkflowTodo(context, workItem.getGuid(), model.getModelData().getMaster().getRECID(), 
						model.getModelData().getMaster().getValueAsGUID(BillConst.f_billDefine), userID);
			}else{
				BusinessProcessManager.removeUsers(context,workItem,userList);			
			}
			approvalJson.put("responsecode", "1");
			approvalJson.put("responsemessage", "删除加签人成功");
		} catch (Exception e) {
			approvalJson.put("responsecode", "0");
			message = e.getMessage();
			approvalJson.put("responsemessage", message);
		}
		writer.println(approvalJson);
		writer.flush();
		writer.close();
		
	}
	/**
	 * 获取节点所有参与者的同意状态
	 *  void
	 * @param context 
	 * @param curStaffMap 存储当前参与者及同意状态
	 * @param workItem 
	 * @param model 
	 */
	private void getAllApproveUser(Context context, LinkedHashMap<GUID, Integer> curStaffMap, IWorkItem workItem,
			BillModel model, FUnitOption Fo049) {
		List<ParticipantObject> participantList = workItem.getParticipants();
		for(int i=0; i<participantList.size(); i++){
			ParticipantObject po = participantList.get(i);			
			final String uid = po.getUserguid();
//			FUserLinkStaff userStaff = model.getContext().find(FUserLinkStaff.class, GUID.tryValueOf(uid));
//			FBaseDataObject obj=null;
//			if(userStaff != null)
//				obj = userStaff.findStaff(model.getModelData().getMaster().getValueAsGUID("UNITID"));
//			if(null == obj) continue;
			curStaffMap.put(GUID.tryValueOf(uid), po.getAction());
		}
		if(Fo049 != null && !Fo049.getBoolValue()){
			HashMap<FBaseDataObject,Integer> map = getAddApprover(context, model.getModelData().getMaster().getValueAsGUID("UNITID"),
					workItem.getGuid(),model.getModelData().getMaster().getRECID(),
					model.getModelData().getMaster().getValueAsGUID(BillConst.f_billDefine));
			Iterator<Entry<FBaseDataObject, Integer>> iter = map.entrySet().iterator();
			while (iter.hasNext()){
				Entry<FBaseDataObject, Integer> entry = iter.next();
				FBaseDataObject staff = (FBaseDataObject)entry.getKey();
				int action = (Integer) entry.getValue();
				GUID userStaff = (GUID) staff.getFieldValue("LINKUSER");
				if(userStaff != null){
					curStaffMap.put(userStaff, action);					
				}
			}
		}	
	}
	/**
	 * 获取节点所有加签人
	 * @param unitID
	 * @param workItemID
	 * @param billID
	 * @param billdefineID
	 * @return  HashMap<FBaseDataObject,Integer>
	 */
	private HashMap<FBaseDataObject, Integer> getAddApprover(Context context, GUID unitID,GUID workItemID,GUID billID,GUID billdefineID){
		StringBuffer sql = new StringBuffer();
		sql.append("define query getQueryStaff(@WORKITEMID GUID,@BILLID GUID,@BILLDEFINEID GUID) begin ");
		sql.append(" select t.SUGGESTUSERID as useid,t.APPROVEDSUGGEST as APPROVEDSUGGEST from FO_WORKFLOW_APPROVE as t ");
		sql.append(" where t.WORKITEMID=@WORKITEMID and t.BILLID = @BILLID and t.BILLDEFINEID=@BILLDEFINEID ");
		sql.append(" end ");
		QueryStatementDefine query = (QueryStatementDefine)context.parseStatement(sql.toString());
		RecordSet rs = context.openQuery(query,workItemID,billID,billdefineID);
		if(rs != null){
			HashMap<FBaseDataObject, Integer> map = new HashMap<FBaseDataObject, Integer>();
			while(rs.next()){
				GUID uid = rs.getFields().get(0).getGUID();
				FUserLinkStaff userStaff = context.find(FUserLinkStaff.class, uid);
				FBaseDataObject obj=null;
				if(userStaff == null) continue;
				obj = userStaff.findStaff(unitID);
				if(obj == null) continue;
				String suggest = rs.getFields().get(1).getString();
				map.put(obj,suggest == null?0:1);
			}
			return map;
		}
		return null;
	}

	/**
	 * 获取审批信息 传入参数：用户名、机构id、单据数据id,单据定义id
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	private void getApprovalinfo(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		if(userstr == null) userstr = (String)request.getParameter("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);

		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		String businessObjectType = (String)request.getParameter("businessObjectType");
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject approvalJson = new JSONObject();
		JSONArray iteminfos = new JSONArray();
		String responsecode = "0";
		String responsemessage = "";
		try {
			writer = response.getWriter();
			if(businessObjectType!=null && ("多维表".equals(businessObjectType)||"上报分组".equals(businessObjectType))){
				approvalJson.put("approvalinfos", iteminfos);
				approvalJson.put("responsecode", "1");
				approvalJson.put("responsemessage", responsemessage);
				BaseApprovalTask task = new BaseApprovalTask();
				task.businessInstanceID = GUID.tryValueOf(billDataID);
				List<FBusinessInstanceAndWorkItem> listFApproveHistoryItems = ApprovalTaskManager.getFBApprovalHistoryList(context, task);
				List<FApproveHistoryItem> approveHistoryItems = ApprovalTaskManager.getApprovalHistoryList(context, task);
				Map<GUID, FApproveHistoryItem> nodeTitles = new HashMap<GUID, FApproveHistoryItem>();
				for (FApproveHistoryItem approveHistoryItem : approveHistoryItems) {
					GUID key = approveHistoryItem.getWorkItemGuid();
					nodeTitles.put(key, approveHistoryItem);
				}
				for (FBusinessInstanceAndWorkItem listFApproveHistoryItem : listFApproveHistoryItems) {
					JSONObject itemJson = new JSONObject();
					JSONArray iteminfo = new JSONArray();
					itemJson.put("items", iteminfo);
					if (listFApproveHistoryItem.getApproveDate() == 0) {
						continue;
					} else {
						if (nodeTitles != null
								&& nodeTitles.containsKey(listFApproveHistoryItem
										.getWorkItemID())) {
							FApproveHistoryItem historyItem = nodeTitles
									.get(listFApproveHistoryItem.getWorkItemID());
							itemJson.put("title", historyItem.getNodeTitle());
							List<GUID> userIds = listFApproveHistoryItem
									.getAllUserIds();
							for (GUID userId : userIds) {
								JSONObject json = new JSONObject();
								json.put("approvalUser", historyItem.getUserTitle());
								json.put("approvalResult",
										historyItem.getApproveResult());
								json.put("approvalDate", historyItem.getDate());
								String suggest = listFApproveHistoryItem
										.getSuggest();
								json.put("approvalSuggest", suggest + " ");
								iteminfo.put(json);
							}
							iteminfos.put(itemJson);
						}
					}
				}
				for (FBusinessInstanceAndWorkItem listFApproveHistoryItem : listFApproveHistoryItems) {
					JSONObject itemJson = new JSONObject();
					JSONArray iteminfo = new JSONArray();
					itemJson.put("items", iteminfo);
					if (listFApproveHistoryItem.getApproveDate() == 0) {
						if (nodeTitles != null
								&& nodeTitles.containsKey(listFApproveHistoryItem
										.getWorkItemID())) {
							FApproveHistoryItem historyItem = nodeTitles
									.get(listFApproveHistoryItem.getWorkItemID());
							IWorkItem workItem = WorkflowRunUtil.loadWorkItem(
									context, listFApproveHistoryItem
											.getWorkItemID().toString());
							List<ParticipantObject> ps = workItem.getParticipants();
							String approvalUser = "";
							for (ParticipantObject p : ps) {
								FUser duser = context.find(FUser.class,
										GUID.tryValueOf(p.getUserguid()));
								if (duser != null) {
									approvalUser = approvalUser + duser.getTitle()
											+ ";";
								}
							}
							if (approvalUser != null && !"".equals(approvalUser)) {
								approvalUser = "等待"
										+ approvalUser.substring(0,
												approvalUser.length() - 1) + "审批";
							}
							itemJson.put("title", historyItem.getUnitTitle());
							JSONObject json = new JSONObject();
							json.put("approvalUser", approvalUser);
							json.put("approvalResult", "");
							json.put("approvalDate", "");
							json.put("approvalSuggest", "");
							iteminfo.put(json);
							iteminfos.put(itemJson);
						}
					}
				}
			}else{
				FBillDefine billDefine = BillCentre.findBillDefine(context,
						GUID.tryValueOf(billDefineID));
				BillModel model = BillCentre.createBillModel(context, billDefine);
				model.load(GUID.tryValueOf(billDataID));

				approvalJson.put("approvalinfos", iteminfos);
				approvalJson.put("responsecode", "1");
				approvalJson.put("responsemessage", responsemessage);
				BaseApprovalTask task = new BaseApprovalTask();
				task.businessInstanceID = GUID.tryValueOf(billDataID);
				List<FBusinessInstanceAndWorkItem> listFApproveHistoryItems = ApprovalTaskManager
						.getFBApprovalHistoryList(context, task);
				List<FApproveHistoryItem> approveHistoryItems = ApprovalTaskManager
						.getApprovalHistoryList(context, task);
				Map<GUID, FApproveHistoryItem> nodeTitles = new HashMap<GUID, FApproveHistoryItem>();
				for (FApproveHistoryItem approveHistoryItem : approveHistoryItems) {
					GUID key = approveHistoryItem.getWorkItemGuid();
					nodeTitles.put(key, approveHistoryItem);
				}
				for (FBusinessInstanceAndWorkItem listFApproveHistoryItem : listFApproveHistoryItems) {
					JSONObject itemJson = new JSONObject();
					JSONArray iteminfo = new JSONArray();
					itemJson.put("items", iteminfo);
					if (listFApproveHistoryItem.getApproveDate() == 0) {
						continue;
					} else {
						if (nodeTitles != null
								&& nodeTitles.containsKey(listFApproveHistoryItem
										.getWorkItemID())) {
							FApproveHistoryItem historyItem = nodeTitles
									.get(listFApproveHistoryItem.getWorkItemID());
							IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context, listFApproveHistoryItem.getWorkItemID().toString());
							String userTitles = historyItem.getUserTitle();
							String[] users = userTitles.split(";");	
							if (workItem.getState() == EnumWorkItemState.ACTIVE) {//环节名称
								List<ParticipantObject> ps = workItem.getParticipants();
								StringBuffer wait = new StringBuffer();
								StringBuffer doApproval = new StringBuffer();
								for (ParticipantObject p : ps) {
									boolean isDo = false;
									FUser fuser = context.find(FUser.class,GUID.tryValueOf(p.getUserguid()));
									for (int i = 0; i < users.length; i++) {
										if (fuser.getTitle().equals(users[i])) {
											isDo = true;
											break;
										} 
									}
									if (!isDo) {
										wait.append(fuser.getTitle()+"；");
									}
								}
								for (int i = 0; i < users.length; i++) {
									doApproval.append(users[i]+"；");
								}
								wait.deleteCharAt(wait.length()-1);
								doApproval.deleteCharAt(doApproval.length()-1);
								itemJson.put("title", "等待"+wait.toString()+"；"+doApproval.toString()+"审批...");
							} else {
								itemJson.put("title", historyItem.getNodeTitle());
							}
							String userSuggests = historyItem.getMessage();
							String[] suggests = userSuggests.split(";");
							if (users.length > 1) {
								for (int i = 0; i < users.length; i++) {
									JSONObject json = new JSONObject();
									json.put("approvalUser", users[i]);
									json.put("approvalResult",suggests[i].split(",审批意见")[0]);
									json.put("approvalDate", suggests[i].split("审批时间：")[1]);
									json.put("approvalSuggest", suggests[i].split("审批意见:")[1].split(",")[0]);
									iteminfo.put(json);
								}
							} else {
								JSONObject json = new JSONObject();
								json.put("approvalUser", userTitles);
								json.put("approvalResult",historyItem.getApproveResult());
								json.put("approvalDate", historyItem.getDate());
								json.put("approvalSuggest", historyItem.getMessage());
								iteminfo.put(json);
							}
							//获取不参与流程审批的加签人审批意见和审批时间
							loadNotEffectWorkFlowRoute(context, model, workItem, iteminfo);
							
							iteminfos.put(itemJson);
						}
					}
				}
				for (FBusinessInstanceAndWorkItem listFApproveHistoryItem : listFApproveHistoryItems) {
					JSONObject itemJson = new JSONObject();
					JSONArray iteminfo = new JSONArray();
					itemJson.put("items", iteminfo);
					if (listFApproveHistoryItem.getApproveDate() == 0) {
						if (nodeTitles != null
								&& nodeTitles.containsKey(listFApproveHistoryItem
										.getWorkItemID())) {
							FApproveHistoryItem historyItem = nodeTitles
									.get(listFApproveHistoryItem.getWorkItemID());
							IWorkItem workItem = WorkflowRunUtil.loadWorkItem(
									context, listFApproveHistoryItem
											.getWorkItemID().toString());
							List<ParticipantObject> ps = workItem.getParticipants();
							String approvalUser = "";
							for (ParticipantObject p : ps) {
								FUser duser = context.find(FUser.class,
										GUID.tryValueOf(p.getUserguid()));
								if (duser != null) {
									approvalUser = approvalUser + duser.getTitle()
											+ ";";
								}
							}
							if (approvalUser != null && !"".equals(approvalUser)) {
								approvalUser = "等待"
										+ approvalUser.substring(0,
												approvalUser.length() - 1) + "审批";
							}
							itemJson.put("title", historyItem.getUnitTitle());
							JSONObject json = new JSONObject();
							json.put("approvalUser", approvalUser);
							json.put("approvalResult", "");
							json.put("approvalDate", "");
							json.put("approvalSuggest", "");
							iteminfo.put(json);
							iteminfos.put(itemJson);
						}
					}
				}
			}
		} catch (Exception e) {
			approvalJson.put("approvalinfos", iteminfos);
			approvalJson.put("responsecode", responsecode);
			responsemessage = e.getMessage();
			approvalJson.put("responsemessage", responsemessage);
		}
		writer.println(approvalJson);
		writer.flush();
		writer.close();
	}
	/**
	 * 获取节点不参与流程审批的加签人审批意见
	 *  void
	 * @param context 
	 * @param model 
	 * @param iteminfo 
	 * @param workItem 
	 */
	private void loadNotEffectWorkFlowRoute(Context context, BillModel model, IWorkItem workItem, JSONArray iteminfo) {
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		//获取所有审批过的用户ID
		FBusinessInstanceAndWorkItem fbusinessinstanceandworkitem = getBusinessInstanceAndWorkItem(model, workItem.getGuid());
		for(GUID userID:fbusinessinstanceandworkitem.getAllUserIds()) {
			FUser user = context.find(FUser.class, userID);
			if (user == null) {
				continue;
			}			
			try{
				ProcessInstance process = workItem.getProcessInstance();
				// 得到单据数据的GUID
				GUID billDataGuid = GUID.valueOf(process.getWorkRef());
				// 获得单据定义的GUID
				GUID billDefineGuid = GUID.valueOf((String)process.getParam(WorkflowConsts.SYS_BILLDEFINE, true));
				
				StringBuffer sql  = new StringBuffer();
				sql.append("define query updateTodo(@WORKITEMID GUID,@BILLID GUID ,@BILLDEFINEID GUID ,@APPROVEUSERID GUID)"); //$NON-NLS-1$
				sql.append(" begin "); //$NON-NLS-1$
				sql.append(" select u.title as title,f.SUGGESTDATE as SUGGESTDATE,f.APPROVEDSUGGEST as APPROVEDSUGGEST"); //$NON-NLS-1$
				sql.append(" from  FO_WORKFLOW_APPROVE as f join sm_user as u on u.recid=f.SUGGESTUSERID"); //$NON-NLS-1$
				sql.append(" where f.WORKITEMID = @WORKITEMID and f.BILLID = @BILLID and f.BILLDEFINEID = @BILLDEFINEID"); //$NON-NLS-1$
				sql.append(" and (f.SUGGESTDATE is not null)"); //$NON-NLS-1$
				sql.append(" and (f.APPROVEUSERID= @APPROVEUSERID "); //$NON-NLS-1$
				sql.append(")");
				sql.append(" end"); //$NON-NLS-1$
				RecordSet rs = context.openQuery(
						(QueryStatementDeclare) context.parseStatement(
								sql),workItem.getGuid(),billDataGuid,billDefineGuid,userID);
				if(!rs.isEmpty())
				{
					while(rs.next()){
						JSONObject json = new JSONObject();
						json.put("approvalUser", rs.getFields().get(0).getString());
						json.put("approvalResult",rs.getFields().get(2).getString());
						json.put("approvalDate", dateformat.format(new Date(rs.getFields().get(1).getLong())));
						json.put("approvalSuggest", rs.getFields().get(2).getString());
						json.put("sign",1);
						iteminfo.put(json);
					}
				}
			}catch (Exception e) {
				throw new RuntimeException(e);
			}		
		}
	}
	
	/**
	 * 获取历史审批节点轨迹
	 * @param model
	 * @param workItemId
	 * @return
	 */
	private FBusinessInstanceAndWorkItem getBusinessInstanceAndWorkItem(BillModel model,GUID workItemId){
		List<FBusinessInstanceAndWorkItem> history = new ArrayList<FBusinessInstanceAndWorkItem>();
		FBusinessInstanceAndWorkItem item  = null;
		if(history.size()==0){
			BillApprovalTask task =
			        new BillApprovalTask(((FBillDefine)model
			                .getDefine()).getBillInfo().getRecID(),
			                model);
			task.businessInstanceID = model.getData().getMaster().getRECID();
			history.addAll(model.getWorkFlowProcessor().filterIntanceAndWorkItem(ApprovalTaskManager.getFBApprovalHistoryList(model.getContext(),task)));
		}
		IWorkItem workItem = null;
		for(int i=0;i<history.size();i++){
			workItem = WorkflowRunUtil.loadWorkItem(model.getContext(),
					history.get(i).getWorkItemID().toString());
			if(workItem.getGuid().toString().equals(workItemId.toString())){
				item = history.get(i);
				break;
			}
		}
		return item;
	}

	/**
	 * 获取加签意见和说明
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	private void getAddApprovalSuggest(Context context,
			HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		String workitemid = (String) request.getParameter("workitemid");
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject json = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		String suggest = "";
		String note = "";
		boolean flag = false;
		try {
			writer = response.getWriter();
			FBillDefine billDefine = BillCentre.findBillDefine(context,
					GUID.tryValueOf(billDefineID));
			BillModel model = BillCentre.createBillModel(context, billDefine);
			model.load(GUID.tryValueOf(billDataID));
			String unitId = model.getModelData().getMaster()
					.getValueAsString(BillConst.f_unitID);
			IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context,workitemid);
			boolean isEffectWorkflow = (Boolean) request.getSession().getAttribute("isEffectWorkflow");
			// 根据属性addCustomApproverKey拿到被加签人，每次执行加签动作都会覆盖以前的，既只能拿到当前的被加签人
			String addApprovers = workItem.getProcessInstance().getParam(WorkflowConst.addCustomApproverKey, true).toString();
			if (!isEffectWorkflow) {
				for (String str : addApprovers.split(",")) {
					if (str.equals(user.getID().toString())) {
						flag = true;
						break;
					}
				}
				if (!flag) {// 加签人才要看加签意见
					suggest = WorkflowCenter.getAddApproverSuggest(model,
							GUID.tryValueOf(workitemid));// 加签意见
				}
			}
			for (String str : addApprovers.split(",")) {// 被加签人才要看加签说明
				if (str.equals(user.getID().toString())) {
					note = getApproveUserSuggest(context,
							GUID.tryValueOf(unitId),
							GUID.tryValueOf(billDataID),
							GUID.tryValueOf(billDefineID), workItem,
							isEffectWorkflow).replaceAll("\n", "<br/>");// 获得加签说明,PC换行手机也换行
					break;
				}
			}
			json.put("responsecode", "1");
			json.put("suggest", suggest);
			json.put("note", note);
			json.put("responsemessage", responsemessage);
		} catch (Exception e) {
			json.put("responsecode", responsecode);
			responsemessage = e.getMessage();
			json.put("suggest", suggest);
			json.put("note", note);
			json.put("responsemessage", responsemessage);
		}
		writer.println(json);
		writer.flush();
		writer.close();
	}

	/**
	 * 加签说明
	 * 
	 * @return
	 */
	public String getApproveUserSuggest(Context context, GUID unitID,
			GUID billID, GUID defineID, IWorkItem workItem,
			boolean isEffectWorkflow) {
		User user = context.getLogin().getUser();
		FUser fUser = context.get(FUser.class, user.getID());
		if (isEffectWorkflow) {// 影响工作流
			List<ParticipantObject> poList = workItem.getParticipants();
			for (ParticipantObject po : poList) {
				if (po.getUserguid().equals(fUser.getGuid().toString())) {
					// 根据加签新增的参与者的AddApproverSuggest属性值，拿到加签说明
					return po.getProperty(WorkFlowConst.AddApproverSuggest);
				}
			}
		} else {// 不影响工作流
			StringBuffer sql = new StringBuffer();
			sql.append("define query updateTodo(@WORKITEMID GUID,@BILLID GUID ,@BILLDEFINEID GUID,@userID GUID)");
			sql.append(" begin ");
			sql.append(" select f.APPROVEUSERSUGGEST as APPROVEUSERSUGGEST from  FO_WORKFLOW_APPROVE as f ");
			sql.append(" where f.WORKITEMID = @WORKITEMID and f.BILLID = @BILLID and f.BILLDEFINEID = @BILLDEFINEID");
			sql.append(" and f.SUGGESTUSERID = @userID end ");
			Object value = context.executeScalar(
					(QueryStatementDeclare) context.parseStatement(sql),
					workItem.getGuid(), billID, defineID, fUser.getGuid());
			if (null != value) {
				return String.valueOf(value);
			}
		}
		return "";
	}

	/**
	 * 加签审批操作
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	private void doAddApproval(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		String workitemid = (String) request.getParameter("workitemid");
		String approvalSuggest = (String) request
				.getParameter("approvalsuggest");
		if (approvalSuggest == null || "".equals(approvalSuggest)) {
			approvalSuggest = DefaultAction.ACCEPT.title();
		}
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject json = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		try {
			writer = response.getWriter();
			WorkflowCenter.approveUserSuggestBill(context,
					GUID.tryValueOf(workitemid), GUID.tryValueOf(billDataID),
					GUID.tryValueOf(billDefineID), approvalSuggest,
					user.getID());
			json.put("responsecode", "1");
			json.put("responsemessage", responsemessage);
		} catch (Exception e) {
			json.put("responsecode", responsecode);
			responsemessage = e.getMessage();
			json.put("responsemessage", responsemessage);
		}
		writer.println(json);
		writer.flush();
		writer.close();
	}

	/**
	 * 获取加签说明
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	private void getAddApprovalNote(Context context,
			HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		String workitemid = (String) request.getParameter("workitemid");
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject json = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		String suggest = "";
		try {
			writer = response.getWriter();
			FBillDefine billDefine = BillCentre.findBillDefine(context,
					GUID.tryValueOf(billDefineID));
			BillModel model = BillCentre.createBillModel(context, billDefine);
			BillDataModel billModel = (BillDataModel) model;
			billModel.load(GUID.tryValueOf(billDataID));
			String unitId = billModel.getModelData().getMaster()
					.getValueAsString(BillConst.f_unitID);
			IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context,workitemid);
			boolean isEffectWorkflow = (Boolean) request.getSession()
					.getAttribute("isEffectWorkflow");
			suggest = getApproveUserSuggest(context, GUID.tryValueOf(unitId),
					GUID.tryValueOf(billDataID), GUID.tryValueOf(billDefineID),
					workItem, isEffectWorkflow);
			json.put("responsecode", "1");
			json.put("suggest", suggest);
			json.put("responsemessage", responsemessage);
		} catch (Exception e) {
			json.put("responsecode", responsecode);
			responsemessage = e.getMessage();
			json.put("responsemessage", responsemessage);
		}
		writer.println(json);
		writer.flush();
		writer.close();
	}

	/**
	 * 检查加签是否都已审批通过
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	private void checkAddApproval(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		if(userstr == null) userstr = (String)request.getParameter("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		String workitemid = (String) request.getParameter("workitemid");
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject json = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		String isallapproval = "1";
		FBillDefine billDefine = null;
		BillModel model = null;
		try {
			writer = response.getWriter();
			IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context,workitemid);
			boolean flag = false;
			boolean effectWorkFlow = (Boolean) request.getSession().getAttribute("isEffectWorkflow");
			if (effectWorkFlow) {
				// 根据属性addCustomApproverKey拿到被加签人，每次执行加签动作都会覆盖以前的，既只能拿到当前的被加签人
				String addApprovers = workItem.getProcessInstance().getParam(WorkflowConst.addCustomApproverKey, true).toString();
				for (String str : addApprovers.split(",")) {
					if (str.equals(user.getID().toString())) {
						flag = true;
						break;
					}
				}
				if (!flag) {// 判断当前用户是不是加签人，如果是则flag为false，这时需要校验被加签人是否审批
					billDefine = BillCentre.findBillDefine(context,
							GUID.tryValueOf(billDefineID));
					model = BillCentre.createBillModel(context, billDefine);
					model.load(GUID.tryValueOf(billDataID));
					flag = WorkflowCenter.hasAllAddedApprovalApproved(context,
							model, workItem, user.getID());
				}
			} else {
				billDefine = BillCentre.findBillDefine(context,
						GUID.tryValueOf(billDefineID));
				model = BillCentre.createBillModel(context, billDefine);
				model.load(GUID.tryValueOf(billDataID));
				flag = WorkflowCenter.isAllApproveUserSuggest(context, model,
						GUID.tryValueOf(workitemid),
						GUID.tryValueOf(billDataID),
						GUID.tryValueOf(billDefineID), user.getID());
			}
			if (!flag) {
				isallapproval = "0";
			}
			json.put("responsecode", "1");
			json.put("isallapproval", isallapproval);
			json.put("responsemessage", "还有加签人");
		} catch (Exception e) {
			json.put("responsecode", responsecode);
			responsemessage = e.getMessage();
			json.put("isallapproval", isallapproval);
			json.put("responsemessage", responsemessage);
		}
		writer.println(json);
		writer.flush();
		writer.close();		
	}

	/**
	 * 获取委托意见
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	private void getDelegateOption(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		String workitemid = (String) request.getParameter("workitemid");
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject json = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		String option = "";
		try {
			writer = response.getWriter();
			FBillDefine billDefine = BillCentre.findBillDefine(context,
					GUID.tryValueOf(billDefineID));
			BillModel model = BillCentre.createBillModel(context, billDefine);
			model.load(GUID.tryValueOf(billDataID));
			IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context,workitemid);
			option = WorkflowCenter.getDelegateOption(workItem, context, model);
			json.put("responsecode", "1");
			json.put("option", option);
			json.put("responsemessage", responsemessage);
		} catch (Exception e) {
			json.put("responsecode", responsecode);
			responsemessage = e.getMessage();
			json.put("option", option);
			json.put("responsemessage", responsemessage);
		}
		writer.println(json);
		writer.flush();
		writer.close();
	}

	/**
	 * 获取影像
	 */
	private void getImage(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		response.addHeader("P3P","CP=CAO PSA OUR");
		String userstr = (String) request.getSession().getAttribute("user");
		if(userstr == null) userstr = (String)request.getParameter("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject json = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		String url = "";
		try {
			writer = response.getWriter();
			FBillDefine billDefine = BillCentre.findBillDefine(context,
					GUID.tryValueOf(billDefineID));
			BillModel model = BillCentre.createBillModel(context, billDefine);
			BillDataModel billModel = (BillDataModel) model;
			// 不加载公式
			billModel.load(GUID.tryValueOf(billDataID));
			url = ImageCenterUtil.getImageUrl(billModel,"0",ViewType.MOBIL);
			json.put("responsecode", "1");
			json.put("url", url);
			json.put("responsemessage", responsemessage);
		} catch (Exception e) {
			json.put("responsecode", responsecode);
			responsemessage = e.getMessage();
			json.put("url", url);
			json.put("responsemessage", responsemessage);
		}
		writer.println(json);
		writer.flush();
		writer.close();
	}

	/**
	 * 批量审批同意
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	private void doBatchAccept(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		if(userstr == null) userstr = (String)request.getParameter("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);

		Map<GUID, GUID> recordMaps = new HashMap<GUID, GUID>();
		Map<GUID, GUID> tempMaps = new HashMap<GUID, GUID>();//批审时执行特殊公式用到
		String approvalSuggest = (String) request
				.getParameter("approvalsuggest");
		if (approvalSuggest == null || "".equals(approvalSuggest)) {
			approvalSuggest = DefaultAction.ACCEPT.title();
		}
		String flowguids = (String) request.getParameter("flowguids");
		String businessObjectType = (String)request.getParameter("businessObjectType");
		String[] guidArray = flowguids.split(";");
		/* guidarray包含三个元素，0：billdataid;1:billdefineid;2:workitemid */
		for (int i = 0; i < guidArray.length; i++) {
			String[] guids = guidArray[i].split("_");
			recordMaps
					.put(GUID.tryValueOf(guids[0]), GUID.tryValueOf(guids[2]));
			tempMaps.put(GUID.tryValueOf(guids[0]), GUID.tryValueOf(guids[1]));
		}
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject json = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		String userid = user.getID().toString();
		String id = guidArray[0].split("_")[2];
		String workitemidUserid = id + userid;
		if (request.getSession(true).getAttribute("idUserid") == null || !request.getSession(true).getAttribute("idUserid").toString().equals(workitemidUserid)) {
			HttpSession session = request.getSession(true);
			session.setAttribute("idUserid", workitemidUserid);
			try {
				writer = response.getWriter();
				List<BaseApprovalTask> tasks = new ArrayList<BaseApprovalTask>();
				//记录审批失败的单据ID
				List<GUID> listDataID = new ArrayList<GUID>();
				for (Iterator<GUID> iterator = recordMaps.keySet().iterator(); iterator
						.hasNext();) {
					GUID billDataID = (GUID) iterator.next();
					GUID workitemid = recordMaps.get(billDataID);
					if (workitemid != null) {
						IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context,workitemid.toString());
						//影像状态判断
						GUID billdefineid = tempMaps.get(billDataID);
						if (billdefineid != null) { 
							FBillDefine define = BillCentre.findBillDefine(context, billdefineid);
							BillModel model = BillCentre.createBillModel(context, define);
						    model.loadData(billDataID);
						    String billcode = model.getModelData().getMaster().getValueAsString(BillConst.f_billCode);
							//该待办已审批
							if(workItem.getState() == EnumWorkItemState.COMPLETE){
								listDataID.add(billDataID);
								responsemessage += "单据"+billcode+"审批失败：该待办已审批。\n";
								continue;
							}
						    
						    int imageState = model.getModelData().getMaster().getValueAsInt("IMAGESTATE");
							//控制影像状态
							if (!canAgreeByImage(imageState)) {
								listDataID.add(billDataID);
								responsemessage += "单据"+billcode+"审批失败：影像检查不通过。<br/>";
								continue;
							}
						}
						BaseApprovalTask task = new BaseApprovalTask(workItem,
								DefaultAction.ACCEPT.value(),
								DefaultAction.ACCEPT.title(), approvalSuggest);
						task.businessInstanceID = billDataID;
						tasks.add(task);
					}
				}
				if (responsemessage.length() > 0 ||(tasks != null && tasks.size() > 0)) {
					for(BaseApprovalTask task : tasks){				
						if(task.workItem.getState() == EnumWorkItemState.COMPLETE){
							continue;
						}else{					
							ApprovalTaskManager.approveTask(context,task, false);
						}
						//事务提交
						ContextSPI spi = (ContextSPI)context;
						spi.resolveTrans();
					}
	//				ApprovalTaskManager.mulApproveTask(context, tasks);
					for(GUID billID : listDataID){
						recordMaps.remove(billID);
					}
					for (Iterator<GUID> iterator = recordMaps.keySet().iterator(); iterator
							.hasNext();) {
						GUID billDataID = (GUID) iterator.next();
						GUID workitemid = recordMaps.get(billDataID);
						if (workitemid != null) {
							if(businessObjectType!=null && ("多维表".equals(businessObjectType)||"上报分组".equals(businessObjectType))){
								
							}else{
								if (ListenerGatherer.getiTodoListener() != null) {
									ITodoResult result = ListenerGatherer.getiTodoListener().agreeAfter(workitemid.toString());
								}
							}
						}
					}
					if(responsemessage.length() > 0){
						json.put("responsecode", responsecode);
						json.put("responsemessage", responsemessage);
					}else{
						json.put("responsecode", "1");
						json.put("responsemessage", responsemessage);					
					}
				} else {
					json.put("responsecode", responsecode);
					json.put("responsemessage", "没有可审批的待办");
				}
				/**
				 * 审批完成时清除idUserid，防止下次审批无法进入审批逻辑。
				 */
				session.removeAttribute("idUserid");
			} catch (Exception e) {
				/**
				 * 审批失败时清除idUserid，防止下次审批无法进入审批逻辑。
				 */
				session.removeAttribute("idUserid");
				json.put("responsecode", responsecode);
				responsemessage += e.getMessage();
				json.put("responsemessage", responsemessage);
			}
			writer.println(json);
			writer.flush();
			writer.close();
		}else{
			LogManageUtils.setLogInfo(context, null,System.currentTimeMillis(),"第二次批量审批请求");
			try {
				//responsecode为2时代表第二次审批请求
				writer = response.getWriter();
				json.put("responsecode", "1");
				json.put("responsemessage", "");
			} catch (Exception e) {
				json.put("responsecode", 0);
				json.put("responsemessage", "审批失败");
			}
			writer.println();
			writer.flush();
			writer.close();
		}
	}

	/**
	 * 审批同意
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	private void doAccept(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		if(userstr == null) userstr = (String)request.getParameter("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		String approvalSuggest = (String) request.getParameter("approvalsuggest");
		if (approvalSuggest == null || "".equals(approvalSuggest)) {
			approvalSuggest = DefaultAction.ACCEPT.title();
		}
		//根据页面复选框状态判断是否同意前加签
    	String checked = request.getParameter("checked");
    	if(checked != null){
			request.getSession().setAttribute("checked", checked);
			if(checked.equals("true")){
				request.getSession().setAttribute("sendSignTask",false);
			}else{
				request.getSession().setAttribute("sendSignTask",true);
			}
		}
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		if (unitguid == null) {
			FUser fUser = context.find(FUser.class, userstr.toUpperCase());
			unitguid = fUser.getBelongedUnit();
		}
		String workitemid = (String) request.getParameter("workitemid");
		String taskid = (String) request.getParameter("taskid");
		String businessObjectType = (String)request.getParameter("businessObjectType");
		String isNormal = (String)request.getParameter("isNormal");
		String userid = user.getID().toString();
		List<FApprovalDefine> defines = context
				.getList(FApprovalDefine.class);
		String defineid = "";
		for (FApprovalDefine define : defines) {
			if (define.getTaskDefineID().equals(GUID.tryValueOf(taskid))) {
				defineid = define.getRECID().toString();
				break;
			}
		}
		String workitemidUserid = workitemid + userid;
		// 不知道为什么刘哥的手机会产生多次请求
		if (request.getSession(true).getAttribute("workitemidUserid") == null
				|| !request.getSession(true).getAttribute("workitemidUserid").toString().equals(workitemidUserid)
						|| (businessObjectType!=null && ("多维表".equals(businessObjectType)||"上报分组".equals(businessObjectType)))) {
			HttpSession session = request.getSession(true);
			session.setAttribute("workitemidUserid", workitemidUserid);
			context.getLogin().setUserCurrentOrg(unitguid);
			context.setUserCurrentOrg(unitguid);
			PrintWriter writer = null;
			JSONObject json = new JSONObject();
			String responsecode = "0";
			String responsemessage = "";
			if (workitemid != null && !"".equals(workitemid)) {
				try {
					writer = response.getWriter();
					synchronized (this){
						IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context,workitemid);
						if(businessObjectType!=null && ("多维表".equals(businessObjectType)||"上报分组".equals(businessObjectType))){
							BaseApprovalTask task = new BaseApprovalTask(workItem,DefaultAction.ACCEPT.value(),
									DefaultAction.ACCEPT.title(), approvalSuggest);
							task.businessInstanceID = GUID.tryValueOf(billDataID);
							boolean success = ApprovalTaskManager.approveTask(context,
									task, false);
							if(success){
								json.put("id", defineid);
								json.put("taskid", defineid);
								json.put("responsecode", "1");
								json.put("responsemessage", responsemessage);
							}else{
								/**
								 * 审批失败时清除workitemidUserid，防止下次审批无法进入审批逻辑。
								 */
								session.removeAttribute("workitemidUserid");
								json.put("responsecode", responsecode);
								json.put("responsemessage", "审批失败");
							}
						}else{	
							FBillDefine define = BillCentre.findBillDefine(context, GUID.tryValueOf(billDefineID));
							BillModel model = BillCentre.createBillModel(context, define);
						    model.loadData(GUID.tryValueOf(billDataID));
						    if (workItem == null) {
						    	json.put("responsecode", responsecode);
								json.put("responsemessage", "该待办已被取回");
								json.put("id", defineid);
								json.put("taskid", defineid);
							} else if (workItem.getState() == EnumWorkItemState.COMPLETE) {
						    	json.put("responsecode", "1");
								json.put("responsemessage", "该待办已审批");
								json.put("id", defineid);
								json.put("taskid", defineid);
							}else{
								model.setWorkItem(workItem);			    				   
								int imageState = model.getModelData().getMaster().getValueAsInt("IMAGESTATE");
								//控制影像状态
								if (canAgreeByImage(imageState)) {
									if(isNormal == null){
										BaseApprovalTask task = new BaseApprovalTask(workItem, 
												DefaultAction.ACCEPT.value(), 
												DefaultAction.ACCEPT.title(), approvalSuggest);
										task.businessInstanceID = GUID.tryValueOf(billDataID);
										boolean success = ApprovalTaskManager.approveTask(context, task, false);
										if (success) {
											if ((businessObjectType == null) || ((!"多维表".equals(businessObjectType)) && (!"上报分组".equals(businessObjectType))))
											{
												if (ListenerGatherer.getiTodoListener() != null){
													ListenerGatherer.getiTodoListener().agreeAfter(workitemid);
												}
											}
											json.put("id", defineid);
											json.put("taskid", defineid);
											json.put("responsecode", "1");
											json.put("responsemessage", responsemessage);
										} else {
											/**
											 * 审批失败时清除workitemidUserid，防止下次审批无法进入审批逻辑。
											 */
											session.removeAttribute("workitemidUserid");
											json.put("responsecode", responsecode);
											json.put("responsemessage", "审批失败!");
										}
									}else{
										//加签审批时处理逻辑
										String fApprovalDefine = (String) request.getParameter("fApprovalDefine");
										if(fApprovalDefine !=null && fApprovalDefine.equals("00000000000000000000000000000000")){
											FUser fUser = context.get(FUser.class,user.getID());
											WorkflowUtils.updateFoWorkflowTodo(context, workItem.getGuid(), model.getModelData().getMaster().getRECID(), 
													GUID.tryValueOf(billDefineID), approvalSuggest,fUser.getGuid());
											WorkflowUtils.updateModelNextApproval(context,model,BusinessProcessManager.loadWorkItemByInstanceid(context, model.getModelData().getMaster().getRECID()),false);
											//更新汇总待办
											new FoSumTodoApprovalUtil().updateSumTodoApprovalWithWorkflow(context,(BillModel)model,workItem);
											//加签人不参与审批流程记录操作
											WorkFlowLogUtil.recordAddWorkflowTrace(context, model, workItem);
											json.put("id", defineid);
											json.put("responsecode", "1");
											json.put("responsemessage", "审批成功");
											writer.println(json);
											writer.flush();
											writer.close();
											return;
										}
										if (isNormal.equals("true")) {
											normalAgree(context, billDataID, json, approvalSuggest, businessObjectType,request);
										}else{			    		
											FUnitOptionUtils fUnitOptionUtils = new FUnitOptionUtils();
											//如果为同意前加签生效。根据被加签人是否审批完成判断
											if(fUnitOptionUtils.signAgreeBefore(context,request,workItem,model,user,workitemid,billDataID,billDefineID,json)){
												writer.println(json);
												writer.flush();
												writer.close();
												return;
											}else{
												/*
												 * 同意后加签走这里
												 * 同意前加签同时没有加签人走这里
												 * 同意前加签同时加签人审批完成走这里
												 */
												/**
												 * 同意后加签生效向加签人发待办
												 * 只有参与流程审批时才产生待办。不参与流程审批不产生待办
												 */
												if((Boolean) request.getSession().getAttribute("isEffectWorkflow")){
													fUnitOptionUtils.signAgreeAfter(context, request, workItem, model, user);					    				
												}
												
												normalAgree(context, billDataID, json, approvalSuggest, businessObjectType,request);
												//修改待审批人
												IAction action = new AgreeAction();
												model.messageDialog = FoCommon.getMessageDialog();
												WorkflowUtils.afterAgreeWorkflow(context, model, action);
											}				    	
										}
										json.put("id", defineid);
										json.put("responsecode", "1");
										json.put("responsemessage", responsemessage);
									}
								} else {
									json.put("responsecode", responsecode);
									json.put("responsemessage", "影像未扫描或待重扫");
								}
							}
						}
					}
				} catch (Exception e) {
					LogManageUtils.printExeception(context,"mobile", "审批异常", null, e);
					session.removeAttribute("workitemidUserid");
					if(e.getMessage()!=null && e.getMessage().contains("该工作项已经被其他人员审批")){
						json.put("responsecode", "1");
						json.put("responsemessage", "审批成功");
						json.put("id", defineid);					
					}else{
						json.put("responsecode", responsecode);
						responsemessage = e.getMessage();
						json.put("responsemessage", "审批失败");						
					}
				}
			} else {
				json.put("responsecode", responsecode);
				json.put("responsemessage", "当前用户没有待办！");
			}
			writer.println(json);
			writer.flush();
			writer.close();
		}else{
			PrintWriter writer = null;
			JSONObject json = new JSONObject();
			try {
				writer = response.getWriter();
				json.put("responsecode", "1");
				json.put("responsemessage", "审批成功");
				json.put("id", defineid);
			} catch (IOException e) {
				LogManageUtils.printExeception(context,"mobile", "审批异常", null, e);
				json.put("responsecode", "0");
				json.put("responsemessage", "审批失败");
			}
			writer.println(json);
			writer.flush();
			writer.close();
		}
	}
	
	/**
	 * 柔性提示情况下继续审批
	 * @param context
	 * @param request
	 * @param response
	 */
	private void doSoftAccept(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		if(userstr == null) userstr = (String)request.getParameter("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		String businessObjectType = (String)request.getParameter("businessObjectType");
		String approvalSuggest = (String) request.getParameter("approvalsuggest");
		if (approvalSuggest == null || "".equals(approvalSuggest)) {
			approvalSuggest = DefaultAction.ACCEPT.title();
		}
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		if (unitguid == null) {
			FUser fUser = context.find(FUser.class, userstr.toUpperCase());
			unitguid = fUser.getBelongedUnit();
		}
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		JSONObject json = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		PrintWriter writer = null;
		String workitemid = (String) request.getParameter("workitemid");
		if (workitemid != null && !"".equals(workitemid)) {
			try {
				writer = response.getWriter();
				synchronized (this){
				normalAgree(context, billDataID, json, approvalSuggest, businessObjectType, request);
				
				IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context,workitemid);					 
				FBillDefine define = BillCentre.findBillDefine(context, GUID.tryValueOf(billDefineID));
				BillModel model = BillCentre.createBillModel(context, define);
			    model.loadData(GUID.tryValueOf(billDataID));
			    model.setWorkItem(workItem);
				//修改待审批人
	    		IAction action = new AgreeAction();
	    		model.messageDialog = FoCommon.getMessageDialog();
				WorkflowUtils.afterAgreeWorkflow(context, model, action);
				}
			} catch (Exception e) {
				json.put("responsecode", responsecode);
				responsemessage = e.getMessage();
				json.put("responsemessage", responsemessage);
			}
		} else {
			json.put("responsecode", responsecode);
			json.put("responsemessage", "当前用户没有待办！");
		}

		writer.println(json);
		writer.flush();
		writer.close();
	}
	/**
	 * 正常的同意审批
	 */
	private JSONObject normalAgree(Context context,String billDataID,JSONObject json,String approvalSuggest,
			String businessObjectType,HttpServletRequest request) throws Exception {
		IWorkItem workItem = BusinessProcessManager.loadWorkItemByInstanceid(context,GUID.tryValueOf(billDataID));
		BaseApprovalTask task = new BaseApprovalTask(workItem,DefaultAction.ACCEPT.value(),
				DefaultAction.ACCEPT.title(), approvalSuggest);
		task.businessInstanceID = GUID.tryValueOf(billDataID);
		boolean success = ApprovalTaskManager.approveTask(context,task, false);
		if (success) {
			if(businessObjectType!=null && ("多维表".equals(businessObjectType)||"上报分组".equals(businessObjectType))){
				
			}else{
				if (ListenerGatherer.getiTodoListener() != null) {
					if(request.getSession().getAttribute("sendSignTask").toString().equals("true")){
						ListenerGatherer.getiTodoListener().agreeBefore(workItem.getGuid().toString());// 触发同意前公式的事件
					}else{
						ListenerGatherer.getiTodoListener().agreeAfter(workItem.getGuid().toString());// 触发同意后公式的事件
					}
				}
			}
			json.put("responsecode", "1");
			json.put("responsemessage", "审批成功");
		} else {
			json.put("responsecode", "0");
			json.put("responsemessage", "审批失败");
		}
		return json;
	}
	
	/**
	 * 指定下一个审批人的同意审批
	 * 中建加签流程（同意后加签）的审批和原流程选择下一个审批人的审批走此方法
	 */
	private JSONObject appointNextPersion(Context situation,BillModel model,List<GUID> appointUserIDs,JSONObject json) {
		IAction action = new AgreeAction();
		BillApprovalTask task = new BillApprovalTask(model.getWorkItem(),((IWorkFlowAction)action).getWorkFlowActionID(),
		                model.getApprovalIdea(), model);
		task.setAction(action);
		GUID billDefineGuid = model.getDefine().getBillInfo().getRecID();
		task.businessInstanceID =
		        task.model.getData().getMaster().getRECID();
		task.businessObjectID = billDefineGuid;
		task.workItem = model.getWorkItem();
		if (appointUserIDs != null && appointUserIDs.size() > 0) {
			task.setAppointUserIDs(appointUserIDs);
		}
		WorkflowUtils.agreeWorkFlow(action, task, model);
		model.messageDialog = FoCommon.getMessageDialog();
		WorkflowUtils.afterAgreeWorkflow(situation, model, action);
		json.put("responsecode", "1");
		json.put("responsemessage", "");
		return json;
	}
	
	/**
	 * 执行同意按钮操作之前判断其是否扫描完成影像.
	 * @return  boolean
	 */
	private boolean canAgreeByImage(int imageState){
		if (imageState <= -2) {
			return true;	
		} else {
			// 影像状态为不需要影像或影像扫描已完成或影像重扫完成则同意可用
			if(imageState == FoFieldStateConst.IMAGESTATE.ImageScaned.getValue() 
					|| imageState == FoFieldStateConst.IMAGESTATE.ImageResanned.getValue()
			        || imageState == FoFieldStateConst.IMAGESTATE.ImageArchived.getValue()
			        || imageState == FoFieldStateConst.IMAGESTATE.ImageRepair.getValue()){
				return true;
			} else{
				return false;
			}
		}
	}

	/**
	 * 驳回操作 传入参数：用户名，机构id,单据数据id,单据定义id
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	private void doReject(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		if(userstr == null) userstr = (String)request.getParameter("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);

		String approvalSuggest = (String) request
				.getParameter("approvalsuggest");
		if (approvalSuggest == null || "".equals(approvalSuggest)) {
			approvalSuggest = DefaultAction.REJECT.title();
		}
		String isNormal = (String)request.getParameter("isNormal");
		//根据页面复选框状态判断是否同意前加签
    	String checked = request.getParameter("checked");
    	if(checked != null){
			request.getSession().setAttribute("checked", checked);
			if(checked.equals("true")){
				request.getSession().setAttribute("sendSignTask",false);
			}else{
				request.getSession().setAttribute("sendSignTask",true);
			}
		}
		
		String defineid = "";
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		String taskid = (String) request.getParameter("taskid");
		String businessObjectType = (String)request.getParameter("businessObjectType");
		List<FApprovalDefine> defines = context.getList(FApprovalDefine.class);
		for (FApprovalDefine define : defines) {
			if (define.getTaskDefineID().equals(GUID.tryValueOf(taskid))) {
				defineid = define.getRECID().toString();
				break;
			}
		}

		String workitemid = (String) request.getParameter("workitemid");
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		if (unitguid == null) {
			FUser fUser = context.find(FUser.class, userstr.toUpperCase());
			unitguid = fUser.getBelongedUnit();
		}
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject json = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		if (workitemid != null && !"".equals(workitemid)) {
			try {
				writer = response.getWriter();
				synchronized (this){
				if(businessObjectType!=null && ("多维表".equals(businessObjectType)||"上报分组".equals(businessObjectType))){
					
				}else{
					
					IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context,workitemid);					 
					FBillDefine define = BillCentre.findBillDefine(context, GUID.tryValueOf(billDefineID));
					BillModel model = BillCentre.createBillModel(context, define);
				    model.loadData(GUID.tryValueOf(billDataID));
				    if(model instanceof FoBaseBillModel){
				    	//驳回退影像
				    	try{
					    	AppUtil.getBackImage(context,workItem,model,unitguid);
				    	}catch (InfomationException e){
							json.put("responsecode", responsecode);
							responsemessage = e.getMessage();
							json.put("responsemessage", responsemessage);
			    			writer.println(json);
			    			writer.flush();
			    			writer.close();
			    			return;
				    	}
					}
				    FUnitOptionUtils fUnitOptionUtils = new FUnitOptionUtils();
				    if ((businessObjectType == null) || ((!"多维表".equals(businessObjectType)) && (!"上报分组".equals(businessObjectType))))
				    {
				    	if (ListenerGatherer.getiTodoListener() != null) {
				    		ITodoResult result = ListenerGatherer.getiTodoListener()
				    				.rejectBefore(workitemid);
				    		boolean cando = result.canDo();
				    		if (!cando) {
				    			responsemessage = result.getMessage();
				    			json.put("responsecode", responsecode);
				    			json.put("responsemessage", responsemessage);
				    			writer.println(json);
				    			writer.flush();
				    			writer.close();
				    			return;
				    		}
				    	}
				    }
				    if(isNormal == null){
				        BaseApprovalTask task = new BaseApprovalTask(workItem, 
				          DefaultAction.REJECT.value(), 
				          DefaultAction.REJECT.title(), approvalSuggest);
				        task.businessInstanceID = GUID.tryValueOf(billDataID);
				        boolean success = ApprovalTaskManager.approveTask(context, 
				          task, false);
				        if (success) {
				          if ((businessObjectType == null) || ((!"多维表".equals(businessObjectType)) && (!"上报分组".equals(businessObjectType))))
				          {
				            if (ListenerGatherer.getiTodoListener() != null)
				            {
				              ListenerGatherer.getiTodoListener().rejectAfter(workitemid);
				            }
				          }
				          json.put("id", defineid);
				          json.put("taskid", defineid);
				          json.put("responsecode", "1");
				          json.put("responsemessage", responsemessage);
				        } else {
				          json.put("responsecode", responsecode);
				          json.put("responsemessage", "审批失败!");
				        }
				    }else{
					    if (isNormal.equals("false")) {				    	
					    	//如果为同意前加签生效。根据被加签人是否审批完成判断能否驳回
					    	if(fUnitOptionUtils.signAgreeBefore(context,request,workItem,model,user,workitemid,billDataID,billDefineID,json)){
					    		writer.println(json);
					    		writer.flush();
					    		writer.close();
					    		return;
					    	}
					    }
						BaseApprovalTask task = new BaseApprovalTask(workItem,
								DefaultAction.REJECT.value(),
								DefaultAction.REJECT.title(), approvalSuggest);
						task.businessInstanceID = GUID.tryValueOf(billDataID);
						boolean success = ApprovalTaskManager.approveTask(context,
								task, false);
						if (success) {
							if (ListenerGatherer.getiTodoListener() != null) {
								if(request.getSession().getAttribute("sendSignTask").toString().equals("true")){
									ListenerGatherer.getiTodoListener().rejectBefore(workitemid);
								}else{						
									ListenerGatherer.getiTodoListener().rejectAfter(workitemid);
								}
							}
						}
						json.put("id", defineid);
						json.put("taskid", defineid);
						json.put("responsecode", "1");
						json.put("responsemessage", "审批成功");
					}
				}
				}
			} catch (Exception e) {
				json.put("responsecode", responsecode);
				responsemessage = e.getMessage();
				json.put("responsemessage", responsemessage);
			}
		} else {
			json.put("responsecode", responsecode);
			json.put("responsemessage", "当前用户没有待办！");
		}
		writer.println(json);
		writer.flush();
		writer.close();
	}
	/**
	 * 柔性提示之后驳回操作
	 * @param context
	 * @param request
	 * @param response
	 */
	private void doSoftReject(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		if(userstr == null) userstr = (String)request.getParameter("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);

		String approvalSuggest = (String) request.getParameter("approvalsuggest");
		if (approvalSuggest == null || "".equals(approvalSuggest)) {
			approvalSuggest = DefaultAction.REJECT.title();
		}
		String defineid = "";
		String billDataID = (String) request.getParameter("billDataID");
		String taskid = (String) request.getParameter("taskid");
		String businessObjectType = (String)request.getParameter("businessObjectType");
		List<FApprovalDefine> defines = context.getList(FApprovalDefine.class);
		for (FApprovalDefine define : defines) {
			if (define.getTaskDefineID().equals(GUID.tryValueOf(taskid))) {
				defineid = define.getRECID().toString();
				break;
			}
		}

		String workitemid = (String) request.getParameter("workitemid");
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		if (unitguid == null) {
			FUser fUser = context.find(FUser.class, userstr.toUpperCase());
			unitguid = fUser.getBelongedUnit();
		}
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject json = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		if (workitemid != null && !"".equals(workitemid)) {
			try {
				writer = response.getWriter();
				synchronized (this){
				if(businessObjectType!=null && ("多维表".equals(businessObjectType)||"上报分组".equals(businessObjectType))){
					
				}else{					
					if (ListenerGatherer.getiTodoListener() != null) {
						ITodoResult result = ListenerGatherer.getiTodoListener().rejectBefore(workitemid);
						boolean cando = result.canDo();
						if (!cando) {
							responsemessage = result.getMessage();
							json.put("responsecode", responsecode);
							json.put("responsemessage", responsemessage);
							writer.println(json);
							writer.flush();
							writer.close();
							return;
						}
					}
				}
				IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context, workitemid);
				BaseApprovalTask task = new BaseApprovalTask(workItem,
						DefaultAction.REJECT.value(),
						DefaultAction.REJECT.title(), approvalSuggest);
				task.businessInstanceID = GUID.tryValueOf(billDataID);
				boolean success = ApprovalTaskManager.approveTask(context,
						task, false);
				if (success) {
					if(businessObjectType!=null && ("多维表".equals(businessObjectType)||"上报分组".equals(businessObjectType))){
						
					}else{
						if (ListenerGatherer.getiTodoListener() != null) {
							ListenerGatherer.getiTodoListener().rejectAfter(workitemid);
						}
					}
					json.put("id", defineid);
					json.put("taskid", defineid);
					json.put("responsecode", "1");
					json.put("responsemessage", responsemessage);
				} else {
					json.put("responsecode", responsecode);
					json.put("responsemessage", "审批失败!");
				}
				}
			} catch (Exception e) {
				json.put("responsecode", responsecode);
				responsemessage = e.getMessage();
				json.put("responsemessage", responsemessage);
			}
		} else {
			json.put("responsecode", responsecode);
			json.put("responsemessage", "当前用户没有待办！");
		}
		writer.println(json);
		writer.flush();
		writer.close();
		
	}

	private void doGetback(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);

		String billDataID = (String) request.getParameter("billDataID");
		String workitemid = (String) request.getParameter("workitemid");
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject json = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		try {
			writer = response.getWriter();
			BusinessProcessManager.executeTryRetrieve(context,
					GUID.tryValueOf(workitemid), GUID.tryValueOf(billDataID));// context、工作项ID、表样ID
			json.put("responsecode", "1");
			json.put("responsemessage", responsemessage);
		} catch (Exception e) {
			json.put("responsecode", responsecode);
			responsemessage = e.getMessage();
			json.put("responsemessage", responsemessage);
		}
		writer.println(json);
		writer.flush();
		writer.close();
	}

	/**
	 * 加签 传人参数：用户、机构id,加签用户,加签意见
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	private void addApprovalUser(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		String approvaUser = (String) request.getParameter("approvaluser");
		String approvalSuggest = (String) request.getParameter("approvalsuggest");
		List<GUID> userIds = new ArrayList<GUID>();
		String[] userGuids = approvaUser.split(";");
		for (int i = 0; i < userGuids.length; i++) {
			userIds.add(GUID.tryValueOf(userGuids[i]));
		}

		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		String workitemid = (String) request.getParameter("workitemid");
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject json = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		if (workitemid != null && !"".equals(workitemid)) {
			try {
				writer = response.getWriter();
				boolean effectWorkFlow = (Boolean) request.getSession().getAttribute("isEffectWorkflow");
				
				beforeAgreeAddSign(userIds, effectWorkFlow, user, 
						approvalSuggest, context, billDefineID, billDataID, workitemid);
				
				json.put("responsecode", "1");
				json.put("responsemessage", responsemessage);
			} catch (Exception e) {
				json.put("responsecode", responsecode);
				json.put("responsemessage", responsemessage);
			}
		} else {
			json.put("responsecode", responsecode);
			json.put("responsemessage", "当前用户没有待办！");
		}
		writer.println(json);
		writer.flush();
		writer.close();
	}
	/**
	 * 同意前加签
	 */
	private void beforeAgreeAddSign(List<GUID> userIds,boolean effectWorkFlow, User user,
			String approvalSuggest, Context context, String billDefineID, String billDataID, String workitemid) {
		IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context,workitemid);
		String ids = "";
		Map<GUID, Map<String, String>> userDatas = new HashMap<GUID, Map<String, String>>();
		for (GUID userId : userIds) {
			ids = ids + userId + ",";
			if (effectWorkFlow) { 
				Map<String, String> propertys = new HashMap<String, String>();
				propertys.put(WorkFlowConst.AddApproverStaff, user
						.getID().toString());// 加签人guid
				propertys.put(WorkFlowConst.AddApproverSuggest,
						approvalSuggest.replaceAll("<br/>", "\n"));// 加签说明，PC换行显示
				userDatas.put(userId, propertys);// 被加签人guid
			} else {
				WorkflowApproveImpl impl = new WorkflowApproveImpl();
				impl.setRecid(context.newRECID());
				impl.setApproveDate(System.currentTimeMillis());
				impl.setActionID(0);
				impl.setApprovedSuggest(null);
				impl.setApproveType(GUID
						.valueOf(WorkflowUtils.ApprovedType.addApproved
								.getValue()));
				FUnit unit = EnvCenter.getCurrUnit(context);
				impl.setApproveUnitID(unit.getUnitId());
				impl.setApproveUnitTitle(unit.getUnitName());
				impl.setApproveUserID(user.getID());
				impl.setApproveUserTitle(user.getTitle());
				impl.setBillDefineID(GUID.tryValueOf(billDefineID));
				impl.setBillID(GUID.tryValueOf(billDataID));
				FUser fUser = context.get(FUser.class, userId);
				impl.setSuggestUserID(userId);
				impl.setSuggestUserTitle(fUser.getTitle());
				impl.setWorkItemID(GUID.tryValueOf(workitemid));
				impl.setAppvoreusersuggest(approvalSuggest);
				WorkflowApproveTask task = new WorkflowApproveTask(impl);
				context.handle(task, WorkflowApproveTask.Method.INSERT);// 不影响
			}
		}
		BusinessProcessManager.setProcessParaByIWorkItem(workItem,
				WorkFlowConst.addCustomApproverKey,
				ids.substring(0, ids.length() - 1));// 被加签人才有的属性，每次执行加签动作都会覆盖以前的，既只能拿到当前的被加签人
		BusinessProcessManager.setProcessParaByIWorkItem(workItem,
				"WeChataddApprovers",
				ids.substring(0, ids.length() - 1));// 移动端用来获取当前所有的被加签人标识
		BusinessProcessManager.addUsersToCurrentWorkItem(context,
				workItem, userDatas);// 把被加签人加到参与者里，且给新增的参与者设置AddApproverSuggest属性值，此时就会触发工作流的WorkItemActiveFinish事件
		BusinessProcessManager.setProcessParaByIWorkItem(workItem, // 给工作项添加加签说明
				"AddApproveSuggest", approvalSuggest);
	}

	public static void main(String[] arg) throws Exception {
		JSONObject json = new JSONObject();
		json.put("userid", "001");
		json.put("name", "hanker");

		JSONObject json2 = new JSONObject();
		json2.put("userid", "002");
		json2.put("name", "hanker");

		JSONObject json3 = new JSONObject();
		json3.put("userid", "003");
		json3.put("name", "hanker");

		JSONArray jsonarray = new JSONArray();
		jsonarray.put(json);
		jsonarray.put(json2);

		JSONObject json10 = new JSONObject();
		json10.put("array", jsonarray);

		JSONArray json_temp = (JSONArray) json10.get("array");
		json_temp.put(json3);
	}

}
