package com.misternerd.resteasytox.swift.objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import com.misternerd.resteasytox.swift.helper.BuildableHelper;

public class SwiftFile
{

	protected final Path outputPath;

	protected final String name;

	private final ArrayList<SwiftProtocol> protocols = new ArrayList<>();

	private final ArrayList<SwiftEnum> enums = new ArrayList<>();

	private final ArrayList<SwiftClass> classes = new ArrayList<>();


	public SwiftFile(Path outputPath, String name)
	{
		super();
		this.name = name;
		this.outputPath = outputPath;
	}


	public String build()
	{
		int indent = 0;
		StringBuilder sb = new StringBuilder();

		buildProtocols(sb, indent);
		buildEnums(sb, indent);
		buildClasses(sb, indent);

		return sb.toString();
	}


	public void addProtocol(SwiftProtocol protocol)
	{
		protocols.add(protocol);
	}


	public void addEnum(SwiftEnum enumeration)
	{
		enums.add(enumeration);
	}


	public void addClass(SwiftClass cls)
	{
		classes.add(cls);
	}


	public void writeToFile() throws IOException
	{
		String content = build();
		Files.write(outputPath, content.toString().getBytes("UTF-8"));
	}


	protected void buildProtocols(StringBuilder sb, int indent)
	{
		if (!protocols.isEmpty())
		{
			BuildableHelper.addNewline(sb);

			for (SwiftProtocol protocol : protocols)
			{
				protocol.build(sb);
			}
		}
	}


	protected void buildEnums(StringBuilder sb, int indent)
	{
		if (!enums.isEmpty())
		{
			BuildableHelper.addNewline(sb);

			for (SwiftEnum enumeration : enums)
			{
				enumeration.build(sb);
			}
		}
	}


	protected void buildClasses(StringBuilder sb, int indent)
	{
		if (!classes.isEmpty())
		{
			BuildableHelper.addNewline(sb);

			for (SwiftClass cls : classes)
			{
				cls.build(sb);
			}
		}
	}

}