package com.misternerd.resteasytox.swift.objects;

public class SwiftProperty implements Buildable
{
	private final boolean isStatic;

	private boolean isFinal;

	private SwiftType type;

	private String name;

	private boolean isOptional;


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
