/**
 * 
 */
package com.jiuqi.gmc.mobile.approval.common;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.User;
import com.jiuqi.dna.core.misc.MissingObjectException;
import com.jiuqi.dna.core.spi.application.Application;
import com.jiuqi.dna.core.spi.application.ContextSPI;
import com.jiuqi.dna.core.spi.application.Session;

/**
 * @author
 * 
 */

@SuppressWarnings("serial")
public abstract class DNAHttpServlet extends HttpServlet {
	private Application app;

	/**
	 * ���DNAӦ��
	 * 
	 * @return
	 */
	public Application getApplication() {
		Application app = this.app;
		if (app == null) {
			this.app = app = (Application) this.getServletContext()
					.getAttribute(Application.servlet_context_attr_application);
			if (app == null) {
				throw new MissingObjectException("δ�ҵ�DNA-Application");
			}
		}
		return app;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doWork(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doWork(req, resp);
	}

	/**
	 * ��ȡ�û�
	 * 
	 * @param req
	 * @return
	 */
	protected abstract String getUserName(HttpServletRequest req);

	/**
	 * ִ�в���
	 * 
	 * @param req
	 * @param resp
	 * @param context
	 *            ָ���û������Ļ���
	 */
	protected abstract void doService(HttpServletRequest req,
			HttpServletResponse resp, Context context) throws ServletException,
			IOException;

	/**
	 * ��֤��¼����
	 * 
	 * @param req
	 * @param resp
	 */
	protected abstract boolean check(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException;

	private void doWork(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (!check(req, resp)) {
			return;
		}
		ContextSPI context = null;
		Application application = getApplication();
		Session session = application.newSession(null, null);
		try {
			context = session.newContext(true);
			try {
				User user = getUser(req, context);
				if (user != null) {
					context.changeLoginUser(user);
				}
				doService(req, resp, context);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				context.dispose();
			}
		} finally {
			session.dispose(0);
		}
	}

	private User getUser(HttpServletRequest req, Context context) {
		String userName = getUserName(req);
		if (userName == null) {
			return null;
		}
		return context.find(User.class, getUserName(req));
	}

}
