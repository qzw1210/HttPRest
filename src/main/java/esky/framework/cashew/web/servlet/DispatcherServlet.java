package esky.framework.cashew.web.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.thoughtworks.xstream.XStream;

import esky.framework.cashew.util.PropertiesUtil;
import esky.framework.cashew.util.StringUtil;
import esky.framework.cashew.util.Timer;
import esky.framework.cashew.web.bind.annotation.RequestMethod;
import esky.framework.cashew.web.config.ContextConfig;
import esky.framework.cashew.web.config.NameConfig;
import esky.framework.cashew.web.context.ActionContext;
import esky.framework.cashew.web.context.SpringContext;
import esky.framework.cashew.web.exception.IllegalRequestException;
import esky.framework.cashew.web.exception.NoSuchMethodException;
import esky.framework.cashew.web.exception.NonsupportRequestTypeException;
import esky.framework.cashew.web.util.WebUtil;

@SuppressWarnings("serial")
public class DispatcherServlet extends HttpServlet {

	protected final Log logger = LogFactory.getLog(getClass());

	protected Map<String, Object> urlHandlerMap;

	@Override
	public void init() {
		Timer timer = new Timer();
		getServletContext().log("Initializing Cashew DispatcherServlet '" + getServletName() + "'");
		if (this.logger.isInfoEnabled()) {
			this.logger.info("DispatcherServlet '" + getServletName() + "': initialization started");
		}

		try {
			initContextConfig();
			initStrategies();
			initDispatcherServlet();
		} catch (Exception e) {
			this.logger.error("Context initialization failed", e);
			throw new ExceptionInInitializerError(e);
		}

		if (this.logger.isInfoEnabled()) {
			this.logger.info("DispatcherServlet '" + getServletName() + "': initialization completed in " + timer.end() + " ms");
		}
	}

	protected void initContextConfig() throws IOException {
		ServletConfig config = getServletConfig();
		String contextConfigLocation = config.getInitParameter(NameConfig.CONTEXT_CONFIG);
		if (StringUtil.isEmpty(contextConfigLocation)) {
			contextConfigLocation = getServletContext().getRealPath("") + ContextConfig.DEFAULT_CONFIG_LOCATION;
		} else {
			contextConfigLocation = this.getClass().getClassLoader().getResource("/").getPath() + contextConfigLocation;
		}

		Properties servletConfigProperty = null;
		try {
			servletConfigProperty = PropertiesUtil.getProperties(contextConfigLocation);
		} catch (IOException e) {
			this.logger.error("DispatcherServlet '" + config.getServletName() + "' init-param: " + NameConfig.CONTEXT_CONFIG + " is required.", e);
			throw e;
		}

		String namespace = servletConfigProperty.getProperty(NameConfig.NAMESPACE);
		if (StringUtil.isNotEmpty(namespace)) {
			ContextConfig.NAMESPACE = namespace;
		}

		String extension = servletConfigProperty.getProperty(NameConfig.EXTENSION);
		if (StringUtil.isNotEmpty(extension)) {
			ContextConfig.EXTENSION = extension;
		}

		String scanPackage = servletConfigProperty.getProperty(NameConfig.SCAN_PACKAGE);
		if (StringUtil.isEmpty(scanPackage)) {
			throw new NullPointerException("DispatcherServlet '" + config.getServletName() + "' " + ContextConfig.class.getSimpleName() + ": " + NameConfig.SCAN_PACKAGE + " is required.");
		}

		String[] scanPackages = scanPackage.split(ContextConfig.SPLIT_OPERATOR);
		ContextConfig.SCAN_PACKAGES = scanPackages;

		String charsetEncoding = servletConfigProperty.getProperty(NameConfig.CHARSET_ENCODING);
		if (StringUtil.isNotEmpty(charsetEncoding)) {
			try {
				Charset.forName(charsetEncoding);
				ContextConfig.CHARSET_ENCODING = charsetEncoding;
			} catch (UnsupportedCharsetException e) {
				this.logger.error("DispatcherServlet '" + config.getServletName() + "' " + ContextConfig.class.getSimpleName() + ": " + NameConfig.CHARSET_ENCODING + " " + charsetEncoding + " is unsupported, we will be use default charsetEncoding " + ContextConfig.CHARSET_ENCODING + ".");
			}
		}
	}

	protected void initStrategies() {
		initHandlerMap();
	}

	protected void initHandlerMap() {
		UrlHandlerMap handlerMap = new UrlHandlerMap();
		handlerMap.initUrlHandlerMap();
		this.urlHandlerMap = handlerMap.getUrlHandlerMap();
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Object result = null;
		try {
			WebUtil.setCharacterEncoding(request, response);
			ActionContext.init(getServletContext(), request, response);

			result = processDispatchResult(request, response);
		} catch (Throwable t) {
			result = processError(t);
		} finally {
			outPutResult(request, response, result);
			ActionContext.destroy();
		}
	}

	protected Object processDispatchResult(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (checkMultipart(request)) {
			// TODO to by complete...
		}

		HandlerMethod handlerMethod = getHandler(request);
		if (handlerMethod == null) {
			noHandlerFound(request, response);
		}

		checkRequestMethod(request, handlerMethod.getRequestMethods());

		Class<?> handler = (Class<?>) handlerMethod.getHandler();

		Object instance = getHandlerInstance(handler);

		Method method = handlerMethod.getMethod();
		boolean isVoid = handlerMethod.isVoid();

		Object[] parameters = bindArguments(request, response, handlerMethod);

		method.setAccessible(true);
		Object result = method.invoke(instance, parameters);

		return isVoid ? null : result;
	}

