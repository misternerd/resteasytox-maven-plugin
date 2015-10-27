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
import com.misternerd.resteasytox.javascript.objects.FromJsonMethod;
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

		generateRequestObjects();
		generateResponseObjects();
		generateDtos();
		generateServiceClasses();
	}


	private void generateRequestObjects() throws Exception
	{
		for (Class<?> cls : layout.getRequestClasses())
		{
			JavascriptClass jsClass = new JavascriptClass(getOutputPathFromJavaPackage(cls), cls.getSimpleName());
			List<Field> fields = getPrivateAndProtectedMemberVariables(cls, true);

			writePublicClassConstants(cls, jsClass);
			jsClass.addMemberInitMethod();
			jsClass.addMethod(new FromJsonMethod(fields, layout));
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
			jsClass.addMethod(new FromJsonMethod(fields, layout));
			writePrivateAndProtectedFields(jsClass, fields);
			writePublicGettersAndSetters(jsClass, fields);
			jsClass.addMethod(new ToJsonMethod(fields, layout));

			jsClass.writeToFile();
		}
	}


	private void generateDtos() throws Exception
	{
		for (Class<?> cls : layout.getDtoClasses())
		{
			JavascriptClass jsClass = new JavascriptClass(getOutputPathFromJavaPackage(cls), cls.getSimpleName());
			List<Field> fields = getPrivateAndProtectedMemberVariables(cls, true);

			writePrivateAndProtectedFields(jsClass, fields);
			jsClass.addMemberInitMethod();
			jsClass.addMethod(new FromJsonMethod(fields, layout));
			writePublicGettersAndSetters(jsClass, fields);
			jsClass.addMethod(new ToJsonMethod(fields, layout));

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
		jsClass.addConstant("PATH", path);
	}


	private void writeServiceMethod(JavascriptClass jsClass, ServiceMethod serviceMethod)
	{
		JavascriptMethod method = jsClass.addMethod(serviceMethod.name);

		if(!serviceMethod.headerParams.isEmpty())
		{
			method.addBody("var headerParams = {};");
		}

		if(!serviceMethod.pathParams.isEmpty())
		{
			method.addBody("var pathParams = {}");
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
				jsClass.addConstant(field.getName(), field.getInt(null));
			}
		}

		List<Field> constants = getPublicClassConstants(cls);

		for (Field field : constants)
		{
			jsClass.addConstant(field.getName(), field.getInt(null));
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
