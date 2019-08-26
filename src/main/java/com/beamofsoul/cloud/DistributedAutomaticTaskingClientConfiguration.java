package com.beamofsoul.cloud;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.beamofsoul.cloud.annotation.ConditionalOnAnnotation;
import com.beamofsoul.cloud.annotation.EnableTaskingClient;
import com.beamofsoul.cloud.annotation.Task;
import com.beamofsoul.cloud.execute.MethodBean;
import com.beamofsoul.cloud.register.RegisterInformation;
import com.beamofsoul.cloud.util.MD5Utils;

@ConditionalOnAnnotation(EnableTaskingClient.class)
@ComponentScan
@Configuration
public class DistributedAutomaticTaskingClientConfiguration  implements ApplicationContextAware, InitializingBean {
	
	private static final Map<String, MethodBean> METHOD_BEAN_MAP = new HashMap<>();
	private static ApplicationContext applicationContext = null;
	
	/**   
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)   
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		DistributedAutomaticTaskingClientConfiguration.applicationContext = applicationContext;
	}
	
	/**   
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()   
	 */
	public void afterPropertiesSet() throws Exception {
		Map<String, List<String>> cronExpressionURLMap = loadTasks();
		RegisterInformation.putAll(cronExpressionURLMap);
	}
	
	public static MethodBean getMethodBean(String taskKey) {
		Optional<String> optionalTaskKey = METHOD_BEAN_MAP.keySet().stream().filter(e -> e.startsWith(taskKey)).distinct().findFirst();
		return optionalTaskKey.isPresent() ? METHOD_BEAN_MAP.get(optionalTaskKey.get()) : null;
	}

	private Map<String, List<String>> loadTasks() {
		Map<String, List<String>> cronExpressionURLMap = new HashMap<>();
		String[] allBeanNames = applicationContext.getBeanDefinitionNames();
		for (String beanName : allBeanNames) {
			Object targetBean = applicationContext.getBean(beanName);
			Method[] methods = targetBean.getClass().getMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(Task.class)) {
					Task taskOnMethod = method.getAnnotation(Task.class);
					String currentTaskKey = getTaskKey(targetBean, method, taskOnMethod);
					METHOD_BEAN_MAP.put(currentTaskKey, new MethodBean(method, targetBean));
					if (cronExpressionURLMap.containsKey(taskOnMethod.value())) {
						cronExpressionURLMap.get(taskOnMethod.value()).add(currentTaskKey);
					} else {
						cronExpressionURLMap.put(taskOnMethod.value(), new ArrayList<String>() {
							private static final long serialVersionUID = -1L;
						{
							add(currentTaskKey);
						}});
					}
				}
			}
		}
		return cronExpressionURLMap;
	}
	
	private String getTaskKey(Object targetBean, Method method, Task annotation) {
		String task = MD5Utils.encrypt(targetBean.getClass().getName() + "." + ReflectUtils.getSignature(method));
		if (annotation.weight() > 1) {
			task += (":" + annotation.weight()); 
		}
		return task;
	}
}
