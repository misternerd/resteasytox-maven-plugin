package com.misternerd.resteasytox.swift;

import java.lang.reflect.Field;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.misternerd.resteasytox.AbstractResteasyConverter;
import com.misternerd.resteasytox.RestServiceLayout;
import com.misternerd.resteasytox.swift.objects.SwiftClass;
import com.misternerd.resteasytox.swift.objects.SwiftEnum;
import com.misternerd.resteasytox.swift.objects.SwiftEnumItem;
import com.misternerd.resteasytox.swift.objects.SwiftFile;

public class ResteasyToSwiftConverter extends AbstractResteasyConverter
{

	public ResteasyToSwiftConverter(Path outputPath, String javaPackageName, RestServiceLayout layout)
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
		generateDtos();
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void generateDtos() throws Exception
	{
		for (Class<?> cls : layout.getDtoClasses())
		{
			
			Path classPath = getOutputPathFromJavaPackage(cls);
			
			String name = cls.getSimpleName();
			
			List<Field> enumerations = getEnumConstants(cls);
			
			SwiftFile swiftFile;

			if (enumerations != null)
			{
				swiftFile = new SwiftEnum(classPath, name);
				
				Class<? extends Enum> enumClass = (Class<? extends Enum>) cls;
				for (Field field : enumerations)
				{
					SwiftEnumItem enumItem = new SwiftEnumItem(field.getName(), Enum.valueOf(enumClass, field.getName()).toString());
					((SwiftEnum)swiftFile).addEnumItem(enumItem);
				}
			}
			else
			{
				String superClass = null;
				if (cls.getSuperclass() != null)
				{
					superClass = cls.getSuperclass().getSimpleName();
				}
				
				swiftFile = new SwiftClass(classPath, name, superClass);
			}

			swiftFile.writeToFile();
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

		return Paths.get(outputPath.toString(), cls.getSimpleName() + ".swift");
	}

}
