package com.misternerd.resteasytox.swift.objects;

import java.nio.file.Path;
import java.util.ArrayList;

public class SwiftClass extends SwiftFile
{
	private final String superClass;

	private final ArrayList<SwiftProperty> properties = new ArrayList<>();

	private boolean includeConstructor = false;


	public SwiftClass(Path outputPath, String name, String superClass)
	{
		super(outputPath, name);
		this.superClass = superClass;
	}


	public void addProperty(SwiftProperty property)
	{
		properties.add(property);
	}


	/**
	 * Defaults to false
	 */
	public void setIncludeConstructor(boolean includeConstructor)
	{
		this.includeConstructor = includeConstructor;
	}


	@Override
	public String build()
	{
		StringBuilder sb = new StringBuilder();
		int indent = 0;

		buildFileHeader(sb);

		indent++;
		buildProperties(sb, indent);
		buildConstructor(sb, indent);
		indent--;

		buildFileFooter(sb);

		return sb.toString();
	}


	private void buildFileHeader(StringBuilder sb)
	{
		sb.append("class ").append(name);

		if (superClass != null)
		{
			sb.append(": ").append(superClass);
		}

		sb.append(" {");
	}


	private void buildConstructor(StringBuilder sb, int indent)
	{
		if (includeConstructor)
		{
			SwiftConstructorMethod constructor = new SwiftConstructorMethod(name, properties);
			constructor.buildNewline(sb, indent);
		}
	}


	private void buildProperties(StringBuilder sb, int indent)
	{
		if (!properties.isEmpty())
		{
			for (SwiftProperty property : properties)
			{
				property.buildNewline(sb, indent);
			}
		}
	}
}
