/**============================================================
 * ��Ȩ�� ������� ��Ȩ���� (c) 2002 - 2016
 * ���� com.jiuqi.fo.youse.ui.plantask
 * �޸ļ�¼��
 * ����                ����           ����
 * =============================================================
 * 2017��1��6��       qinjunjie        
 * ============================================================*/

package com.jiuqi.gmc.mobile.approval.intf.plantask;

import com.jiuqi.dna.bap.plantask.intf.runner.Runner;
import com.jiuqi.dna.bap.plantask.intf.runner.RunnerFactory;

/**
 * <p>��������ʧ�ܺ��������ͼƻ�����</p>
 *
 * <p>Copyright: ��Ȩ���� (c) 2002 - 2017<br>
 * Company: ����</p>
 *
 * @author qinjunjie
 * @version 2017��01��05��21:56:39
 */

public class JPushRunnerFactory extends RunnerFactory{

	private static final String RUNNER_GUID = "EB190E13920C440AB5204988F3AA5356";
	
	@Override
	public String getGuid(){
		return RUNNER_GUID;
	}

	@Override
	public String getTitle(){
		return "�������ͼƻ�����";
	}

	@Override
	public String getDescription(){
		return "�������ʹ���ʧ��ʱ��ͨ���˼ƻ������ٴ����ʹ�����Ϣ";
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
