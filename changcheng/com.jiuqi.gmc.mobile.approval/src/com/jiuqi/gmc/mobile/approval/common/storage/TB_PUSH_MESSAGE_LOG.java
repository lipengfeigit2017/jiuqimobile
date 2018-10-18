/**============================================================
 * ��Ȩ�� ������� ��Ȩ���� (c) 2002 - 2016
 * ���� com.jiuqi.gmc.mobile.approval.common.service
 * �޸ļ�¼��
 * ����                ����           ����
 * =============================================================
 * 2017��1��5��       qinjunjie        
 * ============================================================*/

package com.jiuqi.gmc.mobile.approval.common.storage;

import com.jiuqi.dna.core.def.table.TableDeclarator;
import com.jiuqi.dna.core.type.TypeFactory;
import com.jiuqi.dna.core.def.table.TableFieldDefine;
import com.jiuqi.dna.core.def.table.TableFieldDeclare;

/**
 * <p>��Ϣ������־��</p>
 *
 * <p>Copyright: ��Ȩ���� (c) 2002 - 2016<br>
 * Company: ����</p>
 *
 * @author qinjunjie
 * @version 2016��12��22��
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
	

	//���ɵ��øù��췽��.��ǰ��ֻ���ɿ��ʵ����.
	private TB_PUSH_MESSAGE_LOG() {
		super(TABLE_NAME);
		this.table.setTitle("��Ϣ���ͼ�¼");
		this.table.setCategory("ҵ������");
		TableFieldDeclare field;
		
		this.f_BILLDEFINE = field = this.table.newField(FN_BILLDEFINE, TypeFactory.GUID);
		field.setTitle("���ݶ���");
		
		this.f_BILLDATAID = field = this.table.newField(FN_BILLDATAID, TypeFactory.GUID);
		field.setTitle("����ID");
		
		this.f_BILLCODE = field = this.table.newField(FN_BILLCODE, TypeFactory.VARCHAR(50));
		field.setTitle("���ݱ��");
		
		this.f_WORKITEMID = field = this.table.newField(FN_WORKITEMID, TypeFactory.GUID);
		field.setTitle("������ID");
		
		this.f_RECEIVERS = field = this.table.newField(FN_RECEIVERS, TypeFactory.BLOB);
		field.setTitle("������");
		
		this.f_MESSAGE = field = this.table.newField(FN_MESSAGE, TypeFactory.NVARCHAR(300));
		field.setTitle("������Ϣ");
		
		this.f_SENDTIME = field = this.table.newField(FN_SENDTIME, TypeFactory.DATE);
		field.setTitle("����ʱ��");
		
		this.f_RESULTTYPE = field = this.table.newField(FN_RESULTTYPE, TypeFactory.NVARCHAR(300));
		field.setTitle("��������");
		
		this.f_ISPUSHED = field = this.table.newField(FN_ISPUSHED, TypeFactory.BOOLEAN);
		field.setTitle("���ͱ�־");
	}

}
