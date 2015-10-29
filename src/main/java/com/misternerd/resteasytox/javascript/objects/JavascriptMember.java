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


	protected JavascriptMember(String name, String value, boolean escapeContent)
	{
		this.name = name;

		if(value != null && escapeContent)
		{
			value = "'" + value + "'";
		}
		this.value = value;
	}


	protected JavascriptMember(String name, int value)
	{
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
