package com.jiuqi.gmc.mobile.approval.common;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.jiuqi.dna.bap.authority.intf.facade.FUser;
import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.ui.portal.template.def.LoginUtils;
import com.jiuqi.fo.workflow.common.util.WorkflowCenter;
import com.jiuqi.mt2.dna.mobile.portal.intf.facade.FMPortalInfo;
import com.jiuqi.vacomm.utils.sys.BizHttpServlet;

public class AppLoginServlet extends BizHttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5849725157308148315L;

	@Override
	protected void doService(Context context, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String action = (String) request.getParameter("action");
		if ("login".equals(action)) {
			checkValid(context, request, response);
		}
		if ("logout".equals(action)) {
			logout(context, request, response);
		}
		if ("sso".equals(action)) {
			checkSSO(context, request, response);
		}
		if (("checksession").equals(action)) {
			checkSession(context, request, response);
		}
		if (("send").equals(action)) {
			sendValidCode(context, request, response);
		}
		if (("checkAddress").equals(action)) {
			checkAddress(context, request, response);
		}

	}

	/**
	 * 校验服务器地址
	 */
	private void checkAddress(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		json.append("msg", "success");
		response.setContentType("text/text;charset=GBK");
		try {
			response.getWriter().println(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendValidCode(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		String name = request.getParameter("username");
		String pwd = request.getParameter("password");
		JSONObject json = new JSONObject();
		FUser fUser = context.find(FUser.class, name.toUpperCase());

		try {
			if (fUser == null) {
				json.put("msg", "此用户不存在！");
			} else {
				if (fUser.isLocked()) {
					json.put("msg", "此用户已停用！");
				} else {
					boolean valid = fUser.checkPassword(context, pwd);
					if (!valid) {
						json.put("msg", "用户名或密码有误！");
					} else {
						WorkflowCenter.send(context, 60, fUser.getGuid());
						json.put("msg", "success");
					}

				}
			}
		} catch (Exception e) {
			json.put("msg", "发送验证码失败！");
		}
		try {
			response.setContentType("text/text;charset=GBK");
			response.getWriter().println(json);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void checkSession(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		if ((String) request.getSession().getAttribute("user") == null) {
			json.put("msg", "0");
		}
		try {
			response.setContentType("text/text;charset=GBK");
			response.getWriter().println(json);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void checkSSO(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		String name = request.getParameter("username");
		JSONObject json = new JSONObject();
		FUser fUser = context.find(FUser.class, name.toUpperCase());
		if (fUser == null) {
			json.put("msg", "此用户不存在！");
		} else {
			if (fUser.isLocked()) {
				json.put("msg", "此用户已停用！");
			} else {
				json.put("msg", "success");
				json.put("title", fUser.getTitle());
				request.getSession().setAttribute("org",
						fUser.getBelongedUnit());
				request.getSession().setAttribute("user", name);
			}
		}
		try {
			response.setContentType("text/text;charset=GBK");
			response.getWriter().println(json);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void logout(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		request.getSession().removeAttribute("org");
		request.getSession().removeAttribute("user");
		JSONObject json = new JSONObject();
		json.put("msg", "success");
		response.setContentType("text/text;charset=GBK");
		try {
			response.getWriter().println(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void checkValid(Context context, HttpServletRequest request,
			HttpServletResponse response) {
		String name = request.getParameter("username");
		String pwd = request.getParameter("password");
		String validCode = request.getParameter("validcode");
		JSONObject json = new JSONObject();
		// User user = context.find(User.class, name);
		// if(user == null) {
		// json.put("msg", "此用户不存在！");
		// }
		// boolean valid = user.validatePassword(pwd);
		// if(!valid) {
		// json.put("msg", "密码错误！");
		// }
		boolean valid = false;
		FUser fUser = context.find(FUser.class, name.toUpperCase());
		if (fUser != null)
			valid = fUser.checkPassword(context, pwd);
		try {
			if (valid) {
				if (fUser.isLocked()) {
					json.put("msg", "该用户已停用！");
				} else {
					boolean flag = true;
					FMPortalInfo info = context.get(FMPortalInfo.class);
					// 判断是否启用验证码
					if (info.isEnableMessageCheck()) {
						flag = LoginUtils.checkValidateCode(context, validCode,
								60);
					}
					if (flag) {
						json.put("msg", "success");
						json.put("title", fUser.getTitle());
						//add by txj
						json.put("userid", fUser.getGuid());
						json.put("org", fUser.getBelongedUnit());
						request.getSession().setAttribute("org",
								fUser.getBelongedUnit());
						request.getSession().setAttribute("user", name);
					} else {
						json.put("msg", "验证码校验失败！");
					}
				}

			} else {
				json.put("msg", "用户名或密码有误！");
			}
			response.setContentType("text/text;charset=GBK");
			response.getWriter().println(json);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
