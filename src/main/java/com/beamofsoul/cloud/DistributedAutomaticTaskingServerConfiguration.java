package com.beamofsoul.cloud;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.beamofsoul.cloud.annotation.ConditionalOnAnnotation;
import com.beamofsoul.cloud.annotation.EnableTaskingServer;

@ConditionalOnAnnotation(EnableTaskingServer.class)
@ComponentScan
@Configuration
public class DistributedAutomaticTaskingServerConfiguration  implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext = null;
	
	/**   
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)   
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		DistributedAutomaticTaskingServerConfiguration.applicationContext = applicationContext;
	}
	
	public static <T> T getBean(Class<T> clazz) {
		return applicationContext.getBean(clazz);
	}

}
