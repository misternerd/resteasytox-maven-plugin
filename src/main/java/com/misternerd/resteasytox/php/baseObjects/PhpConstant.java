package com.misternerd.resteasytox.php.baseObjects;

public class PhpConstant
{

	private final String name;

	private final String value;


	public PhpConstant(String name, String value)
	{
		this.name = name;
		this.value = "'" + value + "'";
	}


	public PhpConstant(String name, int value)
	{
		this.name = name;
		this.value = Integer.toString(value);
	}


	public void build(StringBuilder sb)
	{
		sb.append("const ").append(name).append(" = ").append(value).append(";");
	}
}
