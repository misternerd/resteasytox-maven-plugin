package com.misternerd.resteasytox.php.baseObjects;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractBaseObject
{

	public abstract void build(StringBuilder sb, int indentSize);


	public String getIndent(int indent)
	{
		return StringUtils.repeat("\t", indent);
	}
}
