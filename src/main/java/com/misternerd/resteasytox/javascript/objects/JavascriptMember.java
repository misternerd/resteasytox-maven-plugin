package com.misternerd.resteasytox.javascript.objects;

public class JavascriptMember extends AbstractJavascriptObject
{

	public final String name;

	public final String value;


	protected JavascriptMember(String name)
	{
		this.name = name;
		this.value = null;
	}


	protected JavascriptMember(String name, String value)
	{
		this.name = name;
		this.value = (value != null) ? "'" + value + "'" : null;
	}


	protected JavascriptMember(String name, int value)
	{
		this.name = name;
		this.value = Integer.toString(value);
	}


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
