package test;

import java.nio.charset.Charset;

import com.jiuqi.xlib.XLib;

public class CommonConstants {
	public static final String COMPONENT_ID = "com.jiuqi.gmt.common";
	public static final String RESOURCE_MANAGER_SESSION = "com.jiuqi.gmt.common.session";
	public static final boolean DEBUG = XLib.isDebug(COMPONENT_ID) || XLib.isDebug("com.jiuqi.gmt.common");
	
	public static final String CHARSET_NAME_DEFAULT = "GBK";
	public static final String CHARSET_NAME_URL = "UTF-8";
	public static final Charset CHARSET_DEFAULT = Charset.forName(CHARSET_NAME_DEFAULT);
	public static final Charset CHARSET_URL = Charset.forName(CHARSET_NAME_URL);
	
	public static final String MIME_JSON = "text/json";
	
	public static final String SESSION_ERROR = "{\"result\":{\"type\":\"error\",\"msg\":\"服务器暂时无法响应请求\"}}";
	
	public static final String ARGUMENT_USER = "username";
}
