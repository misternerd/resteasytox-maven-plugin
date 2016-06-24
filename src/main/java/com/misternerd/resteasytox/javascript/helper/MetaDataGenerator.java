package com.misternerd.resteasytox.javascript.helper;

import org.apache.commons.io.IOUtils;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class MetaDataGenerator
{

	private final Path outputPath;

	private final MavenProject project;


	public MetaDataGenerator(Path outputPath, MavenProject project)
	{
		this.outputPath = outputPath;
		this.project = project;
	}


	public void createFiles() throws IOException
	{
		createNpmPackageFile();
	}


	private void createNpmPackageFile() throws IOException
	{
		InputStream stream = getClass().getClassLoader().getResourceAsStream("js_package_template.json");
		String content = IOUtils.toString(stream);
		content = content.replace("##VERSION##", project.getVersion());
		Files.write(new File(outputPath.toFile(), "package.json").toPath(), content.getBytes("UTF-8"));
	}

}