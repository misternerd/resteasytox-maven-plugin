package com.misternerd.resteasytox.swift.objects;

import java.util.ArrayList;
import java.util.List;

import com.misternerd.resteasytox.swift.helper.BuildableHelper;

public class SwiftMethod extends Buildable
{
	protected static final String INIT_FUNCTION_NAME = "init";

	private final String name;

	private final List<SwiftProperty> parameters = new ArrayList<>();

	private final List<String> body = new ArrayList<>();


	public SwiftMethod(String name)
	{
		super();
		this.name = name;
	}


	public void addParameter(SwiftProperty parameter)
	{
		parameters.add(parameter);
	}


	public void addBody(String line)
	{
		body.add(line);
	}


	public void build(StringBuilder sb, int indent)
	{
		BuildableHelper.addIndent(sb, indent);

		if (INIT_FUNCTION_NAME.equals(name))
		{
			sb.append(INIT_FUNCTION_NAME);
		}
		else
		{
			sb.append("func ").append(name);
		}
		sb.append("(");
		for (int i = 0; i < parameters.size(); i++)
		{
			SwiftProperty parameter = parameters.get(i);
			parameter.build(sb);

			if (i < parameters.size() - 1)
			{
				sb.append(", ");
			}
		}
		sb.append(") {");

		indent++;
		for (String line : body)
		{
			BuildableHelper.addNewline(sb);
			BuildableHelper.addIndent(sb, indent);
			sb.append(line);
		}
		indent--;

		BuildableHelper.addNewline(sb);
		BuildableHelper.addIndent(sb, indent);
		sb.append("}");
	}


	@Override
	public void buildNewline(StringBuilder sb, int indent)
	{
		BuildableHelper.addSpace(sb);

		build(sb, indent);
	}


	@Override
	public void build(StringBuilder sb)
	{
		build(sb, 0);
	}

}
