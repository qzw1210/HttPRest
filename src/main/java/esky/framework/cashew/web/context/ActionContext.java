package esky.framework.cashew.web.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ActionContext implements Serializable{
 
	private static final long serialVersionUID = 5482527381977491046L;
	
	private ServletContext servletContext;
	private HttpSession session;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Map<String, Cookie> cookies;

	private static ThreadLocal<ActionContext> contexts = new ThreadLocal<ActionContext>();
	
	public static void init(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
		ActionContext context = new ActionContext();
		context.servletContext = servletContext;
		context.request = request;
		context.response = response;
		context.session = request.getSession(false);
		context.cookies = new HashMap<String, Cookie>();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie ck : cookies) {
				context.cookies.put(ck.getName(), ck);
			}
		}
		contexts.set(context);
	}
	
	public static ActionContext getCurrent() {
		return contexts.get();
	}
	
	public ServletContext getContext() {
		return servletContext;
	}

	public HttpSession getSession() {
		return session;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public Map<String, Cookie> getCookies() {
		return cookies;
	}

	public static void destroy() {
		if (contexts != null) {
			contexts.remove();
		}
	}
}