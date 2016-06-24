package com.misternerd.resteasytox.javascript.objects.types;

/**
 * Used to generate Typescript mappings for the generated REST client.
 *
 * Date: 22.06.2016
 */
public class JavascriptType
{

	public final String name;


	public JavascriptType(String name)
	{
		this.name = name;
	}


	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}

		JavascriptType that = (JavascriptType) o;

		return name != null ? name.equals(that.name) : that.name == null;

	}


	@Override
	public int hashCode()
	{
		return name != null ? name.hashCode() : 0;
	}
}
