package com.misternerd.resteasytox.swift.objects;

import java.util.ArrayList;

public class SwiftConstructorMethod extends SwiftMethod
{

	public SwiftConstructorMethod(ArrayList<SwiftProperty> properties)
	{
		super(INIT_FUNCTION_NAME);

		for (SwiftProperty property : properties)
		{
			addParameter(property);
			addBody(property.lineForConstructor());
		}

		addBody("super.init()");
	}

}
