package com.misternerd.resteasytox.php.baseObjects;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class PhpClass
{

	private final Path outputPath;

	public final PhpNamespace namespace;

	public final String className;

	public final PhpType extendsClass;

	private final List<PhpImport> fileImports = new ArrayList<>();

	private final List<PhpConstant> constants = new ArrayList<>();

	private final Set<PhpType> typeImports = new HashSet<>();

	private final List<PhpMember> members = new ArrayList<>();

	private final List<PhpMethod> methods = new ArrayList<>();


	public PhpClass(Path outputPath, PhpNamespace namespace, String className, PhpType extendsClass)
	{
		this.outputPath = outputPath;
		this.namespace = namespace;
		this.className = className;
		this.extendsClass = extendsClass;

		if(extendsClass != null)
		{
			addTypeImport(extendsClass);
		}
	}


	public void addFileImport(PhpClass phpClass, boolean required, boolean includeOnce)
	{
		String relativeFilename = StringUtils.difference(getRelativeFileName(), phpClass.getRelativeFileName());
		fileImports.add(new PhpImport(relativeFilename, required, includeOnce));
	}


	public void addTypeImport(PhpType type)
	{
		if(type instanceof PhpBasicType)
		{
			return;
		}

		typeImports.add(type);
	}


	public PhpConstant addConstant(String name, String value)
	{
		PhpConstant result = new PhpConstant(name, value);
		constants.add(result);
		return result;
	}


	public PhpConstant addConstant(String name, int value)
	{
		PhpConstant result = new PhpConstant(name, value);
		constants.add(result);
		return result;
	}


	public PhpMember addMember(PhpVisibility visibility, PhpType type, String name, String value)
	{
		PhpMember result = new PhpMember(visibility, false, type, name, value);
		members.add(result);
		return result;
	}


	public PhpMember addMember(PhpVisibility visibility, boolean isStatic, PhpType type, String name, String value)
	{
		PhpMember result = new PhpMember(visibility, isStatic, type, name, value);
		members.add(result);
		return result;
	}


	public PhpMethod addMethod(PhpVisibility visibility, boolean isStatic, String name, Set<PhpParameter> parameters, String body)
	{
		PhpMethod result = new PhpMethod(this, visibility, isStatic, name, parameters, body);
		methods.add(result);
		return result;
	}


	public void addMethod(PhpMethod method)
	{
		methods.add(method);
	}


	public void addGetter(String memberName)
	{
		PhpMember member = getMemberByName(memberName);
		addMethod(new PhpGetter(this, member));
	}


	public void addSetter(String memberName)
	{
		PhpMember member = getMemberByName(memberName);
		addMethod(new PhpSetter(this, member));
	}


	private PhpMember getMemberByName(String memberName)
	{
		for(PhpMember member : members)
		{
			if(member.name.equals(memberName))
			{
				return member;
			}
		}

		throw new NullPointerException("Did not find any field named" + memberName);
	}


	public String build()
	{
		StringBuilder result = new StringBuilder();

		appendFileHeaderWithNamespace(result);
		appendFileImports(result);
		appendTypeImports(result);
		appendClassHeader(result);
		appendConstants(result);
		appendMembers(result);
		appendMethods(result);
		appendClassFooter(result);

		return result.toString();
	}


	private void appendFileHeaderWithNamespace(StringBuilder result)
	{
		result.append("<?php");
		result.append("\n\nnamespace ").append(namespace.toAbsoluteNamespace(false)).append(";");
	}


	private void appendFileImports(StringBuilder result)
	{
		if(fileImports.isEmpty())
		{
			return;
		}

		result.append("\n\n");

		for(PhpImport fileImport : fileImports)
		{
			fileImport.build(result, 0);
		}

	}


	private void appendTypeImports(StringBuilder result)
	{
		if (typeImports.isEmpty())
		{
			return;
		}

		result.append("\n");

		for (PhpType phpImport : typeImports)
		{
			phpImport.buildAsImport(result, 0);
		}
	}


	private void appendClassHeader(StringBuilder result)
	{
		result.append("\n\n\nclass ").append(className);

		if (extendsClass != null)
		{
			extendsClass.buildAsClassExtends(result);
		}

		result.append("\n{");
	}


	private void appendConstants(StringBuilder result)
	{
		if (constants.isEmpty())
		{
			return;
		}

		for (PhpConstant constant : constants)
		{
			result.append("\n\n\t");
			constant.build(result);
		}
	}


	private void appendMembers(StringBuilder result)
	{
		if (members.isEmpty())
		{
			return;
		}

		for (PhpMember member : members)
		{
			member.build(result, 1);
		}
	}


	private void appendMethods(StringBuilder result)
	{
		if (methods.isEmpty())
		{
			return;
		}

		for (PhpMethod method : methods)
		{
			method.build(result, 1);
		}
	}


	private void appendClassFooter(StringBuilder result)
	{
		result.append("\n\n}");
	}


	public void writeToFile() throws IOException
	{
		Path outputFile = getOrCreateOutputFile(outputPath, className, namespace.namespace);
		String content = build();
		Files.write(outputFile, content.toString().getBytes("UTF-8"));
	}


	private Path getOrCreateOutputFile(Path outputPath, String name, String[] namespace) throws IOException
	{
		Path outputDir = Paths.get(outputPath.toString() + File.separator + StringUtils.join(namespace, File.separator));

		if (!Files.isDirectory(outputDir))
		{
			Files.createDirectories(outputDir);
		}

		return Paths.get(String.format("%s%s%s", outputDir.toString(), File.separator, getOutputFilename()));
	}


	public String getRelativeFileName()
	{
		return String.format("%s%s%s", StringUtils.join(namespace.namespace, File.separator), File.separator, getOutputFilename());
	}


	public String getOutputFilename()
	{
		return String.format("%s.php", className);
	}

}
