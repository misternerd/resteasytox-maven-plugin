package com.misternerd.resteasytox.javascript.objects;

public class JavascriptPrivateConstant extends AbstractJavascriptObject
{
	private final String name;

	private final String value;


	protected JavascriptPrivateConstant(String name, String value)
	{
		this.name = name;
		this.value = "'" + value + "'";
	}


	protected JavascriptPrivateConstant(String name, int value)
	{
		this.name = name;
		this.value = Integer.toString(value);
	}


	public void build(StringBuilder sb, int indentCount)
	{
		sb.append("\n\n").append(getIndent(indentCount)).append("const ").append(name).append(" = ").append(value).append(";");
	}

}
