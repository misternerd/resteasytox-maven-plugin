package com.misternerd.resteasytox.swift.objects;

public class SwiftProperty extends Buildable implements ParameterBuildable
{
	private final boolean isStatic;

	private boolean isFinal;

	private SwiftType type;

	private String name;

	private boolean isOptional;

	private final String defaultValue;


	public SwiftProperty(boolean isStatic, boolean isFinal, SwiftType type, String name, boolean isOptional, String defaultValue)
	{
		super();
		this.isStatic = isStatic;
		this.isFinal = isFinal;
		this.type = type;
		this.name = name;
		this.isOptional = isOptional;
		this.defaultValue = defaultValue;
	}


	public String getName()
	{
		return name;
	}


	public boolean isOptional()
	{
		return isOptional;
	}


	public String lineForConstructor()
	{
		return "self." + name + " = " + name;
	}


	@Override
	public void buildParameter(StringBuilder sb)
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


	@Override
	public void build(StringBuilder sb)
	{
		if (isStatic)
		{
			sb.append("static ");
		}

		if (isFinal)
		{
			sb.append("let ");
		}
		else
		{
			sb.append("var ");
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
