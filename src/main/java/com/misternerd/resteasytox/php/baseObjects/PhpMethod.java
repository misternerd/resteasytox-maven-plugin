package com.misternerd.resteasytox.php.baseObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PhpMethod extends AbstractBaseObject
{

	private final PhpClass phpClass;

	private final boolean isStatic;

	private final PhpVisibility visibility;

	protected String name;

	private final Set<PhpParameter> parameters = new LinkedHashSet<>();

	private final List<String> body = new ArrayList<>();

	private PhpType returnType;


	protected PhpMethod(PhpClass phpClass, PhpVisibility visibility, boolean isStatic, String name, Set<PhpParameter> parameters, String body)
	{
		this.phpClass = phpClass;
		this.visibility = visibility;
		this.isStatic = isStatic;
		this.name = name;

		if (body != null)
		{
			String[] bodyAsLines = body.split("\n");
			this.body.addAll(Arrays.asList(bodyAsLines));
		}

		if(parameters != null && !parameters.isEmpty())
		{
			for(PhpParameter parameter : parameters)
			{
				addParameter(parameter);
			}
		}
	}


	public PhpMethod addBody(String line)
	{
		body.add(line);
		return this;
	}


	public PhpMethod addParameter(PhpParameter parameter)
	{
		parameters.add(parameter);
		phpClass.addTypeImport(parameter.type);
		return this;
	}


	public void setReturnType(PhpType returnType)
	{
		this.returnType = returnType;
	}


	@Override
	public void build(StringBuilder sb, int indentCount)
	{
		String indent = "\n" + getIndent(indentCount);

		boolean hasParams = (!parameters.isEmpty());
		boolean hasReturnType = (returnType != null);

		sb.append("\n\n");

		if(hasParams || hasReturnType)
		{
			sb.append(indent).append("/**");
		}

		Set<PhpParameter> parametersToReplace = new HashSet<>();

		for(PhpParameter param : parameters)
		{
			sb.append(indent).append(" * @param ");
			param.type.build(sb, indentCount);

			if(param.adaptedName != null)
			{
				sb.append(" $").append(param.adaptedName);
				parametersToReplace.add(param);
			}
			else
			{
				sb.append(" $").append(param.originalName);
			}
		}

		if(hasReturnType)
		{
			sb.append(indent).append(" * @return ");
			returnType.build(sb, indentCount);
		}

		if(hasParams || hasReturnType)
		{
			sb.append(indent).append(" */");
		}

		sb.append(indent);

		if (visibility != null)
		{
			sb.append(visibility.toString()).append(" ");
		}

		if(isStatic)
		{
			sb.append("static ");
		}

		sb.append("function ").append(name).append("(");

		if (parameters != null && !parameters.isEmpty())
		{
			for (PhpParameter param : parameters)
			{
				if (param.type != null && param.type.addAsTypeHint)
				{
					sb.append(param.type.name).append(" ");
				}

				sb.append("$").append((param.adaptedName != null) ? param.adaptedName : param.originalName).append(", ");
			}

			sb.delete(sb.length() - 2, sb.length());
		}

		sb.append(")").append(indent).append("{");

		for (String bodyLine : body)
		{
			for(PhpParameter param : parametersToReplace)
			{
				bodyLine = bodyLine.replace("$" + param.originalName, "$" + param.adaptedName);
			}

			sb.append(indent).append("\t").append(bodyLine);
		}

		sb.append(indent).append("}");
	}

}
