package test;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiuqi.xlib.json.JSONException;
import com.jiuqi.xlib.json.JSONObject;

//import definition.interfaces.ICQ_Event;

public class GMT1Servlet extends HttpServlet{
	
	protected void doService(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE"); 
		resp.setHeader("Access-Control-Max-Age", "3600"); 
		resp.setHeader("Access-Control-Allow-Headers", "access-control-allow-methods,access-control-allow-origin,token,DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,serviceName,GMT-Token");

		resp.setStatus(200);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
		doPost(req,rep);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
		
		String serID = req.getHeader("serviceName");
		if(serID==null) {
			serID = req.getParameter("serviceName");
		}

		String token = req.getHeader("GMT-Token");
		if(token==null) {
			token = req.getParameter("GMT-Token");
		}


		/*for (final IRequestService RS : RequestServicesGather.getServiceList()) {
			RS.doService(req, rep, channel.getDnaSession());
		}*/
		Map<String,IRequest1Service> rsMap = Request1ServicesGather.getServicesMap();
		IRequest1Service rs = rsMap.get("testService");
//		JSONObject result = new JSONObject();
		JSON1Response jresponse = new JSON1Response(req,rep);
		jresponse = rs.doService(req, rep);
		
		
		try {
			jresponse.commit();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//if match 
		////获取session    channnel 
		
		//根据 end
		
	}
}