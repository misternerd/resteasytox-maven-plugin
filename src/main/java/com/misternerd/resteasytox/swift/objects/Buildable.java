package com.misternerd.resteasytox.swift.objects;

import com.misternerd.resteasytox.swift.helper.BuildableHelper;

public abstract class Buildable
{
	/**
	 * Should add the String to the StringBuilder
	 */
	abstract public void build(StringBuilder sb);


	/**
	 * Should add the String to the StringBuilder with a leading newline and
	 * indent. Could be overwritten by subclasses to e.g. add more NewLines in
	 * front.
	 */
	public void buildNewline(StringBuilder sb, int indent)
	{
		BuildableHelper.addNewline(sb);
		BuildableHelper.addIndent(sb, indent);
		build(sb);
	}
}
