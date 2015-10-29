package com.misternerd.resteasytox.javascript.objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class JavascriptClass
{

	private final Path outputFile;

	public final String name;

	private final Set<JavascriptParameter> constructorParams = new LinkedHashSet<>();

	private final Set<JavascriptPrivateConstant> privateConstants = new LinkedHashSet<>();

	private final Set<JavascriptPublicConstant> publicConstants = new LinkedHashSet<>();

	private final Set<JavascriptMember> members = new LinkedHashSet<>();

	private final List<JavascriptFunction> privateMethods = new ArrayList<>();

	private final List<JavascriptMethod> publicMethods = new ArrayList<>();


	public JavascriptClass(Path outputPath, String name)
	{
		this.outputFile = outputPath;
		this.name = name;
	}


	public void addConstructorParam(JavascriptParameter param)
	{
		constructorParams.add(param);
	}


	public JavascriptClass addPrivateConstant(String name, String value)
	{
		JavascriptPrivateConstant result = new JavascriptPrivateConstant(name, value);
		privateConstants.add(result);
		return this;
	}


	public JavascriptClass addPrivateConstant(String name, int value)
	{
		JavascriptPrivateConstant result = new JavascriptPrivateConstant(name, value);
		privateConstants.add(result);
		return this;
	}


	public JavascriptClass addPublicConstant(String name, String value)
	{
		JavascriptPublicConstant result = new JavascriptPublicConstant(name, value);
		publicConstants.add(result);
		return this;
	}


	public JavascriptClass addPublicConstant(String name, int value)
	{
		JavascriptPublicConstant result = new JavascriptPublicConstant(name, value);
		publicConstants.add(result);
		return this;
	}


	public Set<JavascriptMember> getMembers()
	{
		return members;
	}


	public JavascriptMember addMember(String name)
	{
		JavascriptMember member = new JavascriptMember(name);
		members.add(member);
		return member;
	}


	public JavascriptMember addMember(String name, String value, boolean escapeContent)
	{
		JavascriptMember member = new JavascriptMember(name, value, escapeContent);
		members.add(member);
		return member;
	}


	public JavascriptMember addMember(String name, int value)
	{
		JavascriptMember member = new JavascriptMember(name, value);
		members.add(member);
		return member;
	}


	public JavascriptFunction addPrivateMethod(String name)
	{
		JavascriptFunction function = new JavascriptFunction(name);
		privateMethods.add(function);
		return function;
	}


	public JavascriptMethod addMethod(String name)
	{
		JavascriptMethod method = new JavascriptMethod(name);
		publicMethods.add(method);
		return method;
	}


	public void addGetter(String memberName)
	{
		JavascriptMember member = getMemberByName(memberName);
		publicMethods.add(new JavascriptGetter(member));
	}


	public void addSetter(String memberName)
	{
		JavascriptMember member = getMemberByName(memberName);
		publicMethods.add(new JavascriptSetter(member));
	}


	private JavascriptMember getMemberByName(String memberName)
	{
		for(JavascriptMember member : members)
		{
			if(member.name.equals(memberName))
			{
				return member;
			}
		}

		throw new NullPointerException("Did not find any field named " + memberName);
	}


	public void addMemberInitMethod()
	{
		publicMethods.add(new InitMembersMethod(this));
	}


	public void addMethod(JavascriptMethod method)
	{
		publicMethods.add(method);
	}


	public String build()
	{
		StringBuilder sb = new StringBuilder();

		buildFileHeader(sb);
		buildPrivateConstants(sb);
		buildClassHeader(sb);
		buildMembers(sb);
		buildPrivateMethods(sb);
		buildPublicMethods(sb);
		buildClassFooter(sb);
		buildPublicConstants(sb);
		buildFileFooter(sb);
http://stackoverflow.com/questions/1114024/constructors-in-javascript-objects
		return sb.toString();
	}


	private void buildFileHeader(StringBuilder sb)
	{
		sb.append("var ").append(name).append(" = (function()\n{");
	}


	private void buildPrivateConstants(StringBuilder sb)
	{
		for(JavascriptPrivateConstant constant : privateConstants)
		{
			constant.build(sb, 1);
		}
	}


	private void buildClassHeader(StringBuilder sb)
	{
		sb.append("\n\n\tvar cls = function(");
		Iterator<JavascriptParameter> it = constructorParams.iterator();

		for(int i = 0, j = constructorParams.size(); i < j; i++)
		{
			JavascriptParameter constructorParam = it.next();
			sb.append(constructorParam.name);

			if(i < j - 1)
			{
				sb.append(", ");
			}
		}

		sb.append(")\n\t{")
			.append("\n\t\tvar self = this;");
	}


	private void buildMembers(StringBuilder sb)
	{
		for(JavascriptMember member : members)
		{
			member.build(sb, 2);
		}
	}


	private void buildPrivateMethods(StringBuilder sb)
	{
		for(JavascriptFunction method : privateMethods)
		{
			method.build(sb, 2);
		}
	}


	private void buildPublicMethods(StringBuilder sb)
	{
		for(JavascriptMethod method : publicMethods)
		{
			method.build(sb, 2);
		}
	}


	private void buildClassFooter(StringBuilder sb)
	{
		sb.append("\n\t};");
	}


	private void buildPublicConstants(StringBuilder sb)
	{
		for(JavascriptPublicConstant constant : publicConstants)
		{
			constant.build(sb, 1);
		}
	}

	private void buildFileFooter(StringBuilder sb)
	{
		sb.append("\n\n\treturn cls;\n})();");
	}


	public void writeToFile() throws IOException
	{
		String content = build();
		Files.write(outputFile, content.toString().getBytes("UTF-8"));
	}

}
