package com.misternerd.resteasytox.javascript;

import com.google.common.base.CaseFormat;
import com.misternerd.resteasytox.AbstractResteasyConverter;
import com.misternerd.resteasytox.RestServiceLayout;
import com.misternerd.resteasytox.base.AbstractDto;
import com.misternerd.resteasytox.base.MethodParameter;
import com.misternerd.resteasytox.base.ServiceClass;
import com.misternerd.resteasytox.base.ServiceMethod;
import com.misternerd.resteasytox.javascript.helper.MetaDataGenerator;
import com.misternerd.resteasytox.javascript.helper.RestClient;
import com.misternerd.resteasytox.javascript.objects.*;
import com.misternerd.resteasytox.javascript.objects.types.JavascriptBasicType;
import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.misternerd.resteasytox.base.ServiceMethod.RequestMethod.GET;


public class ResteasyToJavascriptConverter extends AbstractResteasyConverter
{

	private final Set<Path> generatedJavascriptFiles = new HashSet<>();

	private final Set<Path> generatedTypingFiles = new HashSet<>();

	private final Path sourceOutputPath;

	private final Path distOutputPath;

	private final JavascriptTypeConverter typeConverter;

	private final String namespace;


	public ResteasyToJavascriptConverter(Path outputPath, String javaPackageName, RestServiceLayout layout, String namespace)
	{
		super(outputPath, javaPackageName, layout);
		this.sourceOutputPath = Paths.get(outputPath.toString(), "src");
		this.distOutputPath = Paths.get(outputPath.toString(), "dist");
		this.typeConverter = new JavascriptTypeConverter(layout);
		this.namespace = namespace;
	}


	@Override
	public void convert() throws Exception
	{
		if (!Files.isDirectory(outputPath))
		{
			Files.createDirectories(outputPath);
		}

		deleteDirectoryIfExists(this.sourceOutputPath);
		deleteDirectoryIfExists(this.distOutputPath);
		Files.createDirectories(this.sourceOutputPath);
		Files.createDirectories(this.distOutputPath);

		generateHelperObjects();
		generateRequestObjects();
		generateResponseObjects();
		generateDtos();
		generateServiceClasses();
		generateMetadataFiles();
		combineGeneratedJavascriptFiles();
		combineGeneratedTypingFiles();
	}


	private void deleteDirectoryIfExists(Path path) throws IOException
	{
		if(Files.isDirectory(path))
		{
			FileUtils.deleteDirectory(path.toFile());
		}
	}


	private void generateHelperObjects() throws IOException
	{
		RestClient restClient = new RestClient(sourceOutputPath, namespace, layout);
		restClient.writeToFile();
		generatedJavascriptFiles.add(restClient.getJavascriptOutputFile());
		generatedTypingFiles.add(restClient.getTypingOutputFile());
	}


	private void generateRequestObjects() throws Exception
	{
		for (Class<?> cls : layout.getRequestClasses())
		{
			JavascriptClass jsClass = new JavascriptClass(getOutputPathFromJavaPackage(cls), namespace, cls.getSimpleName());
			List<Field> fields = getPrivateAndProtectedMemberVariables(cls, true);

			writePublicClassConstants(cls, jsClass);
			writePrivateAndProtectedFields(jsClass, fields);
			jsClass.addPublicMethod(new InitFromJsonMethod(jsClass, fields, layout));
			jsClass.addPublicMethod(new InitFromDataMethod(jsClass));
			jsClass.addPublicMethod(new ToJsonMethod(cls, fields, layout));
			writePublicGettersAndSetters(fields);

			jsClass.writeToFile();
		}
	}


	private void generateResponseObjects() throws Exception
	{
		for (Class<?> cls : layout.getResponseClasses())
		{
			JavascriptClass jsClass = new JavascriptClass(getOutputPathFromJavaPackage(cls), namespace, cls.getSimpleName());
			List<Field> fields = getPrivateAndProtectedMemberVariables(cls, true);

			writePublicClassConstants(cls, jsClass);
			writePrivateAndProtectedFields(jsClass, fields);
			jsClass.addPublicMethod(new InitFromJsonMethod(jsClass, fields, layout));
			jsClass.addPublicMethod(new InitFromDataMethod(jsClass));
			writePublicGettersAndSetters(fields);
			jsClass.addPublicMethod(new ToJsonMethod(cls, fields, layout));

			jsClass.writeToFile();
		}
	}


