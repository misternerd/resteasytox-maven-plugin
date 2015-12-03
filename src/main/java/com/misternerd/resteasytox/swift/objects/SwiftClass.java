package com.misternerd.resteasytox.swift.objects;

import java.util.ArrayList;
import java.util.List;

import com.misternerd.resteasytox.swift.helper.BuildableHelper;
import com.misternerd.resteasytox.swift.helper.SwiftMarshallingHelper;

public class SwiftClass extends Buildable
{
	private final String superClass;

	private final String name;

	private final ArrayList<SwiftProperty> properties = new ArrayList<>();

	private final ArrayList<SwiftProperty> superProperties = new ArrayList<>();

	private final ArrayList<SwiftProperty> constants = new ArrayList<>();

	private final ArrayList<SwiftMethod> methods = new ArrayList<>();

	private boolean includeConstructor = false;

	private boolean includeMarshalling = false;

	private boolean includeUnmarshalling = false;

	private boolean supportObjC = false;

	private boolean overrideProtocols = false;


	public SwiftClass(String name, String superClass, boolean supportObjC)
	{
		this.name = name;
		this.superClass = superClass;
		this.supportObjC = supportObjC;
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
	public void setIncludeMarshalling(boolean includeMarshalling)
	{
		this.includeMarshalling = includeMarshalling;
	}


	/**
	 * Defaults to false
	 */
	public void setIncludeUnmarshalling(boolean includeUnmarshalling)
	{
		this.includeUnmarshalling = includeUnmarshalling;
	}


	/**
	 * Defaults to false
	 */
	public void setOverrideProtocols(boolean overrideProtocols)
	{
		this.overrideProtocols = overrideProtocols;
	}


	/**
	 * Defaults to false
	 */
	public void setSupportObjC(boolean supportObjC)
	{
		this.supportObjC = supportObjC;
	}


	@Override
	public void build(StringBuilder sb)
	{
		int indent = 0;

		buildFileHeader(sb);

		indent++;
		buildConstants(sb, indent);
		buildProperties(sb, indent);

		if (includeConstructor)
		{
			buildConstructor(sb, indent);
		}

		if (includeUnmarshalling)
		{
			createUnmarshalling();
		}

		if (includeMarshalling)
		{
			createMarshalling();
		}

		buildMethods(sb, indent);

		indent--;
		buildClassFooter(sb);
	}


	private void buildFileHeader(StringBuilder sb)
	{
		BuildableHelper.addSpace(sb);
		sb.append("class ").append(name);

		if (superClass != null || supportObjC || includeMarshalling || includeUnmarshalling)
		{

			sb.append(": ");

			boolean addComma = false;

			if (superClass != null)
			{
				sb.append(superClass);
				addComma = true;
			}
			else if (supportObjC)
			{
				sb.append("NSObject");
				addComma = true;
			}

			if (!overrideProtocols)
			{
				if (includeMarshalling)
				{
					if (addComma)
					{
						sb.append(", ");
					}
					sb.append(SwiftMarshallingHelper.MARSHALLING_PROTOCOL);
					addComma = true;
				}

				if (includeUnmarshalling)
				{
					if (addComma)
					{
						sb.append(", ");
					}
					sb.append(SwiftMarshallingHelper.UNMARSHALLING_PROTOCOL);
					addComma = true;
				}
			}
		}

		sb.append(" {");
	}


	private void buildConstructor(StringBuilder sb, int indent)
	{
		if (!properties.isEmpty())
		{
			BuildableHelper.addNewline(sb);
			SwiftConstructorMethod constructor = new SwiftConstructorMethod(properties, superProperties, (superClass != null));
			constructor.buildNewline(sb, indent);
		}
	}


	private void createUnmarshalling()
	{
		SwiftMethod method = SwiftMarshallingHelper.createUnmarshallingMethod();
		method.setConvenience(true);
		method.setRequired(true);

		method.addBody("guard let json = json where (json as? [String: AnyObject] != nil) else {");
		method.addBody("\treturn nil");
		method.addBody("}");

		List<SwiftProperty> allProperties = getAllProperties();
		for (SwiftProperty property : allProperties)
		{
			method.addBody(property.lineForUnmarshalling());
		}

		method.addBody("if let");

		List<SwiftProperty> nonOptionalProperties = getAllNonOptionalProperties();
		for (SwiftProperty property : nonOptionalProperties)
		{
			String newLine = String.format("\t%s = %s", property.getName(), property.getName());
			if (nonOptionalProperties.indexOf(property) < nonOptionalProperties.size() - 1)
			{
				newLine += ",";
			}
			method.addBody(newLine);
		}

		method.addBody("{");

		method.addBody("\tself.init(");

		for (SwiftProperty property : allProperties)
		{
			String newLine = String.format("\t\t%s: %s", property.getName(), property.getName());
			if (allProperties.indexOf(property) < allProperties.size() - 1)
			{
				newLine += ",";
			}
			else
			{
				newLine += ")";
			}
			method.addBody(newLine);
		}

		method.addBody("} else {");
		method.addBody("\treturn nil");
		method.addBody("}");

		methods.add(method);
	}


	private void createMarshalling()
	{

		SwiftMethod method = SwiftMarshallingHelper.createMarshallingMethod();
		method.setOverride(overrideProtocols);

		method.addBody("var jsonParameter: [String: AnyObject] = [:]");

		List<SwiftProperty> allProperties = getAllProperties();
		for (SwiftProperty property : allProperties)
		{
			method.addBody(property.lineForMarshalling());
		}

		method.addBody("return jsonParameter");

		methods.add(method);
	}


	private void buildMethods(StringBuilder sb, int indent)
	{
		for (SwiftMethod method : methods)
		{
			BuildableHelper.addNewline(sb);
			method.buildNewline(sb, indent);
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


	private void buildClassFooter(StringBuilder sb)
	{
		BuildableHelper.addNewline(sb);
		sb.append("}");
	}


	/**
	 * Includes super properties
	 */
	private List<SwiftProperty> getAllNonOptionalProperties()
	{
		ArrayList<SwiftProperty> allProperties = new ArrayList<>();

		for (SwiftProperty swiftProperty : superProperties)
		{
			if (!swiftProperty.isOptional())
			{
				allProperties.add(swiftProperty);
			}
		}

		for (SwiftProperty swiftProperty : properties)
		{
			if (!swiftProperty.isOptional())
			{
				allProperties.add(swiftProperty);
			}
		}

		return allProperties;
	}


	/**
	 * Includes super properties
	 */
	private List<SwiftProperty> getAllProperties()
	{
		ArrayList<SwiftProperty> allProperties = new ArrayList<>();

		allProperties.addAll(superProperties);
		allProperties.addAll(properties);

		return allProperties;
	}
}
