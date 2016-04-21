package com.misternerd.resteasytox.swift.objects;

public class SwiftEnumItem extends Buildable
{
	private final String name;

	private final String value;


	public SwiftEnumItem(String name, String value)
	{
		super();
		this.name = name;
		this.value = value;
	}


	@Override
	public void build(StringBuilder sb)
	{
		sb.append("case ").append(name).append(" = \"").append(value).append("\"");
	}
}
