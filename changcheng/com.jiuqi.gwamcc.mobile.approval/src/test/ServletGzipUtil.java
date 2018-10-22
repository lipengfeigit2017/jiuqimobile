package test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * servlet 的 gzip 工具类
 * @author zhouwenjie
 *
 */
public class ServletGzipUtil {
	/**
	 * 判断浏览器是否支持 gzip 压缩
	 * 
	 * @param req
	 * @return boolean 值
	 */
	public static boolean isGzipSupport(HttpServletRequest req) {
		String headEncoding = req.getHeader("accept-encoding");
		if (headEncoding == null || (headEncoding.indexOf("gzip") == -1)) { // 客户端
																			// 不支持
																			// gzip
			return false;
		} else { // 支持 gzip 压缩
			return false;
		}
	}

	/**
	 * 创建 以 gzip 格式 输出的 PrintWriter 对象，如果浏览器不支持 gzip 格式，则创建普通的 PrintWriter 对象，
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	public static PrintWriter createGzipPw(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		PrintWriter pw = null;
		if (isGzipSupport(req)) { // 支持 gzip 压缩
			pw = new PrintWriter(new GZIPOutputStream(resp.getOutputStream()));
			// 在 header 中设置返回类型为 gzip
			resp.setHeader("content-encoding", "gzip");
		} else { // // 客户端 不支持 gzip
			pw = resp.getWriter();
		}
		return pw;
	}
}

