package test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * servlet �� gzip ������
 * @author zhouwenjie
 *
 */
public class ServletGzipUtil {
	/**
	 * �ж�������Ƿ�֧�� gzip ѹ��
	 * 
	 * @param req
	 * @return boolean ֵ
	 */
	public static boolean isGzipSupport(HttpServletRequest req) {
		String headEncoding = req.getHeader("accept-encoding");
		if (headEncoding == null || (headEncoding.indexOf("gzip") == -1)) { // �ͻ���
																			// ��֧��
																			// gzip
			return false;
		} else { // ֧�� gzip ѹ��
			return false;
		}
	}

	/**
	 * ���� �� gzip ��ʽ ����� PrintWriter ��������������֧�� gzip ��ʽ���򴴽���ͨ�� PrintWriter ����
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	public static PrintWriter createGzipPw(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		PrintWriter pw = null;
		if (isGzipSupport(req)) { // ֧�� gzip ѹ��
			pw = new PrintWriter(new GZIPOutputStream(resp.getOutputStream()));
			// �� header �����÷�������Ϊ gzip
			resp.setHeader("content-encoding", "gzip");
		} else { // // �ͻ��� ��֧�� gzip
			pw = resp.getWriter();
		}
		return pw;
	}
}

