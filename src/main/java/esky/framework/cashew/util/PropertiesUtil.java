package esky.framework.cashew.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class PropertiesUtil {

	public static Properties getProperties(InputStream is) throws IOException {
		Properties properties = new Properties();
		properties.load(is);
		return properties;
	}
	
	public static Properties getProperties(String filePath) throws IOException {
		File file = new File(filePath);
		return getProperties(file);
	}
	
	
	public static Properties getProperties(File file) throws IOException {
		Properties properties = new Properties();
		InputStream is = getBufferedInputStream(file);
		properties.load(is);
		is.close();
		return properties;
	}
	
	public static void setProperties(File file, String key, String value) throws IOException {
		Properties properties = getProperties(file);
		properties.setProperty(key, value);
		
		OutputStream out = new FileOutputStream(file);
		properties.store(out, "Update key: " + key + " - value: " + value);
	}
	
	public static String getProperties(File file, String key) throws IOException {
		Properties properties = getProperties(file);
		return properties.getProperty(key);
	}
	
	private static InputStream getBufferedInputStream(File file) throws FileNotFoundException {
		return new BufferedInputStream (new FileInputStream(file));
	}
}
