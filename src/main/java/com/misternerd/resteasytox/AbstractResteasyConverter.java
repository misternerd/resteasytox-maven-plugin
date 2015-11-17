package com.misternerd.resteasytox;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.misternerd.resteasytox.base.FieldComparator;

public abstract class AbstractResteasyConverter
{

	protected final Path outputPath;

	protected final String javaPackageName;

	protected final RestServiceLayout layout;


	public AbstractResteasyConverter(Path outputPath, String javaPackageName, RestServiceLayout layout)
	{
		this.outputPath = outputPath;
		this.javaPackageName = javaPackageName;
		this.layout = layout;
	}


	public abstract void convert() throws Exception;


	protected List<Field> getPublicClassConstants(Class<?> cls)
	{
		List<Field> constants = new ArrayList<>();

		for (Field field : cls.getDeclaredFields())
		{
			if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && !field.isEnumConstant())
			{
				constants.add(field);
			}
		}

		return constants;
	}


	protected List<Field> getEnumConstants(Class<?> cls)
	{
		List<Field> result = new ArrayList<>();

		for (Field field : cls.getDeclaredFields())
		{
			if (field.isEnumConstant())
			{
				result.add(field);
			}
		}

		Collections.sort(result, new FieldComparator());

		return result;
	}


	protected List<Field> getPrivateAndProtectedMemberVariables(Class<?> cls, boolean withSuperclassFields)
	{
		List<Field> members = new ArrayList<>();

		if(withSuperclassFields && cls.getSuperclass() != null)
		{
			for(Field field : cls.getSuperclass().getDeclaredFields())
			{
				if (isPrivateOrProtectedMemberField(field))
				{
					members.add(field);
				}
			}
		}

		for (Field field : cls.getDeclaredFields())
		{
			if (isPrivateOrProtectedMemberField(field))
			{
				members.add(field);
			}
		}

		Collections.sort(members, new FieldComparator());

		return members;
	}


	private boolean isPrivateOrProtectedMemberField(Field field)
	{
		return !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && !Modifier.isPublic(field.getModifiers());
	}

}
