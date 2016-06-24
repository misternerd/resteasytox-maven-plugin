package com.misternerd.resteasytox.javascript.objects;


import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;

public class JavascriptParameter
{

	public final JavascriptType type;

	public final String name;


	public JavascriptParameter(JavascriptType type, String name)
	{
		this.name = name;
		this.type = type;
	}


	public JavascriptParameter(JavascriptPrivateMember member)
	{
		this.type = member.type;
		this.name = "_" + member.name;
	}


	public JavascriptParameter(JavascriptPublicMember member)
	{
		this.type = member.type;
		this.name = "_" + member.name;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}

		if (obj == null || !(obj instanceof JavascriptParameter))
		{
			return false;
		}

		JavascriptParameter other = (JavascriptParameter) obj;

		return ((name != null && name.equals(other.name)) || (name == null && other.name == null));
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
