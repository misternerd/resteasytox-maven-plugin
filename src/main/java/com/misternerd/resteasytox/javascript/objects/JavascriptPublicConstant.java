package com.misternerd.resteasytox.javascript.objects;

import com.misternerd.resteasytox.javascript.objects.types.JavascriptBasicType;
import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;

class JavascriptPublicConstant extends AbstractJavascriptObject
{

	private final JavascriptClass javascriptClass;

	public final String name;

	private final JavascriptType type;

	public final String value;


	JavascriptPublicConstant(JavascriptClass javascriptClass, String name, String value)
	{
		this.javascriptClass = javascriptClass;
		this.name = name;
		this.type = JavascriptBasicType.STRING;
		this.value = "'" + value + "'";
	}


	JavascriptPublicConstant(JavascriptClass javascriptClass, String name, int value)
	{
		this.javascriptClass = javascriptClass;
		this.name = name;
		this.type = JavascriptBasicType.NUMBER;
		this.value = Integer.toString(value);
	}


	@Override
	public void buildAsJavascript(StringBuilder sb, int indentCount)
	{
		sb.append("\n\n").append(getIndent(indentCount))
			.append(javascriptClass.name).append(".").append(name).append(" = function() { return ").append(value).append("; };");
	}


	@Override
	public void buildAsTypescriptTypeing(StringBuilder sb, int indentSize)
	{
		sb.append("\n").append(getIndent(indentSize))
			.append("public static ").append(name).append("(): ").append(type.name).append(";");
	}

}
