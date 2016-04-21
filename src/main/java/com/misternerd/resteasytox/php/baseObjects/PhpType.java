package com.misternerd.resteasytox.php.baseObjects;

public class PhpType extends AbstractBaseObject
{

	public final PhpNamespace namespace;

	public final String name;

	/**
	 * This is added to an import, for example [] if it is an array
	 */
	public final String suffix;

	public final boolean addToTypeComment;

	public final boolean addAsTypeHint;

	public final boolean nullable;


	public PhpType(PhpNamespace namespace, String name, String suffix, boolean addToVarComment, boolean addAsTypeHint, boolean nullable)
	{
		this.namespace = namespace;
		this.name = name;
		this.suffix = suffix;
		this.addToTypeComment = addToVarComment;
		this.addAsTypeHint = false;
		this.nullable = nullable;
	}


	@Override
	public void build(StringBuilder sb, int indentCount)
	{
		if(!addToTypeComment)
		{
			return;
		}

		if (namespace != null)
		{
			String absoluteNamespace = namespace.toAbsoluteNamespace(true);
			sb.append(absoluteNamespace);

			if(!"\\".equals(absoluteNamespace))
			{
				sb.append("\\");
			}
		}

		sb.append(name);

		if(suffix != null)
		{
			sb.append(suffix);
		}

		if(nullable)
		{
			sb.append("|null");
		}
	}


	public void buildAsImport(StringBuilder sb, int indentCount)
	{
		String indent = "\n" + getIndent(indentCount);

		sb.append(indent).append("use ");

		if (namespace != null)
		{
			sb.append(namespace.toAbsoluteNamespace(false)).append("\\");
		}

		sb.append(name).append(";");
	}


	public void buildAsClassExtends(StringBuilder sb)
	{
		sb.append(" extends ").append(name);
	}


	@Override
	public String toString()
	{
		return String.format("%s(namespace=%s, name=%s)", getClass().getSimpleName(), namespace, name);
	}


	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null || getClass() != obj.getClass())
		{
			return false;
		}

		PhpType other = (PhpType) obj;

		if (name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!name.equals(other.name))
		{
			return false;
		}

		if (namespace == null)
		{
			if (other.namespace != null)
			{
				return false;
			}
		}
		else if (!namespace.equals(other.namespace))
		{
			return false;
		}

		return true;
	}

}
