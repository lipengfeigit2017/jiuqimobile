package com.jiuqi.gmc.mobile.approval.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jiuqi.dna.bap.bill.common.model.BillModel;
import com.jiuqi.dna.bap.workflowmanager.common.BusinessObjectLoader;
import com.jiuqi.dna.bap.workflowmanager.common.WFBusinessObject;
import com.jiuqi.dna.bap.workflowmanager.execute.common.BusinessProcessManager;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.consts.ApprovalState;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.entity.BOConditionField;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.entity.TaskListDefine;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.entity.WFBusinessObjectXML;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.facade.FRecord;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.facade.FTaskListDefine;
import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.dna.ui.common.DataObject;
import com.jiuqi.dna.ui.wt.InfomationException;
import com.jiuqi.dna.workflow.intf.facade.IWorkItem;
import com.jiuqi.fo.common.foconst.FieldConst;
import com.jiuqi.fo.common.foconst.FoFieldStateConst;
import com.jiuqi.fo.intf.task.ImageWebConfigTask;
import com.jiuqi.fo.workflow.intf.constant.WorkFlowExtendEnum;
import com.jiuqi.sm.imagecenter.common.ImageConst;
import com.jiuqi.sm.imagecenter.common.ImageTaskLogProcessor;
import com.jiuqi.vacomm.env.EnvCenter;
import com.jiuqi.vacomm.env.FUnitOption;
import com.jiuqi.vacomm.utils.CollectionUtil;
import com.jiuqi.vacomm.utils.StringUtil;

public class AppUtil {

	public static String getTaskListDefineListModelIds(
			List<FTaskListDefine> taskListDefineLists) {
		List<String> lists = new ArrayList<String>();
		for (FTaskListDefine taskListDefineList : taskListDefineLists) {
			ArrayList<BOConditionField> conditionFieldList = taskListDefineList
					.getTaskListDefine().conditionFieldList;
			for (BOConditionField conditionField : conditionFieldList) {
				if (conditionField.getDefaultResult() != null
						&& !"".equals(conditionField.getDefaultResult())) {
					lists.add(conditionField.getDefaultResult());
				}
			}
		}
		String sql = "";
		for (String list : lists) {
			sql = sql + list + ",";
		}
		sql = sql.replaceAll("^,*|,*$", "");
		sql = sql.replaceAll("\"", "");
		return sql;
	}

	/**
	 * 得到portal选择的审批列表定义
	 * 
	 * @param defineGuids
	 */
	public static List<FTaskListDefine> initTaskListDefine(Context context,
			String defineGuids) {
		List<FTaskListDefine> defineList = new ArrayList<FTaskListDefine>();
		String[] guids = defineGuids.split(";");
		for (int i = 0; i < guids.length; i++) {
			FTaskListDefine taskListDefine = context.find(
					FTaskListDefine.class, GUID.valueOf(guids[i]));
			if (taskListDefine != null) {
				defineList.add(taskListDefine);
			}
		}
		return defineList;
	}

	/**
	 * 根据审批列表定义，获取当前用户的已办
	 * 
	 * @param context
	 * @param userId
	 * @param taskListDefineList
	 * @return
	 */
	public static Map<FTaskListDefine, List<FRecord>> getDoneRecords(
			Context context, GUID userId,
			List<FTaskListDefine> taskListDefineList) {
		Map<FTaskListDefine, List<FRecord>> taskListFrecordMaps = new HashMap<FTaskListDefine, List<FRecord>>();
		// List<FRecord> records = new ArrayList<FRecord>();
		Map<FRecord, FTaskListDefine> recordTaskListMap = new HashMap<FRecord, FTaskListDefine>();
		String sql = getTaskListDefineListModelIds(taskListDefineList);
		HashMap<String, Object> moreKey = new HashMap<String, Object>();
		moreKey.put("userId", userId);
		moreKey.put("arg", sql);
		List<FRecord> doneRecords = context.getList(FRecord.class, moreKey,
				false);
		Map<GUID, FTaskListDefine> taskListDefineMaps = getDefineByBusinessobjectID(taskListDefineList);
		final Context fcontext = context;
		final Map<FRecord, FTaskListDefine> frecordTaskListMap = recordTaskListMap;
		sortRecords(fcontext, frecordTaskListMap, doneRecords);
		for (FRecord record : doneRecords) {
			// doneRecordLists.add(record.getBusinessInstanceID());
			GUID businessObjectID = GUID.tryValueOf(record.getFieldMap()
					.get("WORKCATEGORY").toString());
			FTaskListDefine fTaskListDefine = taskListDefineMaps
					.get(businessObjectID);
			// if(!taskListDefineIDs.contains(fTaskListDefine.getID())){
			// continue;
			// }
			if (fTaskListDefine != null) {
				recordTaskListMap.put(record, fTaskListDefine);
			}
			if (taskListFrecordMaps.containsKey(fTaskListDefine)) {
				taskListFrecordMaps.get(fTaskListDefine).add(record);
			} else {
				List<FRecord> currentRecords = CollectionUtil.newArrayList();
				currentRecords.add(record);
				taskListFrecordMaps.put(fTaskListDefine, currentRecords);
			}
			// allRecords.add(record);
		}
		return taskListFrecordMaps;
	}

