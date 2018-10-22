/**============================================================
 * ��Ȩ�� ������� ��Ȩ���� (c) 2002 - 2016
 * ���� com.jiuqi.gmc.mobile.approval.common.command
 * �޸ļ�¼��
 * ����                ����           ����
 * =============================================================
 * 2016��12��22��       qinjunjie        
 * ============================================================*/

package com.jiuqi.gmc.mobile.approval.common.command;

import com.jiuqi.dna.core.service.AsyncInfo;
import com.jiuqi.dna.core.service.AsyncInfo.SessionMode;
import com.jiuqi.fo.common.comm.FoCommand;
import com.jiuqi.gmc.mobile.approval.intf.task.PushMessageTask;

/**
 * <p>
 * ��������Command��ʹ���첽��ʽ����
 * </p>
 * <p>Copyright: ��Ȩ���� (c) 2002 - 2016<br>
 * Company: ����</p>
 *
 * @author qinjunjie
 * @version 2016��12��22��
 */

public class JPushCommand extends FoCommand{
	
	private PushMessageTask task;
	
	public JPushCommand(PushMessageTask task){
		this.task = task;
	}
	
	@Override
	public void execute(){
		AsyncInfo async = new AsyncInfo();
		async.setSessionMode(SessionMode.INDIVIDUAL_ANONYMOUS);
		getCtx().asyncHandle(task, PushMessageTask.Method.ADD, async);
	}

}