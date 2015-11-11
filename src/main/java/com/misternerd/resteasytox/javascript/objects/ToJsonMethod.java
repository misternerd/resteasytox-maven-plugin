package com.misternerd.resteasytox.javascript.objects;

import java.lang.reflect.Field;
import java.util.List;

import com.misternerd.resteasytox.RestServiceLayout;
import com.misternerd.resteasytox.base.AbstractDto;

public class ToJsonMethod extends JavascriptMethod
{

	private final Class<?> javaClass;

	private final RestServiceLayout layout;


	public ToJsonMethod(Class<?> javaClass, List<Field> members, RestServiceLayout layout)
	{
		super("toJson");
		this.javaClass = javaClass;
		this.layout = layout;

		addParameter(new JavascriptParameter("dontEncode"));
		addBody("var result = {};");
		addLine();

		writeFieldsToBody(members, layout);
		addTypeInformationIfApplicable();

		addLine()
		.addBody("if(dontEncode)")
		.addBody("{")
			.addBody("\treturn result;")
		.addBody("}")
		.addBody("else")
		.addBody("{")
			.addBody("\treturn JSON.stringify(result);")
		.addBody("}");
	}


	private void writeFieldsToBody(List<Field> members, RestServiceLayout layout)
	{
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
	}


	private void writeDtoBody(String memberName)
	{
		addBody(String.format("result['%s'] = (self.%s != null) ? self.%s.toJson(true) : null;", memberName, memberName, memberName));
	}


	private void writeDefaultBody(String memberName)
	{
		addBody("result['%s'] = self.%s;", memberName, memberName);
	}


	private void addTypeInformationIfApplicable()
	{
		if(javaClass.getSuperclass() == null || !layout.abstractDtos.containsKey(javaClass.getSuperclass()))
		{
			return;
		}

		AbstractDto abstractDto = layout.abstractDtos.get(javaClass.getSuperclass());

		for(String typeName : abstractDto.implementingClassesByTypeName.keySet())
		{
			if(abstractDto.implementingClassesByTypeName.get(typeName).equals(javaClass))
			{
				addBody(String.format("result['%s'] = '%s';", abstractDto.typeInfoField, typeName));
				break;
			}
		}

	}

}
