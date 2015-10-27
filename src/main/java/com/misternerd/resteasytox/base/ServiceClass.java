package com.misternerd.resteasytox.base;

import java.util.List;

public class ServiceClass
{

	public final String name;

	public final String path;

	public final List<ServiceMethod> methods;


	public ServiceClass(String name, String path, List<ServiceMethod> methods)
	{
		this.name = name;
		this.path = path;
		this.methods = methods;
	}


	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Service(name=").append(name).append(", path=").append(path).append(", methods=(");

		for (ServiceMethod method : methods)
		{
			sb.append("\n\t").append(method.toString());
		}

		sb.append("\n)");

		return sb.toString();
	}

}
