package com.misternerd.resteasytox.javascript.objects;

public class JavascriptPublicConstant extends AbstractJavascriptObject
{

	private final String name;

	private final String value;


	protected JavascriptPublicConstant(String name, String value)
	{
		this.name = name;
		this.value = "'" + value + "'";
	}


	protected JavascriptPublicConstant(String name, int value)
	{
		this.name = name;
		this.value = Integer.toString(value);
	}


	@Override
	public void build(StringBuilder sb, int indentCount)
	{
		sb.append("\n\n").append(getIndent(indentCount)).append("cls.").append(name).append(" = ").append(value).append(";");
	}

}
