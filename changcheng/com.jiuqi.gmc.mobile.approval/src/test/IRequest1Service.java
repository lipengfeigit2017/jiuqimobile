package test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IRequest1Service {
	public String getName();
	//public void doService(HttpServletRequest request,HttpServletResponse response,ISession session);
	//public JSONObject doService(HttpServletRequest request,HttpServletResponse response,IChannel channel);
	//public void doService();
	public JSON1Response doService(HttpServletRequest request,HttpServletResponse response);
}
