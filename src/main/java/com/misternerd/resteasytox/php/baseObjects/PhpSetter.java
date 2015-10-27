package com.misternerd.resteasytox.php.baseObjects;

import org.apache.commons.lang3.StringUtils;

public class PhpSetter extends PhpMethod
{

	protected PhpSetter(PhpClass phpClass, PhpMember member)
	{
		super(phpClass, PhpVisibility.PUBLIC, false, "set" + StringUtils.capitalize(member.name), null, null);
		addParameter(new PhpParameter(member.type, member.name));
		addBody(String.format("$this->%s = $%s;", member.name, member.name));
		setReturnType(null);
	}

}
