package com.misternerd.resteasytox.javascript.objects;

public class JavascriptPublicConstant extends AbstractJavascriptObject
{

	private final JavascriptClass javascriptClass;

	private final String name;

	private final String value;


	protected JavascriptPublicConstant(JavascriptClass javascriptClass, String name, String value)
	{
		this.javascriptClass = javascriptClass;
		this.name = name;
		this.value = "'" + value + "'";
	}


	protected JavascriptPublicConstant(JavascriptClass javascriptClass, String name, int value)
	{
		this.javascriptClass = javascriptClass;
		this.name = name;
		this.value = Integer.toString(value);
	}


	@Override
	public void build(StringBuilder sb, int indentCount)
	{
		sb.append("\n\n").append(getIndent(indentCount)).append(javascriptClass.name)
			.append(".").append(name).append(" = ").append(value).append(";");
	}

}
