package com.misternerd.resteasytox.php.baseObjects;

import org.apache.commons.lang3.StringUtils;

public class PhpGetter extends PhpMethod
{

	protected PhpGetter(PhpClass phpClass, PhpMember member)
	{
		super(phpClass, PhpVisibility.PUBLIC, false, member.name, null, null);

		if ("bool".equalsIgnoreCase(member.type.name))
		{
			this.name = "is" + StringUtils.capitalize(member.name);
		}
		else
		{
			this.name = "get" + StringUtils.capitalize(member.name);
		}

		addBody(String.format("return $this->%s;", member.name));
		setReturnType(member.type);
	}

}
