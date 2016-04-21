package com.misternerd.resteasytox.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.misternerd.resteasytox.JaxWsAnnotations;
import com.misternerd.resteasytox.base.AbstractDto;

public class ReflectionHelper
{
	/**
	 * Will check the nillable flag of the @XmlElement annotation.
	 * For Swift, this declares if a field is optional in Swift.
	 * 
	 * @return true, if the nillable flag is set.
	 */
	public static boolean isNullableField(Field field, JaxWsAnnotations jaxWsAnnotations)
	{

		Annotation xmlElementAnnotation = field.getAnnotation(jaxWsAnnotations.xmlElement);

		if (xmlElementAnnotation == null)
		{
			return false;
		}

		try
		{
			Method nillableMethod = xmlElementAnnotation.annotationType().getMethod("nillable");

			return (boolean) nillableMethod.invoke(xmlElementAnnotation);
		}
		catch (Exception e)
		{
			// Ignore annotation for now
		}

		return false;
	}


	public static boolean isAbstractDto(Field field, Map<Class<?>, AbstractDto> abstractDtoMap) {
		Class<?> type = field.getType();

		if (field.getGenericType() instanceof ParameterizedType)
		{
			if (List.class.isAssignableFrom(type))
			{
				ParameterizedType parameterized = (ParameterizedType) field.getGenericType();
				Type[] types = parameterized.getActualTypeArguments();

				type = (Class<?>) types[0];
			}
			else if (Map.class.isAssignableFrom(type))
			{
				// TODO: We need to handle Maps
			}
		}

		return abstractDtoMap.containsKey(type);
	}

}
