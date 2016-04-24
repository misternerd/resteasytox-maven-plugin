package com.misternerd.resteasytox.javascript.helper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

import com.misternerd.resteasytox.RestServiceLayout;
import com.misternerd.resteasytox.base.ServiceClass;
import com.misternerd.resteasytox.javascript.objects.JavascriptClass;
import com.misternerd.resteasytox.javascript.objects.JavascriptParameter;

public class RestClient extends JavascriptClass
{

	public RestClient(Path outputPath, RestServiceLayout layout)
	{
		super(Paths.get(outputPath + File.separator + "RestClient.js"), "RestClient");
		buildBody(layout);
	}


	private void buildBody(RestServiceLayout layout)
	{
		for(ServiceClass serviceClass : layout.getServiceClasses())
		{
			addPrivateMember(StringUtils.uncapitalize(serviceClass.name), String.format("new %s(this)", serviceClass.name), false);
		}

		addPrivateMethod("replacePathParamsInPath")
			.addParameter(new JavascriptParameter("path"))
			.addParameter(new JavascriptParameter("pathParams"))
			.addBody("for(var paramName in pathParams)")
			.addBody("{")
				.addBody("\tpath = path.replace('{' + paramName + '}', pathParams[paramName]);")
			.addBody("}")
			.addBody("return REST_BASEPATH + path;");

		addPrivateMethod("decodeDataToObject")
			.addParameter(new JavascriptParameter("jsonString"))
			.addParameter(new JavascriptParameter("resultObject"))
			.addBody("try")
			.addBody("{")
				.addBody("\tif(!jsonString || !resultObject)")
				.addBody("\t{")
					.addBody("\t\treturn jsonString;")
				.addBody("\t}")
				.addBody("\tvar result = resultObject.initFromJson(jsonString);")
				.addBody("\treturn result;")
			.addBody("}")
			.addBody("catch(err)")
			.addBody("{")
				.addBody("\tconsole.log(err);")
			.addBody("}");

		for(ServiceClass serviceClass : layout.getServiceClasses())
		{
			addMethod(StringUtils.uncapitalize(serviceClass.name))
				.addBody("return %s;", StringUtils.uncapitalize(serviceClass.name));
		}

		addGetRequestMethod();
		addPostRequestMethod();
		addPutRequestMethod();
		addDeleteRequestMethod();

	}


	private void addGetRequestMethod()
	{
		addMethod("getRequest")
			.addParameter(new JavascriptParameter("path"))
			.addParameter(new JavascriptParameter("headerParams"))
			.addParameter(new JavascriptParameter("pathParams"))
			.addParameter(new JavascriptParameter("contentType"))
			.addParameter(new JavascriptParameter("resultType"))
			.addParameter(new JavascriptParameter("resultObject"))
			.addBody("path = replacePathParamsInPath(path, pathParams);")
			.addBody("var opts = {contentType: contentType, method: 'GET'};")
			.addBody("if(headerParams) {")
				.addBody("\topts.headers = headerParams;")
			.addBody("}")
			.addBody("if(resultType == 'application/json' && resultObject)")
			.addBody("{")
				.addBody("\topts.converters = {")
					.addBody("\t\t'text json': function(jsonString)")
					.addBody("\t\t{")
						.addBody("\t\t\treturn decodeDataToObject(jsonString, resultObject);")
					.addBody("\t\t}")
				.addBody("\t};")
			.addBody("}")
			.addBody("return $.ajax(path, opts);");
	}


	private void addPostRequestMethod()
	{
		addMethod("postRequest")
			.addParameter(new JavascriptParameter("path"))
			.addParameter(new JavascriptParameter("headerParams"))
			.addParameter(new JavascriptParameter("pathParams"))
			.addParameter(new JavascriptParameter("data"))
			.addParameter(new JavascriptParameter("contentType"))
			.addParameter(new JavascriptParameter("resultType"))
			.addParameter(new JavascriptParameter("resultObject"))
			.addBody("path = replacePathParamsInPath(path, pathParams);")
			.addBody("var opts = {contentType: contentType, method: 'POST'};")
			.addBody("if(headerParams)")
			.addBody("{")
			.addBody("\topts.headers = headerParams;")
			.addBody("}")
			.addBody("if(data)")
			.addBody("{")
			.addBody("\topts.data = data.toJson(false);")
			.addBody("}")
			.addBody("if(resultType == 'application/json' && resultObject)")
			.addBody("{")
			.addBody("\topts.converters = {")
			.addBody("\t\t'text json': function(jsonString)")
			.addBody("\t\t{")
			.addBody("\t\t\treturn decodeDataToObject(jsonString, resultObject);")
			.addBody("\t\t}")
			.addBody("\t};")
			.addBody("}")
			.addBody("return $.ajax(path, opts);");
	}


	private void addPutRequestMethod()
	{
		addMethod("putRequest")
			.addParameter(new JavascriptParameter("path"))
			.addParameter(new JavascriptParameter("headerParams"))
			.addParameter(new JavascriptParameter("pathParams"))
			.addParameter(new JavascriptParameter("data"))
			.addParameter(new JavascriptParameter("contentType"))
			.addParameter(new JavascriptParameter("resultType"))
			.addParameter(new JavascriptParameter("resultObject"))
			.addBody("path = replacePathParamsInPath(path, pathParams);")
			.addBody("var opts = {contentType: contentType, method: 'PUT'};")
			.addBody("if(headerParams)")
			.addBody("{")
			.addBody("\topts.headers = headerParams;")
			.addBody("}")
			.addBody("if(data)")
			.addBody("{")
			.addBody("\topts.data = data.toJson(false);")
			.addBody("}")
			.addBody("if(resultType == 'application/json' && resultObject)")
			.addBody("{")
			.addBody("\topts.converters = {")
			.addBody("\t\t'text json': function(jsonString)")
			.addBody("\t\t{")
			.addBody("\t\t\treturn decodeDataToObject(jsonString, resultObject);")
			.addBody("\t\t}")
			.addBody("\t};")
			.addBody("}")
			.addBody("return $.ajax(path, opts);");
	}


	private void addDeleteRequestMethod()
	{
		addMethod("deleteRequest")
			.addParameter(new JavascriptParameter("path"))
			.addParameter(new JavascriptParameter("headerParams"))
			.addParameter(new JavascriptParameter("pathParams"))
			.addParameter(new JavascriptParameter("contentType"))
			.addParameter(new JavascriptParameter("resultType"))
			.addParameter(new JavascriptParameter("resultObject"))
			.addBody("path = replacePathParamsInPath(path, pathParams);")
			.addBody("var opts = {contentType: contentType, method: 'DELETE'};")
			.addBody("if(headerParams) {")
			.addBody("\topts.headers = headerParams;")
			.addBody("}")
			.addBody("if(resultType == 'application/json' && resultObject)")
			.addBody("{")
			.addBody("\topts.converters = {")
			.addBody("\t\t'text json': function(jsonString)")
			.addBody("\t\t{")
			.addBody("\t\t\treturn decodeDataToObject(jsonString, resultObject);")
			.addBody("\t\t}")
			.addBody("\t};")
			.addBody("}")
			.addBody("return $.ajax(path, opts);");
	}

}
