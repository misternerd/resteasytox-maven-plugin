package com.misternerd.resteasytox.base;

import java.util.Comparator;

public class MethodParameterComparator implements Comparator<MethodParameter>
{

	@Override
	public int compare(MethodParameter method1, MethodParameter method2)
	{
		if(method1 == null || method2 == null)
		{
			throw new NullPointerException("Either method1 or method2 is null");
		}

		return method1.name.compareTo(method2.name);
	}

}
