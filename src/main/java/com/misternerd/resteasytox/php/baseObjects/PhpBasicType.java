package com.misternerd.resteasytox.php.baseObjects;

public class PhpBasicType extends PhpType
{

	public static final PhpBasicType STRING = new PhpBasicType("string", true, false, false);

	public static final PhpBasicType BOOLEAN = new PhpBasicType("bool", true, false, false);

	public static final PhpBasicType INT = new PhpBasicType("int", true, false, false);

	public static final PhpBasicType FLOAT = new PhpBasicType("float", true, false, false);

	public static final PhpBasicType ARRAY = new PhpBasicType("array", true, true, false);

	public static final PhpBasicType MIXED = new PhpBasicType("mixed", true, false, false);


	public PhpBasicType(String name, boolean addToVarComment, boolean addAsTypeHint, boolean nullable)
	{
		super(null, name, null, addToVarComment, addAsTypeHint, nullable);
	}

}
