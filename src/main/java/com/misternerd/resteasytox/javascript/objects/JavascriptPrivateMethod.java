package com.misternerd.resteasytox.javascript.objects;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JavascriptPrivateMethod extends AbstractJavascriptObject
{

	public String name;

	private final Set<JavascriptParameter> parameters = new LinkedHashSet<>();

	private final List<String> body = new ArrayList<>();


	protected JavascriptPrivateMethod(String name)
	{
		this.name = name;
	}


	public JavascriptPrivateMethod addBody(String line, Object... args)
	{
		body.add(String.format(line, args));
		return this;
	}


	public JavascriptPrivateMethod addBodyWithIndent(String line, int indent, Object... args)
	{
		body.add(String.format(getIndent(indent) + line, args));
		return this;
	}


	public JavascriptPrivateMethod addLine()
	{
		body.add("");
		return this;
	}

	public JavascriptPrivateMethod addParameter(JavascriptParameter parameter)
	{
		parameters.add(parameter);
		return this;
	}


	@Override
	public void buildAsJavascript(StringBuilder sb, int indentCount)
	{
		String indent = "\n" + getIndent(indentCount);

		sb.append("\n\n").append(indent);
		sb.append("function ").append(name).append("(");

		if (!parameters.isEmpty())
		{
			for (JavascriptParameter param : parameters)
			{
				sb.append(param.name).append(", ");
			}

			sb.delete(sb.length() - 2, sb.length());
		}

		sb.append(")").append(indent).append("{");

		for (String bodyLine : body)
		{
			if(body.isEmpty())
			{
				sb.append("\n");
			}
			else
			{
				sb.append(indent).append("\t").append(bodyLine);
			}
		}

		sb.append(indent).append("}");
	}


	@Override
	public void buildAsTypescriptTypeing(StringBuilder sb, int indentSize)
	{
	}

}
