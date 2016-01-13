package com.misternerd.resteasytox.swift;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.misternerd.resteasytox.AbstractResteasyConverter;
import com.misternerd.resteasytox.RestServiceLayout;
import com.misternerd.resteasytox.base.AbstractDto;
import com.misternerd.resteasytox.base.ServiceClass;
import com.misternerd.resteasytox.base.ServiceMethod;
import com.misternerd.resteasytox.swift.helper.FileHelper;
import com.misternerd.resteasytox.swift.helper.ReflectionHelper;
import com.misternerd.resteasytox.swift.helper.SwiftMarshallingHelper;
import com.misternerd.resteasytox.swift.helper.SwiftTypeHelper;
import com.misternerd.resteasytox.swift.objects.*;

public class ResteasyToSwiftConverter extends AbstractResteasyConverter
{
	private boolean generateAlamofireServices;
	private boolean supportObjC;


	public ResteasyToSwiftConverter(Path outputPath, String javaPackageName, RestServiceLayout layout, boolean generateAlamofireServices, boolean supportObjC)
	{
		super(outputPath, javaPackageName, layout);
		this.generateAlamofireServices = generateAlamofireServices;
		this.supportObjC = supportObjC;
	}


	@Override
	public void convert() throws Exception
	{
		if (!Files.isDirectory(outputPath))
		{
			Files.createDirectories(outputPath);
		}

		generateHelper();
		generateBaseClasses();
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
			SwiftFile swiftFile = new SwiftFile(classPath, name);

			SwiftEnum swiftEnum = mayGeneratePlainEnum(cls, name);

			if (swiftEnum != null)
			{
				swiftFile.addEnum(swiftEnum);
			}
			else
			{
				String superClass = getSuperClassName(cls);

				SwiftClass swiftClass = new SwiftClass(name, superClass, supportObjC);

				if (layout.getDtoClasses().contains(cls.getSuperclass()))
				{
					// Superclass is also a dto and will include the needed
					// protocols
					swiftClass.setOverrideProtocols(true);
				}

				if (layout.abstractDtos.containsKey(cls))
				{
					AbstractDto abstractDto = layout.abstractDtos.get(cls);

					SwiftMethod method = SwiftMarshallingHelper.createUnmarshallingMethodForAbstractClass(abstractDto.abstractClass.getSimpleName());
					swiftClass.addMethod(method);

					SwiftMethod arrayMethod = SwiftMarshallingHelper.createUnmarshallingForAbstractClass(abstractDto);
					swiftClass.addMethod(arrayMethod);
				}

				List<Field> fields = getPrivateAndProtectedMemberVariables(cls, false);
				writeProperties(swiftClass, fields);

				List<Field> superProperties = getMemberVariablesOfAllSuperclasses(cls);
				writePropertiesOfSuper(swiftClass, superProperties);

				swiftClass.setIncludeConstructor(true);
				swiftClass.setIncludeMarshalling(generateAlamofireServices);
				swiftClass.setIncludeUnmarshalling(generateAlamofireServices);

				swiftFile.addClass(swiftClass);
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

			SwiftFile swiftFile = new SwiftFile(classPath, name);
			SwiftClass swiftClass = new SwiftClass(name, superClass, supportObjC);
			swiftFile.addClass(swiftClass);

			if (layout.getRequestClasses().contains(cls.getSuperclass()))
			{
				// Superclass is also a request and will include the needed
				// protocols
				swiftClass.setOverrideProtocols(true);
			}

			List<Field> fields = getPrivateAndProtectedMemberVariables(cls, false);
			writeProperties(swiftClass, fields);

			List<Field> superProperties = getMemberVariablesOfAllSuperclasses(cls);
			writePropertiesOfSuper(swiftClass, superProperties);

			List<Field> constants = getPublicClassConstants(cls);
			writeConstants(swiftClass, constants);

			swiftClass.setIncludeConstructor(true);
			swiftClass.setIncludeMarshalling(generateAlamofireServices);

			swiftFile.writeToFile();

		}
	}


	private void generateBaseClasses() throws Exception
	{
		for (Class<?> cls : layout.getBaseClasses())
		{

			Path classPath = getOutputPathFromJavaPackage(cls);

			String name = cls.getSimpleName();

			// A DTO may just be a plain enum, so we don't have to create a
			// class in this case.
			SwiftFile swiftFile = new SwiftFile(classPath, name);

			SwiftEnum swiftEnum = mayGeneratePlainEnum(cls, name);

			if (swiftEnum == null)
			{
				String superClass = getSuperClassName(cls);

				SwiftClass swiftClass = new SwiftClass(name, superClass, supportObjC);

				List<Field> fields = getPrivateAndProtectedMemberVariables(cls, false);
				writeProperties(swiftClass, fields);

				List<Field> superProperties = getMemberVariablesOfAllSuperclasses(cls);
				writePropertiesOfSuper(swiftClass, superProperties);

				List<Field> constants = getPublicClassConstants(cls);
				writeConstants(swiftClass, constants);

				swiftClass.setIncludeConstructor(true);

				swiftFile.addClass(swiftClass);
			}

			swiftFile.writeToFile();
		}
	}


