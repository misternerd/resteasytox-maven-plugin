package com.misternerd.resteasytox.javascript.objects;

import com.misternerd.resteasytox.javascript.objects.types.JavascriptBasicType;
import org.apache.commons.lang3.StringUtils;

public class JavascriptGetter extends JavascriptPublicMethod
{

	protected JavascriptGetter(JavascriptPrivateMember member)
	{
		super(member.name, member.type);

		if (JavascriptBasicType.BOOLEAN.equals(member.type))
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
