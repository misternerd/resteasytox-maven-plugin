package com.misternerd.resteasytox.php;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.misternerd.resteasytox.RestServiceLayout;
import com.misternerd.resteasytox.php.baseObjects.PhpBasicType;
import com.misternerd.resteasytox.php.baseObjects.PhpNamespace;
import com.misternerd.resteasytox.php.baseObjects.PhpType;

public class PhpTypeLib
{

	private final PhpNamespace phpBaseNamespace;

	private final String javaPackageName;

	private final RestServiceLayout layout;

	private final Map<String, PhpType> typeMap = new HashMap<>();


	public PhpTypeLib(PhpNamespace phpBaseNamespace, String javaPackageName, RestServiceLayout layout)
	{
		this.phpBaseNamespace = phpBaseNamespace;
		this.javaPackageName = javaPackageName;
		this.layout = layout;

		typeMap.put("boolean", PhpBasicType.BOOLEAN);
		typeMap.put("integer", PhpBasicType.INT);
		typeMap.put("int", PhpBasicType.INT);
		typeMap.put("float", PhpBasicType.FLOAT);
		typeMap.put("double", PhpBasicType.FLOAT);
		typeMap.put("string", PhpBasicType.STRING);
		typeMap.put("date", new PhpType(new PhpNamespace(phpBaseNamespace, "dto"), "Date", null, true, true, false));
	}


	public PhpType getPhpType(Field field, boolean nullable)
	{
		Class<?> cls = field.getType();

		if (field.getGenericType() instanceof ParameterizedType)
		{
			ParameterizedType parameterized = (ParameterizedType) field.getGenericType();
			Type[] types = parameterized.getActualTypeArguments();

			if (List.class.isAssignableFrom(cls))
			{
				PhpType phpType = getPhpType((Class<?>) types[0], nullable);
				return new PhpType(phpType.namespace, phpType.name, "[]", true, false, nullable);
			}

			if (Map.class.isAssignableFrom(cls))
			{
				return clonePhpTypeOverwriteNullable(PhpBasicType.ARRAY, nullable);
			}
		}

		return getPhpType(cls, nullable);
	}


	public PhpType getPhpType(Class<?> type, boolean nullable)
	{
		if (layout.getDtoClasses().contains(type))
		{
			return new PhpType(new PhpNamespace(phpBaseNamespace, "dto"), type.getSimpleName(), null, true, true, nullable);
		}

		if(layout.getRequestClasses().contains(type) || layout.getResponseClasses().contains(type))
		{
			String namespaceString = type.getPackage().getName().replace(javaPackageName + ".", "").replace(".", "\\");
			return new PhpType(new PhpNamespace(phpBaseNamespace, namespaceString), type.getSimpleName(), null, true, true, nullable);
		}

		if (Map.class.isAssignableFrom(type) || List.class.isAssignableFrom(type))
		{
			return clonePhpTypeOverwriteNullable(PhpBasicType.ARRAY, nullable);
		}

		if(typeMap.containsKey(type.getSimpleName().toLowerCase()))
		{
			return clonePhpTypeOverwriteNullable(typeMap.get(type.getSimpleName().toLowerCase()), nullable);
		}

		return clonePhpTypeOverwriteNullable(PhpBasicType.MIXED, nullable);
	}


	private PhpType clonePhpTypeOverwriteNullable(PhpType type, boolean nullable)
	{
		if(type instanceof PhpBasicType)
		{
			return new PhpBasicType(type.name, type.addToTypeComment, type.addAsTypeHint, nullable);
		}
		else
		{
			return new PhpType(type.namespace, type.name, type.suffix, type.addToTypeComment, type.addAsTypeHint, nullable);
		}
	}

}