	private void generateResponseObjects() throws Exception
	{
		for (Class<?> cls : layout.getResponseClasses())
		{
			Path classPath = getOutputPathFromJavaPackage(cls);
			String name = cls.getSimpleName();
			String superClass = getSuperClassName(cls);

			SwiftFile swiftFile = new SwiftFile(classPath, name);
			SwiftClass swiftClass = new SwiftClass(name, superClass, supportObjC);
			swiftFile.addClass(swiftClass);
			

			if (layout.getResponseClasses().contains(cls.getSuperclass()))
			{
				// Superclass is also a response and will include the needed
				// protocols
				swiftClass.setOverrideProtocols(true);
			}

			List<Field> fields = getPrivateAndProtectedMemberVariables(cls, false);
			writeProperties(swiftClass, fields);

			List<Field> superProperties = getMemberVariablesOfAllSuperclasses(cls);
			writePropertiesOfSuper(swiftClass, superProperties);

			List<Field> constants = getPublicClassConstants(cls);
			writeConstants(swiftClass, constants);

			swiftClass.setIncludeConstructor(true);
			swiftClass.setIncludeUnmarshalling(generateAlamofireServices);

			swiftFile.writeToFile();

		}
	}


	private void generateServiceClasses() throws IOException
	{

		for (ServiceClass serviceClass : layout.getServiceClasses())
		{

			// TODO: We might want to add a superclass here
			Path filePath = FileHelper.getOrCreateFilePath(outputPath, "service", serviceClass.name, FileHelper.FILE_EXTENSION_SWIFT);

			SwiftFile swiftFile = new SwiftFile(filePath, serviceClass.name);
			SwiftClass swiftClass = new SwiftClass(serviceClass.name, "AbstractService", supportObjC);
			swiftFile.addClass(swiftClass);

			SwiftProperty property = new SwiftProperty(true, true, SwiftTypeHelper.getSwiftTypeFromClass(serviceClass.path.getClass()), "servicePath", false, "\"" + serviceClass.path + "\"", supportObjC);
			swiftClass.addConstant(property);

			for (ServiceMethod method : serviceClass.methods)
			{
				writeServiceMethods(swiftClass, method);
			}

			swiftFile.writeToFile();

		}
	}


	private void generateHelper() throws IOException
	{
		SwiftMarshallingHelper.generateMarshallingHelper(outputPath, supportObjC);

	}


	private String getSuperClassName(Class<?> cls)
	{
		String superClass = null;
		if (cls.getSuperclass() != null && cls.getSuperclass() != Object.class)
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
		propertiesForFields(fields).forEach(swiftClass::addProperty);
	}


	private void writePropertiesOfSuper(SwiftClass swiftClass, List<Field> fields)
	{
		propertiesForFields(fields).forEach(swiftClass::addSuperProperty);
	}

	private List<SwiftProperty> propertiesForFields(List<Field> fields) {
		List<SwiftProperty> properties = new ArrayList<>();
		for (Field field : fields)
		{
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			boolean isFinal = Modifier.isFinal(field.getModifiers());
			boolean isOptional = ReflectionHelper.isOptional(field, layout.getAnnotations());
			boolean isAbstract = layout.abstractDtos.containsKey(field.getClass());
			String defaultValue = getDefaultValue(field);

			SwiftProperty property = new SwiftProperty(isStatic, isFinal, SwiftTypeHelper.getSwiftType(field), field.getName(), isOptional, defaultValue, supportObjC);
			property.setAbstract(isAbstract);

			properties.add(property);
		}
		return properties;
	}


	private void writeConstants(SwiftClass swiftClass, List<Field> fields)
	{
		for (Field field : fields)
		{
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			boolean isFinal = Modifier.isFinal(field.getModifiers());
			boolean isOptional = ReflectionHelper.isOptional(field, layout.getAnnotations());
			String defaultValue = getDefaultValue(field);

			SwiftProperty property = new SwiftProperty(isStatic, isFinal, SwiftTypeHelper.getSwiftType(field), field.getName(), isOptional, defaultValue, supportObjC);
			swiftClass.addConstant(property);
		}
	}


	private void writeServiceMethods(SwiftClass swiftClass, ServiceMethod serviceMethod)
	{
		SwiftServiceMethod method = new SwiftServiceMethod(serviceMethod, supportObjC);
		swiftClass.addMethod(method);
	}


	/**
	 * Will generate an SwiftEnum from the class if EnumConstants are provided.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private SwiftEnum mayGeneratePlainEnum(Class<?> cls, String name)
	{
		List<Field> enumerations = getEnumConstants(cls);

		if (enumerations.isEmpty())
		{
			return null;
		}

		SwiftEnum swiftEnum = new SwiftEnum(name, supportObjC);

		Class<? extends Enum> enumClass = (Class<? extends Enum>) cls;
		for (Field field : enumerations)
		{
			swiftEnum.addEnumItem(field.getName(), Enum.valueOf(enumClass, field.getName()).toString());
		}

		swiftEnum.setIncludeMarshalling(generateAlamofireServices);
		swiftEnum.setIncludeUnmarshalling(generateAlamofireServices);

		return swiftEnum;
	}


	private Path getOutputPathFromJavaPackage(Class<?> cls) throws IOException
	{
		String pathExtended = cls.getPackage().getName().replace(javaPackageName, "").replace(".", File.separator);
		Path outputPath = Paths.get(this.outputPath.toString(), pathExtended);

		if (!Files.isDirectory(outputPath))
		{
			Files.createDirectories(outputPath);
		}

		return Paths.get(outputPath.toString(), cls.getSimpleName() + FileHelper.FILE_EXTENSION_SWIFT);
	}

}
