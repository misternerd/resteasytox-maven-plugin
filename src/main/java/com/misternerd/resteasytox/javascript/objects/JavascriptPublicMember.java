package com.misternerd.resteasytox.javascript.objects;

import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;

public class JavascriptPublicMember extends AbstractJavascriptObject
{

	public final JavascriptType type;

	public final String name;

	public final String value;

	final boolean noInitialization;


	JavascriptPublicMember(JavascriptType type, String name)
	{
		this.type = type;
		this.name = name;
		this.value = null;
		this.noInitialization = false;
	}


	public JavascriptPublicMember(JavascriptType type, String name, String value, boolean escapeContent, boolean noInitialization)
	{
		this.type = type;
		this.name = name;

		if(value != null && escapeContent)
		{
			value = "'" + value + "'";
		}
		this.value = value;
		this.noInitialization = noInitialization;
	}


	@Override
	public void buildAsJavascript(StringBuilder sb, int indentSize)
	{
		String indent = getIndent(indentSize);

		sb.append("\n\n").append(indent).append("this.").append(name).append(" = ");

		if (value != null)
		{
			sb.append(value);
		}
		else
		{
			sb.append("null");
		}

		sb.append(";");
	}


	@Override
	public void buildAsTypescriptTypeing(StringBuilder sb, int indentSize)
	{
		sb.append("\n").append(getIndent(indentSize)).append(name).append(": ").append(type.name).append(";");
	}

}
