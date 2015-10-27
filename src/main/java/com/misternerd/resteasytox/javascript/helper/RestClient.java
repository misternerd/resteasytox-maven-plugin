package com.misternerd.resteasytox.javascript.helper;

import java.nio.file.Path;

import com.misternerd.resteasytox.javascript.objects.JavascriptClass;

public class RestClient extends JavascriptClass
{

	public RestClient(Path outputPath)
	{
		super(outputPath, "RestClient");
		// TODO Auto-generated constructor stub
	}
	
	
	private void buildBody()
	{
		
	}
/*
 * 
var RestClient = function()
{
	const BASE_PATH = 'http://localhost:8080/';

	var self = this;


	function replacePathParamsInPath(path, pathParams)
	{
		for(var paramName in pathParams)
		{
			path = path.replace('{' + paramName + '}', pathParams[paramName]);
		}

		return path;
	}


	function decodeDataToObject(data, type, resultObject)
	{
		if(!data || !resultObject || type != 'json')
		{
			return data;
		}

		return resultObject.fromJson(data);
	}


	this.getRequest = function(path, headerParams, pathParams, contentType, resultType, resultObject)
	{
		path = replacePathParamsInPath(path, pathParams);
		var opts = {
			contentType: contentType,
			method: 'GET'
		};

		if(headerParams)
		{
			opts.headers = headerParams;
		}

		if(resultType == 'application/json' && resultObject)
		{
			opts.dataFilter = function(data, type)
			{
				return decodeDataToObject(data, type, resultObject);
			};
		}

		return $.ajax(path, opts);
	};


	this.postRequest = function(path, headerParams, pathParams, data, contentType, resultObject)
	{
		path = replacePathParamsInPath(path, pathParams);
		var opts = {
			contentType: contentType,
			method: 'POST'
		};

		if(headerParams)
		{
			opts.headers = headerParams;
		}

		if(data)
		{
			opts.data = data;
		}

		if(resultType == 'application/json' && resultObject)
		{
			opts.dataFilter = function(data, type)
			{
				return decodeDataToObject(data, type, resultObject);
			};
		}

		return $.ajax(path, opts);
	};

};
 */
}
