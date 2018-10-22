/**============================================================
 * 版权： 久其软件 版权所有 (c) 2002 - 2016
 * 包： com.jiuqi.fo.youse.ui.plantask
 * 修改记录：
 * 日期                作者           内容
 * =============================================================
 * 2017年1月6日       qinjunjie        
 * ============================================================*/
package com.jiuqi.gmc.mobile.approval.common.service;

import java.util.Date;
import java.util.List;

import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.da.DBCommand;
import com.jiuqi.dna.core.da.RecordSet;
import com.jiuqi.dna.core.da.RecordSetField;
import com.jiuqi.dna.core.service.Publish;
import com.jiuqi.dna.core.service.Service;
import com.jiuqi.gmc.mobile.approval.common.JPushUtil;
import com.jiuqi.gmc.mobile.approval.intf.facade.FPushMessage;
import com.jiuqi.gmc.mobile.approval.intf.impl.PushMessageImpl;
import com.jiuqi.gmc.mobile.approval.intf.task.PushMessageTask;
import com.jiuqi.util.StringUtils;

/**
 * <p>
 * 待办消息推送Service
 * </p>
 *
 * <p>
 * Copyright: 版权所有 (c) 2002 - 2016<br>
 * Company: 久其
 * </p>
 *
 * @author qinjunjie
 * @version 2017-01-04 22:48:07
 */

public class PushMsgService extends Service {

	protected PushMsgService() {
		super("待办消息推送PushMsgService");
	}

	/**
	 * 插入消息记录表
	 */
	@Publish
	protected class InsertPushMessageHandler extends TaskMethodHandler<PushMessageTask, PushMessageTask.Method> {

		protected InsertPushMessageHandler() {
			super(PushMessageTask.Method.ADD);
		}

		@Override
		protected void handle(Context context, PushMessageTask task) throws Throwable {
			PushMessageImpl pushMessageImpl = task.getPushMessageImpl();
			//先删除记录，如果推送失败后再插入记录
			String delSql = " define delete deletePushMessage(@billdefine guid,@billdataid guid)" + 
					" begin" + 
					" delete from PUSH_MESSAGE_LOG as t" + 
					" where t.BILLDEFINE = @billdefine and t.BILLDATAID = @billdataid" + 
					" end ";
			DBCommand del = context.prepareStatement(delSql);
			try {
				del.setArgumentValues(task.getPushMessageImpl().getBillDefine(),task.getPushMessageImpl().getBillDataID());
				del.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				del.unuse();
			}
			JPushUtil jpushUtil = new JPushUtil();
			boolean flag = jpushUtil.pushMessage(context, pushMessageImpl);
			// 如果推送失败，记录到表中，随后通过计划任务再次推送
			if (!flag) {
				StringBuffer dnaSQL = new StringBuffer();
				dnaSQL.append(
						" define insert insertPushMessage(@recid guid,@recver long,@billdefine guid,@billdataid guid,"
								+ "@billcode string,@workitemid guid,@receivers bytes,@message string,@sendtime date,"
								+ "@resulttype string,@ispushed boolean)");
				dnaSQL.append(" begin");
				dnaSQL.append(
						" insert into PUSH_MESSAGE_LOG(recid,recver,billdefine,billdataid,billcode,workitemid,receivers,"
								+ "message,sendtime,resulttype,ispushed)");
				dnaSQL.append(" values(@recid,@recver,@billdefine,@billdataid,@billcode,@workitemid,@receivers,"
						+ "@message,@sendtime,@resulttype,@ispushed)");
				dnaSQL.append(" end ");
				DBCommand dbc = context.prepareStatement(dnaSQL);
				try {
					dbc.setArgumentValues(context.newRECID(), context.newRECVER(),
							pushMessageImpl.getBillDefine(), pushMessageImpl.getBillDataID(), pushMessageImpl.getBillCode(),
							pushMessageImpl.getWorkItemID(), StringUtils.join(pushMessageImpl.getReceivers(), "###").getBytes(),
							pushMessageImpl.getMessage(), new Date(),
							pushMessageImpl.getResultType(), pushMessageImpl.isPushed());
					dbc.executeUpdate();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					dbc.unuse();
				}
			}

		}
	}

	/**
	 * 查询推送失败的消息
	 * */
	@Publish
	protected class QueryPushMessageHandler extends ResultListProvider<FPushMessage> {

		@Override
		protected void provide(Context context, List<FPushMessage> list) throws Throwable {
			StringBuffer sb = new StringBuffer();
			sb.append("define query QueryPushMessage() begin");
			sb.append(
					" select t.RECID,t.RECVER,t.BILLDEFINE,t.BILLDATAID,t.BILLCODE,t.WORKITEMID,t.RECEIVERS,"
					+ "t.MESSAGE,t.SENDTIME,t.RESULTTYPE,t.ISPUSHED");
			sb.append(" from PUSH_MESSAGE_LOG as t");
			sb.append(" where t.ISPUSHED = 0");
			sb.append(" end ");
			DBCommand dbc = context.prepareStatement(sb.toString());
			PushMessageImpl pushMessageImpl;
			try {
				RecordSet rs = dbc.executeQuery();
				while (rs.next()) {
					RecordSetField recid = rs.getFields().get(0);
					RecordSetField recver = rs.getFields().get(1);
					RecordSetField billDefine = rs.getFields().get(2);
					RecordSetField billDataID = rs.getFields().get(3);
					RecordSetField billCode = rs.getFields().get(4);
					RecordSetField workItemID = rs.getFields().get(5);
					RecordSetField receivers = rs.getFields().get(6);
					RecordSetField message = rs.getFields().get(7);
					RecordSetField sendTime = rs.getFields().get(8);
					RecordSetField resultType = rs.getFields().get(9);
					pushMessageImpl = new PushMessageImpl();
					pushMessageImpl.setRecid(recid.getGUID());
					pushMessageImpl.setRecver(recver.getLong());
					pushMessageImpl.setBillDefine(billDefine.getGUID());
					pushMessageImpl.setBillDataID(billDataID.getGUID());
					pushMessageImpl.setBillCode(billCode.getString());
					pushMessageImpl.setWorkItemID(workItemID.getGUID());
					pushMessageImpl.setReceivers(new String(receivers.getBytes()).split("###"));
					pushMessageImpl.setMessage(message.getString());
					pushMessageImpl.setSendTime(new Date(sendTime.getDate()));
					pushMessageImpl.setResultType(resultType.getString());
					pushMessageImpl.setPushed(false);
					list.add(pushMessageImpl);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				dbc.unuse();
			}
		}

	}

}
