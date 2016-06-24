package com.misternerd.resteasytox.javascript.objects;

import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JavascriptMethod extends AbstractJavascriptObject
{

	public String name;

	public final JavascriptType returnType;

	public final Set<JavascriptParameter> parameters = new LinkedHashSet<>();

	private final List<String> body = new ArrayList<>();


	protected JavascriptMethod(String name, JavascriptType returnType)
	{
		this.name = name;
		this.returnType = returnType;
	}


	public JavascriptMethod addBody(String line, Object... args)
	{
		body.add(String.format(line, args));
		return this;
	}


	public JavascriptMethod addLine()
	{
		body.add("");
		return this;
	}

	public JavascriptMethod addParameter(JavascriptParameter parameter)
	{
		parameters.add(parameter);
		return this;
	}


	@Override
	public void build(StringBuilder sb, int indentCount)
	{
		String indent = "\n" + getIndent(indentCount);

		sb.append("\n\n").append(indent);
		sb.append("this.").append(name).append(" = function(");

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
			if(bodyLine.isEmpty())
			{
				sb.append("\n");
			}
			else
			{
				sb.append(indent).append("\t").append(bodyLine);
			}
		}

		sb.append(indent).append("};");
	}

}
