package esky.framework.cashew.web.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import esky.framework.cashew.web.config.ContextConfig;

public class WebUtil {
 
	private static final String POST_METHOD = "POST";
	public static final String MULTIPART = "multipart/";
	 
	public static void outPutResult(HttpServletResponse response, String ContentType, String result) throws IOException {
		response.setContentType(ContentType);
		OutputStream os = response.getOutputStream();
		Writer out = new OutputStreamWriter(os, ContextConfig.CHARSET_ENCODING);
		out.write(result);
		out.flush();
		out.close();
		os.flush();
		os.close();
	}
	
	public static void setCharacterEncoding(ServletRequest request, ServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding(ContextConfig.CHARSET_ENCODING);
		response.setCharacterEncoding(ContextConfig.CHARSET_ENCODING);
	}
	
	public static boolean isMultipartContent(HttpServletRequest request) {
		if (!POST_METHOD.equalsIgnoreCase(request.getMethod())) {
            return false;
        }
		
		String contentType = request.getContentType();
		if (contentType == null) {
			return false;
		}
		if (contentType.toLowerCase(Locale.ENGLISH).startsWith(MULTIPART)) {
			return true;
		}
		return false;
	}
}