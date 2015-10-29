package com.misternerd.resteasytox.javascript.objects;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.misternerd.resteasytox.RestServiceLayout;


public class InitFromJsonMethod extends JavascriptMethod
{

	private final RestServiceLayout layout;


	public InitFromJsonMethod(List<Field> members, RestServiceLayout layout)
	{
		super("initFromJson");
		this.layout = layout;
		createJsonDecoder();

		for (Field member : members)
		{
			String memberName = member.getName();
			Class<?> cls = member.getType();

			addLine();
			addBody("if(jsonData['%s'])", memberName);
			addBody("{");

			boolean bodyComplete = false;

			if (!bodyComplete && layout.getDtoClasses().contains(cls))
			{
				bodyComplete = createDtoAssignment(member, memberName);
			}

			if(!bodyComplete && member.getGenericType() instanceof ParameterizedType)
			{
				bodyComplete = createParameterizedAssignment(member, memberName, cls);
			}

			if(!bodyComplete)
			{
				bodyComplete = createDefaultAssignment(memberName);
			}

			addBody("}");
		}

		addBody("return self;");
	}


	private void createJsonDecoder()
	{
		addParameter(new JavascriptParameter("jsonData"));
		addBody("if(typeof jsonData == 'string')");
		addBody("{");
		addBody("\tjsonData = JSON.parse(jsonData);");
		addBody("}");
	}


	private boolean createDtoAssignment(Field member, String memberName)
	{
		String className = member.getType().getSimpleName();
		addBody("\t%s = new %s();", memberName, className);
		addBody("\t%s.initFromJson(jsonData['%s']);", memberName, memberName);
		return true;
	}


	private boolean createParameterizedAssignment(Field member, String memberName, Class<?> cls)
	{
		ParameterizedType parameterized = (ParameterizedType) member.getGenericType();
		Type[] types = parameterized.getActualTypeArguments();

		if (List.class.isAssignableFrom(cls) && types.length == 1)
		{
			Class<?> concreteClass = (Class<?>) types[0];

			if(layout.getDtoClasses().contains(concreteClass))
			{
				createListAssignment(memberName, concreteClass);
				return true;
			}
		}

		if (Map.class.isAssignableFrom(cls) && types.length == 2)
		{
			createMapAssignment(memberName, (Class<?>) types[0], (Class<?>) types[1]);
			return true;
		}

		return false;
	}


	private void createListAssignment(String memberName, Class<?> concreteClass)
	{
		addBody("\t%s = [];", memberName);
		addLine();
		addBody("\tfor(var index in jsonData['%s'])", memberName);
		addBody("\t{");
		addBody("\t\t%s[index] = new %s();", memberName, concreteClass.getSimpleName());
		addBody("\t\t%s[index].initFromJson(jsonData['%s']);", memberName, memberName);
		addBody("\t}");
	}


	private void createMapAssignment(String memberName, Class<?> keyClass, Class<?> valueClass)
	{
		addBody("\t%s = {};", memberName);
		addLine();
		addBody("\tfor(var index in jsonData['%s'])", memberName);
		addBody("\t{");

		if(layout.getDtoClasses().contains(valueClass))
		{
			addBody("\t\tvar value = new %s();", valueClass.getSimpleName());
			addBody("\t\tvalue.initFromJson(jsonData['%s'][index]);");
		}
		else
		{
			addBody("\t\tvar value = jsonData['%s'][index];", memberName);
		}

		addBody("\t\t%s[index] = value;", memberName);
		addBody("\t}");
	}

	private boolean createDefaultAssignment(String memberName)
	{
		addBody("\t%s = jsonData['%s'];", memberName, memberName);
		return true;
	}

}
