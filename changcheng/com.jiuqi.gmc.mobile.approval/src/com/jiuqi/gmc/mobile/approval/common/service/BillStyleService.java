package com.jiuqi.gmc.mobile.approval.common.service;

import java.util.List;

import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.da.ORMAccessor;
import com.jiuqi.dna.core.service.Publish;
import com.jiuqi.dna.core.service.Service;
import com.jiuqi.gmc.mobile.approval.key.BillStyleKey;
import com.jiuqi.gmc.mobile.approval.orm.GetMobileBillTp;
import com.jiuqi.mt2.dna.mobile.bill.intf.facade.FBillStyleTemplate;
import com.jiuqi.mt2.dna.mobile.bill.intf.impl.BillStyleTemplateImpl;
import com.jiuqi.mt2.spi.bill.metadata.MobileBillDefine;
import com.jiuqi.mt2.spi.log.MobileLog;
import com.jiuqi.xlib.json.JSONObject;
/**
 * 单据样式模板服务
 * 复写BillStyleTemplateService优化获取移动终端配置信息效率
 * <p>Copyright: 版权所有 (c) JOIN-CHEER</p>
 *
 * @author lipengfei
 * @version 2017年11月14日
 */
public class BillStyleService extends Service {

	private GetMobileBillTp getMobileBillTp;
	
	protected BillStyleService(GetMobileBillTp getMobileBillTp) {
		super("BillStyleService");
		this.getMobileBillTp = getMobileBillTp;
	}
	
	@Publish
	public class GetStyleProvider extends OneKeyResultProvider<FBillStyleTemplate,BillStyleKey> {
		
		@Override
		protected FBillStyleTemplate provide(Context context, BillStyleKey key)
				throws Throwable {
			ORMAccessor<BillStyleTemplateImpl> orm = context.newORMAccessor(getMobileBillTp);
			List<BillStyleTemplateImpl> templateList = null;
			try {
				templateList = orm.fetch(new Object[0]);
				for (BillStyleTemplateImpl template : templateList) {
					if (template.getContentinfoBytes() != null) {
						MobileBillDefine mbd = new MobileBillDefine();
						String info = new String(template.getContentinfoBytes());
						JSONObject json = new JSONObject(info);
						if(json.optString("temp_id").equals(key.getBillDefineID())){
							mbd.deSerialise(new String(template
									.getContentinfoBytes()));
							template.setMobileBillDefine(mbd);
							return template ;							
						}else{
							continue;
						}
					}
				}
			} catch (Exception e) {
				MobileLog.logError(e);
			} finally {
				if (orm != null) {
					orm.unuse();
				}
			}

			return null;
		}

	}
}
