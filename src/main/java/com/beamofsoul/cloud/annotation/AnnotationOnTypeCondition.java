package com.beamofsoul.cloud.annotation;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class AnnotationOnTypeCondition implements Condition {

	/**   
	 * @see org.springframework.context.annotation.Condition#matches(org.springframework.context.annotation.ConditionContext, org.springframework.core.type.AnnotatedTypeMetadata)   
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(ConditionalOnAnnotation.class.getName());
		Class<? extends Annotation> annotationType = (Class<? extends Annotation>) annotationAttributes.get("value");
		String[] beanNamesForAnnotation = context.getBeanFactory().getBeanNamesForAnnotation(annotationType);
		return (beanNamesForAnnotation != null && beanNamesForAnnotation.length > 0);
	}

}
