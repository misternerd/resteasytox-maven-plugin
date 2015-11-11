package com.misternerd.resteasytox.swift.objects;

import java.nio.file.Path;

public class SwiftClass extends SwiftFile
{
	private final String superClass;


	public SwiftClass(Path outputPath, String name, String superClass)
	{
		super(outputPath, name);
		this.superClass = superClass;
	}


	@Override
	public String build()
	{
		StringBuilder sb = new StringBuilder();

		buildFileHeader(sb);

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


	public void buildFileFooter(StringBuilder sb)
	{
		sb.append("}");
	}
}
