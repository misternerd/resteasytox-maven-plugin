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

	private JavascriptType parentType;

	private final Set<JavascriptParameter> constructorParams = new LinkedHashSet<>();

	private final Set<JavascriptPrivateConstant> privateConstants = new LinkedHashSet<>();

	private final Set<JavascriptPublicConstant> publicConstants = new LinkedHashSet<>();

	private final Set<JavascriptPublicMember> publicMembers = new LinkedHashSet<>();

	private final Set<JavascriptPrivateMember> privateMembers = new LinkedHashSet<>();

	private final List<JavascriptPrivateMethod> privateMethods = new ArrayList<>();

	private final List<JavascriptPublicMethod> publicMethods = new ArrayList<>();


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


	public JavascriptPublicMember addPublicMember(JavascriptType type, String name, boolean initInConstructor)
	{
		JavascriptPublicMember member = new JavascriptPublicMember(type, name);

		if(initInConstructor)
		{
			member = new JavascriptPublicMember(type, name, "_" + name, false, false);
			constructorParams.add(new JavascriptParameter(member));
		}

		publicMembers.add(member);
		return member;
	}


	public JavascriptPublicMember addPublicMember(JavascriptPublicMember member)
	{
		publicMembers.add(member);
		return member;
	}


	Set<JavascriptPublicMember> getPublicMembers()
	{
		return publicMembers;
	}


	protected JavascriptPrivateMember addPrivateMember(JavascriptType type, String name, String value, boolean escapeValue)
	{
		JavascriptPrivateMember member = new JavascriptPrivateMember(type, name, value, escapeValue);
		privateMembers.add(member);
		return member;
	}


	protected JavascriptPrivateMethod addPrivateMethod(String name, JavascriptType returnType)
	{
		JavascriptPrivateMethod function = new JavascriptPrivateMethod(name);
		privateMethods.add(function);
		return function;
	}


	public JavascriptPublicMethod addPublicMethod(String name, JavascriptType returnType)
	{
		JavascriptPublicMethod method = new JavascriptPublicMethod(name, returnType);
		publicMethods.add(method);
		return method;
	}


	public void addPublicMethod(JavascriptPublicMethod method)
	{
		publicMethods.add(method);
	}


	public JavascriptClass setParentType(JavascriptType parentType)
	{
		this.parentType = parentType;
		return this;
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
			constant.buildAsJavascript(sb, 1);
		}
	}


	private void buildClassHeader(StringBuilder sb)
	{
		if(!publicConstants.isEmpty() || !privateConstants.isEmpty())
		{
			sb.append("\n");
		}

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
			member.buildAsJavascript(sb, 2);
		}
	}


	private void buildPrivateMembers(StringBuilder sb)
	{
		for(JavascriptPrivateMember member : privateMembers)
		{
			member.buildAsJavascript(sb, 2);
		}
	}


	private void buildPrivateMethods(StringBuilder sb)
	{
		for(JavascriptPrivateMethod method : privateMethods)
		{
			method.buildAsJavascript(sb, 2);
		}
	}


	private void buildPublicMethods(StringBuilder sb)
	{
		for(JavascriptPublicMethod method : publicMethods)
		{
			method.buildAsJavascript(sb, 2);
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
			constant.buildAsJavascript(sb, 1);
		}
	}

	private void buildFileFooter(StringBuilder sb)
	{
		sb.append("\n\n\treturn ").append(name).append(";\n})();");
	}


	private String buildTypescriptTyping()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("\n\texport class ").append(name);

		if(parentType != null)
		{
			sb.append(" extends ").append(parentType.name);
		}

		sb.append(" {");

		addTypescriptPublicConstants(sb);
		addTypescriptMembers(sb);
		addTypescriptEmptyConstructor(sb);
		addTypescriptFullConstructor(sb);
		addTypescriptMethods(sb);

		sb.append("\n\t}");

		return sb.toString();
	}


	private void addTypescriptPublicConstants(StringBuilder sb)
	{
		for(JavascriptPublicConstant constant : publicConstants)
		{
			constant.buildAsTypescriptTypeing(sb, 2);
		}

		if(!publicConstants.isEmpty())
		{
			sb.append("\n");
		}
	}


	private void addTypescriptMembers(StringBuilder sb)
	{
		for(JavascriptPublicMember member : publicMembers)
		{
			member.buildAsTypescriptTypeing(sb, 2);
		}

		if(!publicMembers.isEmpty())
		{
			sb.append("\n");
		}
	}


	private void addTypescriptEmptyConstructor(StringBuilder sb)
	{
		sb.append("\n\t\tconstructor();");
	}


	private void addTypescriptFullConstructor(StringBuilder sb)
	{
		sb.append("\n\t\tconstructor(");

		for(Iterator<JavascriptParameter> it = constructorParams.iterator(); it.hasNext(); )
		{
			JavascriptParameter param = it.next();
			sb.append(param.name).append("? : ").append(param.type.name);

			if(it.hasNext())
			{
				sb.append(", ");
			}
		}

		sb.append(");");
	}


	private void addTypescriptMethods(StringBuilder sb)
	{
		for(JavascriptPublicMethod method : publicMethods)
		{
			method.buildAsTypescriptTypeing(sb, 2);
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
