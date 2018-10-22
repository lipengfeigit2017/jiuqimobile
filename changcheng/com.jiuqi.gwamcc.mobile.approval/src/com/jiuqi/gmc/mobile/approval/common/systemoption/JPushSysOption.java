/**============================================================
 * ��Ȩ�� ������� ��Ȩ���� (c) 2002 - 2016
 * ���� com.jiuqi.gmc.mobile.approval.common.systemoption
 * �޸ļ�¼��
 * ����                ����           ����
 * =============================================================
 * 2016��12��22��       qinjunjie        
 * ============================================================*/

package com.jiuqi.gmc.mobile.approval.common.systemoption;

import java.util.ArrayList;
import java.util.List;

import com.jiuqi.dna.bap.systemoptions.common.define.Item;
import com.jiuqi.dna.bap.systemoptions.common.define.SystemOptionDefine;
import com.jiuqi.dna.bap.systemoptions.common.type.BoolType;
import com.jiuqi.dna.bap.systemoptions.common.type.StringType;
import com.jiuqi.dna.bap.systemoptions.intf.facade.SystemOptionsItemsEntry.StorageType;

/**
 * <p>��������ϵͳѡ��</p>
 *
 * <p>Copyright: ��Ȩ���� (c) 2002 - 2016<br>
 * Company: ����</p>
 *
 * @author qinjunjie
 * @version 2016��12��22��
 */

@SuppressWarnings("restriction")
public class JPushSysOption implements SystemOptionDefine {
    
	/**
	 * �Ƿ����ü�������
	 */
	public static final String IS_PUSH_MSG="IS_PUSH_MSG";
	public static final String APP_KEY="APP_KEY";
	public static final String MASTER_SECRET="MASTER_SECRET";
	
    @Override
    public String getkey() {
        return "JPushSysOption";
    }

    @Override
    public String getName() {
        return "��������ϵͳѡ��";
    }

    @Override
    public String getGroupName() {
        return "�ⲿϵͳ";
    }

    @Override
    public String getPageName() {
        return "JPushSettingPage";
    }

    @Override
    public Boolean isLinkItems() {
        return false;
    }

	@Override
    public List<Item> getItems() {
		return null;
    }
    
}