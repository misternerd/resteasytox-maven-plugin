package com.misternerd.resteasytox.javascript.objects;

import com.misternerd.resteasytox.php.baseObjects.PhpParameter;

public class JavascriptParameter
{

	public final String name;


	public JavascriptParameter(String name)
	{
		this.name = name;
	}


	public JavascriptParameter(JavascriptMember member)
	{
		this.name = "_" + member.name;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}

		if (obj == null || !(obj instanceof PhpParameter))
		{
			return false;
		}

		PhpParameter other = (PhpParameter) obj;

		return ((name != null && name.equals(other.originalName)) || (name == null && other.originalName == null));
	}


	@Override
	public int hashCode()
	{
		int result = 31;

		if (name != null)
		{
			result += 13 * name.hashCode();
		}

		return result;
	}


	@Override
	public String toString()
	{
		return String.format("%s(name=%s)", getClass().getSimpleName(), name);
	}
}
