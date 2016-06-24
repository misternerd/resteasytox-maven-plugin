package com.misternerd.resteasytox.javascript.objects;

import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class JavascriptClass
{

	protected final Path jsOutputFile;

	protected final Path tsOutputFile;

	protected final String namespace;

	public final String name;

	private final Set<JavascriptParameter> constructorParams = new LinkedHashSet<>();

	private final Set<JavascriptPrivateConstant> privateConstants = new LinkedHashSet<>();

	private final Set<JavascriptPublicConstant> publicConstants = new LinkedHashSet<>();

	private final Set<JavascriptPublicMember> publicMembers = new LinkedHashSet<>();

	private final Set<JavascriptPrivateMember> privateMembers = new LinkedHashSet<>();

	private final List<JavascriptFunction> privateMethods = new ArrayList<>();

	private final List<JavascriptMethod> publicMethods = new ArrayList<>();


	public JavascriptClass(Path outputPath, String namespace, String name)
	{
		String outputFileName = outputPath.toString();
		this.jsOutputFile = outputPath;
		this.tsOutputFile = Paths.get(outputFileName.substring(0, outputFileName.length() - 3) + ".d.ts");
		this.namespace = namespace;
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
		JavascriptPublicConstant result = new JavascriptPublicConstant(this, name, value);
		publicConstants.add(result);
		return this;
	}


	public JavascriptClass addPublicConstant(String name, int value)
	{
		JavascriptPublicConstant result = new JavascriptPublicConstant(this, name, value);
		publicConstants.add(result);
		return this;
	}


	public JavascriptPublicMember addPublicMember(JavascriptType type, String name)
	{
		JavascriptPublicMember member = new JavascriptPublicMember(type, name);
		publicMembers.add(member);
		return member;
	}


	public JavascriptPublicMember addPublicMember(JavascriptPublicMember member)
	{
		publicMembers.add(member);
		return member;
	}


	public Set<JavascriptPublicMember> getPublicMembers()
	{
		return publicMembers;
	}


	public JavascriptPrivateMember addPrivateMember(JavascriptType type, String name)
	{
		JavascriptPrivateMember member = new JavascriptPrivateMember(type, name);
		privateMembers.add(member);
		return member;
	}


	public JavascriptPrivateMember addPrivateMember(JavascriptType type, String name, String value, boolean escapeValue)
	{
		JavascriptPrivateMember member = new JavascriptPrivateMember(type, name, value, escapeValue);
		privateMembers.add(member);
		return member;
	}


	public JavascriptFunction addPrivateMethod(String name)
	{
		JavascriptFunction function = new JavascriptFunction(name);
		privateMethods.add(function);
		return function;
	}


	public JavascriptMethod addMethod(String name, JavascriptType returnType)
	{
		JavascriptMethod method = new JavascriptMethod(name, returnType);
		publicMethods.add(method);
		return method;
	}


	public void addGetter(String memberName)
	{
		JavascriptPrivateMember member = getMemberByName(memberName);
		publicMethods.add(new JavascriptGetter(member));
	}


	public void addSetter(String memberName)
	{
		JavascriptPrivateMember member = getMemberByName(memberName);
		publicMethods.add(new JavascriptSetter(member));
	}


	private JavascriptPrivateMember getMemberByName(String memberName)
	{
		for(JavascriptPrivateMember member : privateMembers)
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


	private String buildJavascript()
	{
		StringBuilder sb = new StringBuilder();

		buildFileHeader(sb);
		buildPrivateConstants(sb);
		buildClassHeader(sb);
		buildPrivateMembers(sb);
		buildPublicMembers(sb);
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
		sb.append(namespace).append(".").append(name).append(" = (function()\n{");
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
		sb.append("\n\n\tvar ").append(name).append(" = function(");
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


	private void buildPublicMembers(StringBuilder sb)
	{
		for(JavascriptPublicMember member : publicMembers)
		{
			member.build(sb, 2);
		}
	}


	private void buildPrivateMembers(StringBuilder sb)
	{
		for(JavascriptPrivateMember member : privateMembers)
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
		sb.append("\n\n\treturn ").append(name).append(";\n})();");
	}


	private String buildTypescriptTyping()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("\n\texport class ").append(name).append(" {");

		addTypescriptMembers(sb);
		addTypescriptConstructor(sb);
		addTypescriptMethods(sb);

		sb.append("\n\t}");

		return sb.toString();
	}


	private void addTypescriptMembers(StringBuilder sb)
	{
		for(JavascriptPublicMember member : publicMembers)
		{
			sb.append("\n\t\t").append(member.name).append(": ").append(member.type.name).append(";");
		}
	}


	private void addTypescriptConstructor(StringBuilder sb)
	{
		sb.append("\n\n\t\tconstructor(");

		for(Iterator<JavascriptParameter> it = constructorParams.iterator(); it.hasNext(); )
		{
			JavascriptParameter param = it.next();
			sb.append(param.name);

			if(it.hasNext())
			{
				sb.append(", ");
			}
		}

		sb.append(");");
	}


	private void addTypescriptMethods(StringBuilder sb)
	{
		for(JavascriptMethod method : publicMethods)
		{
			sb.append("\n\t\tpublic ").append(method.name).append("(");

			for(Iterator<JavascriptParameter> iterator = method.parameters.iterator(); iterator.hasNext(); )
			{
				JavascriptParameter param = iterator.next();
				String paramName = param.name;

				if(paramName.startsWith("_"))
				{
					paramName = paramName.substring(1);
				}

				sb.append(paramName).append(": ").append(param.type.name);

				if(iterator.hasNext())
				{
					sb.append(", ");
				}
			}

			sb.append(") : ").append(method.returnType.name).append(" ;");
		}
	}


	public void writeToFile() throws IOException
	{
		String jsContent = buildJavascript();
		Files.write(jsOutputFile, jsContent.getBytes("UTF-8"));
		String typingContent = buildTypescriptTyping();
		Files.write(tsOutputFile, typingContent.getBytes("UTF-8"));
	}

}
