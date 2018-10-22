package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.jiuqi.xlib.json.JSONObject;

public class CommonUtil {
	public static JSONObject acceptJSON(HttpServletRequest request){
		 String acceptjson = "";
		 JSONObject json = null;
		 try {
		 BufferedReader br = new BufferedReader(new InputStreamReader( (ServletInputStream) request.getInputStream(), "utf-8"));  
		             StringBuffer sb = new StringBuffer("");  
		             String temp;  
		             while ((temp = br.readLine()) != null) {  
		                 sb.append(temp);  
		             }  
		             br.close();  
		             acceptjson = sb.toString();  
//		             System.out.println(acceptjson);
		             json = new JSONObject(acceptjson);
		         } catch (Exception e) {  
		             e.printStackTrace();    
		         }
		 return json;
	}
}
