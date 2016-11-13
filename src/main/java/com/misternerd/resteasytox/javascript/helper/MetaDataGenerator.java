package com.misternerd.resteasytox.javascript.helper;

import com.misternerd.resteasytox.Metadata;
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

	private final Metadata metadata;


	public MetaDataGenerator(Path outputPath, MavenProject project, Metadata metadata)
	{
		this.outputPath = outputPath;
		this.project = project;
		this.metadata = metadata;
	}


	public void createFiles() throws IOException
	{
		createNpmPackageFile();
	}


	private void createNpmPackageFile() throws IOException
	{
		InputStream stream = getClass().getClassLoader().getResourceAsStream("js_package_template.json");
		String content = IOUtils.toString(stream);
		content = content.replace("##NAME##", metadata.getName());
		content = content.replace("##VERSION##", project.getVersion());
		content = content.replace("##DESCRIPTION##", metadata.getDescription());
		content = content.replace("##AUTHOR##", metadata.getAuthor());
		content = content.replace("##HOMEPAGE##", metadata.getHomepage());
		content = content.replace("##SCMURL##", metadata.getScmUrl());
		content = content.replace("##EMAIL##", metadata.getEmail());
		Files.write(new File(outputPath.toFile(), "package.json").toPath(), content.getBytes("UTF-8"));
	}

}