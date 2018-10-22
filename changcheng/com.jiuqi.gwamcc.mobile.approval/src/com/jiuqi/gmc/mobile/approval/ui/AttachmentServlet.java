package com.jiuqi.gmc.mobile.approval.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiuqi.dna.bap.bill.intf.entity.BillEnclosure;
import com.jiuqi.dna.bcp.attachment.config.core.FAttachmentConfig;
import com.jiuqi.dna.bcp.attachment.config.core.FAttachmentFile;
import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.sm.attachment.intf.FSmAttachment;
import com.jiuqi.vacomm.utils.document.DocumentConvertFactory;
import com.jiuqi.vacomm.utils.document.IllegalDocumentTypeException;
//import com.jiuqi.vacomm.utils.document.DocumentConvertFactory;
//import com.jiuqi.vacomm.utils.document.IllegalDocumentTypeException;
import com.jiuqi.vacomm.utils.sys.BizHttpServlet;

/**
 * �贫�����accessId����ID��isNew�Ƿ����¸�������
 * 
 * @author lizelong
 * 
 */
public class AttachmentServlet extends BizHttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doService(Context context, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		InputStream input = null;
		OutputStream out = null;
		try {
			response.setHeader("pragma", "no-cache");
			response.setHeader("cache-control", "no-no-cache");
			response.setHeader("expires", "0");

			// ��ȡ����ID�͸���Id
			String accessId = request.getParameter("accessId");
			String isNew = request.getParameter("isNew");// �Ƿ��¸�������0 ���ǣ�1 ��
			String fileName = "";
			String suffix = "";
			byte[] data = null;
			if (isNew.equals("0")) {
				BillEnclosure billEnclosure = context.find(BillEnclosure.class,
						GUID.tryValueOf(accessId));
				// �������ƺ͸�������
				fileName = billEnclosure.getENCLOSURENAME();
				data = billEnclosure.getEnclosureData(context);
				input = new ByteArrayInputStream(data);
			} else {
				FAttachmentConfig config = context
						.find(FAttachmentConfig.class);
				FSmAttachment attachment = context.find(FSmAttachment.class,
						GUID.valueOf(accessId));
				if (attachment != null) {
					FAttachmentFile attachmentfile = attachment
							.getAttachmentFile(context, config);
					fileName = attachment.getFileName();
					data = attachmentfile.getFileData();
					input = new ByteArrayInputStream(data);
				} else
					return;
			}
			out = response.getOutputStream();
			// String userAgent=request.getHeader("USER-AGENT");
			response.setContentType("text/html;charset=gbk");
			int size = 0;
			size = fileName.split("\\.").length;
			suffix = fileName.split("\\.")[size - 1];
			suffix = suffix.toLowerCase();
			// ���ݺ�׺�жϴ������
			if (!suffix.equals("txt")) {
				// ��ȡ���ݵĲ���num��word��num�����ĵ���ͼƬID,pdf��num����ҳ��ID,excel��num����sheet��ID
				String num = request.getParameter("num");
				String baseurl = request.getServletPath() + "?accessId="
						+ accessId + "&isNew=" + isNew;
				String queryString = request.getQueryString();
				try {
					DocumentConvertFactory.create(fileName).convert(input, out,
							baseurl, queryString);
				} catch (IllegalDocumentTypeException e) {
					e.printStackTrace();
				}
			} else {
				out.write(data, 0, data.length);
				out.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// �ر������
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