	/**
	 * 根据审批列表定义，获取当前用户的待办
	 * 
	 * @param context
	 * @param userId
	 * @param taskListDefineList
	 * @return
	 */
	public static Map<FTaskListDefine, List<FRecord>> getWaitRecords(
			Context context, GUID userId,
			List<FTaskListDefine> taskListDefineList) {
		Map<FTaskListDefine, List<FRecord>> taskListFrecordMaps = new HashMap<FTaskListDefine, List<FRecord>>();
		Map<FRecord, FTaskListDefine> recordTaskListMap = new HashMap<FRecord, FTaskListDefine>();
		String sql = getTaskListDefineListModelIds(taskListDefineList);
		HashMap<String, Object> moreKey = new HashMap<String, Object>();
		moreKey.put("userId", userId);
		moreKey.put("arg", sql);
		List<FRecord> waitRecordLists = context.getList(FRecord.class, moreKey,
				true);
		Map<GUID, FTaskListDefine> taskListDefineMaps = getDefineByBusinessobjectID(taskListDefineList);
		final Context fcontext = context;
		final Map<FRecord, FTaskListDefine> frecordTaskListMap = recordTaskListMap;
		for (FRecord waitRecord : waitRecordLists) {
			GUID businessObjectID = GUID.tryValueOf(waitRecord.getFieldMap()
					.get("WORKCATEGORY").toString());
			FTaskListDefine fTaskListDefine = taskListDefineMaps
					.get(businessObjectID);
			recordTaskListMap.put(waitRecord, fTaskListDefine);
			if (taskListFrecordMaps.containsKey(fTaskListDefine)) {
				taskListFrecordMaps.get(fTaskListDefine).add(waitRecord);
			} else {
				List<FRecord> currentRecords = CollectionUtil.newArrayList();
				currentRecords.add(waitRecord);
				taskListFrecordMaps.put(fTaskListDefine, currentRecords);
			}
		}
		return taskListFrecordMaps;
	}

	/**
	 * 根据审批列表定义，获取当前用户的待办
	 * 
	 * @param context
	 * @param userId
	 * @param taskListDefineList
	 * @return
	 */
	public static Map<FTaskListDefine, List<FRecord>> getWaitRecordsBySql(
			Context context, GUID userId,
			List<FTaskListDefine> taskListDefineList) {
		Map<FTaskListDefine, List<FRecord>> taskListFrecordMaps = new HashMap<FTaskListDefine, List<FRecord>>();
		Map<FRecord, FTaskListDefine> recordTaskListMap = new HashMap<FRecord, FTaskListDefine>();
		for (FTaskListDefine taskListDefine : taskListDefineList) {
			List<FTaskListDefine> list = new ArrayList<FTaskListDefine>();
			list.add(taskListDefine);
			String sql = getTaskListDefineListModelIds(list);
			HashMap<String, Object> moreKey = new HashMap<String, Object>();
			moreKey.put("userId", userId);
			moreKey.put("arg", sql);
			List<FRecord> waitRecordLists = context.getList(FRecord.class,
					moreKey, true);
			Map<GUID, FTaskListDefine> taskListDefineMaps = getDefineByBusinessobjectID(taskListDefineList);
			for (FRecord waitRecord : waitRecordLists) {
				GUID businessObjectID = GUID.tryValueOf(waitRecord
						.getFieldMap().get("WORKCATEGORY").toString());
				FTaskListDefine fTaskListDefine = taskListDefineMaps
						.get(businessObjectID);
				recordTaskListMap.put(waitRecord, fTaskListDefine);
				if (taskListFrecordMaps.containsKey(fTaskListDefine)) {
					taskListFrecordMaps.get(fTaskListDefine).add(waitRecord);
				} else {
					List<FRecord> currentRecords = CollectionUtil
							.newArrayList();
					currentRecords.add(waitRecord);
					taskListFrecordMaps.put(fTaskListDefine, currentRecords);
				}
			}
		}
		return taskListFrecordMaps;
	}

