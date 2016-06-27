package com.misternerd.resteasytox.javascript.objects;

import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;

public class InitMembersMethod extends JavascriptPublicMethod
{

	private final JavascriptClass jsClass;


	public InitMembersMethod(JavascriptClass jsClass)
	{
		super("init", new JavascriptType(jsClass.name));
		this.jsClass = jsClass;
	}


	@Override
	public void buildAsJavascript(StringBuilder sb, int indentCount)
	{
		for (JavascriptPublicMember member : jsClass.getPublicMembers())
		{
			if(member.noInitialization)
			{
				continue;
			}

			this.addParameter(new JavascriptParameter(member));
			addBody("self.%s = _%s;", member.name, member.name);
		}

		addBody("return self;");

		super.buildAsJavascript(sb, indentCount);
	}

}
