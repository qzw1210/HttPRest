package esky.framework.cashew.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import esky.framework.cashew.exception.FileUploadIOException;

public class FileUtil {
	
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";
	
	public static void upload(HttpServletRequest request, String filePath, int limit) throws Exception {
		String contentType = request.getContentType();
		if (contentType.indexOf(MULTIPART_FORM_DATA) >= 0) {
			InputStream is = request.getInputStream();
			int formDataLength = request.getContentLength();
			if (formDataLength > limit) {
				throw new FileUploadIOException("Upload file size over the limit (" + limit + " Byte)");
			}
			
			File file = new File(filePath);
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			byte[] bytes = new byte[1024];
			int readLength;
			while (((readLength = is.read(bytes)) != -1)) {
				bos.write(bytes, 0, readLength);
			}
			bos.flush();
			bos.close();
			fos.close();
			is.close();
		}
	}
}
