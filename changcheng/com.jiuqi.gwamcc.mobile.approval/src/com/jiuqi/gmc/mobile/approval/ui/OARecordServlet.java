/**
 * 
 */
package com.jiuqi.gmc.mobile.approval.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.jiuqi.dna.core.da.RecordSet;
import com.jiuqi.dna.core.def.query.QueryStatementDeclare;
import com.jiuqi.dna.core.http.DNAHttpServlet;
import com.jiuqi.dna.core.spi.application.AppUtil;
import com.jiuqi.dna.core.spi.application.ContextSPI;
import com.jiuqi.dna.core.spi.application.Session;
import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.dna.ui.launch.internal.SessionIniterImpl;
import com.jiuqi.dna.ui.launch.internal.channels.http.SessionInitProxyImpl;
/**
 * ´ý°ìÍ¬²½
 * @author liuyanhui
 */
@SuppressWarnings("restriction")
public class OARecordServlet extends DNAHttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doService(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doService(request, response);
	}

	@SuppressWarnings("unchecked")
	private void doService(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		Session session = AppUtil.getDefaultApp().newSession(SessionIniterImpl.INSTANCE,
				new SessionInitProxyImpl(request));
		int count = 0;
		String userId = (String)request.getParameter("userid");
		try{
			ContextSPI context = session.newContext(false);
			StringBuffer dnaSql = new StringBuffer();
			dnaSql.append(" define query bt_datastatelog()\n");
			dnaSql.append(" begin \n");
			dnaSql.append(" select \n");
			dnaSql.append(" i.STATE as piStateField, \n");
			dnaSql.append(" p.NIGUID as NIGUID, \n").append(" n.piguid  as piguid \n");
			dnaSql.append(" from participant as p \n");
			dnaSql.append(" left join NODEINSTANCE as n  on n.recid = p.NIGUID \n");
			dnaSql.append(" left join ProcessInstance as i on i.recid = n.piguid \n");
			dnaSql.append(" where 1=1 and i.STATE = 3 and p.ACTION = 0 \n").append(" and p.WORKCATEGORY is not null \n");
			dnaSql.append(" and p.WORKCATEGORY <> guid'00000000000000000000000000000000'\n");
			dnaSql.append(" and (p.USERID = guid'").append(userId).append("' or p.commission = guid'").append(userId).append("')\n");
			dnaSql.append(" end");
			RecordSet rs = context.openQuery((QueryStatementDeclare) context.parseStatement(dnaSql.toString()));
			while (rs.next()){
				 count = rs.getRecordCount();
			}
			response.getOutputStream().write((""+count).getBytes("UTF-8"));  
			response.setContentType("text/json; charset=UTF-8");
			
		}catch (Exception e) {
			e.printStackTrace();
			response.getOutputStream().write(("Failed:"+e.getMessage()).getBytes("UTF-8"));  
			response.setContentType("text/json; charset=UTF-8");
		}
		
	}

}
