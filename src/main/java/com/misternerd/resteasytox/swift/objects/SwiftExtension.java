package com.misternerd.resteasytox.swift.objects;

import java.util.ArrayList;
import java.util.List;

import com.misternerd.resteasytox.swift.helper.BuildableHelper;

public class SwiftExtension extends Buildable
{
	private final String extendetClass;
	private final ArrayList<SwiftMethod> methods = new ArrayList<>();
	private final List<String> implementedProtocols = new ArrayList<>();


	public SwiftExtension(String extendetClass, List<String> implementedProtocols)
	{
		super();
		this.extendetClass = extendetClass;
		if (implementedProtocols != null)
		{
			this.implementedProtocols.addAll(implementedProtocols);
		}
	}


	public void addMethod(SwiftMethod method)
	{
		methods.add(method);
	}


	@Override
	public void build(StringBuilder sb)
	{
		int indent = 0;
		BuildableHelper.addNewline(sb);
		sb.append("extension ").append(extendetClass);

		if (implementedProtocols.size() > 0)
		{
			sb.append(": ");
			for (String protocol : implementedProtocols)
			{
				sb.append(protocol);
				if (implementedProtocols.indexOf(protocol) < implementedProtocols.size() - 1)
				{
					sb.append(", ");
				}
			}
		}

		sb.append(" {");

		indent++;
		if (!methods.isEmpty())
		{
			for (SwiftMethod method : methods)
			{
				method.buildNewline(sb, indent);
			}

		}

		indent--;
		BuildableHelper.addNewline(sb);
		sb.append("}");

	}


	@Override
	public String toString()
	{
		return "SwiftExtension(extendetClass=" + extendetClass + ", methods=" + methods + ")";
	}
	
}
