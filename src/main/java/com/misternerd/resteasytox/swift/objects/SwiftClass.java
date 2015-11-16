package com.misternerd.resteasytox.swift.objects;

import java.nio.file.Path;
import java.util.ArrayList;

import com.misternerd.resteasytox.swift.helper.BuildableHelper;

public class SwiftClass extends SwiftFile
{
	private final String superClass;

	private final ArrayList<SwiftProperty> properties = new ArrayList<>();

	private final ArrayList<SwiftProperty> constants = new ArrayList<>();

	private final ArrayList<SwiftMethod> methods = new ArrayList<>();

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


	public void addConstant(SwiftProperty property)
	{
		constants.add(property);
	}


	public void addMethod(SwiftMethod method)
	{
		methods.add(method);
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
		buildConstants(sb, indent);
		buildProperties(sb, indent);
		buildConstructor(sb, indent);
		buildMethods(sb, indent);
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
			SwiftConstructorMethod constructor = new SwiftConstructorMethod(properties);
			constructor.buildNewline(sb, indent);
		}
	}


	private void buildMethods(StringBuilder sb, int indent)
	{
		if (!methods.isEmpty())
		{
			BuildableHelper.addNewline(sb);

			for (SwiftMethod method : methods)
			{
				method.buildNewline(sb, indent);
			}
		}
	}


	private void buildConstants(StringBuilder sb, int indent)
	{
		if (!constants.isEmpty())
		{
			BuildableHelper.addNewline(sb);

			for (SwiftProperty constant : constants)
			{
				constant.buildNewline(sb, indent);
			}
		}
	}


	private void buildProperties(StringBuilder sb, int indent)
	{
		if (!properties.isEmpty())
		{
			BuildableHelper.addNewline(sb);

			for (SwiftProperty property : properties)
			{
				property.buildNewline(sb, indent);
			}
		}
	}
}
