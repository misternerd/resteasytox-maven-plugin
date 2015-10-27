package com.misternerd.resteasytox.php.baseObjects;


import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class PhpNamespace
{

	public static final PhpNamespace ROOT = new PhpNamespace("\\");

	public final String[] namespace;


	public PhpNamespace(String[] namespace)
	{
		this.namespace = namespace;
	}


	public PhpNamespace(PhpNamespace parentNamespace, String addedNamespace)
	{
		String[] namespace = convertStringNamespaceToArray(addedNamespace);
		this.namespace = Stream.concat(Arrays.stream(parentNamespace.namespace), Arrays.stream(namespace)).toArray(String[]::new);
	}


	public PhpNamespace(String namespaceString)
	{
		this.namespace = convertStringNamespaceToArray(namespaceString);
	}


	private String[] convertStringNamespaceToArray(String namespaceString)
	{
		if (namespaceString.startsWith("\\"))
		{
			namespaceString = namespaceString.substring(1);
		}

		if(namespaceString.endsWith("\\"))
		{
			namespaceString = namespaceString.substring(0, namespaceString.length() - 1);
		}

		return namespaceString.split("\\\\");
	}


	public String toAbsoluteNamespace(boolean prefixRoot)
	{
		String result = "";

		if (prefixRoot)
		{
			result += "\\";
		}

		result += StringUtils.join(namespace, "\\");

		return result;
	}


	public String toRestRelativeNamespace(String phpRestNamespace)
	{
		String absoluteNamespace = toAbsoluteNamespace(false);
		return absoluteNamespace.replace(phpRestNamespace, "");
	}


	@Override
	public String toString()
	{
		return String.format("%s(namespace=%s)", getClass().getSimpleName(), StringUtils.join(namespace, "\\"));
	}


	@Override
	public boolean equals(Object obj)
	{
		if(obj == null || !(obj instanceof PhpNamespace))
		{
			return false;
		}

		PhpNamespace other = (PhpNamespace) obj;
		return Arrays.deepEquals(namespace, other.namespace);
	}


	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(namespace);
		return result;
	}

}
