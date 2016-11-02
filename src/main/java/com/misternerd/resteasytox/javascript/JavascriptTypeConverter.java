package com.misternerd.resteasytox.javascript;

import com.misternerd.resteasytox.RestServiceLayout;
import com.misternerd.resteasytox.javascript.objects.types.JavascriptArrayType;
import com.misternerd.resteasytox.javascript.objects.types.JavascriptBasicType;
import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maps Java types to Typescript types, which can be used in the .d.ts files.
 *
 * Date: 22.06.2016
 */
public class JavascriptTypeConverter
{


	private final RestServiceLayout layout;

	private final Map<String, JavascriptType> typeMap = new HashMap<>();


	public JavascriptTypeConverter(RestServiceLayout layout)
	{
		this.layout = layout;

		typeMap.put("boolean", JavascriptBasicType.BOOLEAN);
		typeMap.put("integer", JavascriptBasicType.NUMBER);
		typeMap.put("int",  JavascriptBasicType.NUMBER);
		typeMap.put("long",  JavascriptBasicType.NUMBER);
		typeMap.put("float",  JavascriptBasicType.NUMBER);
		typeMap.put("double", JavascriptBasicType.NUMBER);
		typeMap.put("string", JavascriptBasicType.STRING);
		typeMap.put("void", JavascriptBasicType.VOID);
		typeMap.put("date", JavascriptBasicType.NUMBER);
	}


	public JavascriptType getJavascriptType(Field field)
	{
		Class<?> cls = field.getType();

		if (field.getGenericType() instanceof ParameterizedType)
		{
			ParameterizedType parameterized = (ParameterizedType) field.getGenericType();
			Type[] types = parameterized.getActualTypeArguments();

			if (List.class.isAssignableFrom(cls) || Set.class.isAssignableFrom(cls))
			{
				JavascriptType basis = getJavascriptType((Class<?>) types[0]);
				return new JavascriptArrayType(basis);
			}

			if (Map.class.isAssignableFrom(cls))
			{
				//TODO Create map type
				return JavascriptBasicType.ANY;
			}
		}

		return getJavascriptType(cls);
	}


	public JavascriptType getJavascriptType(Class<?> type)
	{
		if (layout.getDtoClasses().contains(type) || layout.getRequestClasses().contains(type) || layout.getResponseClasses().contains(type))
		{
			return new JavascriptType(type.getSimpleName());
		}

		if (List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type))
		{
			//TODO Any method to get the real type here?
			return new JavascriptArrayType(JavascriptBasicType.ANY);
		}

		if (Map.class.isAssignableFrom(type))
		{
			//TODO Create map type
			return JavascriptBasicType.ANY;
		}

		if(typeMap.containsKey(type.getSimpleName().toLowerCase()))
		{
			return typeMap.get(type.getSimpleName().toLowerCase());
		}

		return JavascriptBasicType.ANY;
	}


}
