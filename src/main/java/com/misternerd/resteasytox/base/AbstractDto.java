package com.misternerd.resteasytox.base;

import java.util.Map;

/**
 * Denotes a DTO class which is abstract and has some sub-classes
 * extending it.
 * In frontend languages, these need to be handled in marshalling/unmarshalling.
 */
public class AbstractDto
{

	public final Class<?> abstractClass;

	public final Map<String, Class<?>> implementingClassesByTypeName;

	public final String typeInfoField;


	public AbstractDto(Class<?> abstractClass, Map<String, Class<?>> implementingClassesByTypeName, String typeInfoField)
	{
		this.abstractClass = abstractClass;
		this.implementingClassesByTypeName = implementingClassesByTypeName;
		this.typeInfoField = typeInfoField;
	}

}
