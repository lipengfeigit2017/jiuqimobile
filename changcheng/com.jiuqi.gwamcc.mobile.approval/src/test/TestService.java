package test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiuqi.dna.core.http.DNAHttpServlet;
import com.jiuqi.dna.core.spi.application.ContextSPI;
import com.jiuqi.dna.core.spi.application.Session;
import com.jiuqi.dna.ui.wt.InfomationException;
import com.jiuqi.xlib.json.JSONException;
import com.jiuqi.xlib.json.JSONObject;

@SuppressWarnings("restriction")
public class TestService extends DNAHttpServlet implements IRequest1Service {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String CODE="code";
	private static final String MESSAGE="message";
	
	@Override
	public String getName() {
		return null;
	}
	@Override
	public JSON1Response doService(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject req= CommonUtil.acceptJSON(request);
		JSONObject params = new JSONObject();
		try {
			params = req.getJSONObject("params");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		JSON1Response o = new JSON1Response(request,response);
		Session session = getApplication().newSession(null, null);		
		try{
			session.setHeartbeatTimeoutSec(300);
			session.setSessionTimeoutMinutes(0);
			ContextSPI context = session.newContext(true);
			try{
				o = getToDoListSize(context, request, response, params);
			}catch(Exception e){
				try {
					o.put(CODE, "-1");
					if (e.getCause() instanceof InfomationException) {
						o.put(MESSAGE, e.getCause().getMessage());
					}else{
						o.put(MESSAGE, "����������������ϵ����Ա");
					}
				} catch (JSONException e1) {
					
				}
				context.resolveTrans();
			}
			finally{
				context.dispose();
			}			
		}finally{
			session.dispose(0L);
		}
		return o;
	}
	
	/**
	 * ��ȡ���д��죨�ƶ��ˣ�
	 * @param context
	 * @param request
	 * @param response
	 * @param params
	 * @return
	 */
	private JSON1Response getToDoListSize(ContextSPI context,
			HttpServletRequest request, HttpServletResponse response,
			JSONObject params) {
		
		//�������
		//��Ӧ���
		JSON1Response json = new JSON1Response(request,response);
		try {
			String approvalSuggest = params.getString("approvalsuggest");

					 json.put(CODE, "-1");
					 json.put(MESSAGE, "�����ѱ�ȡ�أ�����������");
			//TODO:��Ϣ����
		} catch (Exception e) {
			try {
				json.put(CODE, "-1");
				json.put(MESSAGE, "����������������ϵ����Ա");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			context.abort();
		}
		return json;
		
	}

	
}
