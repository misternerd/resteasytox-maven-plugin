package com.misternerd.resteasytox.swift.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHelper
{
	public static final String FILE_EXTENSION_SWIFT = ".swift";

	static public Path getOrCreateFilePath(Path path, String addon, String file, String extension) throws IOException
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
