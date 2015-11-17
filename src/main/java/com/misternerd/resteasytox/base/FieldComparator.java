package com.misternerd.resteasytox.base;

import java.lang.reflect.Field;
import java.util.Comparator;

public class FieldComparator implements Comparator<Field>
{

	@Override
	public int compare(Field field1, Field field2)
	{
		if(field1 == null || field2 == null)
		{
			throw new NullPointerException("One of the two fields is null");
		}

		return field1.getName().compareTo(field2.getName());
	}

}
