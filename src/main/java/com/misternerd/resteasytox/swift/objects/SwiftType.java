package com.misternerd.resteasytox.swift.objects;

public class SwiftType extends Buildable
{
	public static final String BOOL = "Bool";
	public static final String STRING = "String";
	public static final String INT = "Int";
	public static final String FLOAT = "Float";
	public static final String DOUBLE = "Double";
	public static final String NSDATA = "NSData";

	private final String name;

	private boolean isArray;


	public SwiftType(String name)
	{
		this(name, false);
	}


	public SwiftType(String name, boolean isArray)
	{
		this.name = name;
		this.isArray = isArray;
	}


	public String getName()
	{
		return name;
	}


	public boolean isArray()
	{
		return isArray;
	}


	public void setArray(boolean isArray)
	{
		this.isArray = isArray;
	}


	@Override
	public void build(StringBuilder sb)
	{
		if (isArray)
		{
			sb.append("[");
		}

		sb.append(name);

		if (isArray)
		{
			sb.append("]");
		}
	}

}
