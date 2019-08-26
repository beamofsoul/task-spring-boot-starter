package com.beamofsoul.cloud.execute;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodBean {

	private Method method;
	private Object bean;
	
	public MethodBean(Method method, Object bean) {
		this.method = method;
		this.bean = bean;
	}

	public Method getMethod() {
		return this.method;
	}
	
	public Object getBean() {
		return this.bean;
	}
	
	public boolean invoke() {
		try {
			this.method.invoke(this.bean, new Object[] {});
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
