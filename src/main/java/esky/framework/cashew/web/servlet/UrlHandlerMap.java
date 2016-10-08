package esky.framework.cashew.web.servlet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esky.framework.cashew.stereotype.Action;
import esky.framework.cashew.util.ClassUtil;
import esky.framework.cashew.util.StringUtil;
import esky.framework.cashew.web.bind.annotation.Mapping;
import esky.framework.cashew.web.bind.annotation.RequestMethod;
import esky.framework.cashew.web.config.ContextConfig;

public class UrlHandlerMap {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	private final Map<String, Object> urlHandlerMap = new HashMap<String, Object>();
	
	public Map<String, Object> getUrlHandlerMap() {
		return this.urlHandlerMap;
	}
	
	public void setUrlHandlerMap(Map<String, Object> urlMap) {
		this.urlHandlerMap.putAll(urlMap);
	}
	
	protected void initUrlHandlerMap() {
		String[] packages =  ContextConfig.SCAN_PACKAGES;
		List<Class<?>> classeList  = getClasseList(packages);
		getMethodList(classeList); 
	}
	
	private List<Class<?>> getClasseList(String[] packages){
		List<Class<?>> list = new ArrayList<Class<?>>();
		for (String packageName : packages) {
			list.addAll(ClassUtil.getClassListByAnnotation(packageName, Action.class));
		}
		return list;
	}
	
	private Map<String, Object> getMethodList(List<Class<?>> classeList){
		Map<String, Object> map = new HashMap<String, Object>();
		for (Class<?> clazz : classeList) {
			if (clazz.isInterface()) {
				continue;
			}
			if(clazz.isAnnotationPresent(Action.class)) { 
				String classUrl = "";
				if (clazz.isAnnotationPresent(Mapping.class)) {
					Mapping action_clazz = (Mapping) clazz.getAnnotation(Mapping.class);
					classUrl = action_clazz.value(); 
				}
				Method[] ms = clazz.getDeclaredMethods();
				String methodUrl = null;
				for (Method m : ms) {
					if (m.isAnnotationPresent(Mapping.class)) {
						Mapping method = m.getAnnotation(Mapping.class);
						methodUrl = method.value();
						String[] params = method.params();
						RequestMethod[] requestMethods = method.method();
						
						String resultUrl = ContextConfig.NAMESPACE + classUrl + methodUrl;
						String _resultUrl = resultUrl;
						if (StringUtil.countStr(resultUrl, ".") > 1) {
							continue;
						}else if (StringUtil.countStr(resultUrl, ".") == 1) {
							resultUrl = resultUrl.substring(0, resultUrl.lastIndexOf("."));
						}
						map.put(resultUrl, new HandlerMethod(clazz, m, params, requestMethods));
						if (this.logger.isInfoEnabled()) {
							logger.info("Mapped [" + _resultUrl + "] onto " + m);
						}
					}
				}
			}
		}
		setUrlHandlerMap(map);
		return map;
	}
}
