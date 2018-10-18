/**============================================================
 * 版权： 久其软件 版权所有 (c) 2002 - 2016
 * 包： com.jiuqi.gmc.mobile.approval.common.service
 * 修改记录：
 * 日期                作者           内容
 * =============================================================
 * 2017年1月5日       qinjunjie        
 * ============================================================*/

package com.jiuqi.gmc.mobile.approval.common.storage;

import com.jiuqi.dna.core.def.table.TableDeclarator;
import com.jiuqi.dna.core.type.TypeFactory;
import com.jiuqi.dna.core.def.table.TableFieldDefine;
import com.jiuqi.dna.core.def.table.TableFieldDeclare;

/**
 * <p>消息推送日志表</p>
 *
 * <p>Copyright: 版权所有 (c) 2002 - 2016<br>
 * Company: 久其</p>
 *
 * @author qinjunjie
 * @version 2016年12月22日
 */
public final class TB_PUSH_MESSAGE_LOG extends TableDeclarator {

	public static final String TABLE_NAME ="PUSH_MESSAGE_LOG";

	public final TableFieldDefine f_BILLDEFINE;
	public final TableFieldDefine f_BILLDATAID;
	public final TableFieldDefine f_BILLCODE;
	public final TableFieldDefine f_WORKITEMID;
	public final TableFieldDefine f_RECEIVERS;
	public final TableFieldDefine f_MESSAGE;
	public final TableFieldDefine f_SENDTIME;
	public final TableFieldDefine f_RESULTTYPE;
	public final TableFieldDefine f_ISPUSHED;

	public static final String FN_BILLDEFINE ="BILLDEFINE";
	public static final String FN_BILLDATAID ="BILLDATAID";
	public static final String FN_BILLCODE ="BILLCODE";
	public static final String FN_WORKITEMID ="WORKITEMID";
	public static final String FN_RECEIVERS ="RECEIVERS";
	public static final String FN_MESSAGE ="MESSAGE";
	public static final String FN_SENDTIME ="SENDTIME";
	public static final String FN_RESULTTYPE ="RESULTTYPE";
	public static final String FN_ISPUSHED ="ISPUSHED";
	

	//不可调用该构造方法.当前类只能由框架实例化.
	private TB_PUSH_MESSAGE_LOG() {
		super(TABLE_NAME);
		this.table.setTitle("消息推送记录");
		this.table.setCategory("业务主体");
		TableFieldDeclare field;
		
		this.f_BILLDEFINE = field = this.table.newField(FN_BILLDEFINE, TypeFactory.GUID);
		field.setTitle("单据定义");
		
		this.f_BILLDATAID = field = this.table.newField(FN_BILLDATAID, TypeFactory.GUID);
		field.setTitle("单据ID");
		
		this.f_BILLCODE = field = this.table.newField(FN_BILLCODE, TypeFactory.VARCHAR(50));
		field.setTitle("单据编号");
		
		this.f_WORKITEMID = field = this.table.newField(FN_WORKITEMID, TypeFactory.GUID);
		field.setTitle("工作项ID");
		
		this.f_RECEIVERS = field = this.table.newField(FN_RECEIVERS, TypeFactory.BLOB);
		field.setTitle("接收者");
		
		this.f_MESSAGE = field = this.table.newField(FN_MESSAGE, TypeFactory.NVARCHAR(300));
		field.setTitle("推送消息");
		
		this.f_SENDTIME = field = this.table.newField(FN_SENDTIME, TypeFactory.DATE);
		field.setTitle("发送时间");
		
		this.f_RESULTTYPE = field = this.table.newField(FN_RESULTTYPE, TypeFactory.NVARCHAR(300));
		field.setTitle("返回类型");
		
		this.f_ISPUSHED = field = this.table.newField(FN_ISPUSHED, TypeFactory.BOOLEAN);
		field.setTitle("发送标志");
	}

}
