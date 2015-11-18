package com.misternerd.resteasytox.swift.objects;

import java.nio.file.Path;
import java.util.ArrayList;

import com.misternerd.resteasytox.swift.helper.BuildableHelper;

public class SwiftClass extends SwiftFile
{
	private final String superClass;

	private final ArrayList<SwiftProperty> properties = new ArrayList<>();

	private final ArrayList<SwiftProperty> superProperties = new ArrayList<>();

	private final ArrayList<SwiftProperty> constants = new ArrayList<>();

	private final ArrayList<SwiftMethod> methods = new ArrayList<>();

	private boolean includeConstructor = false;

	private boolean includeAlamofire = false;

	private boolean includeJSONHelper = false;


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


	public void addSuperProperty(SwiftProperty property)
	{
		superProperties.add(property);
	}


	/**
	 * Defaults to false
	 */
	public void setIncludeConstructor(boolean includeConstructor)
	{
		this.includeConstructor = includeConstructor;
	}


	/**
	 * Defaults to false
	 */
	public void setIncludeAlamofire(boolean includeAlamofire)
	{
		this.includeAlamofire = includeAlamofire;
	}


	/**
	 * Defaults to false
	 */
	public void setIncludeJSONHelper(boolean includeJSONHelper)
	{
		this.includeJSONHelper = includeJSONHelper;
	}


	@Override
	public String build()
	{
		StringBuilder sb = new StringBuilder();

		int indent = 0;
		buildImports(sb);
		buildFileHeader(sb);

		indent++;
		buildConstants(sb, indent);
		buildProperties(sb, indent);

		if (includeConstructor)
		{
			buildConstructor(sb, indent);
		}

		if (includeJSONHelper)
		{
			buildJSONHelperInit(sb, indent);
		}

		if (includeAlamofire)
		{
			buildAlamofireParameter(sb, indent);
		}

		buildMethods(sb, indent);

		indent--;
		buildFileFooter(sb);

		return sb.toString();
	}


	private void buildImports(StringBuilder sb)
	{
		if (includeJSONHelper)
		{
			sb.append("import JSONHelper");
		}
	}


	private void buildFileHeader(StringBuilder sb)
	{
		BuildableHelper.addSpace(sb);
		sb.append("class ").append(name);

		if (superClass != null)
		{
			sb.append(": ").append(superClass);
		}

		sb.append(" {");
	}


	private void buildConstructor(StringBuilder sb, int indent)
	{
		SwiftConstructorMethod constructor = new SwiftConstructorMethod(properties, superProperties, (superClass != null));
		constructor.buildNewline(sb, indent);
	}


	private void buildJSONHelperInit(StringBuilder sb, int indent)
	{
		BuildableHelper.addSpace(sb);
		BuildableHelper.addIndent(sb, indent);
		sb.append("required init(data: [String: AnyObject]) {");

		indent++;
		for (SwiftProperty property : properties)
		{
			BuildableHelper.addNewline(sb);
			BuildableHelper.addIndent(sb, indent);
			sb.append(property.getName()).append(" <-- data[\"").append(property.getName()).append("\"]");
		}

		BuildableHelper.addNewline(sb);
		BuildableHelper.addIndent(sb, indent);
		sb.append("super.init(data: data)");

		indent--;
		BuildableHelper.addNewline(sb);
		BuildableHelper.addIndent(sb, indent);
		sb.append("}");
	}


	private void buildAlamofireParameter(StringBuilder sb, int indent)
	{
		BuildableHelper.addSpace(sb);
		BuildableHelper.addIndent(sb, indent);
		sb.append("override func parameters(parameters: [String: AnyObject] = [:]) -> [String: AnyObject] {");

		indent++;
		BuildableHelper.addNewline(sb);
		BuildableHelper.addIndent(sb, indent);
		sb.append("var parameters = super.parameters(parameters)");

		for (SwiftProperty property : properties)
		{
			if (property.isOptional())
			{
				BuildableHelper.addNewline(sb);
				BuildableHelper.addIndent(sb, indent);
				sb.append("if let ").append(property.getName()).append(" = ").append(property.getName()).append(" {");
				indent++;
			}
			BuildableHelper.addNewline(sb);
			BuildableHelper.addIndent(sb, indent);
			sb.append("parameters[\"").append(property.getName()).append("\"] = ").append(property.getName());

			if (property.isOptional())
			{
				indent--;
				BuildableHelper.addNewline(sb);
				BuildableHelper.addIndent(sb, indent);
				sb.append("}");
			}
		}

		BuildableHelper.addNewline(sb);
		BuildableHelper.addIndent(sb, indent);
		sb.append("return parameters");

		indent--;
		BuildableHelper.addNewline(sb);
		BuildableHelper.addIndent(sb, indent);
		sb.append("}");
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
