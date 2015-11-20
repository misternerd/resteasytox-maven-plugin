package com.misternerd.resteasytox.swift.objects;

import java.util.ArrayList;
import java.util.List;

import com.misternerd.resteasytox.swift.helper.BuildableHelper;

public class SwiftEnum extends Buildable
{
	private final List<SwiftEnumItem> enumItems = new ArrayList<>();

	private final String name;


	public SwiftEnum(String name)
	{
		super();
		this.name = name;
	}


	public void addEnumItem(String name, String value)
	{
		SwiftEnumItem enumItem = new SwiftEnumItem(name, value);
		enumItems.add(enumItem);
	}


	@Override
	public void build(StringBuilder sb)
	{
		BuildableHelper.addSpace(sb);
		buildEnumHeader(sb);
		buildEnum(sb);
		buildEnumFooter(sb);
	}


	private void buildEnumHeader(StringBuilder sb)
	{
		sb.append("enum ").append(name).append(": String {");
	}


	private void buildEnum(StringBuilder sb)
	{
		for (SwiftEnumItem item : enumItems)
		{
			sb.append("\n\t");
			item.build(sb);
		}
	}


	private void buildEnumFooter(StringBuilder sb)
	{
		BuildableHelper.addNewline(sb);
		sb.append("}");
	}
}
