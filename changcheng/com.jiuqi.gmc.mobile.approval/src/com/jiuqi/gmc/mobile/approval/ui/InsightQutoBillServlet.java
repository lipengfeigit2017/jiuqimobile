package com.jiuqi.gmc.mobile.approval.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jiuqi.budget.common.utils.BudgetUtils;
import com.jiuqi.dna.bap.basedata.common.util.BaseDataCenter;
import com.jiuqi.dna.bap.basedata.intf.facade.FBaseDataObject;
import com.jiuqi.dna.bap.bill.common.model.BillCentre;
import com.jiuqi.dna.bap.bill.common.model.BillModel;
import com.jiuqi.dna.bap.bill.intf.entity.BillEnclosure;
import com.jiuqi.dna.bap.bill.intf.facade.model.FBillDefine;
import com.jiuqi.dna.bap.model.common.define.base.BusinessObject;
import com.jiuqi.dna.bap.model.common.define.base.Field;
import com.jiuqi.dna.bap.model.common.define.base.Table;
import com.jiuqi.dna.bap.model.common.type.FieldType;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.entity.TaskListDefine;
import com.jiuqi.dna.bap.workflowmanager.execute.intf.facade.FTaskListDefine;
import com.jiuqi.dna.core.Context;
import com.jiuqi.dna.core.User;
import com.jiuqi.dna.core.def.table.TableDefine;
import com.jiuqi.dna.core.spi.application.ContextSPI;
import com.jiuqi.dna.core.type.GUID;
import com.jiuqi.gmc.mobile.approval.common.AccessoriesUtils;
import com.jiuqi.mt2.dna.mobile.bill.intf.facade.FBillStyleTemplate;
import com.jiuqi.mt2.dna.mobile.todo.facade.FApprovalDefine;
import com.jiuqi.mt2.spi.bill.metadata.MobileBillDefine;
import com.jiuqi.mt2.spi.bill.metadata.PageDefine;
import com.jiuqi.mt2.spi.common2.table.IMShowTemplate;
import com.jiuqi.mt2.spi.common2.table.IMTableCell;
import com.jiuqi.mt2.spi.common2.table.IMTableRow;
import com.jiuqi.sm.attachment.intf.FSmAttachment;
import com.jiuqi.vacomm.model.common.BillDataModel;
import com.jiuqi.vacomm.utils.sys.BizHttpServlet;

/**
 * 透视引用费控单据servlet
 * 
 * @Title: InsightQutoBillServlet.java
 * @Package com.jiuqi.fo.mobile.approval.ui
 * @Description: TODO
 * @author lizelong
 * @date 2016年4月5日 下午9:30:37
 */
public class InsightQutoBillServlet extends BizHttpServlet {

	private static final long serialVersionUID = 8814524037488830265L;

	/**
	 * 获取审批定义.<br>
	 * 
	 * @param context
	 * @param model
	 * @return String
	 */
	private String getTaskDefine(BillModel model, GUID billDefine) {
		List<FTaskListDefine> list = model.getContext().getList(
				FTaskListDefine.class);
		for (int i = 0; i < list.size(); i++) {
			TaskListDefine taskListDefine = list.get(i).getTaskListDefine();
			if (taskListDefine != null) {
				if (taskListDefine.businessObjectList.size() > 0) {
					if (taskListDefine.businessObjectList.get(0).getID()
							.equals(billDefine))
						return taskListDefine.getID().toString();
				}
			}
		}

		return null;
	}

	/**
	 * 获取主表的配置信息
	 * 
	 * @param mainPage
	 * @return
	 */
	private Map<String, String> getMainDatas(PageDefine mainPage,
			List<String> mainDataList) {
		Map<String, String> mainDatas = new HashMap<String, String>();
		IMShowTemplate mShowTemp = mainPage.getmInputLayout();
		int mRows = mShowTemp.getTable().getRowCount();
		for (int i = 0; i < mRows; i++) {
			IMTableRow templ = mShowTemp.getTable().getRow(i);
			if (templ.getColCount() > 1) {
				IMTableCell cell0 = templ.getCol(0);
				String key = cell0.getTitle();
				IMTableCell cell1 = templ.getCol(1);
				String value = cell1.getTitle();
				if (value != null && value.contains("#")) {
					value = value.substring(value.indexOf("#") + 1,
							value.length() - 1);
					mainDatas.put(value, key);
					mainDataList.add(value);
				} else {
					mainDatas.put(value, key);
					mainDataList.add(value);
				}
			}

		}
		return mainDatas;
	}

