package com.misternerd.resteasytox.javascript.objects;

import java.lang.reflect.Field;
import java.util.List;

import com.misternerd.resteasytox.RestServiceLayout;

public class ToJsonMethod extends JavascriptMethod
{

	public ToJsonMethod(List<Field> members, RestServiceLayout layout)
	{
		super("toJson");
		addBody("var result = {};");
		addLine();
 
		for (Field member : members)
		{
			String memberName = member.getName();
			Class<?> cls = member.getType();

			if (layout.getDtoClasses().contains(cls))
			{
				writeDtoBody(memberName);
			}
			else
			{
				writeDefaultBody(memberName);
			}
		}

		addLine();
		addBody("return JSON.stringify(result);");
	}


	private void writeDtoBody(String memberName)
	{
		addBody(String.format("result['%s'] = %s.toJson();", memberName, memberName));
	}


	private void writeDefaultBody(String memberName)
	{
		addBody("result['%s'] = %s;", memberName, memberName);
	}

}
