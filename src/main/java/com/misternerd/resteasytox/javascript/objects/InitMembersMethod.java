package com.misternerd.resteasytox.javascript.objects;

public class InitMembersMethod extends JavascriptMethod
{

	private final JavascriptClass jsClass;


	public InitMembersMethod(JavascriptClass jsClass)
	{
		super("init");
		this.jsClass = jsClass;
	}


	@Override
	public void build(StringBuilder sb, int indentCount)
	{
		for (JavascriptMember member : jsClass.getMembers())
		{
			this.addParameter(new JavascriptParameter(member));
			addBody("%s = _%s;", member.name, member.name);
		}

		super.build(sb, indentCount);
	}

}
