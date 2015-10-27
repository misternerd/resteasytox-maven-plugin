package com.misternerd.resteasytox.php.helperObjects;

import java.io.IOException;
import java.nio.file.Path;

import com.misternerd.resteasytox.php.baseObjects.PhpClass;
import com.misternerd.resteasytox.php.baseObjects.PhpNamespace;
import com.misternerd.resteasytox.php.baseObjects.PhpType;

public abstract class AbstractHelperObject
{

	protected final PhpNamespace namespace;

	protected final PhpClass phpClass;

	protected final Path outputPath;


	public AbstractHelperObject(Path outputPath, PhpNamespace namespace, String className, PhpType extendsClass)
	{
		this.namespace = namespace;
		this.phpClass = new PhpClass(outputPath, namespace, className, extendsClass);
		this.outputPath = outputPath;
	}


	public PhpClass getPhpClass()
	{
		return phpClass;
	}


	public void write() throws IOException
	{
		phpClass.writeToFile();
	}

}
