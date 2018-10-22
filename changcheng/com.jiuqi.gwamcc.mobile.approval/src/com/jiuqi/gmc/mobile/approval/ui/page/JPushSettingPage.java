package com.jiuqi.gmc.mobile.approval.ui.page;

import java.util.regex.Pattern;

import com.jiuqi.dna.bap.systemoptions.intf.message.MB0607_SaveSystemOptionsItemsMessage;
import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.situation.MessageListener;
import com.jiuqi.dna.core.situation.MessageTransmitter;
import com.jiuqi.dna.core.situation.Situation;
import com.jiuqi.dna.ui.wt.widgets.Composite;
import com.jiuqi.dna.ui.wt.widgets.Form;
import com.jiuqi.vacomm.utils.ConfigUtil;
import com.jiuqi.vacomm.utils.ConfigUtil.SaveType;
import com.jiuqi.vacomm.utils.StringUtil;
import com.jiuqi.vacomm.utils.ui.WindowUtil;

public class JPushSettingPage<TControls extends JPushSettingPageControls> extends Form<JPushSettingPageControls> {

	/**
	 * 是否启用极光推送
	 */
	public static final String IS_PUSH_MSG="IS_PUSH_MSG";
	public static final String APP_KEY="APP_KEY";
	public static final String MASTER_SECRET="MASTER_SECRET";
	private static final Pattern PUSH_PATTERNS = Pattern.compile("[^a-zA-Z0-9]");
	
	public JPushSettingPage(Composite parent) {
		super(parent);
		initConfigData();
		initListener();
	}
	
	private void initConfigData(){
		controls.chk_push.setSelection(ConfigUtil.getSysBoolValue(getContext(), IS_PUSH_MSG));
		controls.txt_app_key.setText(ConfigUtil.getString(getContext(),APP_KEY, SaveType.SYS));
		controls.txt_master_key.setText(ConfigUtil.getString(getContext(),MASTER_SECRET, SaveType.SYS));
	}
	
	private void initListener(){
		getContext().regMessageListener(
				MB0607_SaveSystemOptionsItemsMessage.class,
				new MessageListener<MB0607_SaveSystemOptionsItemsMessage>() {

					@Override
					public void onMessage(
							Situation context,
							MB0607_SaveSystemOptionsItemsMessage message,
							MessageTransmitter<MB0607_SaveSystemOptionsItemsMessage> transmitter) {
						String appKey = controls.txt_app_key.getText();
						String masterSecret = controls.txt_master_key.getText();
						if ((StringUtil.isEmpty(appKey)) || (StringUtil.isEmpty(masterSecret))) {
							WindowUtil.showMessage("App Key和Master Secret都不能为空");
							return;
						}
						if ((appKey.length() == 24) && (masterSecret.length() == 24) && (!(PUSH_PATTERNS.matcher(appKey).find()))
								&& (!(PUSH_PATTERNS.matcher(masterSecret).find()))){
							ConfigUtil.saveSysSystemOption((Context)context, IS_PUSH_MSG, controls.chk_push.getSelection());
							ConfigUtil.saveString(context, APP_KEY, SaveType.SYS, controls.txt_app_key.getText());
							ConfigUtil.saveString(context, MASTER_SECRET, SaveType.SYS, controls.txt_master_key.getText());
							WindowUtil.showMessage("保存成功!");
						}else{
							WindowUtil.showMessage("App key和Master Secret格式错误");
						}
						
					}
					
				});
	}
	
}
