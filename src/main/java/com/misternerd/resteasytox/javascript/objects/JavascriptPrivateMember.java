package com.misternerd.resteasytox.javascript.objects;

import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;

class JavascriptPrivateMember extends AbstractJavascriptObject
{

	public final JavascriptType type;

	public final String name;

	public final String value;


	JavascriptPrivateMember(JavascriptType type, String name, String value, boolean escapeContent)
	{
		this.type = type;
		this.name = name;

		if(value != null && escapeContent)
		{
			value = "'" + value + "'";
		}
		this.value = value;
	}


	@Override
	public void buildAsJavascript(StringBuilder sb, int indentSize)
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


	@Override
	public void buildAsTypescriptTypeing(StringBuilder sb, int indentSize)
	{
	}

}
