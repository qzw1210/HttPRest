package esky.framework.cashew.web.servlet;

import java.lang.reflect.Method;

import esky.framework.cashew.web.bind.annotation.RequestMethod;

public class HandlerMethod {
	private Class<?> handler;
	private Method method;
	private boolean isVoid;
	private Class<?>[] parameterTypes;
	private String[] requiredParams;
	private RequestMethod[] requestMethods;
	
	public HandlerMethod() {
	}

	public HandlerMethod(Class<?> handler, Method method) {
		this.handler = handler;
		this.method = method;
	}
	
	public HandlerMethod(Class<?> handler, Method method, String[] requiredParams, RequestMethod[] requestMethods) {
		this.handler = handler;
		this.method = method;
		this.requiredParams = requiredParams;
		this.requestMethods = requestMethods;
	}

	public Object getHandler() {
		return handler;
	}

	public void setHandler(Class<?> handler) {
		this.handler = handler;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	public boolean isVoid() {
		if (this.method != null) {
			return method.getReturnType().toString().equals("void");
		}
		return isVoid;
	}

	public void setVoid(boolean isVoid) {
		this.isVoid = isVoid;
	}
	
	public Class<?>[] getParameterTypes() {
		if (this.method != null) {
			return this.method.getParameterTypes();
		}
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
	
	public String[] getRequiredParams() {
		return requiredParams;
	}

	public void setRequiredParams(String[] requiredParams) {
		this.requiredParams = requiredParams;
	}

	public RequestMethod[] getRequestMethods() {
		if (requestMethods == null) {
			return new RequestMethod[]{RequestMethod.GET, RequestMethod.POST};
		}
		return requestMethods;
	}

	public void setRequestMethods(RequestMethod[] requestMethods) {
		this.requestMethods = requestMethods;
	}

	@Override
	public String toString() {
		return method.toString();
	}
}
