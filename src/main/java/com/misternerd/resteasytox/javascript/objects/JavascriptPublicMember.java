package com.misternerd.resteasytox.javascript.objects;

public class JavascriptPublicMember extends AbstractJavascriptObject
{

	public final String name;

	public final String value;

	public final boolean noInitialization;


	protected JavascriptPublicMember(String name)
	{
		this.name = name;
		this.value = null;
		this.noInitialization = false;
	}


	public JavascriptPublicMember(String name, String value, boolean escapeContent, boolean noInitialization)
	{
		this.name = name;

		if(value != null && escapeContent)
		{
			value = "'" + value + "'";
		}
		this.value = value;
		this.noInitialization = noInitialization;
	}


	protected JavascriptPublicMember(String name, int value)
	{
		this.name = name;
		this.value = Integer.toString(value);
		this.noInitialization = false;
	}


	@Override
	public void build(StringBuilder sb, int indentSize)
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

}
