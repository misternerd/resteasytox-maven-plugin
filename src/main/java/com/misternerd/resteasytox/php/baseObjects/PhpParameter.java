package com.misternerd.resteasytox.php.baseObjects;

import org.apache.commons.lang3.StringUtils;

public class PhpParameter
{

	public final PhpType type;

	public final String originalName;

	/**
	 * If the original name doesn't work with PHP, replace it.
	 */
	public final String adaptedName;

	public final String value;


	public PhpParameter(PhpType type, String name)
	{
		this(type, name, null);
	}


	public PhpParameter(PhpType type, String name, String value)
	{
		if(type == null | name == null)
		{
			throw new IllegalArgumentException("PhpParam required name=" + name + " and type=" + type + " not to be null");
		}

		this.type = type;
		this.originalName = name;
		this.adaptedName = getAdaptedNameOrNull(originalName);
		this.value = value;
	}


	private String getAdaptedNameOrNull(String originalName)
	{
		String adaptedName = originalName.replaceAll("[^a-zA-Z0-9]+", "");

		if (!originalName.equals(adaptedName))
		{
			return StringUtils.uncapitalize(adaptedName);
		}

		return null;
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

		return ( (type != null && type.equals(other.type)) || (type == null && other.type == null) )
			&& ( (originalName != null && originalName.equals(other.originalName)) || (originalName == null && other.originalName == null) );
	}


	@Override
	public int hashCode()
	{
		int result = 298;

		if (type != null)
		{
			result += 7 * type.hashCode();
		}

		if (originalName != null)
		{
			result += 13 * originalName.hashCode();
		}

		return result;
	}


	@Override
	public String toString()
	{
		return String.format("%s(type=%s, name=%s)", getClass().getSimpleName(), type, originalName);
	}

}
