package test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiuqi.xlib.json.JSONException;
import com.jiuqi.xlib.json.JSONObject;

public class JSON1Response extends JSONObject {
	private HttpServletRequest request;
	private HttpServletResponse response;

	public JSON1Response(HttpServletRequest request,HttpServletResponse response) {
		this.request = request;
		this.response = response;
		response.setCharacterEncoding(CommonConstants.CHARSET_NAME_DEFAULT);
	}

	public void commit() throws JSONException, IOException {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setContentType(CommonConstants.MIME_JSON);
		response.setCharacterEncoding(CommonConstants.CHARSET_NAME_URL);
		
		PrintWriter writer = null ;

		if(ServletGzipUtil.isGzipSupport(request))
			writer = ServletGzipUtil.createGzipPw(request, response);
		else
			writer = response.getWriter();
		
		this.toString(writer);
		writer.close();
		
		// response.getWriter().append(this.toString());
		// response.getOutputStream().write(this.toString().getBytes(XSpiDNA.CHARSET_NAME));
	}
}
