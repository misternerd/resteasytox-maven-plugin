package com.misternerd.resteasytox.javascript.objects;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.misternerd.resteasytox.RestServiceLayout;
import com.misternerd.resteasytox.base.AbstractDto;
import com.misternerd.resteasytox.javascript.objects.types.JavascriptBasicType;
import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;


public class InitFromJsonMethod extends JavascriptPublicMethod
{

	private final RestServiceLayout layout;


	public InitFromJsonMethod(JavascriptClass jsClass, List<Field> members, RestServiceLayout layout)
	{
		super("initFromJson", new JavascriptType(jsClass.name));
		this.layout = layout;
		createJsonDecoder();

		for (Field member : members)
		{
			String memberName = member.getName();
			Class<?> cls = member.getType();

			addLine();
			addBody("if('%s' in jsonData && jsonData['%s'] != null)", memberName, memberName);
			addBody("{");

			boolean bodyComplete = false;

			if (!bodyComplete && layout.getDtoClasses().contains(cls))
			{
				bodyComplete = createDtoAssignment(jsClass.namespace, member, memberName);
			}

			if(!bodyComplete && member.getGenericType() instanceof ParameterizedType)
			{
				bodyComplete = createParameterizedAssignment(jsClass.namespace, member, memberName, cls);
			}

			if(!bodyComplete)
			{
				bodyComplete = createDefaultAssignment(memberName);
			}

			addBody("}");
			addBody("else");
			addBody("{");
				addBody("\tself.%s = null;", memberName);
			addBody("}");
		}

		addBody("return self;");
	}


	private void createJsonDecoder()
	{
		addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "jsonData"));
		addBody("if(jsonData == null)");
		addBody("{");
			addBody("\treturn null;");
		addBody("}");
		addLine();
		addBody("if(typeof jsonData == 'string')");
		addBody("{");
		addBody("\tjsonData = JSON.parse(jsonData);");
		addBody("}");
	}


	private boolean createDtoAssignment(String namespace, Field member, String memberName)
	{
		if(layout.abstractDtos.containsKey(member.getType()))
		{
			AbstractDto abstractDto = layout.abstractDtos.get(member.getType());

			addBody("\tvar classType = jsonData['%s']['%s'];", memberName, abstractDto.typeInfoField);
			addBody("\tswitch(classType)");
			addBody("\t{");
				for(String typeName : abstractDto.implementingClassesByTypeName.keySet())
				{
					String concreteName = abstractDto.implementingClassesByTypeName.get(typeName).getSimpleName();
					addBody("\t\tcase '%s':", typeName);
					addBody("\t\t{");
						addBody("\t\t\tself.%s = new %s.%s();", memberName, namespace, concreteName);
						addBody("\t\t\tself.%s.initFromJson(jsonData['%s']);", memberName, memberName);
						addBody("\t\t\tself.%s.TYPE = '%s';", memberName, concreteName);
						addBody("\t\t\tbreak;");
					addBody("\t\t}");
				}
			addBody("\t}");
		}
		else
		{
			String className = member.getType().getSimpleName();
			addBody("\tself.%s = new %s.%s();", memberName, namespace, className);
			addBody("\tself.%s.initFromJson(jsonData['%s']);", memberName, memberName);
		}

		return true;
	}


	private boolean createParameterizedAssignment(String namespace, Field member, String memberName, Class<?> cls)
	{
		ParameterizedType parameterized = (ParameterizedType) member.getGenericType();
		Type[] types = parameterized.getActualTypeArguments();

		if (List.class.isAssignableFrom(cls) && types.length == 1)
		{
			Class<?> concreteClass = (Class<?>) types[0];

			if(layout.getDtoClasses().contains(concreteClass))
			{
				createListAssignment(namespace, memberName, concreteClass);
				return true;
			}
		}

		if (Map.class.isAssignableFrom(cls) && types.length == 2)
		{
			createMapAssignment(namespace, memberName, (Class<?>) types[0], (Class<?>) types[1]);
			return true;
		}

		return false;
	}


	private void createListAssignment(String namespace, String memberName, Class<?> concreteClass)
	{
		if(layout.abstractDtos.containsKey(concreteClass))
		{
			createInheritanceListAssignment(namespace, memberName, concreteClass);
		}
		else
		{
			createDefaultListAssignment(namespace, memberName, concreteClass);
		}
	}


	private void createInheritanceListAssignment(String namespace, String memberName, Class<?> concreteClass)
	{
		AbstractDto abstractDto = layout.abstractDtos.get(concreteClass);

		addBody("\tself.%s = [];", memberName);
		addLine();
		addBody("\tfor(var i in jsonData['%s'])", memberName);
		addBody("\t{");
			addBody("\t\tvar classType = jsonData['%s'][i]['%s'];", memberName, abstractDto.typeInfoField);
			addBody("\t\tswitch(classType)");
			addBody("\t\t{");
				for(String typeName : abstractDto.implementingClassesByTypeName.keySet())
				{
					String concreteName = abstractDto.implementingClassesByTypeName.get(typeName).getSimpleName();
					addBody("\t\t\tcase '%s':", typeName);
					addBody("\t\t\t{");
						addBody("\t\t\t\tself.%s[i] = new %s.%s();", memberName, namespace, concreteName);
						addBody("\t\t\t\tself.%s[i].initFromJson(jsonData['%s'][i]);", memberName, memberName);
						addBody("\t\t\t\tself.%s[i].TYPE = '%s';", memberName, concreteName);
						addBody("\t\t\t\tbreak;");
					addBody("\t\t\t}");
				}
			addBody("\t\t}");
		addBody("\t}");
	}


	private void createDefaultListAssignment(String namespace, String memberName, Class<?> concreteClass)
	{
		addBody("\tself.%s = [];", memberName);
		addLine();
		addBody("\tfor(var i in jsonData['%s'])", memberName);
		addBody("\t{");
		addBody("\t\tself.%s[i] = new %s.%s();", memberName, namespace, concreteClass.getSimpleName());
		addBody("\t\tself.%s[i].initFromJson(jsonData['%s'][i]);", memberName, memberName);
		addBody("\t}");
	}


	private void createMapAssignment(String namespace, String memberName, Class<?> keyClass, Class<?> valueClass)
	{
		//TODO add inheritance
		addBody("\tself.%s = {};", memberName);
		addLine();
		addBody("\tfor(var index in jsonData['%s'])", memberName);
		addBody("\t{");

		if(layout.getDtoClasses().contains(valueClass))
		{
			addBody("\t\tvar value = new %s.%s();", namespace, valueClass.getSimpleName());
			addBody("\t\tvalue.initFromJson(jsonData['%s'][index]);", memberName);
		}
		else
		{
			addBody("\t\tvar value = jsonData['%s'][index];", memberName);
		}

		addBody("\t\tself.%s[index] = value;", memberName);
		addBody("\t}");
	}


	private boolean createDefaultAssignment(String memberName)
	{
		addBody("\tself.%s = jsonData['%s'];", memberName, memberName);
		return true;
	}

}
