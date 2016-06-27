package com.misternerd.resteasytox.javascript.objects;

import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;

import java.util.*;

public class JavascriptPublicMethod extends AbstractJavascriptObject
{

	public String name;

	private final JavascriptType returnType;

	private final Set<JavascriptParameter> parameters = new LinkedHashSet<>();

	private final List<String> body = new ArrayList<>();


	protected JavascriptPublicMethod(String name, JavascriptType returnType)
	{
		this.name = name;
		this.returnType = returnType;
	}


	public JavascriptPublicMethod addBody(String line, Object... args)
	{
		body.add(String.format(line, args));
		return this;
	}


	JavascriptPublicMethod addLine()
	{
		body.add("");
		return this;
	}

	public JavascriptPublicMethod addParameter(JavascriptParameter parameter)
	{
		parameters.add(parameter);
		return this;
	}


	@Override
	public void buildAsJavascript(StringBuilder sb, int indentCount)
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


	@Override
	public void buildAsTypescriptTypeing(StringBuilder sb, int indentSize)
	{
		String indent = getIndent(indentSize);
		sb.append("\n").append(indent).append("public ").append(name).append("(");

		for(Iterator<JavascriptParameter> iterator = parameters.iterator(); iterator.hasNext(); )
		{
			JavascriptParameter param = iterator.next();
			String paramName = param.name;

			if(paramName.startsWith("_"))
			{
				paramName = paramName.substring(1);
			}

			sb.append(paramName).append(": ").append(param.type.name);

			if(iterator.hasNext())
			{
				sb.append(", ");
			}
		}

		sb.append(") : ").append(returnType.name).append(" ;");
	}

}
