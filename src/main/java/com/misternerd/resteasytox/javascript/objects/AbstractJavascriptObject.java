package com.misternerd.resteasytox.javascript.objects;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractJavascriptObject
{

	public abstract void build(StringBuilder sb, int indentSize);


	public String getIndent(int indent)
	{
		return StringUtils.repeat("\t", indent);
	}

}
