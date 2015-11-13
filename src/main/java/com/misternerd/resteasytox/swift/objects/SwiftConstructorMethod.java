package com.misternerd.resteasytox.swift.objects;

import java.util.ArrayList;

public class SwiftConstructorMethod extends SwiftMethod
{

	public SwiftConstructorMethod(String name, ArrayList<SwiftProperty> properties)
	{
		super(name);

		for (SwiftProperty property : properties)
		{
			SwiftParameter parameter = new SwiftParameter(property, null);
			addParameter(parameter);
			addBody(parameter.lineForConstructor());
		}

		addBody("super.init()");
	}

}
