package com.misternerd.resteasytox.javascript.objects;

import com.misternerd.resteasytox.javascript.objects.types.JavascriptArrayType;
import com.misternerd.resteasytox.javascript.objects.types.JavascriptBasicType;
import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;

import java.util.stream.Collectors;

public class InitFromDataMethod extends JavascriptPublicMethod
{

	private final JavascriptClass jsClass;


	public InitFromDataMethod(JavascriptClass jsClass)
	{
		super("initFromData", new JavascriptType(jsClass.name));
		this.jsClass = jsClass;
		addAllMembersAsPrivateVariable();
	}


	private void addAllMembersAsPrivateVariable()
	{
		String membersAsString = jsClass.getPublicMembers().stream()
				.filter(member -> !member.noInitialization)
				.map(member -> member.name)
				.collect(Collectors.joining("', '"));

		jsClass.addPrivateMember(JavascriptArrayType.STRING_ARRAY, "validFields", "['" + membersAsString + "']", false);
	}


	@Override
	public void buildAsJavascript(StringBuilder sb, int indentCount)
	{
		addParameter(new JavascriptParameter(JavascriptBasicType.OBJECT, "data"));

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

		super.buildAsJavascript(sb, indentCount);
	}

}
