package com.misternerd.resteasytox.swift.objects;

public class SwiftParameter extends SwiftProperty
{

	private final String defaultValue;


	public SwiftParameter(boolean isStatic, boolean isFinal, SwiftType type, String name, boolean isOptional, String defaultValue)
	{
		super(isStatic, isFinal, type, name, isOptional);
		this.defaultValue = defaultValue;
	}


	public SwiftParameter(SwiftProperty property, String defaultValue)
	{
		super(property.isStatic, property.isFinal, property.type, property.name, property.isOptional);
		this.defaultValue = defaultValue;
	}


	public String lineForConstructor()
	{
		return "self." + name + " = " + name;
	}


	@Override
	public void build(StringBuilder sb)
	{
		if (isFinal)
		{
			sb.append("let ");
		}

		sb.append(name).append(": ");

		type.build(sb);

		if (isOptional)
		{
			sb.append("?");
		}

		if (defaultValue != null)
		{
			sb.append(" = ").append(defaultValue);
		}
	}

}
