package com.misternerd.resteasytox.swift.helper;

public class BuildableHelper
{
	public static void addIndent(StringBuilder sb, int indent)
	{
		for (int i = 0; i < indent; i++)
		{
			sb.append("\t");
		}
	}


	public static void addNewline(StringBuilder sb)
	{
		sb.append("\n");
	}


	/**
	 * Will add a some Newlines to the StringBuilder.
	 */
	public static void addSpace(StringBuilder sb)
	{
		sb.append("\n\n");
	}
}
