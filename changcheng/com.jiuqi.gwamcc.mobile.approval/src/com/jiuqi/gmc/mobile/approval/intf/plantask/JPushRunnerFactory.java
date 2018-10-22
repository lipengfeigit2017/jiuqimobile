/**============================================================
 * 版权： 久其软件 版权所有 (c) 2002 - 2016
 * 包： com.jiuqi.fo.youse.ui.plantask
 * 修改记录：
 * 日期                作者           内容
 * =============================================================
 * 2017年1月6日       qinjunjie        
 * ============================================================*/

package com.jiuqi.gmc.mobile.approval.intf.plantask;

import com.jiuqi.dna.bap.plantask.intf.runner.Runner;
import com.jiuqi.dna.bap.plantask.intf.runner.RunnerFactory;

/**
 * <p>极光推送失败后重新推送计划任务</p>
 *
 * <p>Copyright: 版权所有 (c) 2002 - 2017<br>
 * Company: 久其</p>
 *
 * @author qinjunjie
 * @version 2017年01月05日21:56:39
 */

public class JPushRunnerFactory extends RunnerFactory{

	private static final String RUNNER_GUID = "EB190E13920C440AB5204988F3AA5356";
	
	@Override
	public String getGuid(){
		return RUNNER_GUID;
	}

	@Override
	public String getTitle(){
		return "极光推送计划任务";
	}

	@Override
	public String getDescription(){
		return "极光推送待办失败时，通过此计划任务再次推送待办信息";
	}

	@Override
	public boolean isNewRunner() {
		return true;
	}
	
	@Override
	public Runner createRunner(){
		return new JPushRunner();
	}

}
