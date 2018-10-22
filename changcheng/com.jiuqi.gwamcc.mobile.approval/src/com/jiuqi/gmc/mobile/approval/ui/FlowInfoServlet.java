package com.jiuqi.gmc.mobile.approval.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;


//import com.jiuqi.budget.bdorg.intf.utils.OrgVersionUtils;
import com.jiuqi.budget.common.period.DataPeriod;
import com.jiuqi.budget.common.utils.BudgetConsts;
import com.jiuqi.budget.common.utils.BudgetUtils;
import com.jiuqi.budget.dim.intf.facade.FBtBudgetDim;
import com.jiuqi.budget.dim.intf.utils.DimConsts;
import com.jiuqi.budget.formula.intf.facade.FBTFormulaScheme;
import com.jiuqi.budget.hybercube.intf.facade.common.HybercubeViewUtils;
import com.jiuqi.budget.hybercube.intf.facade.data.HybercubeViewRunning;
import com.jiuqi.budget.hybercube.intf.facade.declare.DimenOutsideDeclare;
import com.jiuqi.budget.hybercube.intf.facade.declare.HybercubeViewDeclare;
import com.jiuqi.budget.hybercube.intf.facade.define.DimenOutsideDefine;
import com.jiuqi.budget.hybercube.intf.facade.define.FBtScheme;
import com.jiuqi.budget.hybercube.intf.facade.task.HyberViewRunningTask;
import com.jiuqi.budget.hybercube.intf.facade.util.LinkidUtils;
import com.jiuqi.budget.input.intf.impl.BtDataStateImpl;
import com.jiuqi.budget.reportgroup.intf.facade.ReportGroupFacade;
//import com.jiuqi.budget.solution.intf.facade.SolutionIntf;
import com.jiuqi.dna.bap.authority.intf.facade.FUser;
import com.jiuqi.dna.bap.authority.intf.util.AuthorityUtils;
import com.jiuqi.dna.bap.basedata.common.util.BaseDataCenter;
import com.jiuqi.dna.bap.basedata.intf.facade.FBaseDataObject;
import com.jiuqi.dna.bap.basedata.intf.info.QueryObjectListInfo;
import com.jiuqi.dna.bap.basedata.intf.type.AuthType;
import com.jiuqi.dna.bap.basedata.intf.util.IBaseDataContextHandle;
import com.jiuqi.dna.bap.bill.common.model.BillCentre;
import com.jiuqi.dna.bap.bill.common.model.BillModel;
import com.jiuqi.dna.bap.bill.intf.entity.BillEnclosure;
import com.jiuqi.dna.bap.bill.intf.facade.model.FBillDefine;
import com.jiuqi.dna.bap.bill.intf.model.BillConst;
import com.jiuqi.dna.bap.datastructure.facade.ZBDataType;
import com.jiuqi.dna.bap.model.common.define.base.BusinessObject;
import com.jiuqi.dna.bap.model.common.define.base.Field;
import com.jiuqi.dna.bap.model.common.define.base.Table;
import com.jiuqi.dna.bap.model.common.type.FieldType;
import com.jiuqi.dna.bap.model.common.util.BDMultiSelectUtil;
import com.jiuqi.dna.bap.multorg.common.orgtype.OrgMainType;
import com.jiuqi.dna.bap.multorg.common.util.BDOperUtil;
import com.jiuqi.dna.bap.multorg.intf.orgtree.FOrgNode;
import com.jiuqi.dna.bap.org.core.OrgCategory;
import com.jiuqi.dna.bap.workflowmanager.common.BusinessObjectLoader;
import com.jiuqi.dna.bap.workflowmanager.common.FBusinessObject;
import com.jiuqi.dna.bap.workflowmanager.common.WFBusinessObject;
import com.jiuqi.dna.bap.workflowmanager.common.parse.Button;
import com.jiuqi.dna.bap.workflowmanager.define.common.WorkflowDefineManager;
import com.jiuqi.dna.bap.workflowmanager.execute.common.BusinessProcessManager;
import com.jiuqi.dna.bap.workflowmanager.execute.common.util.WorkflowRunUtil;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.consts.ApprovalState;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.entity.BOResultField;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.entity.TaskListDefine;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.entity.WFBusinessObjectXML;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.facade.FRecord;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.facade.FTaskListDefine;
import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.User;
import com.jiuqi.dna.core.da.RecordSet;
import com.jiuqi.dna.core.def.query.QueryStatementDeclare;
import com.jiuqi.dna.core.def.query.QueryStatementDefine;
import com.jiuqi.dna.core.def.table.TableDefine;
import com.jiuqi.dna.core.misc.MissingObjectException;
import com.jiuqi.dna.core.spi.application.Application;
import com.jiuqi.dna.core.spi.application.ContextSPI;
import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.dna.ui.wt.graphics.ImageDescriptor;
import com.jiuqi.dna.ui.wt.grid2.grid2data.Grid2Data;
import com.jiuqi.dna.ui.wt.grid2.grid2data.GridCellData;
import com.jiuqi.dna.workflow.WorkflowException;
import com.jiuqi.dna.workflow.engine.EnumWorkItemState;
import com.jiuqi.dna.workflow.engine.object.ParticipantObject;
import com.jiuqi.dna.workflow.intf.facade.IWorkItem;
import com.jiuqi.dnadb.main.DataCenter;
import com.jiuqi.fo.common.foconst.FieldConst;
import com.jiuqi.fo.common.foconst.WorkflowConst;
import com.jiuqi.fo.common.utils.FoChangeBillUtil;
//import com.jiuqi.fo.intf.utilis.FoSumTodoApprovalUtil;
import com.jiuqi.fo.workflow.common.util.WorkflowCenter;
import com.jiuqi.fo.workflow.intf.impl.WorkflowApproveImpl;
import com.jiuqi.fo.workflow.intf.task.WorkflowApproveTask;
import com.jiuqi.fo.workflow.intf.util.WorkFlowConst;
import com.jiuqi.fo.workflow.intf.util.WorkflowUtils;
import com.jiuqi.gmc.mobile.approval.common.AccessoriesUtils;
import com.jiuqi.gmc.mobile.approval.common.AppUtil;
import com.jiuqi.gmc.mobile.approval.common.FUnitOptionUtils;
import com.jiuqi.gmc.mobile.approval.key.BillStyleKey;
import com.jiuqi.mt2.dna.mobile.bill.intf.facade.FBillStyleTemplate;
import com.jiuqi.mt2.dna.mobile.todo.facade.FApprovalDefine;
import com.jiuqi.mt2.dna.service.todo.impl.MetadataImpl;
import com.jiuqi.mt2.dna.service.todo.impl.TodoListStream;
import com.jiuqi.mt2.dna.service.todo.internal.ListenerGatherer;
import com.jiuqi.mt2.dna.service.todo.util.MWFUtil;
import com.jiuqi.mt2.dna.service.todo.util.MWorkflowUtil;
import com.jiuqi.mt2.spi.bill.metadata.MobileBillDefine;
import com.jiuqi.mt2.spi.bill.metadata.PageDefine;
import com.jiuqi.mt2.spi.call.IStream;
import com.jiuqi.mt2.spi.common2.table.IMField;
import com.jiuqi.mt2.spi.common2.table.IMShowTemplate;
import com.jiuqi.mt2.spi.common2.table.IMTableCell;
import com.jiuqi.mt2.spi.common2.table.IMTableRow;
import com.jiuqi.mt2.spi.log.MobileLog;
import com.jiuqi.mt2.spi.todo.SPITodo;
import com.jiuqi.mt2.spi.todo.model.ITodoItem;
import com.jiuqi.mt2.spi.todo.model.impl.TodoItem;
import com.jiuqi.sm.attachment.intf.FSmAttachment;
import com.jiuqi.util.JqLib;
import com.jiuqi.vacomm.env.EnvCenter;
import com.jiuqi.vacomm.env.FUnit;
import com.jiuqi.vacomm.env.FUnitOption;
import com.jiuqi.vacomm.utils.sys.BizHttpServlet;
import com.jiuqi.xlib.utils.StringUtil;

