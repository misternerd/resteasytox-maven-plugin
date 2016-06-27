package com.misternerd.resteasytox.javascript.objects;

import org.apache.commons.lang3.StringUtils;

public class JavascriptSetter extends JavascriptPublicMethod
{

	protected JavascriptSetter(JavascriptPrivateMember member)
	{
		super("set" + StringUtils.capitalize(member.name), member.type);
		addParameter(new JavascriptParameter(member.type, "_" + member.name));
		addBody("%s = _%s;", member.name, member.name);
	}

}
