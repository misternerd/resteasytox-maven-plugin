package com.misternerd.resteasytox.swift.objects;

import java.util.ArrayList;
import java.util.List;

import com.misternerd.resteasytox.swift.helper.BuildableHelper;
import com.misternerd.resteasytox.swift.helper.SwiftMarshallingHelper;

public class SwiftEnum extends Buildable
{
	private final List<SwiftEnumItem> enumItems = new ArrayList<>();

	private final String name;

	private final ArrayList<SwiftMethod> methods = new ArrayList<>();

	private boolean includeMarshalling = false;

	private boolean includeUnmarshalling = false;

	private boolean supportObjC = false;


	public SwiftEnum(String name, boolean supportObjC)
	{
		super();
		this.name = name;
		this.supportObjC = supportObjC;
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


	public void addEnumItem(String name, String value)
	{
		SwiftEnumItem enumItem = new SwiftEnumItem(name, value);
		enumItems.add(enumItem);
	}


	public void addMethod(SwiftMethod method)
	{
		methods.add(method);
	}


	@Override
	public void build(StringBuilder sb)
	{
		if (includeUnmarshalling)
		{
			createUnmarshalling();
		}

		if (includeMarshalling)
		{
			createMarshalling();
		}

		int indent = 0;
		BuildableHelper.addSpace(sb);
		buildEnumHeader(sb);
		buildEnum(sb);
		indent++;
		buildEnumMethods(sb, indent);
		buildEnumFooter(sb);
	}


	private void buildEnumHeader(StringBuilder sb)
	{
		sb.append("enum ").append(name).append(": String");

		if (includeMarshalling || includeUnmarshalling)
		{
			if (includeMarshalling)
			{
				sb.append(", ").append(SwiftMarshallingHelper.MARSHALLING_PROTOCOL);
			}

			if (includeUnmarshalling)
			{
				sb.append(", ").append(SwiftMarshallingHelper.UNMARSHALLING_PROTOCOL);
			}
		}
		
		sb.append(" {");
	}


	private void buildEnum(StringBuilder sb)
	{
		for (SwiftEnumItem item : enumItems)
		{
			sb.append("\n\t");
			item.build(sb);
		}
	}


	private void buildEnumMethods(StringBuilder sb, int indent)
	{
		for (SwiftMethod method : methods)
		{
			BuildableHelper.addNewline(sb);
			method.buildNewline(sb, indent);
		}
	}


	private void buildEnumFooter(StringBuilder sb)
	{
		BuildableHelper.addNewline(sb);
		sb.append("}");
	}


	private void createUnmarshalling()
	{
		SwiftMethod method = SwiftMarshallingHelper.createUnmarshallingMethod(supportObjC);

		method.addBody("guard let enumString = json as? String else {");
		method.addBody("\treturn nil");
		method.addBody("}");
		method.addBody("guard let enumDto = " + name + "(rawValue: enumString) else {");
		method.addBody("\treturn nil");
		method.addBody("}");

		method.addBody("self = enumDto");

		methods.add(method);
	}


	private void createMarshalling()
	{

		SwiftMethod method = SwiftMarshallingHelper.createMarshallingMethod();
		method.addBody("return self.rawValue");
		methods.add(method);
	}
}
