package com.misternerd.resteasytox.php.baseObjects;

public enum PhpVisibility
{
	PRIVATE, PROTECTED, PUBLIC;

	@Override
	public String toString()
	{
		return name().toLowerCase();
	}
}