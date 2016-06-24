package com.misternerd.resteasytox.javascript.objects.types;

/**
 * Basic typescript types for easy access.
 *
 * 22.06.2016
 */
public class JavascriptBasicType extends JavascriptType
{

	public static final JavascriptBasicType BOOLEAN = new JavascriptBasicType("boolean");

	public static final JavascriptBasicType NUMBER = new JavascriptBasicType("number");

	public static final JavascriptBasicType STRING = new JavascriptBasicType("string");

	public static final JavascriptBasicType ANY = new JavascriptBasicType("any");

	public static final JavascriptBasicType VOID = new JavascriptBasicType("void");

	public static final JavascriptBasicType OBJECT = new JavascriptBasicType("Object");


	private JavascriptBasicType(String name)
	{
		super(name);
	}

}
