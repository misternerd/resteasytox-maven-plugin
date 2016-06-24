package com.misternerd.resteasytox.javascript.objects.types;

/**
 * An array encapsulates another type basis and add the array definition.
 *
 * Date: 22.06.2016
 */
public class JavascriptArrayType extends JavascriptType
{

	public static final JavascriptArrayType STRING_ARRAY = new JavascriptArrayType(JavascriptBasicType.STRING);

	public static final JavascriptArrayType NUMBER_ARRAY = new JavascriptArrayType(JavascriptBasicType.NUMBER);


	public JavascriptArrayType(JavascriptType basis)
	{
		super(basis.name + "[]");
	}

}
