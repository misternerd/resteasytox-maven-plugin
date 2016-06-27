package com.misternerd.resteasytox.javascript.objects;

import org.apache.commons.lang3.StringUtils;

abstract class AbstractJavascriptObject
{

	public abstract void buildAsJavascript(StringBuilder sb, int indentSize);


	public abstract void buildAsTypescriptTypeing(StringBuilder sb, int indentSize);


	public String getIndent(int indent)
	{
		return StringUtils.repeat("\t", indent);
	}

}
