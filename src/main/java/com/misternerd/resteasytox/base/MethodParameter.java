package com.misternerd.resteasytox.base;

public class MethodParameter
{

	public final String name;

	public final Class<? extends Object> type;


	public MethodParameter(String name, Class<? extends Object> type)
	{
		this.name = name;
		this.type = type;
	}


	@Override
	public String toString()
	{
		return String.format("Param(name=%s, type=%s)", name, type.getSimpleName());
	}

}
