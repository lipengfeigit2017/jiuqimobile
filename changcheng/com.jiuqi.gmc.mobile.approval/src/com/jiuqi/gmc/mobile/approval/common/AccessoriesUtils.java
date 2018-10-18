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
	 * ��ȡbap���ݿⵥ�ݵĸ����б�
	 * 
	 * @param context
	 *            ������
	 * @param defineID
	 *            ���ݶ���ID
	 * @param billID
	 *            ����ID
	 * @return
	 */
	public static List<BillEnclosure> getBillEnclosure(Context context,
			GUID defineID, GUID billID) {
		// �����б�
		List<BillEnclosure> list = new ArrayList<BillEnclosure>();

		// ģ�Ͷ���
		FBillDefine define = BillCentre.findBillDefine(context, defineID);
		// ��ȡģ��
		BillModel model = BillCentre.createBillModel(context, define);
		// ��������
		model.loadData(billID);

		// ��ѯ�󶨵��ݵĸ����б�
		List<BillEnclosure> bList = model.getBillEnclosures();
		GUID recid = null;
		if (bList != null && bList.size() > 0) {
			list.addAll(bList);
			recid = bList.get(0).getBILLID();
		}
		List<IField> fields = model.getDefine().getMasterTable().getFields();
		// guid��
		DataType guid = model.getDefine().getMasterTable().findField("RECID")
				.getField().getType();
		for (IField field : fields) {
			// ϵͳ�ֶβ���
			if (model.isSysField(field.getName())) {
				continue;
			}
			// GUID ������û�й����������Ҳ�Ϊ�գ�������Ϊ�����������ֶ�
			if (field.getField() != null
					&& field.getField().getType().equals(guid)) {
				if (field.getRelationTable() == null) {
					GUID value = model.getModelData().getMaster()
							.getValueAsGUID(field.getName());
					if (value != null) {
						// ��ѯ�󶨵�����ĳ�ֶεĸ����б�
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
