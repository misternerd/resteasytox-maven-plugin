package com.misternerd.resteasytox.swift;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.misternerd.resteasytox.AbstractResteasyConverter;
import com.misternerd.resteasytox.RestServiceLayout;
import com.misternerd.resteasytox.base.ServiceClass;
import com.misternerd.resteasytox.base.ServiceMethod;
import com.misternerd.resteasytox.swift.helper.ReflectionHelper;
import com.misternerd.resteasytox.swift.helper.SwiftTypeHelper;
import com.misternerd.resteasytox.swift.objects.SwiftClass;
import com.misternerd.resteasytox.swift.objects.SwiftEnum;
import com.misternerd.resteasytox.swift.objects.SwiftFile;
import com.misternerd.resteasytox.swift.objects.SwiftProperty;
import com.misternerd.resteasytox.swift.objects.SwiftServiceMethod;

public class ResteasyToSwiftConverter extends AbstractResteasyConverter
{
	private static final String FILE_EXTENSION = ".swift";

	private boolean generateAlamofireServices;


	public ResteasyToSwiftConverter(Path outputPath, String javaPackageName, RestServiceLayout layout, boolean generateAlamofireServices)
	{
		super(outputPath, javaPackageName, layout);
		this.generateAlamofireServices = generateAlamofireServices;
	}


	@Override
	public void convert() throws Exception
	{
		if (!Files.isDirectory(outputPath))
		{
			Files.createDirectories(outputPath);
		}

		generateDtos();
		generateRequestObjects();
		generateResponseObjects();

		if (generateAlamofireServices)
		{
			generateServiceClasses();
		}
	}


	private void generateDtos() throws Exception
	{
		for (Class<?> cls : layout.getDtoClasses())
		{

			Path classPath = getOutputPathFromJavaPackage(cls);

			String name = cls.getSimpleName();

			// A DTO may just be a plain enum, so we don't have to create a
			// class in this case.
			SwiftFile swiftFile = mayGeneratePlainEnum(cls, classPath, name);

			if (swiftFile == null)
			{
				String superClass = getSuperClassName(cls);

				SwiftClass swiftClass = new SwiftClass(classPath, name, superClass);

				List<Field> fields = getPrivateAndProtectedMemberVariables(cls, false);
				writeProperties(swiftClass, fields);

				swiftClass.setIncludeConstructor(true);

				// TODO: Integrate generation from and to json object

				swiftFile = swiftClass;
			}

			swiftFile.writeToFile();
		}
	}


	private void generateRequestObjects() throws Exception
	{
		for (Class<?> cls : layout.getRequestClasses())
		{
			Path classPath = getOutputPathFromJavaPackage(cls);
			String name = cls.getSimpleName();
			String superClass = getSuperClassName(cls);

			SwiftClass swiftClass = new SwiftClass(classPath, name, superClass);

			List<Field> fields = getPrivateAndProtectedMemberVariables(cls, false);
			writeProperties(swiftClass, fields);

			List<Field> constants = getPublicClassConstants(cls);
			writeConstants(swiftClass, constants);

			swiftClass.setIncludeConstructor(true);
			swiftClass.setIncludeAlamofire(generateAlamofireServices);

			swiftClass.writeToFile();

		}
	}


	private void generateResponseObjects() throws Exception
	{
		for (Class<?> cls : layout.getResponseClasses())
		{
			Path classPath = getOutputPathFromJavaPackage(cls);
			String name = cls.getSimpleName();
			String superClass = getSuperClassName(cls);

			SwiftClass swiftClass = new SwiftClass(classPath, name, superClass);

			List<Field> fields = getPrivateAndProtectedMemberVariables(cls, false);
			writeProperties(swiftClass, fields);

			List<Field> constants = getPublicClassConstants(cls);
			writeConstants(swiftClass, constants);

			swiftClass.setIncludeConstructor(true);
			swiftClass.setIncludeJSONHelper(generateAlamofireServices);

			swiftClass.writeToFile();

		}
	}


