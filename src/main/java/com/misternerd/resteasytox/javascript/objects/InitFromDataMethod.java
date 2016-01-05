package com.misternerd.resteasytox.javascript.objects;

import java.util.stream.Collectors;

public class InitFromDataMethod extends JavascriptMethod
{

	private final JavascriptClass jsClass;


	public InitFromDataMethod(JavascriptClass jsClass)
	{
		super("initFromData");
		this.jsClass = jsClass;
		addAllMembersAsPrivateVariable();
	}


	private void addAllMembersAsPrivateVariable()
	{
		String membersAsString = jsClass.getPublicMembers().stream()
				.filter(member -> !member.noInitialization)
				.map(member -> member.name)
				.collect(Collectors.joining("', '"));

		jsClass.addPrivateMember("validFields", "['" + membersAsString + "']", false);
	}


	@Override
	public void build(StringBuilder sb, int indentCount)
	{
		addParameter(new JavascriptParameter("data"));



		addBody("if(typeof data != 'object') {")
			.addBody("\treturn;")
		.addBody("}")
		.addLine()
		.addBody("for(var index in data) {")
			.addBody("\tif(validFields.indexOf(index) >= 0) {")
				.addBody("\t\tself[index] = data[index];")
			.addBody("\t}")
		.addBody("}")
		.addLine()
		.addBody("return self;");

		super.build(sb, indentCount);
	}

}