	protected Object getHandlerInstance(Class<?> handler) throws InstantiationException, IllegalAccessException {
		Object object = null;

		try {
			object = getInstanceWithSpringContext(handler);

			if (object == null) {
				object = handler.newInstance();
			}
		} catch (ClassNotFoundException e) {
			object = handler.newInstance();
		}

		return object;
	}

	protected Object getInstanceWithSpringContext(Class<?> handler) throws ClassNotFoundException {
		Object object = null;
		try {
			ApplicationContext applicationContext = SpringContext.getApplicationContext();
			if (applicationContext != null) {
				object = applicationContext.getBean(handler);
			} else {
				throw new ClassNotFoundException();
			}
		} catch (ClassNotFoundException e) {
			throw e;
		} catch (NoClassDefFoundError e) {
			throw new ClassNotFoundException();
		}
		return object;
	}

	protected Object processError(Throwable t) {
		if (logger.isErrorEnabled()) {
			logger.error(t.getMessage(), t);
		}
		t.printStackTrace();
		return StringUtil.stackTrace2String(t);
	}

	protected void outPutResult(HttpServletRequest request, HttpServletResponse response, Object result) throws IOException {
		String resultStr = null;
		String contentType = request.getHeader(NameConfig.CONTENT_TYPE);
		
		String format = request.getParameter(NameConfig.FORMAT);
		if (StringUtil.isEmpty(format) && StringUtil.isNotEmpty(contentType)) {
			if (contentType.contains(ContextConfig.CONTENT_TYPE_JSON)) {
				format = NameConfig.JSON;
			} else if (contentType.contains(ContextConfig.CONTENT_TYPE_XML)) {
				format = NameConfig.XML;
			}
		}
		
		if (StringUtil.isEmpty(format)) {
			contentType = ContextConfig.DEFAULT_CONTENT_TYPE;
			resultStr = JSONObject.toJSONString(result);
		} else {
			if (format.equalsIgnoreCase(NameConfig.JSON)) {
				contentType = ContextConfig.CONTENT_TYPE_JSON;
				resultStr = JSONObject.toJSONString(result);
			} else if (format.equalsIgnoreCase(NameConfig.XML)) {
				contentType = ContextConfig.CONTENT_TYPE_XML;
				XStream xStream = new XStream();
				if (result != null) {
					xStream.alias(result.getClass().getSimpleName(), result.getClass());
				}
				resultStr = xStream.toXML(result);
			} else {
				contentType = ContextConfig.DEFAULT_CONTENT_TYPE;
				resultStr = JSONObject.toJSONString(result);
			}
		}
		WebUtil.outPutResult(response, contentType, resultStr);
	}

	protected boolean checkMultipart(HttpServletRequest request) {
		boolean isMultipart = WebUtil.isMultipartContent(request);
		return isMultipart;
	}

	protected HandlerMethod getHandler(HttpServletRequest request) {
		String url = checkUrl(request);
		return (HandlerMethod) urlHandlerMap.get(url);
	}

	protected String checkUrl(HttpServletRequest request) {
		String servletPath = request.getServletPath();
		String path = servletPath.substring(servletPath.lastIndexOf("/"), servletPath.length());

		if (StringUtil.countStr(path, ".") > 1) {
			throw new IllegalRequestException();
		}

		if (null == ContextConfig.EXTENSION) {
			if (StringUtil.countStr(path, ".") == 1) {
				path = servletPath.substring(0, servletPath.lastIndexOf("."));
				return path;
			}
			return servletPath;
		} else {
			if (!path.toLowerCase().endsWith("." + ContextConfig.EXTENSION.toLowerCase())) {
				throw new IllegalRequestException();
			}
			return servletPath;
		}
	}

	protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) {
		// TODO to be complete...
		String requestUri = request.getRequestURI();
		throw new NoSuchMethodException("No mapping found for HTTP request with URI [" + requestUri + "] in DispatcherServlet with name '" + getServletName() + "'");
	}

	protected void checkRequestMethod(HttpServletRequest request, RequestMethod[] requestMethods) {
		if (requestMethods != null && requestMethods.length > 0) {
			String requestMethod = request.getMethod();
			if (!Arrays.toString(requestMethods).contains(requestMethod)) {
				throw new NonsupportRequestTypeException("Request method '" + requestMethod + "' not supported");
			}
		}
	}

	protected Object[] bindArguments(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws InstantiationException, IllegalAccessException {
		Object[] parameters;
		bindAnnotationArguments(request, response, handlerMethod);

		Class<?>[] parameterTypes = handlerMethod.getParameterTypes();
		parameters = bindWebArguments(request, response, parameterTypes);
		return parameters;
	}

	protected Object[] bindAnnotationArguments(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws InstantiationException, IllegalAccessException {
		return null;
	}

	protected Object[] bindWebArguments(HttpServletRequest request, HttpServletResponse response, Class<?>[] parameterTypes) throws InstantiationException, IllegalAccessException {
		Object[] parameters = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> parameterType = parameterTypes[i];
			if (parameterType.isAssignableFrom(ServletContext.class)) {
				parameters[i] = getServletContext();
			} else if (parameterType.isAssignableFrom(HttpSession.class)) {
				parameters[i] = request.getSession();
			} else if (parameterType.isAssignableFrom(HttpServletRequest.class)) {
				parameters[i] = request;
			} else if (parameterType.isAssignableFrom(HttpServletResponse.class)) {
				parameters[i] = response;
			} else if (parameterType.isAssignableFrom(Cookie.class)) {
				parameters[i] = request.getCookies();
			} else {
				parameters[i] = null;
			}
		}
		return parameters;
	}

	protected void initDispatcherServlet() throws ServletException {
		// subclass to be override
	}

}
