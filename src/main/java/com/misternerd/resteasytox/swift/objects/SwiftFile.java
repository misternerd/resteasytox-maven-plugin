package com.misternerd.resteasytox.swift.objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.misternerd.resteasytox.swift.helper.BuildableHelper;

public class SwiftFile
{

	protected final Path outputPath;

	protected final String name;

	private final ArrayList<SwiftEnum> enums = new ArrayList<>();

	private final ArrayList<SwiftProtocol> protocols = new ArrayList<>();

	private final ArrayList<SwiftExtension> extensions = new ArrayList<>();

	private final ArrayList<SwiftMethod> methods = new ArrayList<>();

	private final ArrayList<SwiftClass> classes = new ArrayList<>();

	private final List<String> imports = new ArrayList<>();


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

		buildImports(sb, indent);
		buildMethods(sb, indent);
		buildEnums(sb, indent);
		buildProtocols(sb, indent);
		buildExtensions(sb, indent);
		buildClasses(sb, indent);

		return sb.toString();
	}


	public void addImport(String importModule)
	{
		imports.add(importModule);
	}


	public void addProtocol(SwiftProtocol protocol)
	{
		protocols.add(protocol);
	}


	public void addExtension(SwiftExtension extension)
	{
		extensions.add(extension);
	}
	
	
	/**
	 * @param method Only static methods makes sense in this scope
	 */
	public void addMethod(SwiftMethod method)
	{
		methods.add(method);
	}


	public void addExtensions(List<SwiftExtension> extensions)
	{
		this.extensions.addAll(extensions);
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


	private void buildImports(StringBuilder sb, int indent)
	{
		sb.append("import Foundation");
		for (String moduleImport : imports)
		{
			BuildableHelper.addNewline(sb);
			sb.append("import ").append(moduleImport);
		}
	}


	private void buildMethods(StringBuilder sb, int indent)
	{
		for (SwiftMethod method : methods)
		{
			BuildableHelper.addNewline(sb);
			method.buildNewline(sb, indent);
		}
	}

	private void buildExtensions(StringBuilder sb, int indent)
	{
		if (!extensions.isEmpty())
		{
			BuildableHelper.addNewline(sb);

			for (SwiftExtension extension : extensions)
			{
				BuildableHelper.addNewline(sb);
				extension.build(sb);
			}
		}
	}


	private void buildProtocols(StringBuilder sb, int indent)
	{
		if (!protocols.isEmpty())
		{
			BuildableHelper.addNewline(sb);

			for (SwiftProtocol protocol : protocols)
			{
				BuildableHelper.addNewline(sb);
				protocol.build(sb);
			}
		}
	}


	private void buildEnums(StringBuilder sb, int indent)
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


	private void buildClasses(StringBuilder sb, int indent)
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