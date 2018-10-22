package com.jiuqi.gmc.mobile.approval.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiuqi.dna.bap.bill.intf.entity.BillEnclosure;
import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.spi.application.ContextSPI;
import com.jiuqi.dna.core.spi.application.Session;
import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.gmc.mobile.approval.common.AccessoriesUtils;
import com.jiuqi.sm.attachment.intf.FSmAttachment;

public class AttachmentListServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {
		String billId = req.getParameter("billId");
		String modelId = req.getParameter("modelId");
		String userName = req.getParameter("userName");
		response.setHeader("pragma", "no-cache");
		response.setHeader("cache-control", "no-no-cache");
		response.setHeader("expires", "0");

		Session session = com.jiuqi.dna.core.spi.application.AppUtil
				.getDefaultApp().getSystemSession();
		Context context = session.newContext(false);

		List<BillEnclosure> enclosures = AccessoriesUtils.getBillEnclosure(
				context, GUID.tryValueOf(modelId), GUID.tryValueOf(billId));
		String html = "";
		try {
			if (enclosures.size() > 0) {
				getHtmlBillEnclosure(enclosures, response);
			} else {
				getHtmlAttachment(
						AccessoriesUtils.getBillAccachment(context,
								GUID.valueOf(billId)), response);// lzl新附件管理
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 释放context
			if (context != null) {
				((ContextSPI) context).dispose();
			}
		}

	}

	/**
	 * 获取url
	 * 
	 * @param billId
	 *            单据ID
	 * @param accessId
	 *            附件Id
	 * @return
	 */
	private String getUrl(String accessId, String fileName, String isNew) {
		// 读取配置文件，获取IP和端口
		StringBuffer url = new StringBuffer("");

		url.append("/attachment_item_app");
		/*
		 * Content-Disposition
		 * 设置为：inline;filename=xxx.xxx。Android在下载打开文件时检测不到文件名。在这里添加文件名，就可以正确显示。
		 */
		// url.append(fileName);
		url.append("?accessId=");
		url.append(accessId);
		url.append("&isNew=");
		url.append(isNew);
		return url.toString();
	}

	/**
	 * 获取html内容
	 * 
	 * @param billId
	 * @param enclosures
	 * @return
	 */
	private void getHtmlBillEnclosure(List<BillEnclosure> enclosures,
			HttpServletResponse response) throws Exception {
		StringBuffer html = new StringBuffer();
		String name = new String();
		String url = new String();
		String houzhui = new String();
		int size;

		StringBuffer prefixBuffer = new StringBuffer("");
		prefixBuffer.append("/newweb");
		String prefix = prefixBuffer.toString();
		// 图标待修改
		String wordiconPath = prefix + "/image/fileicon/Word.png";
		String exceliconPath = prefix + "/image/fileicon/Excel.png";
		String accessoryiconPath = prefix + "/image/fileicon/accessory.png";
		String pdficonPath = prefix + "/image/fileicon/pdf.png";
		String photoiconPath = prefix + "/image/fileicon/photo.png";
		String powerpointiconPath = prefix + "/image/fileicon/PowerPoint.png";
		String txticonPath = prefix + "/image/fileicon/txt.png";
		org.json.JSONArray jsonArray = new org.json.JSONArray();

		for (BillEnclosure billEnclosure : enclosures) {
			name = billEnclosure.getENCLOSURENAME();
			url = getUrl(billEnclosure.getRECID().toString(),
					billEnclosure.getENCLOSURENAME(), "0");
			size = name.split("\\.").length;
			houzhui = name.split("\\.")[size - 1];
			houzhui = houzhui.toLowerCase();
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("name", name);
			jsonObject.put("url", url);

			if (houzhui.equals("doc") || houzhui.equals("docx"))
				jsonObject.put("icon", wordiconPath);
			else if (houzhui.equals("xls") || houzhui.equals("xlsx"))
				jsonObject.put("icon", exceliconPath);
			else if (houzhui.equals("pdf"))
				jsonObject.put("icon", pdficonPath);
			else if (houzhui.equals("jpg") || houzhui.equals("jpeg")
					|| houzhui.equals("png") || houzhui.equals("bmp"))
				jsonObject.put("icon", photoiconPath);
			else if (houzhui.equals("ppt") || houzhui.equals("pptx"))
				jsonObject.put("icon", powerpointiconPath);
			else if (houzhui.equals("txt"))
				jsonObject.put("icon", txticonPath);
			else
				jsonObject.put("icon", accessoryiconPath);

			jsonArray.put(jsonObject);
		}

		// 输出json对象
		response.setContentType("text/text;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		writer.println(jsonArray.toString());
		writer.flush();
		writer.close();
	}

	private void getHtmlAttachment(List<FSmAttachment> attachmentList,
			HttpServletResponse response) throws Exception {
		StringBuffer html = new StringBuffer();
		String name = new String();
		String url = new String();
		String houzhui = new String();
		int size;

		StringBuffer prefixBuffer = new StringBuffer("");
		prefixBuffer.append("/newweb");
		String prefix = prefixBuffer.toString();
		// 图标待修改
		String wordiconPath = prefix + "/image/fileicon/Word.png";
		String exceliconPath = prefix + "/image/fileicon/Excel.png";
		String accessoryiconPath = prefix + "/image/fileicon/accessory.png";
		String pdficonPath = prefix + "/image/fileicon/pdf.png";
		String photoiconPath = prefix + "/image/fileicon/photo.png";
		String powerpointiconPath = prefix + "/image/fileicon/PowerPoint.png";
		String txticonPath = prefix + "/image/fileicon/txt.png";
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for (FSmAttachment attachement : attachmentList) {
			name = attachement.getFileName();
			url = getUrl(attachement.getRecid().toString(),
					attachement.getFileName(), "1");
			size = name.split("\\.").length;
			houzhui = name.split("\\.")[size - 1];
			houzhui = houzhui.toLowerCase();
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("name", name);
			jsonObject.put("url", url);

			if (houzhui.equals("doc") || houzhui.equals("docx"))
				jsonObject.put("icon", wordiconPath);
			else if (houzhui.equals("xls") || houzhui.equals("xlsx"))
				jsonObject.put("icon", exceliconPath);
			else if (houzhui.equals("pdf"))
				jsonObject.put("icon", pdficonPath);
			else if (houzhui.equals("jpg") || houzhui.equals("jpeg")
					|| houzhui.equals("png") || houzhui.equals("bmp"))
				jsonObject.put("icon", photoiconPath);
			else if (houzhui.equals("ppt") || houzhui.equals("pptx"))
				jsonObject.put("icon", powerpointiconPath);
			else if (houzhui.equals("txt"))
				jsonObject.put("icon", txticonPath);
			else
				jsonObject.put("icon", accessoryiconPath);

			jsonArray.put(jsonObject);
		}

		// 输出json对象
		response.setContentType("text/text;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		writer.println(jsonArray.toString());
		writer.flush();
		writer.close();
	}
}
