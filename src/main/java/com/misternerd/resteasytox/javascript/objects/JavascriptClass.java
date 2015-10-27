package com.misternerd.resteasytox.javascript.objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class JavascriptClass
{

	private final Path outputFile;

	private final String name;

	private final Set<JavascriptConstant> constants = new LinkedHashSet<>();

	private final Set<JavascriptMember> members = new LinkedHashSet<>();

	private final List<JavascriptMethod> methods = new ArrayList<>();


	public JavascriptClass(Path outputPath, String name)
	{
		this.outputFile = outputPath;
		this.name = name;
	}


	public JavascriptConstant addConstant(String name, String value)
	{
		JavascriptConstant result = new JavascriptConstant(name, value);
		constants.add(result);
		return result;
	}


	public JavascriptConstant addConstant(String name, int value)
	{
		JavascriptConstant result = new JavascriptConstant(name, value);
		constants.add(result);
		return result;
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


	public JavascriptMember addMember(String name, String value)
	{
		JavascriptMember member = new JavascriptMember(name, value);
		members.add(member);
		return member;
	}


	public JavascriptMember addMember(String name, int value)
	{
		JavascriptMember member = new JavascriptMember(name, value);
		members.add(member);
		return member;
	}


	public JavascriptMethod addMethod(String name)
	{
		JavascriptMethod method = new JavascriptMethod(name);
		methods.add(method);
		return method;
	}
	
	
	public void addGetter(String memberName)
	{
		JavascriptMember member = getMemberByName(memberName);
		methods.add(new JavascriptGetter(member));
	}
	
	
	public void addSetter(String memberName)
	{
		JavascriptMember member = getMemberByName(memberName);
		methods.add(new JavascriptSetter(member));
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
		methods.add(new InitMembersMethod(this));
	}


	public void addMethod(JavascriptMethod method)
	{
		methods.add(method);
	}


	public String build()
	{
		StringBuilder sb = new StringBuilder();

		buildFileHeader(sb);
		buildConstants(sb);
		buildClassHeader(sb);
		buildMembers(sb);
		buildMethods(sb);
		
		buildClassFooter(sb);
		buildFileFooter(sb);
http://stackoverflow.com/questions/1114024/constructors-in-javascript-objects
		return sb.toString();
	}


	private void buildFileHeader(StringBuilder sb)
	{
		sb.append("var ").append(name).append(" = (function()")
			.append("\n{");
		
	}


	private void buildConstants(StringBuilder sb)
	{
		for(JavascriptConstant constant : constants)
		{
			constant.build(sb, 1);
		}
	}


	private void buildClassHeader(StringBuilder sb)
	{
		sb.append("\n\n\tvar cls = function()")
			.append("\n\t{")
			.append("\n\t\tvar self = this;");
	}


	private void buildMembers(StringBuilder sb)
	{
		for(JavascriptMember member : members)
		{
			member.build(sb, 2);
		}
	}


	private void buildMethods(StringBuilder sb)
	{
		for(JavascriptMethod method : methods)
		{
			method.build(sb, 2);
		}
		
	}
	
	
	private void buildClassFooter(StringBuilder sb)
	{
		sb.append("\n\t};\n\n\treturn cls;");
	}


	private void buildFileFooter(StringBuilder sb)
	{
		sb.append("\n})();");
	}
	
	
	public void writeToFile() throws IOException
	{
		String content = build();
		Files.write(outputFile, content.toString().getBytes("UTF-8"));
	}

}