	/**
	 * 根据审批列表定义，获取当前用户的待办
	 * 
	 * @param context
	 * @param userId
	 * @param taskListDefineList
	 * @return
	 */
	public static Map<FTaskListDefine, List<FRecord>> getWaitRecordsByJK(
			Context context, GUID userId,
			List<FTaskListDefine> taskListDefineList, ApprovalState approvalType) {
		Map<FTaskListDefine, List<FRecord>> taskListFrecordMaps = new HashMap<FTaskListDefine, List<FRecord>>();
		for (FTaskListDefine taskListDefine : taskListDefineList) {
			List<FRecord> recordList = BusinessProcessManager.getFRecords(
					context, taskListDefine.getID(), userId, approvalType);
			taskListFrecordMaps.put(taskListDefine, recordList);
		}
		return taskListFrecordMaps;
	}

	public static List<FRecord> getWaitRecords(Context context, GUID userId,
			DataObject dataobject) {
		List<FRecord> records = new ArrayList<FRecord>();
		Map<FRecord, FTaskListDefine> recordTaskListMap = new HashMap<FRecord, FTaskListDefine>();
		List<FTaskListDefine> taskListDefineList = getTaskListDefineList(
				context, dataobject);
		String sql = getTaskListDefineListModelIds(taskListDefineList);
		HashMap<String, Object> moreKey = new HashMap<String, Object>();
		moreKey.put("userId", userId);
		moreKey.put("arg", sql);
		List<FRecord> waitRecordLists = context.getList(FRecord.class, moreKey,
				true);
		Map<GUID, FTaskListDefine> taskListDefineMaps = getDefineByBusinessobjectID(taskListDefineList);
		for (FRecord waitRecord : waitRecordLists) {
			GUID businessObjectID = GUID.tryValueOf(waitRecord.getFieldMap()
					.get("WORKCATEGORY").toString());
			FTaskListDefine fTaskListDefine = taskListDefineMaps
					.get(businessObjectID);
			if (!sql.contains(fTaskListDefine.getID().toString())) {
				continue;
			}
			recordTaskListMap.put(waitRecord, fTaskListDefine);
			records.add(waitRecord);
		}
		return records;
	}

	/**
	 * 将得到的FRecord进行排序
	 */
	private static void sortRecords(final Context context,
			final Map<FRecord, FTaskListDefine> recordTaskListMap,
			List<FRecord> records) {
		Collections.sort(records, new Comparator<FRecord>() {
			public int compare(FRecord record1, FRecord record2) {
				if (record1 == null || record2 == null) {
					return 0;
				} else {
					FTaskListDefine taskListDefine1 = recordTaskListMap
							.get(record1);
					FTaskListDefine taskListDefine2 = recordTaskListMap
							.get(record2);
					String arrTime1 = getSingleAprovalRecord(context, record1,
							taskListDefine1.getID(), "REACHTIME");
					String arrTime2 = getSingleAprovalRecord(context, record2,
							taskListDefine2.getID(), "REACHTIME");
					if (arrTime1 == null || "".equals(arrTime1)
							|| arrTime2 == null || "".equals(arrTime2)) {
						return 0;
					} else {
						return arrTime1.compareTo(arrTime2) * -1;
					}
				}
			}
		});
	}

	/**
	 * 根据字段名称显示单个字段值
	 * 
	 * @param context
	 * @param record
	 * @param defineID
	 * @param names
	 * @return List<String>
	 */
	public static String getSingleAprovalRecord(Context context,
			FRecord record, GUID defineID, String name) {
		String data = "";
		FTaskListDefine fdefine = context.get(FTaskListDefine.class, defineID);
		TaskListDefine taskListDefine = fdefine.getTaskListDefine();
		GUID businessObjectID = null;
		if (taskListDefine.businessObjectList.size() > 0) {
			businessObjectID = taskListDefine.businessObjectList.get(0).getID();
		} else {
			return data;
		}
		if (businessObjectID == null) {
			return data;
		}
		GUID businessInstanceId = record.getBusinessInstanceID();
		WFBusinessObject businessObject = (WFBusinessObject) BusinessObjectLoader
				.findBusinessObject(context, businessObjectID);
		data = businessObject.getFieldValue(context, businessInstanceId, name,
				record);
		return data;
	}

	public static List<FTaskListDefine> getTaskListDefineList(Context context,
			DataObject dataobject) {
		List<FTaskListDefine> taskListDefineList = CollectionUtil
				.newArrayList();// 待审列表
		String defineGuids = dataobject.getString("defineGuids");
		if (!StringUtil.isEmpty(defineGuids)) {
			String[] guids = defineGuids.split(";");
			for (int i = 0; i < guids.length; i++) {
				FTaskListDefine taskListDefine = context.find(
						FTaskListDefine.class, GUID.valueOf(guids[i]));
				if (taskListDefine != null) {
					if (taskListDefineList == null) {
						taskListDefineList = new ArrayList<FTaskListDefine>();
					}
					taskListDefineList.add(taskListDefine);
				}
			}
		}
		return taskListDefineList;
	}

