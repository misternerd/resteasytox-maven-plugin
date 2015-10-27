package com.misternerd.resteasytox.php.baseObjects;

public class PhpImport extends AbstractBaseObject
{

	public final String name;

	public final boolean required;

	public final boolean includeOnce;

	public PhpImport(String name, boolean required, boolean includeOnce)
	{
		this.name = name;
		this.required = required;
		this.includeOnce = includeOnce;
	}


	@Override
	public void build(StringBuilder sb, int indentSize)
	{
		sb.append(getIndent(indentSize)).append((required) ? "require" : "include");

		if(includeOnce)
		{
			sb.append("_once");
		}

		sb.append(" '").append(name).append("';\n");
	}

}