	/**
	 * 获取子表的配置信息
	 * 
	 * @param detailPages
	 * @return
	 */
	private Map<String, Map<String, String>> getDetailDatas(
			List<PageDefine> detailPages,
			Map<String, List<String>> detailDataList) {
		Map<String, Map<String, String>> detailDatas = new HashMap<String, Map<String, String>>();
		for (PageDefine detailPage : detailPages) {
			if (detailPage.isHidden()) {
				continue;
			}
			IMShowTemplate dShowTemp = detailPage.getmInputLayout();
			int dRows = dShowTemp.getTable().getRowCount();
			Map<String, String> mainDatas = new HashMap<String, String>();
			List<String> cells = new ArrayList<String>();
			for (int i = 0; i < dRows; i++) {
				IMTableRow templ = dShowTemp.getTable().getRow(i);
				if (templ != null && templ.getColCount() > 1) {
					IMTableCell cell0 = templ.getCol(0);
					String key = cell0.getTitle();
					IMTableCell cell1 = templ.getCol(1);
					String value = cell1.getTitle();
					if (value != null && value.contains("#")) {
						value = value.substring(value.indexOf("#") + 1,
								value.length() - 1);
						mainDatas.put(value, key);
						cells.add(value);
					} else {
						mainDatas.put(value, key);
						cells.add(value);
					}
				}
			}
			detailDatas.put(detailPage.getName(), mainDatas);
			detailDataList.put(detailPage.getName(), cells);
		}
		return detailDatas;
	}

