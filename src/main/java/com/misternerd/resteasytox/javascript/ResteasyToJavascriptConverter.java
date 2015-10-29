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
import com.misternerd.resteasytox.base.MethodParameter;
import com.misternerd.resteasytox.base.ServiceClass;
import com.misternerd.resteasytox.base.ServiceMethod;
import com.misternerd.resteasytox.base.ServiceMethod.RequestMethod;
import com.misternerd.resteasytox.javascript.helper.RestClient;
import com.misternerd.resteasytox.javascript.objects.InitFromJsonMethod;
import com.misternerd.resteasytox.javascript.objects.InitMembersMethod;
import com.misternerd.resteasytox.javascript.objects.JavascriptClass;
import com.misternerd.resteasytox.javascript.objects.JavascriptMethod;
import com.misternerd.resteasytox.javascript.objects.JavascriptParameter;
import com.misternerd.resteasytox.javascript.objects.ToJsonMethod;


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
			jsClass.addMemberInitMethod();
			jsClass.addMethod(new InitFromJsonMethod(fields, layout));
			writePrivateAndProtectedFields(jsClass, fields);
			writePublicGettersAndSetters(jsClass, fields);
			jsClass.addMethod(new ToJsonMethod(fields, layout));

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
			jsClass.addMemberInitMethod();
			jsClass.addMethod(new InitFromJsonMethod(fields, layout));
			writePrivateAndProtectedFields(jsClass, fields);
			writePublicGettersAndSetters(jsClass, fields);
			jsClass.addMethod(new ToJsonMethod(fields, layout));

			jsClass.writeToFile();
		}
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void generateDtos() throws Exception
	{
		for (Class<?> cls : layout.getDtoClasses())
		{
			JavascriptClass jsClass = new JavascriptClass(getOutputPathFromJavaPackage(cls), cls.getSimpleName());
			List<Field> fields = getPrivateAndProtectedMemberVariables(cls, true);
			List<Field> enumConstants = getEnumConstants(cls);

			if(!enumConstants.isEmpty())
			{
				Class<? extends Enum> enumClass = (Class<? extends Enum>) cls;

				for(Field field : enumConstants)
				{
					jsClass.addPublicConstant(field.getName(), Enum.valueOf(enumClass, field.getName()).toString());
				}

				jsClass.addMember("value");
				jsClass.addMethod(new InitMembersMethod(jsClass));
				jsClass.addMethod("initFromJson")
					.addParameter(new JavascriptParameter("jsonData"))
					.addBody("value = jsonData;")
					.addBody("return self;");
				jsClass.addGetter("value");
				jsClass.addSetter("value");
				jsClass.addMethod("toJson")
					.addParameter(new JavascriptParameter("dontEncode"))
					.addBody("if(dontEncode)")
					.addBody("{")
						.addBody("\treturn value;")
					.addBody("}")
					.addBody("else")
					.addBody("{")
						.addBody("\treturn JSON.stringify(value);")
					.addBody("}");
			}
			else
			{
				writePrivateAndProtectedFields(jsClass, fields);
				jsClass.addMemberInitMethod();
				jsClass.addMethod(new InitFromJsonMethod(fields, layout));
				writePublicGettersAndSetters(jsClass, fields);
				jsClass.addMethod(new ToJsonMethod(fields, layout));
			}

			jsClass.writeToFile();
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

		if(serviceMethod.bodyParam != null)
		{
			method.addParameter(new JavascriptParameter("bodyData"));
		}
		else
		{
			method.addBody("var bodyData = null;");
		}

		for(MethodParameter param : serviceMethod.headerParams)
		{
			method.addParameter(new JavascriptParameter(convertParamNameToCorrectFormat(param)));
		}

		for(MethodParameter param : serviceMethod.pathParams)
		{
			method.addParameter(new JavascriptParameter(convertParamNameToCorrectFormat(param)));
		}

		if(RequestMethod.POST.equals(serviceMethod.httpMethod))
		{
			method.addBody("var request = restClient.postRequest(PATH + '%s', headerParams, pathParams, bodyData, '%s', '%s', %s);",
					serviceMethod.path, serviceMethod.requestContentType, serviceMethod.responseContentType,
					(serviceMethod.returnType != null) ? "new " + serviceMethod.returnType.getSimpleName() + "()" : null);
			method.addBody("return request;");
		}
		else
		{
			method.addBody("var request = restClient.getRequest(PATH + '%s', headerParams, pathParams, '%s', '%s', %s);",
					serviceMethod.path, serviceMethod.requestContentType, serviceMethod.responseContentType,
					(serviceMethod.returnType != null) ? "new " + serviceMethod.returnType.getSimpleName() + "()" : null);
			method.addBody("return request;");
		}

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
			jsClass.addMember(field.getName());
		}
	}


	private void writePublicGettersAndSetters(JavascriptClass jsClass, List<Field> fields)
	{
		for (Field field : fields)
		{
			String name = field.getName();
			jsClass.addGetter(name);
			jsClass.addSetter(name);
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
