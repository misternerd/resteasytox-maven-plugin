package com.misternerd.resteasytox.javascript.objects;

import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;

public class JavascriptPrivateMember extends AbstractJavascriptObject
{

	public final JavascriptType type;

	public final String name;

	public final String value;


	protected JavascriptPrivateMember(JavascriptType type, String name)
	{
		this.type = type;
		this.name = name;
		this.value = null;
	}


	protected JavascriptPrivateMember(JavascriptType type, String name, String value, boolean escapeContent)
	{
		this.type = type;
		this.name = name;

		if(value != null && escapeContent)
		{
			value = "'" + value + "'";
		}
		this.value = value;
	}


	protected JavascriptPrivateMember(JavascriptType type, String name, int value)
	{
		this.type = type;
		this.name = name;
		this.value = Integer.toString(value);
	}


	@Override
	public void build(StringBuilder sb, int indentSize)
	{
		String indent = getIndent(indentSize);

		sb.append("\n\n").append(indent).append("var ").append(name).append(" = ");

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

}
