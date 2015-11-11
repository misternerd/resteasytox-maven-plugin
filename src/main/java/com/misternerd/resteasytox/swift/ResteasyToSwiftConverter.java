package com.misternerd.resteasytox.swift;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.misternerd.resteasytox.AbstractResteasyConverter;
import com.misternerd.resteasytox.RestServiceLayout;

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


	private void generateDtos() throws Exception
	{
		for (Class<?> cls : layout.getDtoClasses())
		{
			String superClass = null;
			if (cls.getSuperclass() != null)
			{
				superClass = cls.getSuperclass().getSimpleName();
			}

			SwiftClass swiftClass = new SwiftClass(getOutputPathFromJavaPackage(cls), cls.getSimpleName(), superClass);

			swiftClass.writeToFile();
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
