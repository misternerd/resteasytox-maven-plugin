package com.misternerd.resteasytox.javascript.helper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.misternerd.resteasytox.javascript.objects.types.JavascriptArrayType;
import com.misternerd.resteasytox.javascript.objects.types.JavascriptBasicType;
import com.misternerd.resteasytox.javascript.objects.types.JavascriptType;
import org.apache.commons.lang3.StringUtils;

import com.misternerd.resteasytox.RestServiceLayout;
import com.misternerd.resteasytox.base.ServiceClass;
import com.misternerd.resteasytox.javascript.objects.JavascriptClass;
import com.misternerd.resteasytox.javascript.objects.JavascriptParameter;

public class RestClient extends JavascriptClass
{

	public RestClient(Path outputPath, String namespace, RestServiceLayout layout)
	{
		super(Paths.get(outputPath + File.separator + "Client.js"), namespace, "Client");
		buildBody(layout);
	}


	private void buildBody(RestServiceLayout layout)
	{
		addConstructorParam(new JavascriptParameter(JavascriptBasicType.STRING, "restBaseUrl"));

		for(ServiceClass serviceClass : layout.getServiceClasses())
		{
			addPrivateMember(new JavascriptType(serviceClass.name), StringUtils.uncapitalize(serviceClass.name),
				String.format("new %s.%s(this)", namespace, serviceClass.name), false);
		}

		addReplacePathParamsMethod();
		addDecodeDataToObjectMethod();
		addGettersForServiceClasses(layout);
		addGetRequestMethod();
		addPostRequestMethod();
		addPutRequestMethod();
		addDeleteRequestMethod();
	}


	private void addReplacePathParamsMethod()
	{
		addPrivateMethod("replacePathParamsInPath")
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "path"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "pathParams"))
			.addBody("for(var paramName in pathParams)")
			.addBody("{")
				.addBody("\tpath = path.replace('{' + paramName + '}', pathParams[paramName]);")
			.addBody("}")
			.addBody("return restBaseUrl + path;");
	}


	private void addDecodeDataToObjectMethod()
	{
		addPrivateMethod("decodeDataToObject")
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "jsonString"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.ANY, "resultObject"))
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
	}


	private void addGettersForServiceClasses(RestServiceLayout layout)
	{
		for(ServiceClass serviceClass : layout.getServiceClasses())
		{
			addMethod(StringUtils.uncapitalize(serviceClass.name), new JavascriptType(serviceClass.name))
				.addBody("return %s;", StringUtils.uncapitalize(serviceClass.name));
		}
	}


	private void addGetRequestMethod()
	{
		addMethod("getRequest", JavascriptBasicType.ANY)
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "path"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "headerParams"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "pathParams"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "contentType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING,"resultType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.ANY, "resultObject"))
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
		addMethod("postRequest", JavascriptBasicType.ANY)
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "path"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "headerParams"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "pathParams"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.OBJECT, "data"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "contentType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "resultType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.ANY, "resultObject"))
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
		addMethod("putRequest", JavascriptBasicType.ANY)
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "path"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "headerParams"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "pathParams"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.OBJECT, "data"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "contentType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "resultType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.ANY, "resultObject"))
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
		addMethod("deleteRequest", JavascriptBasicType.ANY)
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "path"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "headerParams"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "pathParams"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "contentType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "resultType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.ANY, "resultObject"))
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


	public Path getJavascriptOutputFile()
	{
		return jsOutputFile;
	}


	public Path getTypingOutputFile()
	{
		return tsOutputFile;
	}

}