public class FlowInfoServlet extends BizHttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8814724007498830255L;

	@Override
	protected void doService(Context context, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		response.setHeader("pragma", "no-cache");
		response.setHeader("cache-control", "no-cache");
		response.setHeader("expires", "0");
		response.setContentType("text/text;charset=UTF-8");
		if ("flowlist".equals(action)) {//获取审批类型选项的集合
			getFlowList(context, request, response);
		} else if ("flowitem".equals(action)) {//获取每个审批类型选项的具体审批数据
			getFlowitem(context, request, response);
		} else if ("billinfo".equals(action)) {
			getBillinfo(context, request, response);
		} else if ("flowcount".equals(action)) {
			getFlowCount(context, request, response);
		} else if ("roles".equals(action)) {
			try {
				getRolesAndUsers(context, request, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("addapprovalusers".equals(action)) {
			getAddApprovalUsers(context, request, response);
		} else if ("approvallist".equals(action)) {
			getApprovalList(context, request, response);
		} else if ("attachmentlist".equals(action)) {
			getAttachmentList(context, request, response);
		}
	}
	
	/**
	 * 新界面附件列表
	 */
	public void getAttachmentList(Context context, HttpServletRequest request, HttpServletResponse response) {
		JSONArray maininfo = new JSONArray();
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			String userstr = (String)request.getSession().getAttribute("user");
			User user = context.find(User.class, userstr.toLowerCase());
			context.changeLoginUser(user);
			context.newRECID();
			boolean isList = true;
			getAttachment(maininfo, response, request, isList);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.println(maininfo);
			writer.flush();
			writer.close();
		}
	}
	
	/**
	 * 新界面已办待办一起展示
	 */
	public void getApprovalList(Context context, HttpServletRequest request, HttpServletResponse response) {
		String userstr = (String) request.getParameter("user");
		if (userstr == null|| userstr.equals("")|| userstr.equals("null")) {
			userstr = (String)request.getSession().getAttribute("user");
		}
		request.getSession().setAttribute("user", userstr);
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		GUID userID = user.getID();
		GUID unitguid = (GUID)request.getSession().getAttribute("org");
		if (unitguid == null || unitguid.equals("")) {
			FUser fUser = context.get(FUser.class, user.getID());
			unitguid = fUser.getBelongedUnit();
			request.getSession().setAttribute("org",fUser.getBelongedUnit());
		}
		context.getLogin().setUserCurrentOrg(unitguid);		
		context.setUserCurrentOrg(unitguid);
		JSONObject jsonName = new JSONObject();
		PrintWriter writer = null;
		String type = request.getParameter("type");
		ApprovalState approvalType = ApprovalState.WAIT;
		try {
			List<FApprovalDefine> defines = context.getList(FApprovalDefine.class);
			JSONArray jsonItems = new JSONArray();
			writer = response.getWriter();
			int num = 0;
			if (!type.equals("0")) {
				jsonName.put("hasdonelist",jsonItems);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());
				calendar.add(Calendar.MONTH, -3);
				for(FApprovalDefine define : defines){
					FTaskListDefine ftaskDefine = context.get(FTaskListDefine.class,define.getTaskDefineID());
					if(ftaskDefine != null) {
						List<FRecord> recordList = BusinessProcessManager.getFRecords(context,ftaskDefine.getID(), userID, ApprovalState.WAITHIGHER);
						String title = define.getTitle();
						//获取图片配置信息
						JSONObject json = new JSONObject();
						String imageUrl = getIcon(context,define,request);
						json.put("imageurl", imageUrl);
						json.put("title", title);
						json.put("id", define.getRECID());
						num = recordList.size();
						json.put("num", num);
						jsonItems.put(json);
					}
				}
			} else {
				jsonName.put("todolist",jsonItems);
				for(FApprovalDefine define : defines){
					FTaskListDefine ftaskDefine = context.get(FTaskListDefine.class,define.getTaskDefineID());
					if(ftaskDefine != null) {
						List<FRecord> recordList = BusinessProcessManager.getFRecords(context,ftaskDefine.getID(), userID, approvalType);
						String title = define.getTitle();
						//获取图片配置信息
						JSONObject json = new JSONObject();
						String imageUrl = getIcon(context,define,request);
						json.put("imageurl", imageUrl);
						json.put("title", title);
						json.put("id", define.getRECID());
						num = recordList.size();
						json.put("num", num);
						jsonItems.put(json);
					}
				 }
				 if("0".equals(type)){
					String title = "我的加签";
					JSONObject json = new JSONObject();
					json.put("title", title);
					json.put("id", GUID.emptyID);
					if(WorkflowCenter.getToDoApprovalNum(context)!=null)
						num=WorkflowCenter.getToDoApprovalNum(context);
					json.put("num", num);
					jsonItems.put(json);
				 }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		writer.println(jsonName);
		writer.flush();
		writer.close();
	}
	
	/**
	 * 获取移动端审批设置图标
	 * @param define  void
	 */
	private String getIcon(Context context, FApprovalDefine define, HttpServletRequest request) {	
		String configs = define.getConfig();
		JSONObject object = new JSONObject(configs);
		if(!object.isNull("iconID")){
			GUID iconID = GUID.tryValueOf(object.get("iconID").toString());
			if(iconID!=null){
				ImageDescriptor img = context.find(ImageDescriptor.class, iconID);
				if(img!=null){
					String url = "http://"+request.getLocalAddr()+":"+request.getLocalPort();
					return url+img.getDNAURI();
				}
			}
		}		
		return null;
	}

	/**
	 * 获取根据移动客户端配置审批列表定义获取待办数据数目； 传人参数 ：用户名 、机构 审批列表定义过滤条件中增加上报对象ID,供sql查询待办数据
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	public void getFlowCount(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		String userstr = (String) request.getParameter("user");
		if (userstr == null|| userstr.equals("")|| userstr.equals("null")) {
			userstr = (String)request.getSession().getAttribute("user");
		}
		request.getSession().setAttribute("user", userstr);
		if(userstr == null) userstr = (String)request.getParameter("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		GUID userID = user.getID();

		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);

		String defineId = (String) request.getParameter("id");
		JSONObject jsonName = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		JSONArray jsonItems = new JSONArray();
		PrintWriter writer = null;
		String type = "0";
		ApprovalState approvalType = ApprovalState.WAIT;
		ApprovalState approvalEnd = ApprovalState.WAIT;
		if ("0".equals(type)) {
			approvalType = ApprovalState.WAIT;
		} else if ("1".equals(type)) {
			approvalType = ApprovalState.WAITHIGHER;
			approvalEnd = ApprovalState.PASS;
		}
		try {
			writer = response.getWriter();
			if (GUID.emptyID.equals(GUID.tryValueOf(defineId))
					&& "0".equals(type)) {
				String title = "我的加签";
				jsonName.put("resultlist", jsonItems);
				jsonName.put("responsecode", "1");
				jsonName.put("responsemessage", responsemessage);
				JSONObject json = new JSONObject();
				json.put("title", title);
				json.put("id", GUID.emptyID);
				int num = 0;
				if (WorkflowCenter.getToDoApprovalNum(context) != null)
					num = WorkflowCenter.getToDoApprovalNum(context);
				json.put("num", num);
				jsonItems.put(json);
			} else {
				List<FApprovalDefine> defines = context
						.getList(FApprovalDefine.class);
				for (FApprovalDefine define : defines) {
					int count = 0;
					if (defineId.equals(define.getRECID().toString())) {
						String cate = define.getTaskDefineID().toString();
						List<FRecord> recordList = BusinessProcessManager
								.getFRecords(context, GUID.valueOf(cate),
										userID, approvalType);
						// 新增加关于添加审批结束数据，  以前的需求只有查看已提交，等待下一步审批
						// 在flowtype == 1 的基础上，  approvalType  增加对    审批数据的查询实现
						// 调用的接口  依赖于  BusinessProcessManager.getFRecords(context, GUID.valueOf(cate),userID, approvalEnd);
						// 最后把查询的值放入recordList中
						if(approvalEnd.equals(ApprovalState.PASS))
						{
							List<FRecord> recordListApprovalEnd = BusinessProcessManager      //获取defineID  对应的  item  集合
									.getFRecords(context, GUID.valueOf(cate),
											userID, approvalEnd);
							if(recordListApprovalEnd.size() > 0)
							{
							    recordList.addAll(recordListApprovalEnd);
							}
						}
						
						jsonName.put("resultlist", jsonItems);
						jsonName.put("responsecode", "1");
						jsonName.put("responsemessage", responsemessage);
						String title = define.getTitle();
						JSONObject json = new JSONObject();
						json.put("title", title);
						json.put("id", define.getRECID());
						if (recordList != null && recordList.size() > 0) {
							count = recordList.size();
						}
						json.put("num", count);
						jsonItems.put(json);
					}
				}
			}
		} catch (Exception e) {
			jsonName.put("resultlist", jsonItems);
			jsonName.put("responsecode", responsecode);
			responsemessage = e.getMessage();
			jsonName.put("responsemessage", responsemessage);
		}
		writer.println(jsonName);
		writer.flush();
		writer.close();
	}

	/**
	 * 获取根据移动客户端配置审批列表定义获取待办数据数目； 传人参数 ：用户名 、机构 审批列表定义过滤条件中增加上报对象ID,供sql查询待办数据
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	public void getFlowList(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		String flowtype = (String) request.getParameter("flowtype");
		String userstr = (String) request.getParameter("user");
		if (userstr == null|| userstr.equals("")|| userstr.equals("null")) {
			userstr = (String)request.getSession().getAttribute("user");
		}
		request.getSession().setAttribute("user", userstr);
		if(userstr == null) userstr = (String)request.getParameter("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		GUID userID = user.getID();
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		//add by tianxuejun
		if(unitguid == null){
			if(request.getParameter("org") != null){
				unitguid = GUID.tryValueOf((String)request.getParameter("org"));
			}
		}
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		JSONObject jsonName = new JSONObject();
		String responsecode = "0";
		String responsemessage = "";
		JSONArray jsonItems = new JSONArray();
		PrintWriter writer = null;
		String type = flowtype;
		ApprovalState approvalType = ApprovalState.WAIT;  
		ApprovalState approvalEnd = ApprovalState.WAIT;   //这里的代码进行逻辑的判断实现 
		
		if ("0".equals(type)) {
			approvalType = ApprovalState.WAIT;
		} else if ("1".equals(type)) {
			approvalType = ApprovalState.WAITHIGHER;
			approvalEnd = ApprovalState.PASS;
		}
		try {
			writer = response.getWriter();
			List<FTaskListDefine> taskListDefineLists = new ArrayList<FTaskListDefine>();// 审批任务列表定义集合，有数据且不重复的
			//获取待审批单据类型的集合
			List<FApprovalDefine> defines = context  
					.getList(FApprovalDefine.class);
			Map<FTaskListDefine, FApprovalDefine> maps = new HashMap<FTaskListDefine, FApprovalDefine>();
			for (FApprovalDefine define : defines) {
				FTaskListDefine ftaskDefine = context.get(
						FTaskListDefine.class, define.getTaskDefineID());// 移动端审批任务列表定义和审批任务列表定义是一一对应关系define.getTaskDefineID()
				TaskListDefine taskDefine = ftaskDefine.getTaskListDefine();
				if (taskDefine.businessObjectList.size() == 0)
					continue;
				if (taskListDefineLists != null
						&& taskListDefineLists.contains(ftaskDefine)) {
					continue;
				} else {
					maps.put(ftaskDefine, define);// 审批任务列表定义和移动端审批任务列表定义                    
					taskListDefineLists.add(ftaskDefine);
				}
			}
			Map<FTaskListDefine, List<FRecord>> defineAndRecords = AppUtil
					.getWaitRecordsByJK(context, userID, taskListDefineLists,
							approvalType);
			Map<FTaskListDefine, List<FRecord>> defineAndRecordsEnd = new HashMap<FTaskListDefine, List<FRecord>>();
			//用来增加逻辑实现添加审批结束数据,如果是已办进行数据查询,否则不查询
			if(approvalEnd.equals(ApprovalState.PASS))
			{
				defineAndRecordsEnd = AppUtil
						.getWaitRecordsByJK(context, userID, taskListDefineLists,
								approvalEnd);
			}
			Map<FApprovalDefine, Integer> defineCount = new HashMap<FApprovalDefine, Integer>();
			for (FTaskListDefine taskListDefine : taskListDefineLists) {
				FApprovalDefine define = maps.get(taskListDefine);
				List<FRecord> counts = defineAndRecords.get(taskListDefine);
				List<FRecord> countsEnd = defineAndRecordsEnd.get(taskListDefine);
				if (counts != null) {
					if(countsEnd != null)
					{
						defineCount.put(define, counts.size() + countsEnd.size());// 添加审批结束数据的个数  如果存在的话
					}
					else
					{
					    defineCount.put(define, counts.size());// 移动端审批任务列表定义和与之对应的待办数目
					}
				}
			}
			jsonName.put("resultlist", jsonItems);
			jsonName.put("responsecode", "1");
			jsonName.put("responsemessage", responsemessage);
			jsonName.put("type", type);
			int num = 0;
			for (FApprovalDefine define : defines) {
				String title = define.getTitle();
				JSONObject json = new JSONObject();
				json.put("title", title);// 移动端审批任务列表定义名称
				json.put("id", define.getRECID());// 移动端审批任务列表定义id
				if (defineCount.get(define) != null)
					num = defineCount.get(define);
				json.put("num", num);// 移动端审批任务列表定义的待办数目
				jsonItems.put(json);
			}
			if ("0".equals(type)) {
				String title = "我的加签";
				JSONObject json = new JSONObject();
				json.put("title", title);
				json.put("id", GUID.emptyID);
				if (WorkflowCenter.getToDoApprovalNum(context) != null)
					num = WorkflowCenter.getToDoApprovalNum(context);
				json.put("num", num);
				jsonItems.put(json);
			}
		} catch (Exception e) {
			jsonName.put("resultlist", jsonItems);
			jsonName.put("responsecode", responsecode);
			responsemessage = e.getMessage();
			jsonName.put("responsemessage", responsemessage);
			e.printStackTrace();
		}
		writer.println(jsonName);
		writer.flush();
		writer.close();
	}

	/**
	 * 获取审批定义.<br>
	 * 
	 * @param context
	 * @param model
	 * @return String
	 */
	private String getTaskDefine(Context context, GUID billDefineID,
			GUID billDataID) {
		FBillDefine billDefine = BillCentre.findBillDefine(context,
				billDefineID);
		BillModel model = BillCentre.createBillModel(context, billDefine);
		model.load(billDataID);
		List<FTaskListDefine> list = model.getContext().getList(
				FTaskListDefine.class);
		for (int i = 0; i < list.size(); i++) {
			TaskListDefine taskListDefine = list.get(i).getTaskListDefine();
			if (taskListDefine != null) {
				if (taskListDefine.businessObjectList.size() > 0) {
					if (taskListDefine.businessObjectList.get(0).getID()
							.equals(billDefineID))
						return taskListDefine.getID().toString();
				}
			}
		}
		return null;
	}
	
	/**
	 * 通过单据定义查找移动审批列表定义
	 */
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

	/**
	 * 获取审批列表定义下的具体待办数据 传入参数：用户名、机构id,移动端审批列表定义Id
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	private void getFlowitem(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		String userstr = (String) request.getParameter("user");
		if (userstr == null|| userstr.equals("")|| userstr.equals("null")) {
			userstr = (String)request.getSession().getAttribute("user");
		}
		request.getSession().setAttribute("user", userstr);
		if(userstr == null) userstr = (String)request.getParameter("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		GUID userID = user.getID();                                             
		String defineId = (String) request.getParameter("id");// 移动端审批任务列表定义
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		// context.getLogin().setUserCurrentOrg(unitguid);
		// context.setUserCurrentOrg(unitguid);
		String taskid = "";
		List<FApprovalDefine> defines = context.getList(FApprovalDefine.class);
		for (FApprovalDefine define : defines) {
			if (define.getRECID().equals(GUID.tryValueOf(defineId))) {
				taskid = define.getTaskDefineID().toString();// 拿到对应的审批任务列表定义 
				break;                              
			}
		}
		HashMap<String, String> configMap = new HashMap<String, String>();
		PrintWriter writer = null;
		JSONObject resultObjectJson = new JSONObject();
		JSONArray itemsJson = new JSONArray();
		String responsecode = "0";
		String responsemessage = "";
		String type = (String) request.getParameter("flowtype");
		String batchapproveaction = "0";
		String batchwfrejectaction = "0";
		int sum = 0;
		ApprovalState approvalType = ApprovalState.WAIT;                          
		ApprovalState approvalEnd = ApprovalState.WAIT;                           
		if ("0".equals(type)) {
			approvalType = ApprovalState.WAIT;
		} else if ("1".equals(type)) {
			approvalType = ApprovalState.WAITHIGHER;
			approvalEnd = ApprovalState.PASS;//添加审批结束数据， 在已办的情况下
		}
		try {
			writer = response.getWriter();
			resultObjectJson.put("resultlist", itemsJson);
			resultObjectJson.put("responsecode", "1");
			resultObjectJson.put("responsemessage", responsemessage);
			resultObjectJson.put("type", type);
			if (GUID.emptyID.equals(GUID.tryValueOf(defineId))) {              //这里获取的是什么数据，   批量操作， 批量·拒绝·
				resultObjectJson.put("batchapproveaction", batchapproveaction);
				resultObjectJson
						.put("batchwfrejectaction", batchwfrejectaction);
				RecordSet rs = getRecordSet(context, userID);//加签待办
				while (rs.next()) {
					sum ++;
					JSONObject itemsObjectJson = new JSONObject();
					JSONObject itemObjectJson = new JSONObject();
					itemsObjectJson.put("item", itemObjectJson);
					JSONArray itemJson = new JSONArray();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String billcode = rs.getFields().get(0).getString();
					GUID billDefineID = rs.getFields().get(1).getGUID();
					GUID billDataID = rs.getFields().get(2).getGUID();
					GUID workitemid = rs.getFields().get(3).getGUID();
					String creattime = sdf.format(rs.getFields().get(4)
							.getDate());
					String createruser = rs.getFields().get(5).getString();
					String summitruser = rs.getFields().get(6).getString();
					String approvaluser = rs.getFields().get(7).getString();
					String orgtilte = rs.getFields().get(9).getString();
					itemObjectJson.put("billDataID", billDataID.toString());
					itemObjectJson.put("billDefineID", billDefineID.toString());
					itemObjectJson.put("property", itemJson);
					if (taskid.equals("")) {
						taskid = getTaskDefine(context, billDefineID,
								billDataID);
					}
					itemObjectJson.put("taskid", taskid);
					itemObjectJson.put("workitemid", workitemid.toString());
					itemObjectJson.put("commissionuser", "1");
					JSONObject jsons0 = new JSONObject();
					jsons0.put("title", "单据编号");
					jsons0.put("value", billcode);
					itemJson.put(jsons0);
					JSONObject jsons1 = new JSONObject();
					jsons1.put("title", "单据日期");
					jsons1.put("value", creattime);
					itemJson.put(jsons1);
					JSONObject jsons2 = new JSONObject();
					jsons2.put("title", "申请人");
					jsons2.put("value", summitruser);
					itemJson.put(jsons2);
					JSONObject jsons3 = new JSONObject();
					jsons3.put("title", "审批人");
					jsons3.put("value", approvaluser);
					itemJson.put(jsons3);
					JSONObject jsons4 = new JSONObject();
					jsons4.put("title", "组织机构");
					jsons4.put("value", orgtilte);
					itemJson.put(jsons4);
					itemsJson.put(itemsObjectJson);
				}
			} else {
				List<String> cells = new ArrayList<String>();
				for (FApprovalDefine define : defines) {                         
					if (defineId.equals(define.getRECID().toString())) {          //判断所选的  ID  是不是大的  flowList集合里面的
						configMap = getRecordFileds(define, cells);// 这个map存放移动审批列表界面配置的具体内容（A列的值，B列的值）
						String cate = define.getTaskDefineID().toString();
						List<FRecord> recordList = BusinessProcessManager      
								.getFRecords(context, GUID.valueOf(cate),
										userID, approvalType);
						// 新增加关于添加审批结束数据，  以前的需求只有查看已提交，等待下一步审批
						// 在flowtype == 1 的基础上，  approvalType  增加对    审批数据的查询实现
						// 调用的接口  依赖于  BusinessProcessManager.getFRecords(context, GUID.valueOf(cate),userID, approvalEnd);
						// 最后把查询的值放入recordList中
						if(approvalEnd.equals(ApprovalState.PASS))
						{
							List<FRecord> recordListApprovalEnd = BusinessProcessManager      //获取defineID  对应的  item  集合
									.getFRecords(context, GUID.valueOf(cate),
											userID, approvalEnd);
							if(recordListApprovalEnd.size() > 0)
							{
							    recordList.addAll(recordListApprovalEnd);
							}
						}
						FTaskListDefine ftaskDefine = context.get(
								FTaskListDefine.class, GUID.valueOf(cate));
						TaskListDefine taskDefine = ftaskDefine
								.getTaskListDefine();
						String businessObjectType = "";
						ArrayList<WFBusinessObjectXML> objectList = taskDefine.businessObjectList;
						if(objectList!=null && objectList.size()>0){
							businessObjectType = taskDefine.businessObjectList.get(0).getType();
						}
						if(businessObjectType!=null && ("多维表".equals(businessObjectType)||"上报分组".equals(businessObjectType))){
							// add by tianxuejun
							if (taskDefine.actionList.size() > 0) {
								for (int j = 0; j < taskDefine.actionList.size(); j++) {
									String actionName = taskDefine.actionList.get(j).getName();
									if ("BatchAccept".equalsIgnoreCase(actionName) || "ChildBatchAccept".equalsIgnoreCase(actionName)) {
										batchapproveaction = "1";
									} 
								}
							}
						}else{
							if (taskDefine.actionList.size() > 0) {
								for (int j = 0; j < taskDefine.actionList.size(); j++) {
									if ("BatchApproveAction"
											.equalsIgnoreCase(taskDefine.actionList
													.get(j).getName())) {
										batchapproveaction = "1";
									} else if ("BatchWFRejectAction"
											.equalsIgnoreCase(taskDefine.actionList
													.get(j).getName())) {
										batchwfrejectaction = "1";
									}
								}
							}
						}
						resultObjectJson.put("batchapproveaction",
								batchapproveaction);
						resultObjectJson.put("batchwfrejectaction",
								batchwfrejectaction);
						IStream<ITodoItem> allData = getItemListStream(cate,
								context, recordList, -1);
						ITodoItem[] list = allData.nexts();
						sum = list.length;
						for (int j = 0; j < list.length; j++) {
							ITodoItem dataValue = list[j];
							JSONObject itemsObjectJson = new JSONObject();
							JSONObject itemObjectJson = new JSONObject();
							itemsObjectJson.put("item", itemObjectJson);
							JSONArray itemJson = new JSONArray();
							String workitemid = dataValue.getId();
							String commissionuser = "1";
							IWorkItem work = WorkflowRunUtil.loadWorkItem(context, workitemid);
							if (work != null) {
								List<ParticipantObject> participants = work
										.getParticipants();
								for (ParticipantObject participant : participants) {
									String commission = participant
											.getCommission();
									if (commission != null
											&& commission.contains(userID
													.toString())) {
										commissionuser = "0";
									}
								}
							}
							String billDataID = dataValue.getBillDataID();
							String billDefineID = dataValue.getBillDefineID();
							boolean isUrgent = dataValue.isUrgent();
							String urgent = "0";
							if (isUrgent) {
								urgent = "1";
							}
							itemObjectJson.put("billDataID", billDataID);
							itemObjectJson.put("billDefineID", billDefineID);
							itemObjectJson.put("businessObjectType", businessObjectType);
							itemObjectJson.put("property", itemJson);
							itemObjectJson.put("workitemid", workitemid);
							itemObjectJson.put("taskid", taskid);
							itemObjectJson
									.put("commissionuser", commissionuser);
							itemObjectJson.put("urgent", urgent);
							for (String arg : cells) {
								if (configMap.containsKey(arg)) {
									String value = configMap.get(arg);
									JSONObject jsons = new JSONObject();
									jsons.put("title", value);
									IMField fiedMoney = dataValue.getField(arg);
									String billValue = "";
									if("BT_DATASTATE_LOGDATATIME".equals(arg)){
										BtDataStateImpl state = context.get(BtDataStateImpl.class, GUID.tryValueOf(billDataID));
										DataPeriod dataperiod = new DataPeriod(state.getYear(),state.getPeriod(),state.getPeriodNum());
										if(dataperiod!=null){
											billValue = dataperiod.getTitle();
										}
										jsons.put("value", billValue);
									}else if(arg.contains("BT_DATASTATE_LOGLINKID") && arg.contains("[")){
										BtDataStateImpl state = context.get(BtDataStateImpl.class, GUID.tryValueOf(billDataID));
										arg = arg.substring(arg.indexOf("[")+1,arg.length()-1);
										 String linkid = state.getLinkId();
										 if(linkid!=null && linkid.contains(arg)){
											 if(linkid.contains(",")){
												 String[] dimouts = linkid.split(",");
												 for(String dim : dimouts){
													String[] dims = dim.split("=");
													 if(dims[0].equals(arg)){
														String dimId = dim.substring(dim.indexOf("=")+1);
														FBtBudgetDim btBudgetDim = context.get(FBtBudgetDim.class, arg);
														if(btBudgetDim!=null){
															FBaseDataObject baseDataObject = BaseDataCenter.findObject(context, btBudgetDim.getDimTableName(), GUID.tryValueOf(dimId));
															billValue = baseDataObject.getStdName();
														}
													 }
												 }
											 }else{
												 String[] dims = linkid.split("=");
												 String dimId = dims[1];
												 FBtBudgetDim btBudgetDim = context.get(FBtBudgetDim.class, dims[0]);
												 if(btBudgetDim!=null){
													FBaseDataObject baseDataObject = BaseDataCenter.findObject(context, btBudgetDim.getDimTableName(), GUID.tryValueOf(dimId));
													billValue = baseDataObject.getStdName();
												 }
											 }
										 }
										 jsons.put("value", billValue);
									}else if (fiedMoney != null
											&& fiedMoney.getValueType() != null
											&& fiedMoney.getValueType().name()
													.equals("STRING")) {
										billValue = fiedMoney.getStringValue();
										if ("false".equals(billValue)) {
											billValue = "否";
										} else if ("true".equals(billValue)) {
											billValue = "是";
										}
										jsons.put("value", billValue);
									} else if (fiedMoney != null
											&& fiedMoney.getValueType() != null
											&& fiedMoney.getValueType().name()
													.equals("DOUBLE")) {
										Double valu = fiedMoney
												.getDoubleValue();
										billValue = valu + "";
										jsons.put("value", billValue);
									} else {
										jsons.put("value", billValue);
									}
									itemJson.put(jsons);
								}
							}
							itemsJson.put(itemsObjectJson);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultObjectJson.put("resultlist", itemsJson);
			resultObjectJson.put("responsecode", responsecode);
			responsemessage = e.getMessage();
			resultObjectJson.put("responsemessage", responsemessage);
			resultObjectJson.put("sum", sum);
		}
		resultObjectJson.put("sum", sum);
		writer.println(resultObjectJson);
		writer.flush();
		writer.close();
	}

	public Application getApp() {
		Application a = null;
		a = (Application) getServletContext().getAttribute("dna-application");
		if (a == null) {
			throw new MissingObjectException("未找到DNA-Application");
		}
		return a;
	}

	/**
	 * 加签说明 
	 * @return
	 */
	public String getApproveUserSuggest(Context context, GUID unitID,
			GUID billID, GUID defineID, IWorkItem workItem,
			boolean isEffectWorkflow) {
		User user = context.getLogin().getUser();
		FUser fUser = context.get(FUser.class, user.getID());
		if (isEffectWorkflow) {// 影响工作流
			return BusinessProcessManager.getProcessParaByIWorkItem(workItem,
					"AddApproveSuggest");
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
	 * 获取单据详细信息 传人参数：用户名、机构id、单据定义id,单据数据id
	 * 
	 * @param context
	 * @param request
	 * @param response
	 */
	private void getBillinfo(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		String userstr = (String) request.getParameter("user");
		if (userstr == null|| userstr.equals("")|| userstr.equals("null")) {
			userstr = (String)request.getSession().getAttribute("user");
		}
		request.getSession().setAttribute("user", userstr);
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		String businessObjectType = (String)request.getParameter("businessObjectType");
		String workitemid = (String) request.getParameter("workitemid");
		String defineid = (String) request.getParameter("taskid");// 审批列表定义
		FApprovalDefine fApprovalDefine = null;// 移动端审批列表定义
		if (defineid == null || "".equals(defineid)) {
			defineid = (String) request.getParameter("id");
		}
		String reportObjectCode = "";
		if(billDefineID!=null && !"".equals(billDefineID) && businessObjectType!=null && !"".equals(businessObjectType)){
			if("多维表".equals(businessObjectType)){
				HybercubeViewDeclare declare =context.find(HybercubeViewDeclare.class,GUID.valueOf(billDefineID));
				if(declare!=null){
					reportObjectCode = declare.getName();
				}
			}else if("上报分组".equals(businessObjectType)){
				ReportGroupFacade group= context.get(ReportGroupFacade.class, GUID.valueOf(billDefineID));
				if(group!=null){
					reportObjectCode = group.getScode();
				}
			}
		}
		HttpSession session1 = request.getSession();
		String type = (String) request.getParameter("type");
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		
		if (unitguid == null || unitguid.equals("")) {
			FUser fUser = context.get(FUser.class, user.getID());
			unitguid = fUser.getBelongedUnit();
			request.getSession().setAttribute("org",fUser.getBelongedUnit());
		}
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject detailJsonName = new JSONObject();
		String navigationTitle = "";// 导航栏标题
		String fapprovaltasklistid = (String) request
				.getParameter("fapprovaltasklistid");// 移动端审批列表定义Id
		List<FApprovalDefine> defines = context.getList(FApprovalDefine.class);// 移动端审批列表定义集合
		for (FApprovalDefine define : defines) {
			if (fapprovaltasklistid == null || fapprovaltasklistid.equals("null")
					|| fapprovaltasklistid.equals("")) {
				if (define.getTaskDefineID().equals(GUID.tryValueOf(defineid))) {// 审批列表定义找到移动端审批列表定义Id
					navigationTitle = define.getTitle();
					fApprovalDefine = define;
					break;
				}
			} else {
				if (define.getRECID().equals(
						GUID.tryValueOf(fapprovaltasklistid))) {
					navigationTitle = define.getTitle();
					fApprovalDefine = define;
					break;
				}
			}
		}
		// 判断单据是否取回或已审批
		com.jiuqi.dna.core.spi.application.Session session = getApp().newSession(null, null);
		ContextSPI ctx = null;
		IWorkItem workItem = null;
		session.setHeartbeatTimeoutSec(300);
		session.setSessionTimeoutMinutes(0);
		ctx = session.newContext(true);
		// add by tianxuejun 
		ctx.changeLoginUser(user);
		ctx.getLogin().setUserCurrentOrg(unitguid);
		ctx.setUserCurrentOrg(unitguid);
		
		workItem = WorkflowRunUtil.loadWorkItem(ctx, workitemid);
		List<ParticipantObject> ParticipantList = workItem.getParticipants();
		try {
			writer = response.getWriter();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		BillStyleKey key = new BillStyleKey();
		key.setBillDefineID(billDefineID);
		FBillStyleTemplate style = context.find(FBillStyleTemplate.class,key);
		
		if(businessObjectType!=null && ("多维表".equals(businessObjectType)||"上报分组".equals(businessObjectType))){
			 String getback = "1";
			 Grid2Data grid=null;
			 JSONArray maininfo = new JSONArray();
			 JSONArray subinfos = new JSONArray();
			 String responsemessage = "";
			 String addapprovalsuggest = "0";
			 context.setUserCurrentOrg(unitguid);
			 BtDataStateImpl state = context.get(BtDataStateImpl.class, GUID.tryValueOf(billDataID));
			 if(state!=null){
				 detailJsonName.put("maininfo",maininfo);
				 detailJsonName.put("responsecode", "1");
				 detailJsonName.put("billDataID", billDataID);
				 detailJsonName.put("billDefineID", billDataID);
				 detailJsonName.put("id",  defineid);
				 detailJsonName.put("taskid",  defineid);//修改的地方
				 detailJsonName.put("responsemessage", responsemessage);
				 detailJsonName.put("workitemid", workitemid);
				 detailJsonName.put("getback", getback);
				 detailJsonName.put("AgreeActionList", "同意");
				 detailJsonName.put("RejectActionList", "驳回");
				 Map<String,String> mainDatas = new HashMap<String,String>();
				 Map<String,Map<String,String>> detailDatas = new HashMap<String,Map<String,String>>();
				 Map<String,Grid2Data> gridMaps = new HashMap<String,Grid2Data>();
				 String maintitle ="";
				 List<String> mainDataList = new ArrayList<String>();
				 Map<String,List<String>> detailDataList = new HashMap<String,List<String>>();
				 List<PageDefine> detailPages = new ArrayList<PageDefine>();

				 MobileBillDefine billdefine = style.getMobileBillDefine();
				 if(style.getCode().equals(reportObjectCode)){
					 PageDefine mainPage = billdefine.getMasterPage();
					 maintitle = style.getTitle();
					 mainDatas = getBudgetMainDatas(context,mainPage,mainDataList);
					 detailPages = billdefine.getDetailPages();
					 detailDatas = getBudgetDetailDatas(detailPages,detailDataList);
				 }
				 detailJsonName.put("maintitle",maintitle);
				 String viewName = "";
				 for(String mainData : mainDataList){
					 if(mainDatas!=null && mainDatas.containsKey(mainData)){
						 if(mainData.startsWith("name")){
							 String[] viewNames = mainData.split(":");
							 viewName = viewNames[1];
							 if(gridMaps!=null && gridMaps.containsKey(viewName)){
								 grid = gridMaps.get(viewName);
							 }else{
								 grid = initView(context,viewName,state);
								 gridMaps.put(viewName, grid);
							 }
						 }else{
							 String title = mainDatas.get(mainData);
							 JSONObject jsons = new JSONObject();
							 jsons.put("title", title);
							 String value =getFieldValues(context,GUID.tryValueOf(billDataID),mainData,grid);
							 jsons.put("value", value);
							 maininfo.put(jsons);
						 }
					 }
				 }
				 detailJsonName.put("subinfos",subinfos);
				 for(PageDefine detailPage : detailPages){
					 JSONObject subinfoJsons = new JSONObject();
					 JSONArray subinfo = new JSONArray();
					 subinfoJsons.put("subinfo", subinfo);
					 String subinfotitle ="";
					 Map<String,String> detailData = detailDatas.get(detailPage.getName());
					 List<String> cells =  detailDataList.get(detailPage.getName());
					 int startRow =0;
					 int endRow =0;
					 for(String cell : cells){
						if(detailData.containsKey(cell)){
							if(cell.startsWith("name")){
								String[] arrs = cell.split(",");
								String[] viewNames = arrs[0].split(":");
								String view = viewNames[1];
								if(gridMaps!=null && gridMaps.containsKey(view)){
									 grid = gridMaps.get(view);
								 }else{
									 grid = initView(context,view,state);
									 gridMaps.put(view, grid);
								 }
								HybercubeViewDeclare viewdec = context.find(HybercubeViewDeclare.class, viewName);
								subinfotitle = viewdec.getTitle();
								String[] startrows = arrs[1].split(":");
								startRow = Integer.parseInt(startrows[1]);
								String[] endrows = arrs[2].split(":");
								endRow = Integer.parseInt(endrows[1]);
								break;
							}
						}
					 }
					 endRow = grid.getRowCount();
					 for(int i = startRow; i<endRow;i++){
						 JSONObject itemJsons = new JSONObject();
						 JSONArray items = new JSONArray();
						 itemJsons.put("item", items);
						 for(String cell : cells){
							if(detailData.containsKey(cell)){
								if(cell.startsWith("name")){
									continue;
								}
								 String title = detailData.get(cell);
								 JSONObject jsons = new JSONObject();
								 jsons.put("title", title);
								 if(cell.contains("*")){
									 cell=cell.replace("*", i+""); 
								 }
								 String value ="";
								 value = getFieldValues(context,GUID.tryValueOf(billDataID),cell,grid);
								 jsons.put("value", value);
								 items.put(jsons);
							}
						 }
						 subinfo.put(itemJsons);
					 }
					 subinfoJsons.put("subinfotitle", subinfotitle);
					 subinfos.put(subinfoJsons);
				 } 
			 }
		}else{
			// 获取系统选项加签人影不影响工作流
			FBillDefine billDefine = BillCentre.findBillDefine(context,GUID.tryValueOf(billDefineID));
			BillModel model = BillCentre.createBillModel(context, billDefine);
			model.loadData(GUID.tryValueOf(billDataID));
			GUID UNITID = model.getModelData().getMaster().getValueAsGUID(FieldConst.f_unitID);
			boolean isEffectWorkflow = EnvCenter.getUnitOption(context,UNITID,
					"FO049").getBoolValue();
			session1.setAttribute("isEffectWorkflow", isEffectWorkflow);
			if (workItem == null && !type.equals("1")) {
				detailJsonName.put("hasDone", "该待办已被取回");
			} else if (workItem.getState() == EnumWorkItemState.COMPLETE && !type.equals("1")) {
				detailJsonName.put("hasDone", "该待办已审批");
			} else if (ParticipantList != null && ParticipantList.size() > 0) {
				//判断单据是否被审批
				boolean IsApprove = false;
				//当前审批人是否参与流程审批
				boolean IsSignUser = false;
				for (ParticipantObject participantObject : ParticipantList) {
					String userID = participantObject.getUserguid();
					String commission = participantObject.getCommission();
					String loginUserID = user.getID().toString();
					int action = participantObject.getAction();
					
					if (userID.equals(loginUserID)|| (commission != null &&commission.equals(loginUserID))) {
						if (action == 1 && !type.equals("1")) {
							detailJsonName.put("hasDone", "该待办已同意");
							IsApprove = true;
							break;
						} else if (action == 99 && !type.equals("1")) {
							detailJsonName.put("hasDone", "该待办已驳回");
							IsApprove = true;
							break;
						}
						IsSignUser = true;
					}
				}
				if(!IsSignUser && !isEffectWorkflow){					
					//判断加签人是否全部审批
					String value = CheckApprover(context, GUID.valueOf(workitemid), GUID.valueOf(billDataID), GUID.valueOf(billDefineID), user.getID());
					if("0".equals(value)){
						IsApprove = true;
					}
				}

				if(!IsApprove){
					JSONArray maininfo = new JSONArray();
					JSONArray subinfos = new JSONArray();							
					FUnitOptionUtils fUnitOptionUtils = new FUnitOptionUtils();
					//获取工作流强制加签时显示“同意后加签”选项
					Boolean FO037 = fUnitOptionUtils.getFUnitOption(context, UNITID, "FO037").getBoolValue();
					//获取加签选择界面显示时默认勾选"同意后加签生效"选项
					Boolean FO031 = fUnitOptionUtils.getFUnitOption(context, UNITID, "FO031").getBoolValue();
					//获取被加签人未审批时，提示类型,存到session中
					int FO057 = fUnitOptionUtils.getFUnitOption(context, UNITID, "FO057").getIntValue();														
					//存到session中，在审批界面用到。							
					request.getSession().setAttribute("FO037",FO037);
					request.getSession().setAttribute("FO031",FO031);
					request.getSession().setAttribute("FO057",String.valueOf(FO057));
					request.getSession().setAttribute("showCheckedBox",FO037);
					request.getSession().setAttribute("firstTime", true);
					//页面复选框不可见时，根据系统选项控制是同意前发待办还是同意后发待办
					Boolean sendSignTask = false;
					if(!FO037){
						sendSignTask = fUnitOptionUtils.sendSignTask(FO031, FO037);
						//存到session中，在审批界面用到。
						request.getSession().setAttribute("sendSignTask",sendSignTask);
					}
					
					String responsecode = "0";
					String responsemessage = "";
					String getback = "1";
					String addapprovalsuggest = "0";
					String AddApproverAction = "0";
					String AddApproverActionList = "0";
					String AttachmentActionList = "0";
					String showImages = "0";//中国二十冶“影像按钮”
					String AgreeActionList = "0";
					String RejectActionList = "0";
					String[] qutoShowValues = new String[64];
					String[] qutoBillDefineIds = new String[64];
					String[] qutoBillDataIds = new String[64];
					String qutoShowTitle = new String();
					try {
						// 获取字段值
						if ("1".equals(type) && BusinessProcessManager.isCanReBack(context,GUID.tryValueOf(workitemid))) {
							getback = "0";
						}
						// 获取流程定义里绑定的所有按钮
						FBusinessObject businessObject = BusinessObjectLoader.findBusinessObject(context,GUID.valueOf(workItem.getWorkCategory()));
						Button[] listBillAction = WorkflowDefineManager.getBindingUIButtonsByNode(workItem.getActiveNode(), businessObject.getWFBusinessObjectID());
						for (int i = 0; i < listBillAction.length; i++) {
							if (listBillAction[i].name.equals("AddApproverAction")) {
								AddApproverAction = "1";
								AddApproverActionList = listBillAction[i].title;
							} else if (listBillAction[i].name.equals("accessories")) {
								AttachmentActionList = listBillAction[i].title;
							} else if (listBillAction[i].name.equals("GmcAttachmentNewAction")) {
								AttachmentActionList = listBillAction[i].title;
							} else if (listBillAction[i].name.equals("ACCEPT")) {
								AgreeActionList = listBillAction[i].title;
							} else if (listBillAction[i].name.equals("REJECT")) {
								RejectActionList = listBillAction[i].title;
							} else if (listBillAction[i].name.equals("showImages")) {
								showImages = listBillAction[i].title;
							}
						}
						detailJsonName.put("maininfo", maininfo);
						detailJsonName.put("responsecode", "1");
						
						detailJsonName.put("showCheckedBox", FO037);
						detailJsonName.put("FO031", FO031);
						detailJsonName.put("FO057", String.valueOf(FO057));
						
						detailJsonName.put("billDataID", billDataID);
						detailJsonName.put("billDefineID", billDefineID);
						detailJsonName.put("id", defineid);
						detailJsonName.put("taskid", defineid);
						detailJsonName.put("responsemessage",responsemessage);
						detailJsonName.put("workitemid", workitemid);
						detailJsonName.put("getback", getback);
						detailJsonName.put("navigationTitle",navigationTitle);
						detailJsonName.put("AddApproverAction",AddApproverAction);// 是否显示加签按钮“1”显示，这是加签按钮在详情界面下方的标识
						detailJsonName.put("AddApproverActionList",AddApproverActionList);// 是否显示加签按钮“0”不显示，这是加签按钮在详情界面右上的按钮列表的名称
						detailJsonName.put("AttachmentActionList",AttachmentActionList);// 是否显示附件按钮“0”不显示，这是附件按钮在详情界面右上的按钮列表的名称
						detailJsonName.put("showImages",showImages);
						if (AgreeActionList.equals("0")) {
							AgreeActionList = "同意";
						}
						detailJsonName.put("AgreeActionList",AgreeActionList);
						if (RejectActionList.equals("0")) {
							RejectActionList = "驳回";
						}
						detailJsonName.put("RejectActionList",RejectActionList);
						List<GUID> userIds = getAddApprover(context,GUID.tryValueOf(workitemid),GUID.tryValueOf(billDataID),GUID.tryValueOf(billDefineID));
						if (userIds != null && userIds.size() > 0) {
							detailJsonName.put("addapprovalsuggest", "1");
						} else {
							detailJsonName.put("addapprovalsuggest",addapprovalsuggest);
						}
						// 获得加签说明和加签意见
						String suggest = "";
						String note = "";
						boolean flag = false;
						Object addApproversobject = workItem.getProcessInstance().getParam(WorkflowConst.addCustomApproverKey,true);
						if (addApproversobject != null) {
							String addApprovers = addApproversobject.toString();// 根据属性addCustomApproverKey拿到所有的被加签人
							if (!isEffectWorkflow) {
								for (String str : addApprovers.split(",")) {
									if (str.equals(user.getID().toString())) {
										flag = true;
										break;
									}
								}
								if (!flag) {// 加签人才要看加签意见
									suggest = WorkflowCenter.getAddApproverSuggest(model,GUID.tryValueOf(workitemid));// 加签意见
								}
							}
							for (String str : addApprovers.split(",")) {// 被加签人才要看加签说明
								if (str.equals(user.getID().toString())) {
									String getNote = getApproveUserSuggest(context, unitguid,GUID.tryValueOf(billDataID),GUID.tryValueOf(billDefineID),workItem, isEffectWorkflow);
									if (getNote != null && !getNote.equals("")) {
										note = getNote.replaceAll("\n","<br/>");// 获得加签说明,PC换行手机也换行
									}
									break;
								}
							}
						}
						detailJsonName.put("suggest", suggest);
						detailJsonName.put("note", note);
//						List<FBillStyleTemplate> styles = context.getList(FBillStyleTemplate.class);
						Map<String, String> mainDatas = new HashMap<String, String>();
						Map<String, Map<String, String>> detailDatas = new HashMap<String, Map<String, String>>();
						String maintitle = "";
						List<String> mainDataList = new ArrayList<String>();
						Map<String, List<String>> detailDataList = new HashMap<String, List<String>>();
						//取单据模板中子表名称和顺序
						List<PageDefine> listPageDefine = new ArrayList<PageDefine>();
						MobileBillDefine billdefine = style.getMobileBillDefine();
						if (billdefine.getTempId() != null && billDefineID.equals(billdefine.getTempId())) {
							PageDefine mainPage = billdefine.getMasterPage();
							// maintitle =
							// mainPage.getReferenceTable().getTitle();
							maintitle = style.getTitle();
							mainDatas = getMainDatas(mainPage, mainDataList);
							detailDatas = getDetailDatas(billdefine.getDetailPages(), detailDataList);
							listPageDefine.addAll(billdefine.getDetailPages());
						}
						detailJsonName.put("maintitle", maintitle);
						for (String mainData : mainDataList) {
							if (mainDatas != null && mainDatas.containsKey(mainData)) {
								String title = mainDatas.get(mainData);
								JSONObject jsons = new JSONObject();
								if (title == null || title.equals("#title") || title.equals("#attachment") || title.equals("#qutoShowTitle")
										|| title.equals("#qutoShowValue")
										|| title.equals("#qutoBillDefineId")
										|| title.equals("#qutoBillDataId") || title.equals("#qutoType")) {// 空行、自定义#title站位、附件、引用单据
									if (title == null) {
										jsons.put("title", "null");
										jsons.put("value", "null");
										maininfo.put(jsons);
									} else if (title.equals("#attachment")) {
										getAttachment(maininfo, response, request, false);
									} else if (title.equals("#qutoShowTitle")) {
										if (mainData == null || mainData.equals("")) {
											qutoShowTitle = "引用";
										} else {
											qutoShowTitle = mainData;
										}
									} else if (title.equals("#qutoShowValue")) {
										if (mainData != null && !mainData.equals("")) {
											String values = (String) model.getModelData().getMaster().getFieldValue(mainData).toString();
											if (values.length() > 0) {
												qutoShowValues = values.split(",");
											}
										}
									} else if (title.equals("#qutoBillDefineId")) {
										if (mainData != null && !mainData.equals("")) {
											String values = (String) model.getModelData().getMaster().getFieldValue(mainData).toString();
											if (values.length() > 0) {
												qutoBillDefineIds = values.split(",");
											}
										}
									} else if (title.equals("#qutoBillDataId")) {
										if (mainData != null && !mainData.equals("")) {
											String values = (String) model.getModelData().getMaster().getFieldValue(mainData).toString();
											if (values.length() > 0) {
												qutoBillDataIds = values.split(",");
											}
										}
									} else if (title.equals("#qutoType")) { //报销引申请和借款核销按钮
										if(mainData != null&& mainData.equals("contractQuote")){
											getQutoContractBillMaster(maininfo, qutoShowTitle, model, mainData, context);
										}else if (mainData != null&& !mainData.equals("")) {
											getQutoBillMaster(maininfo, qutoShowTitle, GUID.valueOf(billDataID), mainData, context);
										}
									} else {
										jsons.put("title", "#title");
										jsons.put("value", mainData);
										maininfo.put(jsons);
									}
								} else {
									Object obj = model.getModelData().getMaster().getFieldValue(mainData);
									jsons.put("title", title);
									String value = "";
									if (obj != null) {
										Field field = model.getModelData().getMaster().getTable().getFieldMap().get(mainData);
										if (field != null&& field.getType() == FieldType.GUID) {
											GUID id = (GUID) obj;
											TableDefine relTable = field.getRelationTable();
											String tableName = relTable.getName();
											FBaseDataObject baseDataObject = BaseDataCenter.findObject(context,tableName, id);
											if (baseDataObject != null) {
												value = baseDataObject.getStdName();
												jsons.put("value", value);
											}
										} else if (field != null
												&& field.getType() == FieldType.INT) {
											Integer val = (Integer) obj;
											value = val + "";
											jsons.put("value", value);
										} else if (field != null
												&& field.getType() == FieldType.STRING) {
											value = (String) model.getModelData().getMaster().getFieldValue(field.getName());
											jsons.put("value", value);
										} else if (field != null
												&& field.getType() == FieldType.LONG) {
											Long valu = (Long) model.getModelData().getMaster().getFieldValue(field.getName());
											value = valu + "";
											jsons.put("value", value);
										} else if (field != null && field.getType() == FieldType.NUMERIC) {
											Double valu = (Double) model.getModelData().getMaster().getFieldValue(field.getName());
											value = BudgetUtils.formatValue(valu,true, 2, false);
											jsons.put("value", value);
										} else if (field != null
												&& field.getType() == FieldType.DATE) {
											Date valu = (Date) model.getModelData().getMaster().getFieldValue(field.getName());
											SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
											value = sdf.format(valu);
											jsons.put("value", value);
										} else if (field != null&& field.getType() == FieldType.BOOLEAN) {
											Boolean valu = (Boolean) model.getModelData().getMaster().getFieldValue(field.getName());
											if (valu) {
												value = "是";
											} else {
												value = "否";
											}
											jsons.put("value", value);
										} else if (field != null&& field.getType() == FieldType.TEXT) {
											value = (String) model.getModelData().getMaster().getFieldValue(field.getName());
											jsons.put("value", value);
										}else if (field != null
												&& field.getType() == FieldType.VARBINARY) {
											byte[] bytes = (byte[]) model.getModelData().getMaster().getFieldValue(field.getName());
											GUID[] values = BDMultiSelectUtil.bytesTOGuids(bytes);
											TableDefine relTable = field.getRelationTable();
											String tableName = relTable.getName();
											for (int i = 0; i < values.length; i++) {
												FBaseDataObject fdb = BaseDataCenter.findObject(context,
														tableName, values[i]);
												if (fdb != null) {
													value += fdb.getStdName().toString()+",";
												}
											}
											jsons.put("value", value.substring(0, value.length()-1));
										}
									} else {
										jsons.put("value", "");
									}
									maininfo.put(jsons);
								}
							}
						}
						if (qutoShowValues != null && qutoShowValues.length > 0 && qutoShowValues.length != 64) {
							for (int i = 0; i < qutoShowValues.length; i++) {// 拼装引用源单据的信息给前台（可多个,主表）
								JSONObject jsonOject = new JSONObject();
								StringBuffer url = new StringBuffer();
								jsonOject.put("qutoShowTitle",qutoShowTitle);
								jsonOject.put("qutoShowValue",qutoShowValues[i]);// 源单据显示的信息
								if (i < qutoBillDataIds.length) {
									url.append(qutoBillDataIds[i]);// 源单据recid
									url.append("_");
									url.append(qutoBillDefineIds[i]);// 源单据定义
								}
								jsonOject.put("qutoBillUrl", url);
								jsonOject.put("qutoBillSize",qutoShowValues.length);// 共引用源单据数量
								maininfo.put(jsonOject);
							}
						}
						List<List<BusinessObject>> details = model.getModelData().getDetailsList();
						detailJsonName.put("subinfos", subinfos);
						for(PageDefine define : listPageDefine){								
							for (List<BusinessObject> detail : details) { //每个子表
								if(detail == null || detail.size() == 0){
									continue;
								}
								if(!detail.get(0).getTable().getName().equals(define.getName())){
									continue;
								}
								JSONObject subinfoJsons = new JSONObject();
								JSONArray subinfo = new JSONArray();
								subinfoJsons.put("subinfo", subinfo);
								String subinfotitle = define.getReferenceTable().getTitle();
								int number = 0;
								for (BusinessObject arg : detail) { //子表的每一行记录
									JSONObject itemJsons = new JSONObject();
									JSONArray items = new JSONArray();
									String tableName = arg.getTable().getTable().getName();
									Map<String, String> detailData = detailDatas.get(tableName);
									Table table = arg.getTable();
									Map<String, Field> fieldMap = table.getFieldMap();
									if (detailDataList.containsKey(tableName)) {
										itemJsons.put("item", items);
										subinfotitle = arg.getTable().getTitle();
										String contractCode = null;
										if(null != arg.getTable().find("CONTRACTBILLCODE")){
											contractCode = arg.getValueAsString("CONTRACTBILLCODE");
										}
										List<String> cells = detailDataList.get(tableName);
										JSONObject qutoJsons = new JSONObject();
										for (String cell : cells) {
											if (detailData.containsKey(cell)) {
												String title = detailData.get(cell);
												Field field = fieldMap.get(cell);
												JSONObject jsons = new JSONObject();
												if (title == null || title.equals("#qutoShowTitle") || title.equals("#qutoShowValue")
														|| title.equals("#qutoBillDefineId")
														|| title.equals("#qutoBillDataId") || title.equals("#qutoType")) {
													if (title == null) {
														jsons.put("title","null");
														jsons.put("value","null");
														items.put(jsons);
													} else if (title.equals("#qutoShowTitle")) {
														if (cell == null || cell.equals("")) {
															qutoShowTitle = "引用";
														} else {
															qutoShowTitle = cell;
														}
														qutoJsons.put("qutoShowTitle",qutoShowTitle);
													} else if (title.equals("#qutoShowValue")) {
														if (cell != null && !cell.equals("")) {
															String values = arg.getFieldValue(cell).toString();
															qutoJsons.put("qutoShowValue",values);// 源单据显示的信息
														}
													} else if (title.equals("#qutoBillDataId")) {
														if (cell != null && !cell.equals("")) {
															String values = arg.getFieldValue(cell).toString();
															qutoJsons.put("qutoBillDataId",values);// 源单据recid
														}
													} else if (title.equals("#qutoBillDefineId")) {
														if (cell != null && !cell.equals("")) {
															String values = arg.getFieldValue(cell).toString();
															qutoJsons.put("qutoBillDefineId",values);// 源单据定义
														}
													} else if ( title.equals("#qutoType")) {
														if (cell != null && !cell.equals("")) {
															RecordSet rs = null;
															if(cell.equals("contractQuote") && contractCode != null){//contractCode
																StringBuffer sql = new StringBuffer("define query querytn() begin ");
																sql.append("SELECT T.RECID AS RECID,T.BILLDEFINE AS BILLDEFINE FROM FO_CONTRACT AS T WHERE T.BILLCODE = '");
																sql.append(contractCode);
																sql.append("' end");
																rs = context.openQuery((QueryStatementDeclare) context.parseStatement(sql));
															}else{
																rs = getQutoBillDetail(arg.getRECID(), cell, context); //报销引申请子对子、主对子（通过关系表中的报销子表id查对应的唯一申请单信息），报销引借款只有主对子
															}
															while (rs.next()) {
																JSONObject qutoJson = new JSONObject();
																GUID recid = rs.getFields().get(0).getGUID();
																GUID defineId = rs.getFields().get(1).getGUID();
																FBillDefine qbillDefine = BillCentre.findBillDefine(context,defineId);
																BillModel qmodel = BillCentre.createBillModel(context, qbillDefine);
																qmodel.loadData(recid);
																String billCode = qmodel.getModelData().getMaster().getValueAsString("BILLCODE");
																qutoJson.put("qutoShowTitle",qutoShowTitle);
																qutoJson.put("qutoShowValue",billCode);// 源单据显示的信息
																qutoJson.put("qutoBillDataId",recid);// 源单据recid
																qutoJson.put("qutoBillDefineId",defineId);// 源单据定义
																items.put(qutoJson);
															}
														}
													}
												} else {
													Object obj = arg.getFieldValue(cell);
													jsons.put("title", title);
													String value = "";
													if (obj != null) {
														if (field != null && field.getType() == FieldType.GUID) {
															GUID id = (GUID) obj;
															TableDefine relTable = field.getRelationTable();
															String relTableName = relTable.getName();
															FBaseDataObject baseDataObject = BaseDataCenter.findObject(context,relTableName,id);
															if (baseDataObject != null) {
																value = baseDataObject.getStdName();
																jsons.put("value",value);
															} else {
																jsons.put("value",value);
															}
														} else if (field != null && field.getType() == FieldType.STRING) {
															//付款合同变更单子表信息处理
															field.getTitle();
															field.getName();
															if(tableName.equals("FO_CHANGEBILLINFO_DETAIL")){
																GUID changeBillDefineID = model.getModelData().getMaster().getValueAsGUID("SRCBILLDEFINE");
																//查询子表数据
																Object item = arg.getFieldValue("ITEM_NAME");
																if(item != null && changeBillDefineID != null && (cell.equals("ITEM_NAME")|| cell.equals("ITEM_OLDVALUE") || cell.equals("ITEM_NEWVALUE"))){
																	String ITEM_NAME = item.toString();
																	value = FoChangeBillUtil.getFieldValueByName(context, changeBillDefineID, ITEM_NAME, (String)obj);
																	jsons.put("value",value);
																}else{
																	value = (String) obj;
																	jsons.put("value",value);
																}
															}else{
																value = (String) obj;
																jsons.put("value",value);
															}
														} else if (field != null && field.getType() == FieldType.INT) {
															Integer val = (Integer) obj;
															value = val + "";
															jsons.put("value",value);
														} else if (field != null && field.getType() == FieldType.LONG) {
															Long valu = (Long) obj;
															value = valu + "";
															jsons.put("value",value);
														} else if (field != null && field.getType() == FieldType.NUMERIC) {
															Double valu = (Double) obj;
															value = BudgetUtils.formatValue(valu,true,2,false);
															jsons.put("value",value);
														} else if (field != null && field.getType() == FieldType.DATE) {
															SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
															String creattime = "";
															if (obj instanceof Long) {
																long date = Long.parseLong(obj.toString());
																creattime = sdf.format(date);
															} else if (obj instanceof Date) {
																long date = ((Date) obj).getTime();
																creattime = sdf.format(date);
															} else {
																Long date = Long.valueOf(obj.toString());
																creattime = sdf.format(date);
															}
															jsons.put("value",creattime);
														} else if (field != null
																&& field.getType() == FieldType.BOOLEAN) {																												
															Boolean valu = (Boolean)obj;
															if (valu) {
																value = "是";
															} else {
																value = "否";
															}
															jsons.put("value",value);
														} else if (field != null
																&& field.getType() == FieldType.TEXT) {
															value = (String) obj;
															jsons.put("value",value);
														}else if (field != null&& field.getType() == FieldType.VARBINARY) {
															byte[] bytes = (byte[]) obj;
															GUID[] values = BDMultiSelectUtil.bytesTOGuids(bytes);
															TableDefine relTable = field.getRelationTable();
															String relTableName = relTable.getName();
															for (int i = 0; i < values.length; i++) {
																FBaseDataObject fdb = BaseDataCenter.findObject(context,
																		relTableName, values[i]);
																if (fdb != null) {
																	value += fdb.getStdName().toString()+",";
																}
															}
															jsons.put("value", value.substring(0, value.length()-1));
														} else {
															jsons.put("value","");
														}
													} else {
														jsons.put("value", "");
													}
													items.put(jsons);
												}
											}
										}
										items.put(qutoJsons);
										subinfo.put(itemJsons);
									}
								}
								subinfoJsons.put("subinfotitle", subinfotitle);
								subinfos.put(subinfoJsons);
							}
						}
					} catch (Exception e) {
						detailJsonName.put("maininfo", maininfo);
						detailJsonName.put("responsecode", responsecode);
						responsemessage = e.getMessage();
						detailJsonName.put("responsemessage",responsemessage);
					}
				}
				if (detailJsonName.length() == 0) { //发完消息，再停用委托时，进不去详情界面提示
					detailJsonName.put("hasDone", "该待办已被处理");
				}
			}
		}
		writer.println(detailJsonName);
		writer.flush();
		writer.close();
	}
	
	/**
	 * 引用合同
	 * 根据合同单号查找合同billcode,billdataid
	 * @param maininfo
	 * @param qutoShowTitle
	 * @param billModel
	 * @param mainData
	 * @param context void
	 */
	private void getQutoContractBillMaster(JSONArray maininfo, String qutoShowTitle, BillModel billModel,
            String mainData, Context context){
		if(null != billModel.getModelData().getMaster().getTable().find("CONTRACTBILLCODE") && !StringUtil.isEmpty(billModel.getModelData().getMaster().getValueAsString("CONTRACTBILLCODE"))){
			String contractCode = billModel.getModelData().getMaster().getValueAsString("CONTRACTBILLCODE");
			StringBuffer sql = new StringBuffer("define query querytn() begin ");
			sql.append("SELECT T.RECID AS RECID,T.BILLDEFINE AS BILLDEFINE FROM FO_CONTRACT AS T WHERE T.BILLCODE = '");
			sql.append(contractCode);
			sql.append("' end");
			RecordSet rs = context.openQuery((QueryStatementDeclare) context.parseStatement(sql));
			creatJSON(maininfo, qutoShowTitle, context, rs);
		}
    }
	/**
	 * 主表，通过目标单的recid查询关系表获得原单的billCode、billDataId和billDefineId
	 */
	private void getQutoBillMaster(JSONArray maininfo, String qutoShowTitle, GUID billDataID, String qutoType, Context context){
		if (qutoType.equals("applyBillQuote")) { //报销引申请（报销单引申请单）
			StringBuffer sql = new StringBuffer("define query querytn() begin ");
			sql.append("select t.REQUESTBILLRECID as REQUESTBILLRECID, t.REQUESTBILLDEFINE as REQUESTBILLDEFINE from FO_REQUESTOCCUYLOAD as t \n");
			sql.append("where t.ACCOUNTBILLRECID =guid'");
			sql.append(billDataID).append("' \n");
			sql.append("end");
			RecordSet rs = context.openQuery((QueryStatementDeclare) context
					.parseStatement(sql));
			creatJSON(maininfo, qutoShowTitle, context, rs);
		} else if (qutoType.equals("FoReferenceAction")) { //借款核销按钮（报销单引用借款单）
			StringBuffer sql = new StringBuffer("define query querytn() begin ");
			sql.append("select t.LOANBILLID as LOANBILLID, t.SRCBILLDEFINE as SRCBILLDEFINE from FO_PAYLOADBILL as t \n");
			sql.append("where t.EXPENSEBILLID =guid'");
			sql.append(billDataID).append("' and t.SRCBILLDEFINE IN (select d.RECID as RECID from B0104_BILLDEFINE as d where d.MODELCLASS = 'LoanBillModel') \n"); // 报销引申请和借款核销按钮一起用时会把申请单信息也写到关系表FO_PAYLOADBILL中，此处过滤掉
			sql.append("end");
			RecordSet rs = context.openQuery((QueryStatementDeclare) context
					.parseStatement(sql));
			creatJSON(maininfo, qutoShowTitle, context, rs);
		}
	}
	
	private void creatJSON(JSONArray maininfo, String qutoShowTitle, Context context, RecordSet rs){
		int qutoSize = rs.getRecordCount();
		while (rs.next()) {
			JSONObject jsonOject = new JSONObject();
			StringBuffer url = new StringBuffer();
			GUID recid = rs.getFields().get(0).getGUID();
			GUID defineId = rs.getFields().get(1).getGUID();
			FBillDefine billDefine = BillCentre.findBillDefine(context,defineId);
			BillModel model = BillCentre.createBillModel(context, billDefine);
			model.loadData(recid);
			String billCode = model.getModelData().getMaster().getValueAsString("BILLCODE");
			jsonOject.put("qutoShowTitle",
					qutoShowTitle);
			jsonOject.put("qutoShowValue",
					billCode);// 源单据显示的信息
			url.append(recid);// 源单据recid
			url.append("_");
			url.append(defineId);// 源单据定义
			jsonOject.put("qutoBillUrl", url);
			jsonOject.put("qutoBillSize",
					qutoSize);// 共引用源单据数量
			maininfo.put(jsonOject);
		}
	}
	
	/**
	 * 子表，通过目标单的recid查询关系表获得原单的billCode、billDataId和billDefineId
	 */
	private RecordSet getQutoBillDetail(GUID billDataID, String qutoType, Context context){
		if (qutoType.equals("applyBillQuote")) {
			StringBuffer sql = new StringBuffer("define query querytn() begin ");
			sql.append("select t.REQUESTBILLRECID as REQUESTBILLRECID, t.REQUESTBILLDEFINE as REQUESTBILLDEFINE from FO_REQUESTOCCUYLOAD as t \n");
			sql.append("where t.ACCOUNTDETAILRECID =guid'");
			sql.append(billDataID).append("' \n");
			sql.append("end");
			RecordSet rs = context.openQuery((QueryStatementDeclare) context
					.parseStatement(sql));
			return rs;
		} else if (qutoType.equals("FoReferenceAction")) {
			StringBuffer sql = new StringBuffer("define query querytn() begin ");
			sql.append("select t.LOANBILLID as LOANBILLID, t.SRCBILLDEFINE as SRCBILLDEFINE from FO_PAYLOADBILL as t \n");
			sql.append("where t.EXPENSEDETAILBILLID =guid'");
			sql.append(billDataID).append("' \n");
			sql.append("end");
			RecordSet rs = context.openQuery((QueryStatementDeclare) context
					.parseStatement(sql));
			return rs;
		}
		return null;
	}

	/**
	 * 获得附件列表信息
	 * 
	 * @param maininfo
	 * @param request
	 * @param response
	 *            ,
	 * @param isList 
	 */
	private void getAttachment(JSONArray maininfo,
			HttpServletResponse response, HttpServletRequest req, boolean isList) {
		String billId = req.getParameter("billDataID");
		String modelId = req.getParameter("billDefineID");
		response.setHeader("pragma", "no-cache");
		response.setHeader("cache-control", "no-no-cache");
		response.setHeader("expires", "0");
		com.jiuqi.dna.core.spi.application.Session session = com.jiuqi.dna.core.spi.application.AppUtil
				.getDefaultApp().getSystemSession();
		Context context = session.newContext(false);
		List<BillEnclosure> enclosures = AccessoriesUtils.getBillEnclosure(
				context, GUID.tryValueOf(modelId), GUID.tryValueOf(billId));
		try {
			if (enclosures.size() > 0) {
				getHtmlBillEnclosure(enclosures, maininfo, isList);// 目前知道附件按钮标识为accessories的附件管理走这里
			} else {
				getHtmlAttachment(
						AccessoriesUtils.getBillAccachment(context,
								GUID.valueOf(billId)), maininfo, isList);// 附件按钮标识为GmcAttachmentNewAction的附件管理走这里
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 释放context
			if (context != null) {
				((ContextSPI) context).dispose();
			}
		}
	}

	/**
	 * 获取html内容
	 * 
	 * @param billId
	 * @param enclosures
	 * @param maininfo
	 * @param isList 
	 * @return
	 */
	private void getHtmlBillEnclosure(List<BillEnclosure> enclosures,
			JSONArray maininfo, boolean isList) throws Exception {
		String name = new String();
		String url = new String();
		String houzhui = new String();
		int attachmentSize = 0;
		int size;

		StringBuffer prefixBuffer = new StringBuffer("");
		if (isList) {
			prefixBuffer.append("/mapp");
		} else {
			prefixBuffer.append("/newweb");
		}
		String prefix = prefixBuffer.toString(); 
		// 图标待修改
		String wordiconPath = prefix + "/image/fileicon/Word.png";
		String exceliconPath = prefix + "/image/fileicon/Excel.png";
		String accessoryiconPath = prefix + "/image/fileicon/accessory.png";
		String pdficonPath = prefix + "/image/fileicon/pdf.png";
		String photoiconPath = prefix + "/image/fileicon/photo.png";
		String powerpointiconPath = prefix + "/image/fileicon/PowerPoint.png";
		String txticonPath = prefix + "/image/fileicon/txt.png";

		for (BillEnclosure billEnclosure : enclosures) {
			name = billEnclosure.getENCLOSURENAME();
			url = getUrl(billEnclosure.getRECID().toString(),
					billEnclosure.getENCLOSURENAME(), "0");
			attachmentSize = billEnclosure.getENCLOSURESIZE();
			size = name.split("\\.").length;
			houzhui = name.split("\\.")[size - 1];
			float m = (float) attachmentSize / (float) (1024 * 1024);
			if (m > 1.0) {
				DecimalFormat df = new DecimalFormat("0.00");
				name = name + "(" + df.format(m) + "M)";
			} else {
				name = name + "(" + attachmentSize / 1024 + "K)";
			}
			houzhui = houzhui.toLowerCase();
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("attachmentName", name);
			jsonObject.put("attachmentUrl", url);
			jsonObject.put("attachmentSize", enclosures.size());

			if (houzhui.equals("doc") || houzhui.equals("docx"))
				jsonObject.put("attachmentIcon", wordiconPath);
			else if (houzhui.equals("xls") || houzhui.equals("xlsx"))
				jsonObject.put("attachmentIcon", exceliconPath);
			else if (houzhui.equals("pdf"))
				jsonObject.put("attachmentIcon", pdficonPath);
			else if (houzhui.equals("jpg") || houzhui.equals("jpeg")
					|| houzhui.equals("png") || houzhui.equals("bmp"))
				jsonObject.put("attachmentIcon", photoiconPath);
			else if (houzhui.equals("ppt") || houzhui.equals("pptx"))
				jsonObject.put("attachmentIcon", powerpointiconPath);
			else if (houzhui.equals("txt"))
				jsonObject.put("attachmentIcon", txticonPath);
			else
				jsonObject.put("attachmentIcon", accessoryiconPath);

			maininfo.put(jsonObject);
		}
	}

	private void getHtmlAttachment(List<FSmAttachment> attachmentList,
			JSONArray maininfo, boolean isList) throws Exception {
		String name = new String();
		String url = new String();
		String houzhui = new String();
		String attachmentSize = new String();
		int size;

		StringBuffer prefixBuffer = new StringBuffer("");
		if (isList) {
			prefixBuffer.append("/mapp");
		} else {
			prefixBuffer.append("/newweb");
		}
		String prefix = prefixBuffer.toString();
		// 图标待修改
		String wordiconPath = prefix + "/image/fileicon/Word.png";
		String exceliconPath = prefix + "/image/fileicon/Excel.png";
		String accessoryiconPath = prefix + "/image/fileicon/accessory.png";
		String pdficonPath = prefix + "/image/fileicon/pdf.png";
		String photoiconPath = prefix + "/image/fileicon/photo.png";
		String powerpointiconPath = prefix + "/image/fileicon/PowerPoint.png";
		String txticonPath = prefix + "/image/fileicon/txt.png";

		for (FSmAttachment attachement : attachmentList) {
			name = attachement.getFileName();
			url = getUrl(attachement.getRecid().toString(),
					attachement.getFileName(), "1");
			attachmentSize = attachement.getFileSize().split("KB")[0];
			size = name.split("\\.").length;
			houzhui = name.split("\\.")[size - 1];
			float m = Float.parseFloat(attachmentSize) / (float) (1024);
			if (m > 1.0) {
				DecimalFormat df = new DecimalFormat("0.00");
				name = name + "(" + df.format(m) + "M)";
			} else {
				name = name + "(" + attachmentSize + "K)";
			}
			houzhui = houzhui.toLowerCase();
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("attachmentName", name);
			jsonObject.put("attachmentUrl", url);
			jsonObject.put("attachmentSize", attachmentList.size());

			if (houzhui.equals("doc") || houzhui.equals("docx"))
				jsonObject.put("attachmentIcon", wordiconPath);
			else if (houzhui.equals("xls") || houzhui.equals("xlsx"))
				jsonObject.put("attachmentIcon", exceliconPath);
			else if (houzhui.equals("pdf"))
				jsonObject.put("attachmentIcon", pdficonPath);
			else if (houzhui.equals("jpg") || houzhui.equals("jpeg")
					|| houzhui.equals("png") || houzhui.equals("bmp"))
				jsonObject.put("attachmentIcon", photoiconPath);
			else if (houzhui.equals("ppt") || houzhui.equals("pptx"))
				jsonObject.put("attachmentIcon", powerpointiconPath);
			else if (houzhui.equals("txt"))
				jsonObject.put("attachmentIcon", txticonPath);
			else
				jsonObject.put("attachmentIcon", accessoryiconPath);

			maininfo.put(jsonObject);
		}
	}

	/**
	 * 获取url
	 * 
	 * @param billId
	 *            单据ID
	 * @param accessId
	 *            附件Id
	 * @return
	 */
	private String getUrl(String accessId, String fileName, String isNew) {
		// 读取配置文件，获取IP和端口
		StringBuffer url = new StringBuffer("");

		url.append("/attachment_item_app");
		/*
		 * Content-Disposition
		 * 设置为：inline;filename=xxx.xxx。Android在下载打开文件时检测不到文件名。在这里添加文件名，就可以正确显示。
		 */
		// url.append(fileName);
		url.append("?accessId=");
		url.append(accessId);
		url.append("&isNew=");
		url.append(isNew);
		return url.toString();
	}

	private RecordSet getRecordSet(Context context, GUID userId) {
		List<String> tableNames = new ArrayList<String>();
		StringBuffer sql = new StringBuffer("define query querytn() begin ");
		sql.append("select DISTINCT b.MASTERTABLE as TABLENAME ,t.BILLDEFINEID as BILLDEFINEID  from FO_WORKFLOW_APPROVE as t ");
		sql.append("left join b0104_billdefine as b on t.BILLDEFINEID = b.RECID ");
		sql.append("where t.SUGGESTUSERID =guid'");
		sql.append(userId).append("' \n");
		sql.append(" and t.ACTIONID =").append(0)
				.append(" and (t.SUGGESTDATE is null ) \n");
		sql.append("end");
		RecordSet rs = context.openQuery((QueryStatementDeclare) context
				.parseStatement(sql));
		while (rs.next()) {
			tableNames.add(rs.getFields().get(0).getString());
		}
		if (tableNames.size() == 0) {
			return null;
		}
		StringBuffer mainSql = new StringBuffer(
				"define query queryMain() begin ");
		for (int i = 0; i < tableNames.size(); i++) {
			if (i > 0) {
				mainSql.append(" UNION ");
			}
			mainSql.append("select t.BILLCODE as BILLCODE ,");
			mainSql.append("a.BILLDEFINEID as BILLDEFINEID ,");
			mainSql.append("a.BILLID as BILLID ,");
			mainSql.append("a.WORKITEMID as WORKITEMID ,");
			mainSql.append("t.BILLDATE as CREATEDATE ,");
			mainSql.append("t.CREATEUSER as CREATEUSER ,");
			mainSql.append("t.SUBMITUSER as SUBMITUSER ,");
			mainSql.append("a.APPROVEUSERTITLE as APPROVEUSERTITLE ,");
			mainSql.append("a.APPROVETYPE as APPROVETYPE ,");
			mainSql.append("o.STDNAME as ORGNAME ,");
			mainSql.append("a.RECID as RECID ");
			mainSql.append("from FO_WORKFLOW_APPROVE as a ");
			mainSql.append("join ")
					.append(tableNames.get(i))
					.append(" as t on (a.BILLID = t.RECID and a.BILLDEFINEID = t.BILLDEFINE) ");
			mainSql.append("left join MD_ORG as o on t.UNITID=o.RECID ");
			mainSql.append(" where a.SUGGESTUSERID =guid'");
			mainSql.append(userId).append("' ");
			mainSql.append(" and a.ACTIONID =").append(0)
					.append(" and (a.SUGGESTDATE is null )");
		}
		mainSql.append(" end");
		rs = context.openQuery((QueryStatementDeclare) context
				.parseStatement(mainSql));
		return rs;
	}

	private List<GUID> getAddApprover(Context context, GUID workItemID,
			GUID billID, GUID billdefineID) {
		List<GUID> userIds = new ArrayList<GUID>();
		StringBuffer sql = new StringBuffer();
		sql.append("define query getQueryStaff(@WORKITEMID GUID,@BILLID GUID,@BILLDEFINEID GUID) begin ");
		sql.append(" select t.SUGGESTUSERID as useid,t.APPROVEDSUGGEST as APPROVEDSUGGEST from FO_WORKFLOW_APPROVE as t ");
		sql.append(" where t.WORKITEMID=@WORKITEMID and t.BILLID = @BILLID and t.BILLDEFINEID=@BILLDEFINEID ");
		sql.append(" end ");
		QueryStatementDefine query = (QueryStatementDefine) context
				.parseStatement(sql.toString());
		RecordSet rs = context.openQuery(query, workItemID, billID,
				billdefineID);
		if (rs != null) {
			while (rs.next()) {
				GUID uid = rs.getFields().get(0).getGUID();
				if (userIds != null && userIds.contains(uid)) {
					continue;
				} else {
					userIds.add(uid);
				}
			}
			return userIds;
		}
		return userIds;
	}
	/**
	 * 判断当前加签待办是否被审批
	 * @param context
	 * @param workItemID
	 * @param billID
	 * @param billdefineID
	 * @return  List<GUID>
	 */
	private String CheckApprover(Context context, GUID workItemID,GUID billID, GUID billdefineID,GUID userID) {
		StringBuffer sql = new StringBuffer();
		sql.append("define query updateTodo(@WORKITEMID GUID,@BILLID GUID ,@BILLDEFINEID GUID ,@APPROVEUSERID GUID)");
		sql.append(" begin ");
		sql.append(" select count(1) as num from  FO_WORKFLOW_APPROVE as f ");
		sql.append(" where f.WORKITEMID = @WORKITEMID and f.BILLID = @BILLID and f.BILLDEFINEID = @BILLDEFINEID");
		sql.append(" and (f.SUGGESTDATE is null) and (f.APPROVEDSUGGEST is null)");
		sql.append(" and f.SUGGESTUSERID= GUID'" + userID + "'");
		sql.append(" end");
		int value = (Integer) context.executeScalar((QueryStatementDeclare)context.parseStatement(sql), new Object[] {workItemID, billID, billdefineID, userID });
		
		if ((String.valueOf(value) != null) && (!String.valueOf(value).equals(""))){
			return String.valueOf(value);
		}
		return "";
	}

	/**
	 * 获取主表的配置信息
	 * 
	 * @param mainPage
	 * @return
	 */
	private Map<String, String> getMainDatas(PageDefine mainPage,List<String> mainDataList) {
		Map<String, String> mainDatas = new HashMap<String, String>();
		IMShowTemplate mShowTemp = mainPage.getmInputLayout();
		int mRows = mShowTemp.getTable().getRowCount();
		for (int i = 0; i < mRows; i++) {
			IMTableRow templ = mShowTemp.getTable().getRow(i);
			if (templ.getColCount() > 1) {
				IMTableCell cell0 = templ.getCol(0);
				String key = cell0.getTitle();
				IMTableCell cell1 = templ.getCol(1);
				String value = cell1.getTitle();
				if (value != null && value.contains("#")) {
					value = value.substring(value.indexOf("#") + 1,value.length() - 1);
					mainDatas.put(value, key);
					mainDataList.add(value);
				} else {
					mainDatas.put(value, key);
					mainDataList.add(value);
				}
			}

		}
		return mainDatas;
	}

	/**
	 * 获取子表的配置信息
	 * 
	 * @param detailPages
	 * @return
	 */
	private Map<String, Map<String, String>> getDetailDatas(List<PageDefine> detailPages,Map<String, List<String>> detailDataList) {
		Map<String, Map<String, String>> detailDatas = new HashMap<String, Map<String, String>>();
		for (PageDefine detailPage : detailPages) {
			if (detailPage.isHidden()) {
				continue;
			}
			IMShowTemplate dShowTemp = detailPage.getmInputLayout();
			int dRows = dShowTemp.getTable().getRowCount();
			Map<String, String> mainDatas = new HashMap<String, String>();
			List<String> cells = new ArrayList<String>();
			for (int i = 0; i < dRows; i++) {
				IMTableRow templ = dShowTemp.getTable().getRow(i);
				if (templ != null && templ.getColCount() > 1) {
					IMTableCell cell0 = templ.getCol(0);
					String key = cell0.getTitle();
					IMTableCell cell1 = templ.getCol(1);
					String value = cell1.getTitle();
					if (value != null && value.contains("#")) {
						value = value.substring(value.indexOf("#") + 1,value.length() - 1);
						mainDatas.put(value, key);
						cells.add(value);
					} else {
						mainDatas.put(value, key);
						cells.add(value);
					}
				}
			}
			detailDatas.put(detailPage.getName(), mainDatas);
			detailDataList.put(detailPage.getName(), cells);
		}
		return detailDatas;
	}

	/**
	 * 根据审批定义配置获取待办展示列数据
	 * 
	 * @param define
	 * @return
	 */
	private HashMap<String, String> getRecordFileds(FApprovalDefine define,
			List<String> cells) {
		HashMap<String, String> configMap = new HashMap<String, String>();
		String configs = define.getConfig();
		JSONObject object = new JSONObject(configs);
		if (object.get("list_template") != null) {
			String args = (String) object.get("list_template");
			JSONObject objects = new JSONObject(args);
			if (objects.get("table") != null) {
				JSONObject arg1 = (JSONObject) objects.get("table");
				if (arg1.get("rows") != null) {
					JSONArray arg2 = (JSONArray) arg1.get("rows");// 获得移动审批列表界面配置要显示的字段
					for (int i = 0; i < arg2.length(); i++) {
						JSONObject para = arg2.getJSONObject(i);
						JSONArray arg3 = (JSONArray) para.get("cells");
						if (arg3 != null && arg3.length() > 1) {
							JSONObject nameJsonObject = arg3.getJSONObject(0);
							if (nameJsonObject.has("title")) {
								String name = (String) nameJsonObject
										.get("title");
								JSONObject valueJsonObject = arg3
										.getJSONObject(1);
								String value = (String) valueJsonObject
										.get("title");
								if (value != null && value.contains("#")) {
									value = value.substring(
											value.indexOf("#") + 1,
											value.length() - 1);
									configMap.put(value, name);// 循环获得移动审批列表界面配置的具体内容（value为A列的值，name为B列的值）
									cells.add(value);
								}
							}
						}
					}
				}
			}
		}
		return configMap;
	}

	/**
	 * 根据待办及审批定义封装数据
	 * 
	 * @param cate
	 * @param context
	 * @param recordList
	 * @param pagesize
	 * @return
	 */
	private IStream<ITodoItem> getItemListStream(String cate, Context context,
			List<FRecord> recordList, int pagesize) {
		List<ITodoItem> itemList = new ArrayList<ITodoItem>();
		boolean sucess = fillItemList(cate, context, recordList, itemList);
		if (pagesize == SPITodo.ARGUMENT_SIZE_UNLIMITED) {
			if (!sucess)
				return new TodoListStream(itemList, new MetadataImpl("todos",
						"data"), 5);
			return new TodoListStream(itemList, new MetadataImpl("todos",
					MWorkflowUtil.getMetadata(context, GUID.valueOf(cate))),
					itemList.size());
		}
		if (!sucess)
			return new TodoListStream(itemList, new MetadataImpl("todos",
					"data"), pagesize);
		return new TodoListStream(itemList, new MetadataImpl("todos",
				MWorkflowUtil.getMetadata(context, GUID.valueOf(cate))),
				pagesize);
	}

	private boolean fillItemList(String cate, Context context,
			List<FRecord> recordList, List<ITodoItem> itemList) {
		String businessObjectID = MWorkflowUtil
				.getBusinessObjectIDByTaskDefineID(context, GUID.valueOf(cate));
		FBusinessObject bo = BusinessObjectLoader.findBusinessObject(context,
				GUID.valueOf(businessObjectID));

		// Collections.sort(recordList, new FRecordComparator(MWorkflowUtil
		// .getTableName(context, businessObjectID)));
		boolean sucess = true;
		for (int i = 0; i < recordList.size(); i++) {
			FRecord record = recordList.get(i);
			if (!GUID.emptyID.equals(businessObjectID)) {
				TodoItem todoItem = new TodoItem();
				todoItem.setId(record.getWorkItemID().toString());
				todoItem.setState(0);
				Map<String, Object> map = record.getFieldMap();
				List<BOResultField> fieldList = MWorkflowUtil.getQueryFields(
						context, cate);
				if (fieldList.isEmpty()) {
					sucess = false;
					break;
				}
				for (BOResultField resultField : fieldList) {
					String fieldName = MWFUtil.getFieldName(resultField);
					String fieldValue = null;
					if (null != bo) {
						if (bo instanceof WFBusinessObject) {
							fieldValue = ((WFBusinessObject) bo).getFieldValue(
									context, record.getBusinessInstanceID(),
									resultField.getFieldName(), record);
						} else {
							fieldValue = bo.getFieldValue(context,
									record.getBusinessInstanceID(),
									resultField.getFieldName());
						}
					}
					if (StringUtil.isEmpty(fieldValue)) {
						fieldValue = format(
								context,
								resultField,
								map.get(fieldName) == null ? "" : map
										.get(fieldName));
					} else if (!StringUtil.isEmpty(fieldValue)
							&& resultField.getDataType() == ZBDataType.NUMERIC) {
						fieldValue = BudgetUtils.formatValue(
								Double.parseDouble(fieldValue.toString()),
								true, 2, false);
					} else if (!StringUtil.isEmpty(fieldValue)
							&& (resultField.getDataType() == ZBDataType.DATETIME || resultField
									.getDataType() == ZBDataType.DATE)) {
						fieldValue = format(context, resultField, fieldValue);
					}
					if (StringUtil.isEmpty(fieldValue)) {
						fieldValue = MWFUtil
								.format(context,
										resultField,
										map.get(resultField.getTableName()
												+ resultField.getFieldName()) == null ? ""
												: map.get(resultField
														.getTableName()
														+ resultField
																.getFieldName()));
					}
					if (!StringUtil.isEmpty(fieldValue)
							&& resultField.getDataType() == ZBDataType.BOOLEAN
							&& "URGENTFLAG".equals(resultField.getFieldName())) {
						boolean isUrgent = false;
						if ("true".equals(fieldValue)) {
							isUrgent = true;
						}
						todoItem.setUrgent(isUrgent);
					}
					todoItem.setProperty(fieldName, fieldValue);
				}

				todoItem.setBillDefineID(MWorkflowUtil
						.getBusinessObjectIDByWorkItemID(context,
								record.getWorkItemID()));
				if (ListenerGatherer.getiTodoListener() != null) {
					boolean isUrgent = ListenerGatherer.getiTodoListener()
							.isUrgent(todoItem.getId());
					todoItem.setUrgent(isUrgent);
				}
				if (StringUtil.isEmpty(todoItem.getBillDefineID())) {
				}
				if (map.containsKey("GUIDREF"))
					todoItem.setBillDataID(map.get("GUIDREF").toString());
				if (todoItem != null)
					itemList.add(todoItem);
			}
		}
		return sucess;
	}

	public static String format(Context context, BOResultField resultField,
			Object value) {
		if (value == null || "".equals(value))
			return "";

		if (resultField.getDataType() == ZBDataType.DATETIME
				|| resultField.getDataType() == ZBDataType.DATE) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			// Calendar calendar = new GregorianCalendar();
			String creattime = "";
			try {
				if (value instanceof Long) {
					long date = Long.parseLong(value.toString());
					creattime = sdf.format(date);
				} else if (value instanceof Date) {
					long date = ((Date) value).getTime();
					creattime = sdf.format(date);
				} else {
					Long date = Long.valueOf(value.toString());
					creattime = sdf.format(date);
				}
			} catch (Exception e) {
				MobileLog.logError(e);
				return "";
			}
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			// Long val = Long.valueOf(value.toString());
			// String creattime = sdf.format(val);
			// return creattime;
			return creattime;
		} else if (resultField.getDataType() == ZBDataType.NUMERIC) {
			// DecimalFormat nf = new DecimalFormat("###,###.##");
			return BudgetUtils.formatValue(
					Double.parseDouble(value.toString()), true, 2, false);
		} else if (resultField.getDataType() == ZBDataType.BINARY) {
			String result = "";
			String relateFieldName = resultField.getRelateFieldName();
			String relateTableName = resultField.getRelateTableName();
			GUID[] guids = BDMultiSelectUtil.bytesTOGuids((byte[]) value);
			if (StringUtil.isEmpty(relateTableName)) {
				return "";
			}
			for (GUID guid : guids) {
				FBaseDataObject fb = BaseDataCenter.findObject(context,
						relateTableName, guid);

				if (fb == null)
					continue;

				if ("".equals(result))
					result = fb.getValueAsString(relateFieldName);
				else
					result = result + ","
							+ fb.getValueAsString(relateFieldName);
			}

			return result;
		} else if (resultField.getDataType() == ZBDataType.GUID) {
			String result = "";
			String relateFieldName = resultField.getRelateFieldName();
			String relateTableName = resultField.getRelateTableName();
			if (StringUtil.isEmpty(relateTableName)) {
				return value.toString();
			}
			GUID guid = null;
			if (value instanceof GUID) {
				guid = (GUID) value;
			} else if (value instanceof String) {
				try {
					guid = GUID.valueOf((String) value);
				} catch (Exception e) {
					return value.toString();
				}
			} else if (value instanceof Byte[]) {
				guid = GUID.valueOf((byte[]) value);
			} else {
				guid = GUID.tryValueOf(value.toString());
			}
			if (StringUtil.isEmpty(relateTableName)) {
				return guid.toString();
			}

			FBaseDataObject fb;
			try {

				fb = BaseDataCenter.findObject(context, relateTableName, guid);
				if ("".equals(result))
					result = fb.getValueAsString(relateFieldName);
			} catch (Exception e) {
				MobileLog.logError(e);
			}

			return result;
		}
		return value.toString();
	}

	private void getAddApprovalUsers(Context context,
			HttpServletRequest request, HttpServletResponse response) {
		String userstr = (String) request.getSession().getAttribute("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		String workitemid = (String) request.getParameter("workitemid");
		GUID unitguid = (GUID)request.getSession().getAttribute("org");
		if (unitguid == null || unitguid.equals("")) {
			FUser fUser = context.get(FUser.class, user.getID());
			unitguid = fUser.getBelongedUnit();
			request.getSession().setAttribute("org",fUser.getBelongedUnit());
		}
		context.getLogin().setUserCurrentOrg(unitguid);
		context.setUserCurrentOrg(unitguid);
		PrintWriter writer = null;
		JSONObject json = new JSONObject();
		JSONArray userJsonArray = new JSONArray();
		String responsecode = "0";
		String responsemessage = "";
		if (workitemid != null && !"".equals(workitemid)) {
			try {
				writer = response.getWriter();
				List<GUID> userIds = new ArrayList<GUID>();
				boolean isEffectWorkflow = (Boolean) request.getSession()
						.getAttribute("isEffectWorkflow");
				IWorkItem workItem = WorkflowRunUtil.loadWorkItem(context,workitemid);
				if (isEffectWorkflow) {
					List<ParticipantObject> poList = workItem.getParticipants();
					for (ParticipantObject po : poList) {
						 if(po.getProperty(WorkFlowConst.AddApproverStaff) != null &&								 
								 po.getProperty(WorkFlowConst.AddApproverStaff).toString().equals(user.getID().toString())){
							 userIds.add(GUID.valueOf(po.getUserguid()));
						 }
					}
				} else {
					userIds = getAddApprover(context,GUID.tryValueOf(workitemid),GUID.tryValueOf(billDataID),GUID.tryValueOf(billDefineID));
				}
				for (GUID userId : userIds) {
					FUser approvalUser = context.find(FUser.class, userId);
					JSONObject userJson = new JSONObject();
					userJson.put("username", approvalUser.getTitle());
					userJson.put("userguid", approvalUser.getGuid());
					userJson.put("usercode", approvalUser.getName());
					userJsonArray.put(userJson);
				}
				String signWorkflow = "true";
				String canAddSign = "true";
				String finalApproval = "true";//是否是最后一个审批人
				
				int hasApproveled = 0;
				List<ParticipantObject> poList = workItem.getParticipants();
				for (ParticipantObject po : poList) {
					if (po.getAction() == 0) {
						hasApproveled++;
					}
				}
				if (hasApproveled > 1) {
					finalApproval = "false";
				}
				
				FBillDefine define = BillCentre.findBillDefine(context, GUID.tryValueOf(billDefineID));
				BillModel model = BillCentre.createBillModel(context, define);
			    model.loadData(GUID.tryValueOf(billDataID));
			    
				/*
				 * session中获取是否需要向被加签人发送待办
				 * 控件不可见时,根据系统选项判断是否发待办,可见时根据用户选择和系统选项一起控制是否发待办
				 */
			    if(!(Boolean)request.getSession().getAttribute("firstTime")){    	
			    	if(!(Boolean)request.getSession().getAttribute("showCheckedBox")){					
			    		Boolean sendSignTask = (Boolean) request.getSession().getAttribute("sendSignTask");
			    		if(sendSignTask){		    			
			    			//给被加签人发送待办
			    			sendSignTask(context,request,user,model,workItem);
			    		}	
			    	}else{
			    		if(request.getSession().getAttribute("checked") != null){
			    			Boolean checked = null;
			    			checked = Boolean.parseBoolean(request.getSession().getAttribute("checked").toString());
			    			if(!checked){
			    				//给被加签人发送待办
			    				sendSignTask(context,request,user,model,workItem);
			    				request.getSession().setAttribute("sendSignTask",true);
			    			}else{
			    				request.getSession().setAttribute("sendSignTask",false);
			    			}
			    		}					
			    	}
			    }
			    request.getSession().setAttribute("firstTime",true);
				json.put("signWorkflow", signWorkflow);
				json.put("canAddSign", canAddSign);
				json.put("finalApproval", finalApproval);
				json.put("users", userJsonArray);
				json.put("responsecode", "1");
				json.put("responsemessage", responsemessage);
			} catch (Exception e) {
				json.put("responsecode", responsecode);
				responsemessage = e.getMessage();
				json.put("responsemessage", responsemessage);
			}
		}
		writer.println(json);
		writer.flush();
		writer.close();
	}
	/**
	 * 给被加签人发送待办
	 */
	private void sendSignTask(Context context, HttpServletRequest request, User user, BillModel model, IWorkItem workItem) {
		String selectUsers = request.getParameter("selectUsers");
		boolean hasCustomApprover = false;
		String addPerson = "";
		boolean hasSelectUsers = false;
		try {
			if(workItem.getProcessInstance().getParam(WorkflowConst.addCustomApproverKey, true) != null){
				addPerson = workItem.getProcessInstance().getParam(WorkflowConst.addCustomApproverKey, true).toString();
				hasCustomApprover = true;
			}
		} catch (WorkflowException e) {
			throw new RuntimeException(e.getMessage());
		}
		String ids = "";
		if(selectUsers != null){
			//发送待办
			List<GUID> appointUserIDs = new ArrayList<GUID>();
			if (!selectUsers.equals("")) {
				String[] userGuids = selectUsers.split(";");
				for (int i = 0; i < userGuids.length; i++) {
					String str = userGuids[i].substring(0, userGuids[i].indexOf("_"));
					if(hasCustomApprover){
						if(!addPerson.contains(str)){
							ids = ids + str + ",";
							appointUserIDs.add(GUID.tryValueOf(str));						
						}						
					}else{
						ids = ids + str + ",";
						appointUserIDs.add(GUID.tryValueOf(str));
					}
				}
				if(!ids.equals("")){
					hasSelectUsers = true;
				}
				Map<GUID, Map<String, String>> userDatas = new HashMap<GUID, Map<String, String>>();
				for (GUID userId : appointUserIDs) {
					Map<String, String> propertys = new HashMap<String, String>();
					// 加签人guid
					propertys.put(WorkFlowConst.AddApproverStaff, user.getID().toString());
					// 被加签人guid
					userDatas.put(userId, propertys);
				}
				if (hasCustomApprover && hasSelectUsers) {
					BusinessProcessManager.setProcessParaByIWorkItem(workItem,WorkflowConst.addCustomApproverKey, addPerson + ","+ ids.substring(0, ids.length() - 1));		
					
				} else if(!hasCustomApprover && hasSelectUsers){
					BusinessProcessManager.setProcessParaByIWorkItem(workItem,WorkflowConst.addCustomApproverKey, ids.substring(0, ids.length() - 1));	
				}
				boolean isEffectWorkflow = (Boolean) request.getSession().getAttribute("isEffectWorkflow");
				// 被加签人才有的属性，每次执行加签动作都会覆盖以前的，既只能拿到当前的被加签人
//				BusinessProcessManager.setProcessParaByIWorkItem(workItem,WorkFlowConst.addCustomApproverKey,ids.substring(0, ids.length() - 1));
				
				if(hasSelectUsers){
					BusinessProcessManager.setProcessParaByIWorkItem(workItem,"WeChataddApprovers",ids.substring(0, ids.length() - 1));// 移动端用来获取当前所有的被加签人标识
				}	
				
				if(isEffectWorkflow){
					// 把被加签人加到参与者里，且给新增的参与者设置AddApproverSuggest属性值，此时就会触发工作流的WorkItemActiveFinish事件
					BusinessProcessManager.addUsersToCurrentWorkItem(context,workItem, userDatas);	
//				// 给工作项添加加签说明
					BusinessProcessManager.setProcessParaByIWorkItem(workItem, "AddApproveSuggest", "");					
				}else{
					if(!ids.equals("")){
						String[] users = ids.split(",");
						for (int i = 0; i < users.length; i++) {
							System.out.println(users[i]);
							GUID userID = GUID.tryValueOf(users[i]);
							WorkflowApproveImpl impl = new WorkflowApproveImpl();
							impl.setRecid(context.newRECID());
							impl.setApproveDate(System.currentTimeMillis());
							impl.setActionID(0);
							impl.setApprovedSuggest(null);
							impl.setApproveType(GUID.valueOf(WorkflowUtils.ApprovedType.addApproved.getValue()));
							FUnit unit = EnvCenter.getCurrUnit(context);
							impl.setApproveUnitID(unit.getUnitId());
							impl.setApproveUnitTitle(unit.getUnitName());
//							User user=context.getLogin().getUser();
							impl.setApproveUserID(user.getID());
							impl.setApproveUserTitle(user.getTitle());
							impl.setBillDefineID(model.getModelData().getMaster().getValueAsGUID(BillConst.f_billDefine));
							impl.setBillID(model.getModelData().getMaster().getRECID());
							FUser fUser = context.get(FUser.class,user.getID());
							impl.setSuggestUserID(userID);
							impl.setSuggestUserTitle(fUser.getTitle());
							impl.setWorkItemID(workItem.getGuid());
							WorkflowApproveTask task = new WorkflowApproveTask(impl);
							context.handle(task, WorkflowApproveTask.Method.INSERT);	
						}						
					}
				}
				// 更新待审批人字段（NEXTAPPROVAL）
//				WorkflowUtils.updateModelNextApproval(context,(BillModel)model,workItem,false);
//				//更新汇总待办
//				FoSumTodoApprovalUtil foSumTodoApprovalUtil =new FoSumTodoApprovalUtil();
//				foSumTodoApprovalUtil.addSumTodoApprovalWithWorkflow(context,model,workItem);
			}
		}
		
	}
	/**
	 * 获取部门和用户
	 * 
	 * @param context
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public void getRolesAndUsers(Context context, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		//进入选择加签人界面标记
		request.getSession().setAttribute("firstTime", false);
		String checked = request.getParameter("checked");
		if(checked != null){
			request.getSession().setAttribute("checked", checked);
		}
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		// 获取职员列表
		QueryObjectListInfo infos = new QueryObjectListInfo(
				BudgetConsts.BDG_MD_STAFF);//基础数据设计不启用权限控制
		IBaseDataContextHandle handlestaff = BudgetUtils
				.createContextHandle(unitguid);
		infos.setAuthType(AuthType.ACCESS);
		infos.setContextHandle(handlestaff);
		List<FBaseDataObject> staffList = BaseDataCenter.getObjectList(context,
				infos);

		FUnitOption isDisplay = EnvCenter.getUnitOption(context,unitguid,"FO037");//加签页面显示“同意后加签”选项
		FUnitOption isDefault = EnvCenter.getUnitOption(context,unitguid,"FO031");//加签选择界面显示时默认勾选"同意后加签生效"选项
		HashMap<Object, Object> dataMap = new HashMap<Object, Object>();
		// 循环职员，将职员和对应的部门建立映射，放到json对象中
		for (int i = 0; i < staffList.size(); i++) {
			FBaseDataObject item = staffList.get(i);
			if (!item.getStartFlag())
				continue;// 职员停用，不显示
			if (item.getFieldValue("LINKUSER") == null)
				continue;// 没关联用户，不显示
			// 关联用户停用，也不显示
			FUser fUser = context.find(FUser.class,
					(GUID) item.getFieldValue("LINKUSER"));
			if (fUser == null)
				continue;
			if (fUser.isLocked())
				continue;

			GUID deparementGuid;
			FBaseDataObject departmentNode;
			deparementGuid = (GUID) item.getFieldValue("DEPARTMENTID");
			departmentNode = BaseDataCenter.findObject(context,
					BudgetConsts.TB_MD_DEPARTMENT, deparementGuid);
			if (departmentNode != null) {
				JSONObject userJson = new JSONObject();
				userJson.put("username", item.getStdName());
				userJson.put("userguid", item.getFieldValue("LINKUSER"));
				userJson.put("usercode", item.getStdCode());
				userJson.put("dptname", departmentNode.getStdName());

				if (dataMap.get(deparementGuid) == null) {
					JSONArray userJsonArray = new JSONArray();
					userJsonArray.put(userJson);

					JSONObject dataJson = new JSONObject();
					dataJson.put("user", userJsonArray);
					dataJson.put("department", departmentNode.getStdName());
					dataJson.put("isDisplay", isDisplay.getStringValue());
					dataJson.put("isDefault", isDefault.getStringValue());

					dataMap.put(deparementGuid, dataJson);
				} else {
					JSONObject dataJson = (JSONObject) dataMap
							.get(deparementGuid);
					JSONArray userJsonArray = (JSONArray) dataJson.get("user");
					userJsonArray.put(userJson);
				}
			}
		}
		// 输出json对象
		PrintWriter writer = response.getWriter();
		writer.println(dataMap.values().toString());
		writer.flush();
		writer.close();
	}

	public void getRolesAndUsers1(Context context, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		GUID unitguid = (GUID) request.getSession().getAttribute("org");
		String username = (String) request.getSession().getAttribute("user");

		// 获取职员列表
		QueryObjectListInfo infos = new QueryObjectListInfo(
				BudgetConsts.TB_MD_DEPARTMENT);
		IBaseDataContextHandle handlestaff = BudgetUtils
				.createContextHandle(unitguid);
		User user = context.find(User.class, username.toLowerCase());
		context.changeLoginUser(user);
		infos.setAuthType(AuthType.ACCESS);
		infos.setContextHandle(handlestaff);
		List<FBaseDataObject> departmentList = BaseDataCenter.getObjectList(
				context, infos);

		JSONArray data = new JSONArray();
		// 循环职员，将职员和对应的部门建立映射，放到json对象中
		for (int i = 0; i < departmentList.size(); i++) {
			FBaseDataObject item = departmentList.get(i);
			JSONObject dataJson = new JSONObject();
			dataJson.put("department", item.getStdName());
			List<FUser> userList = getUserIdsByStaffUserId(context, unitguid,
					item.getRECID());
			JSONArray userJsonArray = new JSONArray();
			for (int j = 0; j < userList.size(); j++) {
				JSONObject userJson = new JSONObject();
				userJson.put("username", item.getStdName());
				userJson.put("userguid", item.getRECID());
				userJson.put("usercode", item.getStdCode());
				userJsonArray.put(userJson);
			}
			dataJson.put("user", userJsonArray);
			data.put(dataJson);
		}
		// 输出json对象
		PrintWriter writer = response.getWriter();
		writer.println(data.toString());
		writer.flush();
		writer.close();
	}

	/**
	 * 根据机构id，部门id，获取当前部门下的职员
	 * 
	 * @param context
	 * @param curOrgId
	 * @param curDeptId
	 * @return
	 */
	public static List<FUser> getUserIdsByStaffUserId(Context context,
			GUID curOrgId, GUID curDeptId) {
		List<FUser> userIds = new ArrayList<FUser>();
		if (curOrgId == null) {
			return null;
		}
		GUID staffID = null;
		List<FUser> fusers = AuthorityUtils.getFUsersByUnitGuidUnAuthFilter(
				context, curOrgId);
		try {
			for (FUser fuser : fusers) {
				Map<String, Object> whereMap = new HashMap<String, Object>();
				whereMap.put(BudgetConsts.FB_LINKUSER, fuser.getGuid());
				Map<String, Object> map = null;
				// 获取人用ID关联的职员ID
				map = DataCenter.getMap(context, BudgetConsts.BDG_MD_STAFF,
						whereMap);
				if (map != null) {
					staffID = (GUID) map.get("recid");
					FBaseDataObject staff = BaseDataCenter.findObject(context,
							BudgetConsts.BDG_MD_STAFF, staffID);
					if (staff != null) {
						if (!curDeptId.equals(GUID.emptyID)) {
							GUID deptId = (GUID) staff
									.getFieldValue(BudgetConsts.FB_DEPARTMENTID);
							if (deptId.equals(curDeptId)) {
								userIds.add(fuser);
							}
						} else {
							userIds.add(fuser);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		// 获取职员的基础数据
		return userIds;
	}
	
	/**
	 * 获取主表的配置信息
	 * @param mainPage
	 * @return
	 */
	private Map<String,String> getBudgetMainDatas(Context context,PageDefine mainPage,List<String> mainDataList){
		Map<String,String> mainDatas = new HashMap<String,String>();
		IMShowTemplate mShowTemp = mainPage.getmInputLayout();
		int mRows =mShowTemp.getTable().getRowCount();
		for(int i =0;i<mRows;i++){
			IMTableRow templ = mShowTemp.getTable().getRow(i);
			if(templ!=null && templ.getColCount()>1){
				IMTableCell cell0 = templ.getCol(0);
				String key = cell0.getTitle();
				IMTableCell cell1 = templ.getCol(1);
				String value = cell1.getTitle();
				mainDatas.put(value, key);
				mainDataList.add(value);
			}
		}
		return mainDatas;
	}
	
	
	private String getFieldValues(Context context,GUID businessInstanceId,String fieldName,Grid2Data grid){
		String value = fieldName;
		BtDataStateImpl state = context.get(BtDataStateImpl.class, businessInstanceId);
		if(value!=null && value.contains("BT_DATASTATE_LOG")){
			 value = value.substring(value.indexOf("[")+1,value.length()-1);
			 if(value!=null && "ORGID".equals(value)){
				GUID orgId =  state.getOrgId();
				FOrgNode OrgNode = BDOperUtil.getNodeByRecId(context, new OrgMainType().getTableName(), orgId);
				if(OrgNode!=null){
					value = OrgNode.getTitle();
				}
			 }else if(value!=null && "PERIOD".equals(value)){
				int year = state.getYear();
				String period = state.getPeriod();
				int num = state.getPeriodNum();
				if("Y".equals(period)){
					period = year+"年"+num+"月";
				}else if("N".equals(period)){
					period =year+"年";
				}else if("Z".equals(period)){
					period = year+"年"+num+"周";
				}
				value = period; 
			 }else{
				 String linkid = state.getLinkId();
				 if(linkid!=null && linkid.contains(value)){
					 if(linkid.contains(",")){
						 String[] dimouts = linkid.split(",");
						 for(String dim : dimouts){
							 if(dim.contains(value)){
								String dimId = dim.substring(dim.indexOf("=")+1);
								FBtBudgetDim btBudgetDim = context.get(FBtBudgetDim.class, value);
								if(btBudgetDim!=null){
									FBaseDataObject baseDataObject = BaseDataCenter.findObject(context, btBudgetDim.getDimTableName(), GUID.tryValueOf(dimId));
									value = baseDataObject.getStdName();
								}
							 }
						 }
					 }else{
						 String[] dimouts = linkid.split("=");
						 FBtBudgetDim btBudgetDim = context.get(FBtBudgetDim.class, dimouts[0]);
						 if(btBudgetDim!=null){
							 FBaseDataObject baseDataObject = BaseDataCenter.findObject(context, btBudgetDim.getDimTableName(), GUID.tryValueOf(dimouts[1]));
							 value = baseDataObject.getStdName();
						 }
					 }
				 }
			 }
		}else{
			String indexs = value.substring(value.indexOf("[")+1,value.length()-1);
			if(indexs!=null && indexs.contains(",")){
				String[] arrs = indexs.split(",");
				int row = Integer.parseInt(arrs[0]);
				int col = Integer.parseInt(arrs[1]);
				GridCellData cell = grid.getGridCellData(col, row);
				if(cell!=null){
					value = cell.getShowText();
				}
			}
		}
		return value;
	}
	/**
	 * 获取子表的配置信息
	 * @param detailPages
	 * @return
	 */
	private Map<String,Map<String,String>> getBudgetDetailDatas(List<PageDefine> detailPages,Map<String,List<String>> detailDataList){
		Map<String,Map<String,String>> detailDatas = new HashMap<String,Map<String,String>>();
		for(PageDefine detailPage : detailPages){
			if(detailPage.isHidden()){
				continue;
			}
			IMShowTemplate dShowTemp = detailPage.getmInputLayout();
			int dRows =dShowTemp.getTable().getRowCount();
			Map<String,String> mainDatas = new HashMap<String,String>();
			List<String> cells = new ArrayList<String>();
			for(int i =0;i<dRows;i++){
				IMTableRow templ = dShowTemp.getTable().getRow(i);
				if(templ!=null && templ.getColCount()>1){
					IMTableCell cell0 = templ.getCol(0);
					String key = cell0.getTitle();
					IMTableCell cell1 = templ.getCol(1);
					String value = cell1.getTitle();
					if(value!=null ){
						mainDatas.put(value, key);
						cells.add(value);
					}
				}
			}
			detailDatas.put(detailPage.getName(), mainDatas);
			detailDataList.put(detailPage.getName(), cells);
		}
		return detailDatas;
	}
	
	private Grid2Data initView(Context context,String viewName,BtDataStateImpl state){
		Grid2Data grid=null;
		 if(viewName!=null && !"".equals(viewName)){
				HybercubeViewDeclare view = context.find(HybercubeViewDeclare.class, viewName);
				Map<String,Object> dimMap = LinkidUtils.getDimMap(state.getLinkId());
				DataPeriod dataperiod = new DataPeriod(state.getYear(),state.getPeriod(),state.getPeriodNum());
				List<? extends DimenOutsideDefine> dimenOutsides = view.getDimenOutsides();
				for (DimenOutsideDefine out : dimenOutsides) {
					if (isOrg(context, out.getName())) {
						((DimenOutsideDeclare) out).setCurValue(state.getOrgId());
					} else if (DimConsts.SYS_DIMEN_PERIOD_NAME.equals(out.getName())) {
						((DimenOutsideDeclare) out).setCurValue(dataperiod.toString());
					} else if (DimConsts.SYS_DIMEN_VERSION_NAME.equals(out.getName())) {
						((DimenOutsideDeclare) out).setCurValue("0");
//						if(state!=null && "1".equals(state.getIsAdjust())){
//							FAdjustVersion adjustVersion = context.find(FAdjustVersion.class, state.getRecId());
//							if(adjustVersion!=null){
//								((DimenOutsideDeclare) out).setCurValue(adjustVersion.getVersionId());
//							}
//						}else{
//							((DimenOutsideDeclare) out).setCurValue("0");
//						}
					} else if (DimConsts.SYS_DIMEN_ZBTYPE_NAME.equals(out.getName())) {
						((DimenOutsideDeclare) out).setCurValue("1");
					} else {
						Object obj = dimMap.get(out.getName());
						if(obj!=null){
							((DimenOutsideDeclare) out).setCurValue(dimMap.get(out.getName()));
						}else{
							((DimenOutsideDeclare) out).setCurValue(DimConsts.SUM_DIM_VAL_GUID);
						}
					}
				}
				FBtScheme scheme=HybercubeViewUtils.getSchemeByView(context, view);
				GUID fmlSolutionId=GUID.emptyID;
//				if(state.getSolutionId()!=null){
//					SolutionIntf solution =context.find(SolutionIntf.class, state.getSolutionId()); 
//					if(solution!=null){
//						fmlSolutionId = solution.getFormula_scheme();
//					}
//				}
				if(fmlSolutionId.equals(GUID.emptyID)){
					List<FBTFormulaScheme> formulaSchemes = context.getList(FBTFormulaScheme.class , scheme.getRecId());
					if (formulaSchemes!=null&&formulaSchemes.size()>0) {
						fmlSolutionId=formulaSchemes.get(0).getGuid();
					}
				}
				HyberViewRunningTask task = new HyberViewRunningTask(false, view);
				task.setFmlSolutionId(fmlSolutionId);//null
//				task.setSolutionId(state.getSolutionId());
				task.addParam("orgId", state.getOrgId().toString()); 
				task.addParam("dataPeriod", dataperiod.getName());
				context.handle(task);
				HybercubeViewRunning viewRunning = task.getViewRunning();
				try {
					grid = viewRunning.getExpGridData();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			 }
		 return grid;
	}
	
	private Boolean isOrg(Context context,String dimenName){
		if(JqLib.isEmpty(dimenName)) return false;
		if(DimConsts.SYS_DIMEN_ORG_NAME.equalsIgnoreCase(dimenName) || dimenName.toLowerCase().endsWith("md_bdorg")) return true;
		List<OrgCategory> orgCategories = context.getList(OrgCategory.class);
		if(orgCategories==null || orgCategories.size()==0)
			return false;
		for(OrgCategory orgCategory:orgCategories){
			if(orgCategory.getName().equalsIgnoreCase(dimenName))
				return true;
		}
		return false;
	}
}
