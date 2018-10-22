/**
 * 
 */
package com.jiuqi.gmc.mobile.approval.ui;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

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
public class FlowListServlet extends BizHttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3003986887889608703L;
	public String uname;

	protected void doService(Context arg0, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		response.setHeader("pragma", "no-cache");
		response.setHeader("cache-control", "no-no-cache");
		response.setHeader("expires", "0");
		try {
			response.setContentType("text/text;charset=UTF-8");
			PrintWriter writer = response.getWriter();

			JSONObject json = new JSONObject();
			json.put("name", "ÖÐ¹ú");
			json.put("num", "12");

			response.sendRedirect("newweb/app/index.html");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void getTest(Context arg0, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");

	}

}
