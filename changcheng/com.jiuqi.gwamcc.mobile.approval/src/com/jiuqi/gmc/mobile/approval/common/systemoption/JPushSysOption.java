/**============================================================
 * 版权： 久其软件 版权所有 (c) 2002 - 2016
 * 包： com.jiuqi.gmc.mobile.approval.common.systemoption
 * 修改记录：
 * 日期                作者           内容
 * =============================================================
 * 2016年12月22日       qinjunjie        
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
 * <p>极光推送系统选项</p>
 *
 * <p>Copyright: 版权所有 (c) 2002 - 2016<br>
 * Company: 久其</p>
 *
 * @author qinjunjie
 * @version 2016年12月22日
 */

@SuppressWarnings("restriction")
public class JPushSysOption implements SystemOptionDefine {
    
	/**
	 * 是否启用极光推送
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
        return "极光推送系统选项";
    }

    @Override
    public String getGroupName() {
        return "外部系统";
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