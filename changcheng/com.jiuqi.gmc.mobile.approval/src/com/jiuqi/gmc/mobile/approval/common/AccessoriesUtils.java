package com.jiuqi.gmc.mobile.approval.common;

import java.util.ArrayList;
import java.util.List;

import com.jiuqi.dna.bap.bill.common.model.BillCentre;
import com.jiuqi.dna.bap.bill.common.model.BillModel;
import com.jiuqi.dna.bap.bill.intf.entity.BillEnclosure;
import com.jiuqi.dna.bap.bill.intf.facade.model.FBillDefine;
import com.jiuqi.dna.bap.model.common.define.intf.IField;
import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.type.DataType;
import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.sm.attachment.intf.FSmAttachment;
import com.jiuqi.sm.attachment.intf.querykey.AttachmentQueryKey;

public class AccessoriesUtils {

	/**
	 * 获取bap数据库单据的附件列表
	 * 
	 * @param context
	 *            上下文
	 * @param defineID
	 *            单据定义ID
	 * @param billID
	 *            单据ID
	 * @return
	 */
	public static List<BillEnclosure> getBillEnclosure(Context context,
			GUID defineID, GUID billID) {
		// 附件列表
		List<BillEnclosure> list = new ArrayList<BillEnclosure>();

		// 模型定义
		FBillDefine define = BillCentre.findBillDefine(context, defineID);
		// 获取模型
		BillModel model = BillCentre.createBillModel(context, define);
		// 加载数据
		model.loadData(billID);

		// 查询绑定单据的附件列表
		List<BillEnclosure> bList = model.getBillEnclosures();
		GUID recid = null;
		if (bList != null && bList.size() > 0) {
			list.addAll(bList);
			recid = bList.get(0).getBILLID();
		}
		List<IField> fields = model.getDefine().getMasterTable().getFields();
		// guid型
		DataType guid = model.getDefine().getMasterTable().findField("RECID")
				.getField().getType();
		for (IField field : fields) {
			// 系统字段不读
			if (model.isSysField(field.getName())) {
				continue;
			}
			// GUID 类型且没有关联其他表，且不为空，基本上为附件关联的字段
			if (field.getField() != null
					&& field.getField().getType().equals(guid)) {
				if (field.getRelationTable() == null) {
					GUID value = model.getModelData().getMaster()
							.getValueAsGUID(field.getName());
					if (value != null) {
						// 查询绑定单据中某字段的附件列表
						if (recid != null && value.equals(recid))
							continue;
						List<BillEnclosure> cList = model
								.getBillEnclosures(value);
						if (cList != null && cList.size() > 0) {
							list.addAll(cList);
						}
					}

				}
			}
		}
		return list;

	}

	public static List<FSmAttachment> getBillAccachment(Context context,
			GUID billID) {
		AttachmentQueryKey key = new AttachmentQueryKey();
		key.setBillID(billID);
		List<FSmAttachment> attachments = context.getList(FSmAttachment.class,
				key);
		return attachments;
	}
}
