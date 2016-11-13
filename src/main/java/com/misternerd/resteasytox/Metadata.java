package com.misternerd.resteasytox;

/**
 * Project details configurable via pom.xml
 *
 * Date: 02.11.2016
 */
public class Metadata
{

	private String name = "resteasytoxclient";

	private String description = "Auto-generated REST client for a JAX-RS project";

	private String author = "ResteasyToX Code Generator";

	private String homepage = "https://github.com/misternerd/resteasytox-maven-plugin";

	private String email = "";

	private String scmUrl = "https://github.com/misternerd/resteasytox-maven-plugin";


	public Metadata()
	{
	}


	public String getName()
	{
		return name;
	}


	public String getDescription()
	{
		return description;
	}


	public String getAuthor()
	{
		return author;
	}


	public String getHomepage()
	{
		return homepage;
	}


	public String getEmail()
	{
		return email;
	}


	public String getScmUrl()
	{
		return scmUrl;
	}
}
