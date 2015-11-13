package com.misternerd.resteasytox.swift.objects;

public class SwiftProperty extends Buildable
{
	protected final boolean isStatic;

	protected boolean isFinal;

	protected SwiftType type;

	protected String name;

	protected boolean isOptional;


	public SwiftProperty(boolean isStatic, boolean isFinal, SwiftType type, String name, boolean isOptional)
	{
		super();
		this.isStatic = isStatic;
		this.isFinal = isFinal;
		this.type = type;
		this.name = name;
		this.isOptional = isOptional;
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
	}
}
