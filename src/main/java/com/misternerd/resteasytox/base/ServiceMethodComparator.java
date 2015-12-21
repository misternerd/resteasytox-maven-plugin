package com.misternerd.resteasytox.base;

import java.util.Comparator;

public class ServiceMethodComparator implements Comparator<ServiceMethod>
{

	@Override
	public int compare(ServiceMethod method1, ServiceMethod method2)
	{
		if (method1 == null || method2 == null)
		{
			throw new NullPointerException("One method is null");
		}

		return method1.name.compareTo(method2.name);
	}

}