	/**
	 * 获得附件列表信息
	 * 
	 * @param maininfo
	 * @param request
	 * @param response
	 *            ,
	 */
	private void getAttachment(JSONArray maininfo,
			HttpServletResponse response, HttpServletRequest req) {
		String billId = req.getParameter("billDataID");
		String modelId = req.getParameter("billDefineID");
		response.setHeader("pragma", "no-cache");
		response.setHeader("cache-control", "no-no-cache");
		response.setHeader("expires", "0");
		com.jiuqi.dna.core.spi.application.Session session = com.jiuqi.dna.core.spi.application.AppUtil
				.getDefaultApp().getSystemSession();
		Context context = session.newContext(false);
		List<BillEnclosure> enclosures = AccessoriesUtils.getBillEnclosure(
				context, GUID.tryValueOf(modelId), GUID.tryValueOf(billId));
		try {
			if (enclosures.size() > 0) {
				getHtmlBillEnclosure(enclosures, maininfo);// 目前知道附件按钮标识为accessories的附件管理走这里
			} else {
				getHtmlAttachment(
						AccessoriesUtils.getBillAccachment(context,
								GUID.valueOf(billId)), maininfo);// 附件按钮标识为GmcAttachmentNewAction的附件管理走这里
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
	 * 获取html内容
	 * 
	 * @param billId
	 * @param enclosures
	 * @param maininfo
	 * @return
	 */
	private void getHtmlBillEnclosure(List<BillEnclosure> enclosures,
			JSONArray maininfo) throws Exception {
		String name = new String();
		String url = new String();
		String houzhui = new String();
		int attachmentSize = 0;
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

		for (BillEnclosure billEnclosure : enclosures) {
			name = billEnclosure.getENCLOSURENAME();
			url = getUrl(billEnclosure.getRECID().toString(),
					billEnclosure.getENCLOSURENAME(), "0");
			attachmentSize = billEnclosure.getENCLOSURESIZE();
			size = name.split("\\.").length;
			houzhui = name.split("\\.")[size - 1];
			float m = (float) attachmentSize / (float) (1024 * 1024);
			if (m > 1.0) {
				DecimalFormat df = new DecimalFormat("0.00");
				name = name + "(" + df.format(m) + "M)";
			} else {
				name = name + "(" + attachmentSize / 1024 + "K)";
			}
			houzhui = houzhui.toLowerCase();
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("attachmentName", name);
			jsonObject.put("attachmentUrl", url);
			jsonObject.put("attachmentSize", enclosures.size());

			if (houzhui.equals("doc") || houzhui.equals("docx"))
				jsonObject.put("attachmentIcon", wordiconPath);
			else if (houzhui.equals("xls") || houzhui.equals("xlsx"))
				jsonObject.put("attachmentIcon", exceliconPath);
			else if (houzhui.equals("pdf"))
				jsonObject.put("attachmentIcon", pdficonPath);
			else if (houzhui.equals("jpg") || houzhui.equals("jpeg")
					|| houzhui.equals("png") || houzhui.equals("bmp"))
				jsonObject.put("attachmentIcon", photoiconPath);
			else if (houzhui.equals("ppt") || houzhui.equals("pptx"))
				jsonObject.put("attachmentIcon", powerpointiconPath);
			else if (houzhui.equals("txt"))
				jsonObject.put("attachmentIcon", txticonPath);
			else
				jsonObject.put("attachmentIcon", accessoryiconPath);

			maininfo.put(jsonObject);
		}
	}

	private void getHtmlAttachment(List<FSmAttachment> attachmentList,
			JSONArray maininfo) throws Exception {
		String name = new String();
		String url = new String();
		String houzhui = new String();
		String attachmentSize = new String();
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

		for (FSmAttachment attachement : attachmentList) {
			name = attachement.getFileName();
			url = getUrl(attachement.getRecid().toString(),
					attachement.getFileName(), "1");
			attachmentSize = attachement.getFileSize().split("KB")[0];
			size = name.split("\\.").length;
			houzhui = name.split("\\.")[size - 1];
			float m = Float.parseFloat(attachmentSize) / (float) (1024);
			if (m > 1.0) {
				DecimalFormat df = new DecimalFormat("0.00");
				name = name + "(" + df.format(m) + "M)";
			} else {
				name = name + "(" + attachmentSize + "K)";
			}
			houzhui = houzhui.toLowerCase();
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			jsonObject.put("attachmentName", name);
			jsonObject.put("attachmentUrl", url);
			jsonObject.put("attachmentSize", attachmentList.size());

			if (houzhui.equals("doc") || houzhui.equals("docx"))
				jsonObject.put("attachmentIcon", wordiconPath);
			else if (houzhui.equals("xls") || houzhui.equals("xlsx"))
				jsonObject.put("attachmentIcon", exceliconPath);
			else if (houzhui.equals("pdf"))
				jsonObject.put("attachmentIcon", pdficonPath);
			else if (houzhui.equals("jpg") || houzhui.equals("jpeg")
					|| houzhui.equals("png") || houzhui.equals("bmp"))
				jsonObject.put("attachmentIcon", photoiconPath);
			else if (houzhui.equals("ppt") || houzhui.equals("pptx"))
				jsonObject.put("attachmentIcon", powerpointiconPath);
			else if (houzhui.equals("txt"))
				jsonObject.put("attachmentIcon", txticonPath);
			else
				jsonObject.put("attachmentIcon", accessoryiconPath);

			maininfo.put(jsonObject);
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

	@Override
	protected void doService(Context context, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/text;charset=UTF-8");
		String userstr = (String) request.getSession().getAttribute("user");
		User user = context.find(User.class, userstr.toLowerCase());
		context.changeLoginUser(user);
		String billDataID = (String) request.getParameter("billDataID");
		String billDefineID = (String) request.getParameter("billDefineID");
		FApprovalDefine fApprovalDefine = null;// 移动端审批列表定义
		FBillDefine billDefine = BillCentre.findBillDefine(context,
				GUID.tryValueOf(billDefineID));
		BillModel model = BillCentre.createBillModel(context, billDefine);
		model.load(GUID.tryValueOf(billDataID));
		context = model.getContext();
		String defineid = getTaskDefine(model, GUID.tryValueOf(billDefineID));
		PrintWriter writer = null;
		JSONObject detailJsonName = new JSONObject();
		String navigationTitle = "";// 导航栏标题
		try {
			writer = response.getWriter();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		List<FApprovalDefine> defines = context.getList(FApprovalDefine.class);// 移动端审批列表定义集合
		for (FApprovalDefine define : defines) {
			if (define.getTaskDefineID().equals(GUID.tryValueOf(defineid))) {// 审批列表定义找到移动端审批列表定义Id
				navigationTitle = define.getTitle();
				fApprovalDefine = define;
				break;
			}
		}
		JSONArray maininfo = new JSONArray();
		JSONArray subinfos = new JSONArray();
		String responsecode = "0";
		String responsemessage = "";
		try {
			BillDataModel billModel = (BillDataModel) model;
			// 不加载公式
			billModel.load(GUID.tryValueOf(billDataID));
			detailJsonName.put("maininfo", maininfo);
			detailJsonName.put("responsecode", "1");
			detailJsonName.put("responsemessage", responsemessage);
			detailJsonName.put("navigationTitle", navigationTitle);

			List<FBillStyleTemplate> styles = context
					.getList(FBillStyleTemplate.class);
			Map<String, String> mainDatas = new HashMap<String, String>();
			Map<String, Map<String, String>> detailDatas = new HashMap<String, Map<String, String>>();
			String maintitle = "";
			List<String> mainDataList = new ArrayList<String>();
			Map<String, List<String>> detailDataList = new HashMap<String, List<String>>();
			for (FBillStyleTemplate style : styles) {
				MobileBillDefine billdefine = style.getMobileBillDefine();
				if (billdefine.getTempId() != null
						&& billDefineID.equals(billdefine.getTempId())) {
					PageDefine mainPage = billdefine.getMasterPage();
					maintitle = style.getTitle();
					mainDatas = getMainDatas(mainPage, mainDataList);
					detailDatas = getDetailDatas(billdefine.getDetailPages(),
							detailDataList);
				}
			}
			detailJsonName.put("maintitle", maintitle);
			for (String mainData : mainDataList) {
				if (mainDatas != null && mainDatas.containsKey(mainData)) {
					String title = mainDatas.get(mainData);
					JSONObject jsons = new JSONObject();
					if (title == null || title.equals("#title")
							|| title.equals("#attachment")) {// 空行和自定义#title站位
						if (title == null) {
							jsons.put("title", "null");
							jsons.put("value", "null");
						} else if (title.equals("#attachment")) {
							getAttachment(maininfo, response, request);
						} else {
							jsons.put("title", "#title");
							jsons.put("value", mainData);
						}
						maininfo.put(jsons);
					} else {
						Object obj = billModel.getModelData().getMaster()
								.getFieldValue(mainData);
						jsons.put("title", title);
						String value = "";
						if (obj != null) {
							Field field = billModel.getModelData().getMaster()
									.getTable().getFieldMap().get(mainData);
							if (field != null
									&& field.getType() == FieldType.GUID) {
								GUID id = (GUID) obj;
								TableDefine relTable = field.getRelationTable();
								String tableName = relTable.getName();
								FBaseDataObject baseDataObject = BaseDataCenter
										.findObject(context, tableName, id);
								if (baseDataObject != null) {
									value = baseDataObject.getStdName();
									jsons.put("value", value);
								}
							} else if (field != null
									&& field.getType() == FieldType.INT) {
								Integer val = (Integer) obj;
								value = val + "";
								jsons.put("value", value);
							} else if (field != null
									&& field.getType() == FieldType.STRING) {
								value = (String) billModel.getModelData()
										.getMaster()
										.getFieldValue(field.getName());
								jsons.put("value", value);
							} else if (field != null
									&& field.getType() == FieldType.LONG) {
								Long valu = (Long) billModel.getModelData()
										.getMaster()
										.getFieldValue(field.getName());
								value = valu + "";
								jsons.put("value", value);
							} else if (field != null
									&& field.getType() == FieldType.NUMERIC) {
								Double valu = (Double) billModel.getModelData()
										.getMaster()
										.getFieldValue(field.getName());
								value = BudgetUtils.formatValue(valu, true, 2,
										false);
								jsons.put("value", value);
							} else if (field != null
									&& field.getType() == FieldType.DATE) {
								Date valu = (Date) billModel.getModelData()
										.getMaster()
										.getFieldValue(field.getName());
								SimpleDateFormat sdf = new SimpleDateFormat(
										"yyyy-MM-dd");
								value = sdf.format(valu);
								jsons.put("value", value);
							} else if (field != null
									&& field.getType() == FieldType.BOOLEAN) {
								Boolean valu = (Boolean) billModel
										.getModelData().getMaster()
										.getFieldValue(field.getName());
								if (valu) {
									value = "是";
								} else {
									value = "否";
								}
								jsons.put("value", value);
							} else if (field != null
									&& field.getType() == FieldType.TEXT) {
								value = (String) billModel.getModelData()
										.getMaster()
										.getFieldValue(field.getName());
								jsons.put("value", value);
							}
						} else {
							jsons.put("value", "");
						}
						maininfo.put(jsons);
					}
				}
			}
			List<List<BusinessObject>> details = billModel.getModelData()
					.getDetailsList();
			detailJsonName.put("subinfos", subinfos);
			for (List<BusinessObject> detail : details) {
				JSONObject subinfoJsons = new JSONObject();
				JSONArray subinfo = new JSONArray();
				subinfoJsons.put("subinfo", subinfo);
				String subinfotitle = "";
				for (BusinessObject arg : detail) {
					JSONObject itemJsons = new JSONObject();
					JSONArray items = new JSONArray();
					String tableName = arg.getTable().getTable().getName();
					Map<String, String> detailData = detailDatas.get(tableName);
					Table table = arg.getTable();
					Map<String, Field> fieldMap = table.getFieldMap();
					if (detailDataList.containsKey(tableName)) {
						itemJsons.put("item", items);
						subinfotitle = arg.getTable().getTitle();
						List<String> cells = detailDataList.get(tableName);
						for (String cell : cells) {
							if (detailData.containsKey(cell)) {
								String title = detailData.get(cell);
								Field field = fieldMap.get(cell);
								JSONObject jsons = new JSONObject();
								if (title == null) {
									jsons.put("title", "null");
									jsons.put("value", "null");
									items.put(jsons);
								} else {
									Object obj = arg.getFieldValue(cell);
									jsons.put("title", title);
									String value = "";
									if (obj != null) {
										if (field != null
												&& field.getType() == FieldType.GUID) {
											GUID id = (GUID) obj;
											TableDefine relTable = field
													.getRelationTable();
											String relTableName = relTable
													.getName();
											FBaseDataObject baseDataObject = BaseDataCenter
													.findObject(context,
															relTableName, id);
											if (baseDataObject != null) {
												value = baseDataObject
														.getStdName();
												jsons.put("value", value);
											} else {
												jsons.put("value", value);
											}
										} else if (field != null
												&& field.getType() == FieldType.STRING) {
											value = (String) obj;
											jsons.put("value", value);
										} else if (field != null
												&& field.getType() == FieldType.INT) {
											Integer val = (Integer) obj;
											value = val + "";
											jsons.put("value", value);
										} else if (field != null
												&& field.getType() == FieldType.LONG) {
											Long valu = (Long) obj;
											value = valu + "";
											jsons.put("value", value);
										} else if (field != null
												&& field.getType() == FieldType.NUMERIC) {
											Double valu = (Double) obj;
											value = BudgetUtils.formatValue(
													valu, true, 2, false);
											jsons.put("value", value);
										} else if (field != null
												&& field.getType() == FieldType.DATE) {
											SimpleDateFormat sdf = new SimpleDateFormat(
													"yyyy-MM-dd");
											String creattime = "";
											if (obj instanceof Long) {
												long date = Long.parseLong(obj
														.toString());
												creattime = sdf.format(date);
											} else if (obj instanceof Date) {
												long date = ((Date) obj)
														.getTime();
												creattime = sdf.format(date);
											} else {
												Long date = Long.valueOf(obj
														.toString());
												creattime = sdf.format(date);
											}
											jsons.put("value", creattime);
										} else if (field != null
												&& field.getType() == FieldType.BOOLEAN) {
											Boolean valu = (Boolean) billModel
													.getModelData()
													.getMaster()
													.getFieldValue(
															field.getName());
											if (valu) {
												value = "是";
											} else {
												value = "否";
											}
											jsons.put("value", value);
										} else if (field != null
												&& field.getType() == FieldType.TEXT) {
											value = (String) billModel
													.getModelData()
													.getMaster()
													.getFieldValue(
															field.getName());
											jsons.put("value", value);
										} else {
											jsons.put("value", "");
										}
									} else {
										jsons.put("value", "");
									}
									items.put(jsons);
								}
							}
						}
						subinfo.put(itemJsons);
					}
				}
				subinfoJsons.put("subinfotitle", subinfotitle);
				subinfos.put(subinfoJsons);
			}
		} catch (Exception e) {
			detailJsonName.put("maininfo", maininfo);
			detailJsonName.put("responsecode", responsecode);
			responsemessage = e.getMessage();
			detailJsonName.put("responsemessage", responsemessage);
		}
		writer.println(detailJsonName);
		writer.flush();
		writer.close();
	}
}
