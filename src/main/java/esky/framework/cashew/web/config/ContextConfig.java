package esky.framework.cashew.web.config;

public final class ContextConfig {
	public static final String SPLIT_OPERATOR = ";";
	
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String CONTENT_TYPE_XML = "application/xml";
	
	public static final String DEFAULT_CONTENT_TYPE = "application/json";
	public static final String DEFAULT_CHARSET_ENCODING = "UTF-8";
	
	public static final String DEFAULT_CONFIG_NAME = "cashew.properties";
	public static final String DEFAULT_CONFIG_LOCATION = "/WEB-INF/cashew.properties";
	public static final String DEFAULT_CONFIG_LOCATION_PREFIX = "/WEB-INF/";
	
	public static String CONTENT_TYPE = DEFAULT_CONTENT_TYPE;
	public static String CHARSET_ENCODING = DEFAULT_CHARSET_ENCODING;
	
	public static String EXTENSION = null;
	public static String NAMESPACE = "";
	public static String[] SCAN_PACKAGES;
}
