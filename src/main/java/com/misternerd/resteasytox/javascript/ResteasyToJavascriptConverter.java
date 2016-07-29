package com.misternerd.resteasytox.javascript;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.base.CaseFormat;
import com.misternerd.resteasytox.AbstractResteasyConverter;
import com.misternerd.resteasytox.RestServiceLayout;
import com.misternerd.resteasytox.base.AbstractDto;
import com.misternerd.resteasytox.base.MethodParameter;
import com.misternerd.resteasytox.base.ServiceClass;
import com.misternerd.resteasytox.base.ServiceMethod;
import com.misternerd.resteasytox.base.ServiceMethod.RequestMethod;
import com.misternerd.resteasytox.javascript.helper.RestClient;
import com.misternerd.resteasytox.javascript.objects.*;

import static com.misternerd.resteasytox.base.ServiceMethod.RequestMethod.*;


public class ResteasyToJavascriptConverter extends AbstractResteasyConverter
{

	public ResteasyToJavascriptConverter(Path outputPath, String javaPackageName, RestServiceLayout layout)
	{
		super(outputPath, javaPackageName, layout);
	}


	@Override
	public void convert() throws Exception
	{
		if (!Files.isDirectory(outputPath))
		{
			Files.createDirectories(outputPath);
		}

		generateHelperObjects();
		generateRequestObjects();
		generateResponseObjects();
		generateDtos();
		generateServiceClasses();
	}


	private void generateHelperObjects() throws IOException
	{
		new RestClient(outputPath, layout).writeToFile();
	}


	private void generateRequestObjects() throws Exception
	{
		for (Class<?> cls : layout.getRequestClasses())
		{
			JavascriptClass jsClass = new JavascriptClass(getOutputPathFromJavaPackage(cls), cls.getSimpleName());
			List<Field> fields = getPrivateAndProtectedMemberVariables(cls, true);

			writePublicClassConstants(cls, jsClass);
			writePrivateAndProtectedFields(jsClass, fields);
			jsClass.addMemberInitMethod();
			jsClass.addMethod(new InitFromJsonMethod(fields, layout));
			jsClass.addMethod(new InitFromDataMethod(jsClass));
			jsClass.addMethod(new ToJsonMethod(cls, fields, layout));
			writePublicGettersAndSetters(jsClass, fields);

			jsClass.writeToFile();
		}
	}


	private void generateResponseObjects() throws Exception
	{
		for (Class<?> cls : layout.getResponseClasses())
		{
			JavascriptClass jsClass = new JavascriptClass(getOutputPathFromJavaPackage(cls), cls.getSimpleName());
			List<Field> fields = getPrivateAndProtectedMemberVariables(cls, true);

			writePublicClassConstants(cls, jsClass);
			writePrivateAndProtectedFields(jsClass, fields);
			jsClass.addMemberInitMethod();
			jsClass.addMethod(new InitFromJsonMethod(fields, layout));
			jsClass.addMethod(new InitFromDataMethod(jsClass));
			writePublicGettersAndSetters(jsClass, fields);
			jsClass.addMethod(new ToJsonMethod(cls, fields, layout));

			jsClass.writeToFile();
		}
	}


	private void generateDtos() throws Exception
	{
		for (Class<?> cls : layout.getDtoClasses())
		{
			JavascriptClass jsClass = new JavascriptClass(getOutputPathFromJavaPackage(cls), cls.getSimpleName());
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
				jsClass.addMemberInitMethod();
				jsClass.addMethod(new InitFromJsonMethod(fields, layout));
				jsClass.addMethod(new InitFromDataMethod(jsClass));
				writePublicGettersAndSetters(jsClass, fields);
				jsClass.addMethod(new ToJsonMethod(cls, fields, layout));
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

		jsClass.addPublicMember("value");
		jsClass.addMethod(new InitMembersMethod(jsClass));
		jsClass.addMethod("initFromJson")
			.addParameter(new JavascriptParameter("jsonData"))
			.addBody("self.value = jsonData;")
			.addBody("return self;");
		jsClass.addMethod("toJson")
			.addParameter(new JavascriptParameter("dontEncode"))
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

			for(String implementingClassName : abstractDto.implementingClassesByTypeName.keySet())
			{
				if(abstractDto.implementingClassesByTypeName.get(implementingClassName).equals(cls))
				{
					jsClass.addPublicMember(new JavascriptPublicMember(abstractDto.typeInfoField, implementingClassName, true, true));
				}
			}
		}
	}


	private void generateServiceClasses() throws IOException
	{
		for (ServiceClass serviceClass : layout.getServiceClasses())
		{
			JavascriptClass jsClass = new JavascriptClass(getFilenameForAddedString(serviceClass.path, serviceClass.name + ".js"), serviceClass.name);

			writeServiceHeader(jsClass, serviceClass.name, serviceClass.path);

			for(ServiceMethod method : serviceClass.methods)
			{
				writeServiceMethod(jsClass, method);
			}

			jsClass.writeToFile();
		}
	}


	private void writeServiceHeader(JavascriptClass jsClass, String name, String path)
	{
		jsClass.addPrivateConstant("PATH", path);
		jsClass.addConstructorParam(new JavascriptParameter("restClient"));
	}


	private void writeServiceMethod(JavascriptClass jsClass, ServiceMethod serviceMethod)
	{
		JavascriptMethod method = jsClass.addMethod(serviceMethod.name);

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
			method.addParameter(new JavascriptParameter(convertParamNameToCorrectFormat(param)));
		}

		for(MethodParameter param : serviceMethod.pathParams)
		{
			method.addParameter(new JavascriptParameter(convertParamNameToCorrectFormat(param)));
		}

		if(serviceMethod.bodyParam != null)
		{
			method.addParameter(new JavascriptParameter("bodyData"));
		}
		else
		{
			method.addBody("var bodyData = null;");
		}

		String returnType = "{}";

		if(serviceMethod.returnType != null)
		{
			if(serviceMethod.returnType.isArray())
			{
				returnType = "[]";
			}
			else if(layout.getResponseClasses().contains(serviceMethod.returnType))
			{
				returnType = "new " + serviceMethod.returnType.getSimpleName() + "()";
			}
		}

		createHttpMethodCall(serviceMethod, method, returnType);
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


	private void createHttpMethodCall(ServiceMethod serviceMethod, JavascriptMethod method, String returnType)
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
			jsClass.addPublicMember(field.getName());
		}
	}


	private void writePublicGettersAndSetters(JavascriptClass jsClass, List<Field> fields)
	{
		for (Field field : fields)
		{
			field.getName();
		}
	}


	private Path getOutputPathFromJavaPackage(Class<?> cls) throws IOException
	{
		String pathExtended = cls.getPackage().getName().replace(javaPackageName, "").replace(".", File.separator);
		Path outputPath = Paths.get(this.outputPath.toString(), pathExtended);

		if (!Files.isDirectory(outputPath))
		{
			Files.createDirectories(outputPath);
		}

		return Paths.get(outputPath.toString(), cls.getSimpleName() + ".js");
	}


	private Path getFilenameForAddedString(String added, String filename) throws IOException
	{
		Path outputPath = Paths.get(this.outputPath.toString(), added.split("\\/"));

		if (!Files.isDirectory(outputPath))
		{
			Files.createDirectories(outputPath);
		}

		return Paths.get(outputPath.toString(), filename);
	}

}
