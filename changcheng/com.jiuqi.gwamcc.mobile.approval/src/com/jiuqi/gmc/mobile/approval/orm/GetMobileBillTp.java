package com.jiuqi.gmc.mobile.approval.orm;

import com.jiuqi.dna.core.ObjectQuerier;
import com.jiuqi.dna.core.def.query.QueryColumnDefine;
import com.jiuqi.dna.core.def.query.ORMDeclarator;

public class GetMobileBillTp extends ORMDeclarator<com.jiuqi.mt2.dna.mobile.bill.intf.impl.BillStyleTemplateImpl> {

	public final QueryColumnDefine c_id;
	public final QueryColumnDefine c_code;
	public final QueryColumnDefine c_title;
	public final QueryColumnDefine c_describe;
	public final QueryColumnDefine c_groupid;
	public final QueryColumnDefine c_orderId;
	public final QueryColumnDefine c_contentinfoBytes;
	public final QueryColumnDefine c_mType;
	public final QueryColumnDefine c_updateTime;

	public GetMobileBillTp() {
		this.c_id = this.orm.getColumns().get(0);
		this.c_code = this.orm.getColumns().get(1);
		this.c_title = this.orm.getColumns().get(2);
		this.c_describe = this.orm.getColumns().get(3);
		this.c_groupid = this.orm.getColumns().get(4);
		this.c_orderId = this.orm.getColumns().get(5);
		this.c_contentinfoBytes = this.orm.getColumns().get(6);
		this.c_mType = this.orm.getColumns().get(7);
		this.c_updateTime = this.orm.getColumns().get(8);
	}
}
