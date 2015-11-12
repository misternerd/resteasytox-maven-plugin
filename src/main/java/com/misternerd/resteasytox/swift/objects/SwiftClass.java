package com.misternerd.resteasytox.swift.objects;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SwiftClass extends SwiftFile
{
	private final String superClass;

	private final List<SwiftProperty> properties = new ArrayList<>();


	public SwiftClass(Path outputPath, String name, String superClass)
	{
		super(outputPath, name);
		this.superClass = superClass;
	}


	public void addProperty(SwiftProperty property)
	{
		properties.add(property);
	}


	@Override
	public String build()
	{
		StringBuilder sb = new StringBuilder();

		buildFileHeader(sb);

		buildProperties(sb);

		buildFileFooter(sb);

		return sb.toString();
	}


	public void buildFileHeader(StringBuilder sb)
	{
		sb.append("class ").append(name);

		if (superClass != null)
		{
			sb.append(": ").append(superClass);
		}

		sb.append(" {");
	}


	private void buildProperties(StringBuilder sb)
	{
		for (SwiftProperty property : properties)
		{
			sb.append("\n\t");
			property.build(sb);
		}
	}
}
