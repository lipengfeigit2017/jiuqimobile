/**
 * 
 */
package com.jiuqi.gmc.mobile.approval.ui;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jiuqi.dna.bap.authority.intf.util.AuthorityUtils;
import com.jiuqi.dna.core.Context;
import com.jiuqi.vacomm.utils.sys.BizHttpServlet;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Join-Cheer Corporation Copyright (c) 2014
 * </p>
 * <p>
 * Company: BEIJING JOIN-CHEER SOFTWARE CO.,LTD
 * </p>
 * 
 * @author handengke
 * @version 1.0
 */
public class ToDoListTest extends BizHttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3003986887889608703L;
	public String uname;

	@Override
	protected void doService(Context context, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		if ("login".equals(action)) {
			checkValid(context, request, response);
		}
		if ("flowType".equals(action)) {
			getFlowType(context, request, response);
		}
		if ("flowItems".equals(action)) {
			getFlowItems(context, request, response);
		}
	}

	private void getFlowItems(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub

	}

	private void getFlowType(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			response.setContentType("text/text;charset=UTF-8");
			PrintWriter writer = response.getWriter();

			JSONObject json = new JSONObject();
			json.put("name", "申请单待审批");
			json.put("num", "12");

			JSONObject json1 = new JSONObject();
			json1.put("name", "报销单待审批");
			json1.put("num", "123");

			JSONObject json2 = new JSONObject();
			json2.put("name", "动支单待审批");
			json2.put("num", "125");

			JSONArray jsonArray = new JSONArray();
			jsonArray.put(json);
			jsonArray.put(json1);
			jsonArray.put(json2);

			writer.println(jsonArray);
			// writer.print(uname);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void checkValid(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		String name = request.getParameter("username");
		String pwd = request.getParameter("pwd");
		JSONObject json = new JSONObject();
		// User user = context.find(User.class, name);
		// if(user == null) {
		// json.put("msg", "此用户不存在！");
		// }
		// boolean valid = user.validatePassword(pwd);
		// if(!valid) {
		// json.put("msg", "密码错误！");
		// }
		boolean valid = AuthorityUtils.checkFUserPassword(context, name, pwd);
		try {
			if (valid) {
				json.put("msg", "success");
			} else {
				json.put("msg", "用户名或密码有误！");
			}
			response.setContentType("text/text;charset=GBK");
			response.getWriter().println(json);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
