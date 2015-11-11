package com.misternerd.resteasytox.swift.objects;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SwiftEnum extends SwiftFile
{
	private final List<SwiftEnumItem> enumItems = new ArrayList<>();

	public SwiftEnum(Path outputPath, String name)
	{
		super(outputPath, name);
	}
	
	public void addEnumItem(SwiftEnumItem enumItem){
		enumItems.add(enumItem);
	}


	@Override
	public String build()
	{
		StringBuilder sb = new StringBuilder();

		buildFileHeader(sb);
		buildEnum(sb);
		buildFileFooter(sb);

		return sb.toString();
	}


	private void buildFileHeader(StringBuilder sb)
	{
		sb.append("enum ").append(name).append(": String {");
	}

	private void buildEnum(StringBuilder sb){
		for (SwiftEnumItem item : enumItems)
		{
			sb.append("\n\t");
			item.build(sb);
		}
	}

	private void buildFileFooter(StringBuilder sb)
	{
		sb.append("\n}");
	}
}