	/**
	 * 获取业务对象对应的审批任务列表定义id
	 * 
	 * @param context
	 * @param businessObjectID
	 * @return
	 */
	public static Map<GUID, FTaskListDefine> getDefineByBusinessobjectID(
			List<FTaskListDefine> taskListDefineLists) {
		Map<GUID, FTaskListDefine> taskListDefineMaps = new HashMap<GUID, FTaskListDefine>();
		for (FTaskListDefine taskListDefine : taskListDefineLists) {
			TaskListDefine taskDefine = taskListDefine.getTaskListDefine();
			if (null != taskDefine) {
				if (taskDefine.businessObjectList.size() > 0) {
					for (WFBusinessObjectXML BOXML : taskDefine.businessObjectList) {
						taskListDefineMaps.put(BOXML.getID(), taskListDefine);
					}
				}
			}
		}
		return taskListDefineMaps;
	}
	/**
	 * 驳回后做退影像操作
	 * @param context
	 * @param workItem
	 * @param model
	 * @param unitguid  void
	 */
	public static void getBackImage(Context context, IWorkItem workItem, BillModel model,GUID unitguid) {
		String result = workItem.getActiveNode().getProperty(WorkFlowExtendEnum.REJECT_BACK_IMAGE.getKey());
		if(result!=null&&result.equals("true")){
			if(null!=model.getDefine().getMasterTable().findField(FieldConst.f_barcode)&&
					null!=model.getDefine().getMasterTable().findField(FieldConst.f_imageState)&&
							model.getModelData().getMaster().getValueAsInt(FieldConst.f_imageState)>-1){
				ImageWebConfigTask task = new ImageWebConfigTask(model);
				task.setRejectType("0");
				task.setRejectRemark(model.getApprovalIdea()!=null?model.getApprovalIdea():"费控工作流驳回，影像退单。");
				context.handle(task);
				if(!task.isSuccess()){
					ImageTaskLogProcessor.writeOperateLog(context, task.getBarCode(),
							model.getModelData().getMaster().getRECID(), FoFieldStateConst.IMAGESTATE.WAITSCAN.getValue(), false, new Date(), 
							"单据工作流驳回失败\n原因："+task.getReturnMes(),
							EnvCenter.getCurrUserId(context),ImageConst.TaskSource.WORKFLOWREJECT.getSourceValue());
					throw new InfomationException("单据工作流驳回失败\n原因："+task.getReturnMes());
				}else{
					ImageTaskLogProcessor.writeOperateLog(context, task.getBarCode(),
							model.getModelData().getMaster().getRECID(), FoFieldStateConst.IMAGESTATE.WAITSCAN.getValue(), true, new Date(), 
							null,
							EnvCenter.getCurrUserId(context),ImageConst.TaskSource.WORKFLOWREJECT.getSourceValue());
				}
			}
		}else if(null!=model.getDefine().getMasterTable().findField(FieldConst.f_barcode)&&
				null!=model.getDefine().getMasterTable().findField(FieldConst.f_imageState)&&
						model.getModelData().getMaster().getValueAsInt(FieldConst.f_imageState)>-1){ //当工作流没有配置退单并退影像时，根据管控选项来判断是否退影像
			FUnitOption unitOption = EnvCenter.getUnitOption(context,unitguid,"IMAGE002");
			if(unitOption!=null&&unitOption.getBoolValue()){
				ImageWebConfigTask task = new ImageWebConfigTask(model);
				task.setRejectType("0");
				task.setRejectRemark(model.getApprovalIdea()!=null?model.getApprovalIdea():"费控工作流驳回，影像退单。");
				context.handle(task);
				if(!task.isSuccess()){
					ImageTaskLogProcessor.writeOperateLog(model.getContext(), task.getBarCode(),
							model.getModelData().getMaster().getRECID(), FoFieldStateConst.IMAGESTATE.WAITSCAN.getValue(), false, new Date(), 
							"单据工作流驳回失败\n原因："+task.getReturnMes(),
							EnvCenter.getCurrUserId(model.getContext()),ImageConst.TaskSource.WORKFLOWREJECT.getSourceValue());
					throw new InfomationException("单据工作流驳回失败，原因："+task.getReturnMes());
				}else{
					ImageTaskLogProcessor.writeOperateLog(model.getContext(), task.getBarCode(),
							model.getModelData().getMaster().getRECID(), FoFieldStateConst.IMAGESTATE.WAITSCAN.getValue(), true, new Date(), 
							null,
							EnvCenter.getCurrUserId(model.getContext()),ImageConst.TaskSource.WORKFLOWREJECT.getSourceValue());
				}
			}
		}
		
	}
}
