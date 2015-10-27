package com.misternerd.resteasytox.javascript.objects;

import org.apache.commons.lang3.StringUtils;

public class JavascriptSetter extends JavascriptMethod
{

	protected JavascriptSetter(JavascriptMember member)
	{
		super("set" + StringUtils.capitalize(member.name));
		addParameter(new JavascriptParameter("_" + member.name));
		addBody("%s = _%s;", member.name, member.name);
	}

}