	private void generateServiceClasses() throws IOException
	{

		for (ServiceClass serviceClass : layout.getServiceClasses())
		{

			// TODO: We might want to add a superclass here
			Path filePath = getOrCreateFilePath(outputPath, "service", serviceClass.name + FILE_EXTENSION);

			SwiftClass swiftClass = new SwiftClass(filePath, serviceClass.name, null);

			for (ServiceMethod method : serviceClass.methods)
			{
				writeServiceMethods(swiftClass, method);
			}

			swiftClass.writeToFile();

		}
	}


	private String getSuperClassName(Class<?> cls)
	{
		String superClass = null;
		if (cls.getSuperclass() != null)
		{
			superClass = cls.getSuperclass().getSimpleName();
		}
		return superClass;
	}


	/**
	 * Currently only supports String and int
	 */
	private String getDefaultValue(Field field)
	{

		String defaultValue = null;

		try
		{
			int intValue = field.getInt(null);
			defaultValue = Integer.toString(intValue);
		}
		catch (Exception e)
		{
			// We did not get a value, so there is no default int
		}

		if (defaultValue == null)
		{
			try
			{
				defaultValue = (String) field.get(null);
			}
			catch (Exception e)
			{
				// We did not get a value, so there is no default string
			}
		}

		return defaultValue;
	}


	private void writeProperties(SwiftClass swiftClass, List<Field> fields)
	{
		for (Field field : fields)
		{
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			boolean isFinal = Modifier.isFinal(field.getModifiers());
			boolean isOptional = ReflectionHelper.isOptional(field, layout.getAnnotations());
			String defaultValue = getDefaultValue(field);

			SwiftProperty property = new SwiftProperty(isStatic, isFinal, SwiftTypeHelper.getSwiftType(field), field.getName(), isOptional, defaultValue);
			swiftClass.addProperty(property);
		}
	}


	private void writeConstants(SwiftClass swiftClass, List<Field> fields)
	{
		for (Field field : fields)
		{
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			boolean isFinal = Modifier.isFinal(field.getModifiers());
			boolean isOptional = ReflectionHelper.isOptional(field, layout.getAnnotations());
			String defaultValue = getDefaultValue(field);

			SwiftProperty property = new SwiftProperty(isStatic, isFinal, SwiftTypeHelper.getSwiftType(field), field.getName(), isOptional, defaultValue);
			swiftClass.addConstant(property);
		}
	}


	private void writeServiceMethods(SwiftClass swiftClass, ServiceMethod serviceMethod)
	{
		System.out.println(String.format("ServiceMethod: %s", serviceMethod.name));
		SwiftServiceMethod method = new SwiftServiceMethod(serviceMethod);
		swiftClass.addMethod(method);
	}


	/**
	 * Will generate an SwiftEnum from the class if EnumConstants are provided.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private SwiftEnum mayGeneratePlainEnum(Class<?> cls, Path classPath, String name)
	{
		List<Field> enumerations = getEnumConstants(cls);

		if (enumerations.isEmpty())
		{
			return null;
		}

		SwiftEnum swiftFile = new SwiftEnum(classPath, name);

		Class<? extends Enum> enumClass = (Class<? extends Enum>) cls;
		for (Field field : enumerations)
		{
			swiftFile.addEnumItem(field.getName(), Enum.valueOf(enumClass, field.getName()).toString());
		}

		return swiftFile;
	}


	private Path getOutputPathFromJavaPackage(Class<?> cls) throws IOException
	{
		String pathExtended = cls.getPackage().getName().replace(javaPackageName, "").replace(".", File.separator);
		Path outputPath = Paths.get(this.outputPath.toString(), pathExtended);

		if (!Files.isDirectory(outputPath))
		{
			Files.createDirectories(outputPath);
		}

		return Paths.get(outputPath.toString(), cls.getSimpleName() + FILE_EXTENSION);
	}


	private Path getOrCreateFilePath(Path path, String addon, String file) throws IOException
	{
		Path outputPath = path;
		if (addon != null)
		{
			outputPath = Paths.get(outputPath.toString(), addon);
			if (!Files.isDirectory(outputPath))
			{
				Files.createDirectories(outputPath);
			}
		}

		return Paths.get(outputPath.toString(), file);
	}

}
