package com.misternerd.resteasytox.swift.helper;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.misternerd.resteasytox.swift.objects.SwiftType;

public class SwiftTypeHelper
{
	private static final Map<String, String> typeMap = new HashMap<>();


	static
	{
		typeMap.put("boolean", SwiftType.BOOL);
		typeMap.put("time", SwiftType.STRING); // Workaround. Format: "hh:mm:ss"
		typeMap.put("string", SwiftType.STRING);
		typeMap.put("int", SwiftType.INT);
		typeMap.put("integer", SwiftType.INT);
		typeMap.put("long", SwiftType.INT);
		typeMap.put("float", SwiftType.FLOAT);
		typeMap.put("double", SwiftType.DOUBLE);
		typeMap.put("boolean", SwiftType.BOOL);
		typeMap.put("byte[]", SwiftType.NSDATA);
		typeMap.put("date", SwiftType.NSDATE);
	}


	public static SwiftType getSwiftType(Field field)
	{
		Class<?> type = field.getType();

		SwiftType swiftType = null;
		if (field.getGenericType() instanceof ParameterizedType)
		{
			if (List.class.isAssignableFrom(type))
			{
				ParameterizedType parameterized = (ParameterizedType) field.getGenericType();
				Type[] types = parameterized.getActualTypeArguments();

				swiftType = getSwiftTypeFromClass((Class<?>) types[0]);
				swiftType.setArray(true);
			}
			else if (Map.class.isAssignableFrom(type))
			{
				// TODO: We need to handle Maps
			}
		}

		if (swiftType == null)
		{
			swiftType = getSwiftTypeFromClass(type);
		}

		return swiftType;
	}


	public static SwiftType getSwiftTypeFromClass(Class<?> type)
	{
		String name = type.getSimpleName();

		return getSwiftTypeFromString(name);
	}


	public static SwiftType getSwiftTypeFromString(String name)
	{
		if (typeMap.containsKey(name.toLowerCase()))
		{
			name = typeMap.get(name.toLowerCase());
		}

		return new SwiftType(name);
	}
}
