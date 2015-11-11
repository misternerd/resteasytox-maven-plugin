package com.misternerd.resteasytox.swift;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SwiftClass
{
	private final Path outputPath;

	private final String className;

	private final String superName;


	public SwiftClass(Path outputPath, String className, String superName)
	{
		this.outputPath = outputPath;
		this.className = className;
		this.superName = superName;
	}


	public String build()
	{
		StringBuilder sb = new StringBuilder();

		buildFileHeader(sb);
		buildFileFooter(sb);

		return sb.toString();
	}


	public void buildFileHeader(StringBuilder sb)
	{
		sb.append("class ").append(className);

		if (superName != null)
		{
			sb.append(": ").append(superName);
		}

		sb.append(" {");
	}


	public void buildFileFooter(StringBuilder sb)
	{
		sb.append("}");
	}


	public void writeToFile() throws IOException
	{
		String content = build();
		Files.write(outputPath, content.toString().getBytes("UTF-8"));
	}
}
