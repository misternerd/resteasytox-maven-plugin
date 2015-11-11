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
		for (JavascriptPublicMember member : jsClass.getPublicMembers())
		{
			this.addParameter(new JavascriptParameter(member));
			addBody("self.%s = _%s;", member.name, member.name);
		}

		addBody("return self;");

		super.build(sb, indentCount);
	}

}
