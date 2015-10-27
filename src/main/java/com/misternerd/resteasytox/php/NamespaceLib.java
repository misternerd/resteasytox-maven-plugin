package com.misternerd.resteasytox.php;

import com.misternerd.resteasytox.php.baseObjects.PhpNamespace;

public class NamespaceLib
{

	public static PhpNamespace convertJavaNamespace(PhpNamespace phpBaseNamespace, String javaPackageName, Class<?> type)
	{
		String partialNamespace = type.getPackage().getName().replace(javaPackageName + ".", "").replace(".", "\\");
		return new PhpNamespace(phpBaseNamespace, partialNamespace);
	}

}