	private void generateDtos() throws Exception
	{
		for (Class<?> cls : layout.getDtoClasses())
		{
			JavascriptClass jsClass = new JavascriptClass(getOutputPathFromJavaPackage(cls), namespace, cls.getSimpleName());
			List<Field> fields = getPrivateAndProtectedMemberVariables(cls, true);
			List<Field> enumConstants = getEnumConstants(cls);

			if(!enumConstants.isEmpty())
			{
				generateEnumClass(cls, jsClass, enumConstants);
			}
			else
			{
				addInheritanceInfoToDto(cls, jsClass);
				writePrivateAndProtectedFields(jsClass, fields);
				jsClass.addPublicMethod(new InitFromJsonMethod(jsClass, fields, layout));
				jsClass.addPublicMethod(new InitFromDataMethod(jsClass));
				writePublicGettersAndSetters(fields);
				jsClass.addPublicMethod(new ToJsonMethod(cls, fields, layout));
			}

			jsClass.writeToFile();
		}
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void generateEnumClass(Class<?> cls, JavascriptClass jsClass, List<Field> enumConstants)
	{
		Class<? extends Enum> enumClass = (Class<? extends Enum>) cls;

		for(Field field : enumConstants)
		{
			jsClass.addPublicConstant(field.getName(), Enum.valueOf(enumClass, field.getName()).toString());
		}

		jsClass.addPublicMember(JavascriptBasicType.STRING, "value", true);
		jsClass.addPublicMethod("initFromJson", new JavascriptType(cls.getSimpleName()))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "jsonData"))
			.addBody("self.value = jsonData;")
			.addBody("return self;");
		jsClass.addPublicMethod("toJson", JavascriptBasicType.STRING)
			.addParameter(new JavascriptParameter(JavascriptBasicType.BOOLEAN, "dontEncode"))
			.addBody("if(dontEncode)")
			.addBody("{")
				.addBody("\treturn self.value;")
			.addBody("}")
			.addBody("else")
			.addBody("{")
				.addBody("\treturn JSON.stringify(self.value);")
			.addBody("}");
	}


	private void addInheritanceInfoToDto(Class<?> cls, JavascriptClass jsClass)
	{
		if(cls.getSuperclass() != null && layout.abstractDtos.containsKey(cls.getSuperclass()))
		{
			AbstractDto abstractDto = layout.abstractDtos.get(cls.getSuperclass());
			jsClass.setParentType(new JavascriptType(abstractDto.abstractClass.getSimpleName()));

			for(String implementingClassName : abstractDto.implementingClassesByTypeName.keySet())
			{
				Class<?> implementingClass = abstractDto.implementingClassesByTypeName.get(implementingClassName);

				if(implementingClass.equals(cls))
				{
					JavascriptType type = typeConverter.getJavascriptType(implementingClass);
					jsClass.addPublicMember(new JavascriptPublicMember(type, abstractDto.typeInfoField, implementingClassName, true, true));
				}
			}
		}
	}


	private void generateServiceClasses() throws IOException
	{
		for (ServiceClass serviceClass : layout.getServiceClasses())
		{
			JavascriptClass jsClass = new JavascriptClass(getFilenameForAddedString(serviceClass.path, serviceClass.name + ".js"), namespace, serviceClass.name);

			writeServiceHeader(jsClass, serviceClass);

			for(ServiceMethod method : serviceClass.methods)
			{
				writeServiceMethod(jsClass, method);
			}

			jsClass.writeToFile();
		}
	}


	private void writeServiceHeader(JavascriptClass jsClass, ServiceClass serviceClass)
	{
		jsClass.addPrivateConstant("PATH", serviceClass.path);
		jsClass.addConstructorParam(new JavascriptParameter(new JavascriptType(namespace + ".Client"), "restClient"));
	}


	private void writeServiceMethod(JavascriptClass jsClass, ServiceMethod serviceMethod)
	{
		JavascriptType returnType = new JavascriptType("Promise<" + typeConverter.getJavascriptType(serviceMethod.returnType).name + ">");
		JavascriptPublicMethod method = jsClass.addPublicMethod(serviceMethod.name, returnType);

		if(!serviceMethod.headerParams.isEmpty())
		{
			method.addBody("var headerParams = {};");

			for(MethodParameter param : serviceMethod.headerParams)
			{
				method.addBody("headerParams['%s'] = %s;", param.name, convertParamNameToCorrectFormat(param));
			}
		}
		else
		{
			method.addBody("var headerParams = null;");
		}

		if(!serviceMethod.pathParams.isEmpty())
		{
			method.addBody("var pathParams = {};");

			for(MethodParameter param : serviceMethod.pathParams)
			{
				method.addBody("pathParams['%s'] = %s;", param.name, convertParamNameToCorrectFormat(param));
			}
		}
		else
		{
			method.addBody("var pathParams = null;");
		}

		for(MethodParameter param : serviceMethod.headerParams)
		{
			JavascriptType type = typeConverter.getJavascriptType(param.type);
			method.addParameter(new JavascriptParameter(type, convertParamNameToCorrectFormat(param)));
		}

		for(MethodParameter param : serviceMethod.pathParams)
		{
			JavascriptType type = typeConverter.getJavascriptType(param.type);
			method.addParameter(new JavascriptParameter(type, convertParamNameToCorrectFormat(param)));
		}

		if(serviceMethod.bodyParam != null)
		{
			JavascriptType type = typeConverter.getJavascriptType(serviceMethod.bodyParam.type);
			method.addParameter(new JavascriptParameter(type, "bodyData"));
		}
		else
		{
			method.addBody("var bodyData = null;");
		}

		String returnValue = "{}";

		if(serviceMethod.returnType != null)
		{
			if(serviceMethod.returnType.isArray())
			{
				returnValue = "[]";
			}
			else if(layout.getResponseClasses().contains(serviceMethod.returnType))
			{
				returnValue = String.format("new %s.%s()", namespace, serviceMethod.returnType.getSimpleName());
			}
		}

		createHttpMethodCall(serviceMethod, method, returnValue);
	}


	private String convertParamNameToCorrectFormat(MethodParameter param)
	{
		String paramName = param.name;

		if(paramName.contains("-") || paramName.contains("_"))
		{
			paramName = paramName.toLowerCase().replace("-", "_");
			paramName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, paramName);
		}
		return paramName;
	}


	private void createHttpMethodCall(ServiceMethod serviceMethod, JavascriptPublicMethod method, String returnType)
	{
		String httpMethodName = serviceMethod.httpMethod.name().toLowerCase();

		// GET requires no body
		if(GET == serviceMethod.httpMethod)
		{
			method.addBody("var request = restClient.getRequest(PATH + '%s', headerParams, pathParams, '%s', '%s', %s);",
				serviceMethod.path, serviceMethod.requestContentType, serviceMethod.responseContentType, returnType);
			method.addBody("return request;");
		}
		// POST, PUT and DELETE allow a body
		else
		{
			method.addBody("var request = restClient.%sRequest(PATH + '%s', headerParams, pathParams, bodyData, '%s', '%s', %s);",
				httpMethodName, serviceMethod.path, serviceMethod.requestContentType, serviceMethod.responseContentType, returnType);
			method.addBody("return request;");
		}
	}


	private void writePublicClassConstants(Class<? extends Object> cls, JavascriptClass jsClass) throws Exception
	{
		if (layout.getRequestClasses().contains(cls) || layout.getResponseClasses().contains(cls))
		{
			List<Field> constants = getPublicClassConstants(cls.getSuperclass());

			for (Field field : constants)
			{
				jsClass.addPublicConstant(field.getName(), field.getInt(null));
			}
		}

		List<Field> constants = getPublicClassConstants(cls);

		for (Field field : constants)
		{
			jsClass.addPublicConstant(field.getName(), field.getInt(null));
		}
	}


	private void writePrivateAndProtectedFields(JavascriptClass jsClass, List<Field> fields)
	{
		for (Field field : fields)
		{
			JavascriptType type = typeConverter.getJavascriptType(field);
			jsClass.addPublicMember(type, field.getName(), true);
		}
	}


	private void writePublicGettersAndSetters(List<Field> fields)
	{
		for (Field field : fields)
		{
			field.getName();
		}
	}


	private Path getOutputPathFromJavaPackage(Class<?> cls) throws IOException
	{
		String pathExtended = cls.getPackage().getName().replace(javaPackageName, "").replace(".", File.separator);
		Path outputPath = Paths.get(this.sourceOutputPath.toString(), pathExtended);

		if (!Files.isDirectory(outputPath))
		{
			Files.createDirectories(outputPath);
		}

		Path javascriptFile = Paths.get(outputPath.toString(), cls.getSimpleName() + ".js");
		Path typingFile = Paths.get(outputPath.toString(), cls.getSimpleName() + ".d.ts");

		generatedJavascriptFiles.add(javascriptFile);
		generatedTypingFiles.add(typingFile);

		return javascriptFile;
	}


	private Path getFilenameForAddedString(String added, String filename) throws IOException
	{
		Path outputPath = Paths.get(this.sourceOutputPath.toString(), added.split("\\/"));

		if (!Files.isDirectory(outputPath))
		{
			Files.createDirectories(outputPath);
		}

		Path javascriptFile = Paths.get(outputPath.toString(), filename);
		Path typingFile = Paths.get(outputPath.toString(), filename.replace(".js", ".d.ts"));

		generatedJavascriptFiles.add(javascriptFile);
		generatedTypingFiles.add(typingFile);

		return javascriptFile;
	}


	private void generateMetadataFiles() throws IOException
	{
		new MetaDataGenerator(outputPath, layout.mavenProject).createFiles();
	}


	private void combineGeneratedJavascriptFiles() throws IOException
	{
		Path mainFile = Paths.get(distOutputPath.toString(), "RestClient.js");

		try(BufferedWriter writer = Files.newBufferedWriter(mainFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW))
		{
			writer.write("// if running on nodejs, include polyfill\n");
			writer.write("if(typeof global != 'undefined' && !('fetch' in global))\n{\n");
			writer.write("\tglobal.fetch = require('node-fetch');\n");
			writer.write("}\n\n");
			writer.write("var window = window || global;\n\n");

			writer.write(String.format("var %s = {};\n\n", namespace));

			for(Path file : generatedJavascriptFiles)
			{
				try(BufferedReader reader = Files.newBufferedReader(file))
				{
					String line;
					while((line = reader.readLine()) != null)
					{
						writer.write(line + "\n");
					}
				}

				writer.write("\n\n\n");
			}

			writer.write("\n\n// if running on nodejs, export module\n");
			writer.write("if(typeof module != 'undefined')\n{\n");
			writer.write(String.format("\tmodule.exports = %s;\n", namespace));
			writer.write(String.format("\tmodule.exports.default = %s;\n", namespace));
			writer.write("}\n");
		}
	}


	private void combineGeneratedTypingFiles() throws IOException
	{
		Path mainFile = Paths.get(distOutputPath.toString(), "RestClient.d.ts");

		try(BufferedWriter writer = Files.newBufferedWriter(mainFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW))
		{

			writer.write(String.format("// Type definitions for resteasytoxclient v%s\n", layout.mavenProject.getVersion()));
			writer.write("// Project: https://github.com/misternerd/resteasytox-maven-plugin\n");
			writer.write("// Definitions by: ResteasyToX Code Generator <https://github.com/misternerd/resteasytox-maven-plugin>\n");
			writer.write(String.format("\n\ndeclare module %s {", namespace));

			for(Path file : generatedTypingFiles)
			{
				try(BufferedReader reader = Files.newBufferedReader(file))
				{
					String line;
					while((line = reader.readLine()) != null)
					{
						writer.write("\n" + line);
					}
				}

				writer.write("\n");
			}

			writer.write(String.format("}\n\nexport = %s;", namespace));
		}
	}

}
