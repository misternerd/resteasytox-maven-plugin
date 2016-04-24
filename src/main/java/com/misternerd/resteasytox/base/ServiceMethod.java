package com.misternerd.resteasytox.base;

import java.util.SortedSet;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ServiceMethod
{

	public enum RequestMethod
	{
		POST, GET, PUT, DELETE, PATCH
	}

	public final String name;

	public final String path;

	public final RequestMethod httpMethod;

	public final String requestContentType;

	public final String responseContentType;

	public final SortedSet<MethodParameter> headerParams;

	public final SortedSet<MethodParameter> pathParams;

	public final MethodParameter bodyParam;

	public final Class<? extends Object> returnType;


	public ServiceMethod(String name, String path, RequestMethod httpMethod, String requestContentType,
			String responseContentType, SortedSet<MethodParameter> headerParams, SortedSet<MethodParameter> pathParams,
			MethodParameter bodyParam, Class<? extends Object> returnType)
	{
		this.name = name;
		this.path = path;
		this.httpMethod = httpMethod;
		this.requestContentType = requestContentType;
		this.responseContentType = responseContentType;
		this.headerParams = headerParams;
		this.pathParams = pathParams;
		this.bodyParam = bodyParam;
		this.returnType = returnType;
	}


	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

}
