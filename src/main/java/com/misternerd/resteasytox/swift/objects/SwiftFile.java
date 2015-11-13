package com.misternerd.resteasytox.swift.objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.misternerd.resteasytox.swift.helper.BuildableHelper;

public abstract class SwiftFile
{

	protected final Path outputPath;

	protected final String name;


	public SwiftFile(Path outputPath, String name)
	{
		super();
		this.name = name;
		this.outputPath = outputPath;
	}


	abstract String build();


	public void writeToFile() throws IOException
	{
		String content = build();
		Files.write(outputPath, content.toString().getBytes("UTF-8"));
	}


	protected void buildFileFooter(StringBuilder sb)
	{
		BuildableHelper.addNewline(sb);
		sb.append("}");
	}

}