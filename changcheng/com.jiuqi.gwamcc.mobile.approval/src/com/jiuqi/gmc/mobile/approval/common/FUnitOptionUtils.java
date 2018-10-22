package com.jiuqi.gmc.mobile.approval.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.jiuqi.dna.bap.bill.common.model.BillModel;
import com.jiuqi.dna.bap.workflowmanager.execute.common.BusinessProcessManager;
import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.User;
import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.dna.workflow.WorkflowException;
import com.jiuqi.dna.workflow.engine.object.ParticipantObject;
import com.jiuqi.dna.workflow.intf.facade.IWorkItem;
import com.jiuqi.fo.common.event.FoAddApprovalEvent;
import com.jiuqi.fo.common.foconst.WorkflowConst;
import com.jiuqi.fo.workflow.common.util.WorkflowCenter;
import com.jiuqi.fo.workflow.intf.util.WorkFlowConst;
import com.jiuqi.vacomm.env.EnvCenter;
import com.jiuqi.vacomm.env.FUnitOption;

/**
 * <p>获取需要用到的系统选项信息</p>
 * 加签工具类，用于响应页面处理状态。</br>
 * @author lipengfei
 * @version 2017年03月29日
 * */
public class FUnitOptionUtils {

	/**
	 * 获取系统选项配置的结果
	 * @param context
	 * @param unitguid
	 * @param code
	 * @return
	 */
	public FUnitOption getFUnitOption(Context context, GUID unitguid, String code){
		
		FUnitOption result = EnvCenter.getUnitOption(context, unitguid, code);
		
		return result;
	}
	/**
	 * 页面控件可见时,根据用户选择和系统选项判断选择加签人之后生成待办还是同意之后生成待办</br>
	 * true:选择之后发送待办
	 * false:同意之后发送待办
	 * @param FO031
	 * @param FO037
	 * @param CSCEC001
	 * @param checked
	 * @return
	 */
	public Boolean sendSignTask(Boolean FO031, Boolean FO037, Boolean CSCEC001, Boolean checked){
		
		if(!CSCEC001){
			if(!FO037){
				if(!FO031){
					return true;
				}else{
					return false;
				}
			}
			if(FO037){
				if(!checked){
					return true;
				}else{
					return false;
				} 
			}
		}
		return false;
	}
	/**
	 * 页面控件不可见时，根据系统选项判断选择加签人之后生成待办还是同意之后生成待办</br>
	 * true:选择之后发送待办
	 * false:同意之后发送待办
	 * @param FO031
	 * @param FO037
	 * @return
	 */
	public Boolean sendSignTask(Boolean FO031, Boolean FO037){
		if(!FO037){
			if(!FO031){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	/**
	 * 同意前加签。根据被加签人是否审批完成判断
	 * true 加签人未审批完成
	 * false 加签人审批完成或者无加签人
	 * @param context
	 * @param request
	 * @param workItem
	 * @param model
	 * @param user
	 * @param workitemid
	 * @param billDataID
	 * @param billDefineID
	 * @return
	 * @throws Exception 
	 * @throws JSONException 
	 */
	public boolean signAgreeBefore(Context context,HttpServletRequest request, IWorkItem workItem, BillModel model,
			User user, String workitemid, String billDataID, String billDefineID, JSONObject json) throws Exception {
		Boolean showCheckedBox = (Boolean)request.getSession().getAttribute("showCheckedBox");
		Boolean checked = Boolean.parseBoolean(request.getParameter("checked"));
		Boolean FO031 = (Boolean)(request.getSession().getAttribute("FO031"));						
		String FO057 = request.getSession().getAttribute("FO057").toString();
			if(!agreeAfter(showCheckedBox,checked,FO031,FO057)){
				//判断加签人是否审批完成
				if(!checkAddApproval(context,request,workItem,model,user,workitemid,billDataID,billDefineID)){
					//刚性
					if(FO057.equals("1")){
						json.put("responsecode", "0");
						json.put("responsemessage", "加签人尚未签署意见");
						return true;
					}else{
						json.put("responsecode", "2");
						json.put("responsemessage", "您所选择的加签人尚未签署意见");
						return true;
					}
				}
			}						
		return false;
	}
	
	/**
	 * 同意后加签
	 * @return
	 */
	public boolean signAgreeAfter(Context context,HttpServletRequest request, IWorkItem workItem, BillModel model, User user) {
		Boolean showCheckedBox = (Boolean)request.getSession().getAttribute("showCheckedBox");
		Boolean checked = Boolean.parseBoolean(request.getParameter("checked"));
		Boolean FO031 = (Boolean)(request.getSession().getAttribute("FO031"));				
		if((FO031 && !showCheckedBox)||(checked && showCheckedBox)){
			request.getSession().setAttribute("sendSignTask", false);
	    	String approvaUser = (String) request.getParameter("approvaluser");
	    	if (!approvaUser.equals("")) {
	    		List<GUID> appointUserIDs = new ArrayList<GUID>();
	    		String[] userGuids = approvaUser.split(";");
				for (int i = 0; i < userGuids.length; i++) {
					appointUserIDs.add(GUID.tryValueOf(userGuids[i]));
				}
    			Map<GUID, Map<String, String>> userDatas = new HashMap<GUID, Map<String, String>>();
    			for (GUID userId : appointUserIDs) {
					Map<String, String> propertys = new HashMap<String, String>();
					propertys.put(WorkFlowConst.AddApproverStaff, user.getID().toString());// 加签人guid
					userDatas.put(userId, propertys);// 被加签人guid
    			}
    			BusinessProcessManager.addUsersToCurrentWorkItem(context,workItem, userDatas);
    			FoAddApprovalEvent event = new FoAddApprovalEvent();
				event.setUserDatas(userDatas);
				event.setWorkItem(workItem);
				event.setModel(model);
				context.dispatch(event);
    			return true;
	    	}
		}
		return false;
	}
	/**
	 * 判断当前节点加签人是否审批完成
	 * @param context
	 * @param workItem
	 * @param model
	 * @param user
	 * @return true审批完成。false未审批完成
	 * @throws Exception 
	 */
	@SuppressWarnings("restriction")
	private boolean checkAddApproval(Context context, HttpServletRequest request, IWorkItem workItem, BillModel model, User user, String workitemid, String billDataID, String billDefineID) throws Exception {
		boolean flag = false; 
		boolean effectWorkFlow = (Boolean) request.getSession()
				.getAttribute("isEffectWorkflow");
		if (effectWorkFlow) {
			// 根据属性addCustomApproverKey拿到被加签人，每次执行加签动作都会覆盖以前的，既只能拿到当前的被加签人
			if(workItem.getProcessInstance().getParam(WorkflowConst.addCustomApproverKey, true) == null){
				return true;
			}
			String addApprovers = workItem.getProcessInstance().getParam(WorkflowConst.addCustomApproverKey, true).toString();
			for (String str : addApprovers.split(",")) {
				if (str.equals(user.getID().toString())) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				try {
					if (workItem.getProcessInstance().getParam("33B5E59B6000004185E233B3A7F41A05", true) != null){					
						String approvers = workItem.getProcessInstance().getParam("33B5E59B6000004185E233B3A7F41A05", true).toString();
						Map<String, String> parActionMap = new HashMap<String, String>();
						for (ParticipantObject par : workItem.getParticipants()) {
							parActionMap.put(par.getUserguid(), String.valueOf(par.getAction()));
						}
						boolean temp = false;
						for (String str : approvers.split(",")) {
							if ((parActionMap.get(str) != null) && (((String)parActionMap.get(str)).toString().equals("0"))) {
								return false;
							}
							temp = true;
						}
						//能走到这里，说明选择的加签人都已经审批完成
						if(temp == true){
							return true;
						}
					}
				}
				catch (WorkflowException e) {
					 e.printStackTrace();
				}	
			}
		} else {
			flag = WorkflowCenter.isAllApproveUserSuggest(context, model,
					GUID.tryValueOf(workitemid),
					GUID.tryValueOf(billDataID),
					GUID.tryValueOf(billDefineID), user.getID());
		}
		return flag;
		
	}
	
	/**
	 * 根据系统选项和页面选项判端是否同意前加签<br>
	 * 1.showCheckedBox true checked false 同意前加签
	 * 2.shwoCheckedBox false FO031  false 同意前加签
	 * @return
	 */
	private boolean agreeAfter(Boolean showCheckedBox,Boolean checked,Boolean FO031,String FO057) {
		if(showCheckedBox && !checked){			
				return false;
			}
			if(!showCheckedBox && !FO031){
				return false;
			}
		return true;				
	}
	
}
