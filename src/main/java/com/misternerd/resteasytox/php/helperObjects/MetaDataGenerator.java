package com.misternerd.resteasytox.php.helperObjects;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.misternerd.resteasytox.Metadata;
import org.apache.commons.io.IOUtils;

import com.misternerd.resteasytox.php.baseObjects.PhpNamespace;

public class MetaDataGenerator
{

	private final Path outputPath;

	protected final PhpNamespace namespace;

	private final Metadata metadata;


	public MetaDataGenerator(Path outputPath, PhpNamespace namespace, Metadata metadata)
	{
		this.outputPath = outputPath;
		this.namespace = namespace;
		this.metadata = metadata;
	}


	public void createFiles() throws IOException
	{
		createComposerJson();
		createReadme();
	}


	private void createComposerJson() throws IOException
	{
		InputStream stream = getClass().getClassLoader().getResourceAsStream("php_composer_template.json");
		String content = IOUtils.toString(stream);
		content = content.replace("##NAMESPACE##", namespace.toAbsoluteNamespace(false));
		content = content.replace("##NAME##", metadata.getName());
		content = content.replace("##DESCRIPTION##", metadata.getDescription());
		content = content.replace("##AUTHOR##", metadata.getAuthor());
		content = content.replace("##HOMEPAGE##", metadata.getHomepage());
		content = content.replace("##SCMURL##", metadata.getScmUrl());
		content = content.replace("##EMAIL##", metadata.getEmail());
		Files.write(new File(outputPath.toFile(), "composer.json").toPath(), content.toString().getBytes("UTF-8"));
	}


	private void createReadme() throws IOException
	{
		InputStream stream = getClass().getClassLoader().getResourceAsStream("php_readme_template.md");
		String content = IOUtils.toString(stream);
		content = content.replace("##NAMESPACE##", namespace.toAbsoluteNamespace(true));
		Files.write(new File(outputPath.toFile(), "README.md").toPath(), content.toString().getBytes("UTF-8"));
	}

}
