package com.misternerd.resteasytox.swift.helper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.misternerd.resteasytox.JaxWsAnnotations;

public class ReflectionHelper
{
	/**
	 * Will check the nillable flag of the xmlElement Annotation.
	 * 
	 * @return true, if the nillable flag is set.
	 */
	public static boolean isOptional(Field field, JaxWsAnnotations jaxWsAnnotations)
	{

		Annotation xmlElementAnnotation = field.getAnnotation(jaxWsAnnotations.xmlElement);

		if (xmlElementAnnotation == null)
		{
			return false;
		}

		try
		{
			Method nillableMethod = xmlElementAnnotation.annotationType().getMethod("nillable");
			boolean nillable = (boolean) nillableMethod.invoke(xmlElementAnnotation);

			return nillable;
		}
		catch (Exception e)
		{
			// Ignore annotation for now
		}

		return false;
	}
}
