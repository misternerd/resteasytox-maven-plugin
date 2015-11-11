package com.misternerd.resteasytox.javascript.objects;

import org.apache.commons.lang3.StringUtils;

public class JavascriptGetter extends JavascriptMethod
{

	protected JavascriptGetter(JavascriptPrivateMember member)
	{
		super(member.name);

		if ("bool".equalsIgnoreCase(member.name))
		{
			this.name = "is" + StringUtils.capitalize(member.name);
		}
		else
		{
			this.name = "get" + StringUtils.capitalize(member.name);
		}

		addBody("return %s;", member.name);
	}

}
