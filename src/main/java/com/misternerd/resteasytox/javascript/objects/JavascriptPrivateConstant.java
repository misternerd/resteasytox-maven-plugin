package com.misternerd.resteasytox.javascript.objects;

class JavascriptPrivateConstant extends AbstractJavascriptObject
{
	private final String name;

	private final String value;


	JavascriptPrivateConstant(String name, String value)
	{
		this.name = name;
		this.value = "'" + value + "'";
	}


	@Override
	public void buildAsJavascript(StringBuilder sb, int indentCount)
	{
		sb.append("\n\n").append(getIndent(indentCount)).append("const ").append(name).append(" = ").append(value).append(";");
	}


	@Override
	public void buildAsTypescriptTypeing(StringBuilder sb, int indentSize)
	{
	}

}
