package com.employer.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet implementation class FlowActionServlet
 */
public class FlowActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FlowActionServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		try {
			doService(request, response);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	protected void doService(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, DocumentException {
		String action = request.getPathInfo().substring(1);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		
		if ("save".equals(action)) {
			save(request, response);
		} else if ("update".equals(action)){
			update(request, response);
		}else if("delete".equals(action)){
			delete(request, response);
		} else if("loaddata".equals(action)){
			loadData(request, response);
		}
		
	}

	private void delete(HttpServletRequest request, HttpServletResponse response) {
		String recid = request.getParameter("recid");
		JSONObject jsonName = new JSONObject();
		jsonName.put("code", 0);
		PrintWriter writer = null;
		Element current = null;
		try {
			writer = response.getWriter();
	        String path=this.getServletContext().getRealPath("/")+"data.xml";
			SAXReader sax=new SAXReader();  
		    File xmlFile=new File(path);  
		    Document document=sax.read(xmlFile); 
		    Element root=document.getRootElement();
		    current = root.elementByID(recid);		
		    if(current == null){
				writer.println(jsonName);
				writer.flush();
				writer.close();
				return;
		    }
		    root.remove(current);
            OutputFormat format = OutputFormat.createPrettyPrint();  
            format.setEncoding("gb2312");  
            XMLWriter writer1 = new XMLWriter(  
                    new OutputStreamWriter(new FileOutputStream(path)),format);  
            writer1.write(document);  
            writer1.close(); 
			jsonName.put("code", 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		writer.println(jsonName);
		writer.flush();
		writer.close();
	}

	private void update(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String recid = request.getParameter("recid");
		String ip = request.getParameter("ipname");
		String unit = new String(request.getParameter("unitname").getBytes("iso-8859-1"), "utf-8");
		System.out.println(ip+"---"+unit);
		JSONObject jsonName = new JSONObject();
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
	        String path=this.getServletContext().getRealPath("/")+"data.xml";
			SAXReader sax=new SAXReader();  
		    File xmlFile=new File(path);  
		    Document document=sax.read(xmlFile); 
		    Element root=document.getRootElement();
		    List<Element> list = root.elements();
		    Element current = root.elementByID(recid);
		    
		    Element ipEle = current.element("IP");
		    ipEle.setText(ip);
		    Element unitEle = current.element("UNIT");
		    unitEle.setText(unit);
		    Element timeEle = current.element("TIME");
		    timeEle.setText(getCurrentDate());
		    
            OutputFormat format = OutputFormat.createPrettyPrint();  
            format.setEncoding("gb2312");  
            XMLWriter writer1 = new XMLWriter(  
                    new OutputStreamWriter(new FileOutputStream(path)),format);  
            writer1.write(document);  
            writer1.close(); 
		    
			jsonName.put("code", "成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
		writer.println(jsonName);
		writer.flush();
		writer.close();
	}

	private void loadData(HttpServletRequest request, HttpServletResponse response) throws DocumentException, IOException {
		Object recid = request.getParameter("recid");
		Object ip = request.getParameter("ipname");
		JSONObject jsonName = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		PrintWriter writer = null;
		
		//1.获取SAM接口：  
        SAXReader saxReader = new SAXReader();  
        //2.获取XML文件：  
        String path=this.getServletContext().getRealPath("/")+"data.xml"; 
        Document doc = saxReader.read(path);  
//        //3.获取根节点：  
        Element root = doc.getRootElement();          
        System.out.println("根节点: " + root.getName());                   
      //获取子节点  
        Iterator<?> it = root.elementIterator();  
        while(it.hasNext()){  
            Element elem = (Element) it.next();  
            //获取属性名属性值  
            List<Attribute> li = elem.attributes();  
            for (Attribute att : li ) {  
                System.out.println(att.getName() + "  " + att.getValue() );  
            }  
                      
            //获取子节的子节点  
            Iterator<?> ite = elem.elementIterator();  
            JSONObject jsonTemp = new JSONObject();
            String ID = elem.attributeValue("ID");
            jsonTemp.put("ID", ID);
            while(ite.hasNext()){  
                Element child = (Element) ite.next();  
                System.out.println(child.getName() + "  " + child.getStringValue());  
                jsonTemp.put(child.getName(), child.getStringValue());
            }
            jsonArray.put(jsonTemp);
        }  
		
		try {
			writer = response.getWriter();			
			jsonName.put("data", jsonArray);
			jsonName.put("code", "成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(jsonName);
		writer.println(jsonName);
		writer.flush();
		writer.close();
		
	}
	/**
	 * 新增一个子节点
	 * @param request
	 * @param response
	 * @throws UnsupportedEncodingException
	 */
	private void save(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		long recid = System.currentTimeMillis();
		String ip = request.getParameter("ipname");
		String unit = new String(request.getParameter("unitname").getBytes("iso-8859-1"), "utf-8");
		System.out.println(ip+"---"+unit);
		JSONObject jsonName = new JSONObject();
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
	        String path=this.getServletContext().getRealPath("/")+"data.xml";
			SAXReader sax=new SAXReader();  
		    File xmlFile=new File(path);  
		    Document document=sax.read(xmlFile); 
		    Element root=document.getRootElement();
		    
		    Element newNode = root.addElement("DATA"); 
		    newNode.addAttribute("ID", String.valueOf(recid));
		    Element ipEle = newNode.addElement("IP");
		    ipEle.setText(ip);
		    Element unitEle = newNode.addElement("UNIT");
		    unitEle.setText(unit);
		    Element timeEle = newNode.addElement("TIME");
		    timeEle.setText(getCurrentDate());
		    
            OutputFormat format = OutputFormat.createPrettyPrint();  
            format.setEncoding("gb2312");  
            XMLWriter writer1 = new XMLWriter(  
                    new OutputStreamWriter(new FileOutputStream(path)),format);  
            writer1.write(document);  
            writer1.close(); 
		    
			jsonName.put("code", "成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
		writer.println(jsonName);
		writer.flush();
		writer.close();
	}
	/**
	 * 获取当前时间
	 * @return
	 */
	private String getCurrentDate(){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}
	
}
