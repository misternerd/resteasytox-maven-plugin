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
		addReplaceQueryParamsMethod();
		addDecodeDataToObjectMethod();
		addDefaultRequestMethod();
		addGettersForServiceClasses(layout);
		addGetRequestMethod();
		addPostRequestMethod();
		addPutRequestMethod();
		addDeleteRequestMethod();
	}


	private void addReplacePathParamsMethod()
	{
		addPrivateMethod("replacePathParamsInPath", JavascriptBasicType.STRING)
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "path"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "pathParams"))
			.addBody("for(var paramName in pathParams)")
			.addBody("{")
				.addBody("\tpath = path.replace('{' + paramName + '}', pathParams[paramName]);")
			.addBody("}")
			.addBody("return restBaseUrl + (path).replace(/\\/\\//g, '/');");
	}

	private void addReplaceQueryParamsMethod()
	{
		addPrivateMethod("replaceQueryParamsInPath", JavascriptBasicType.STRING)
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "path"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "queryParams"))
			.addBody("for(var paramName in queryParams)")
			.addBody("{")
				.addBody("\tpath = path.replace('{' + paramName + '}', queryParams[paramName]);")
			.addBody("}")
			.addBody("return (path).replace(/\\/\\//g, '/');");
	}

	private void addDecodeDataToObjectMethod()
	{
		addPrivateMethod("decodeDataToObject", JavascriptBasicType.ANY)
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


	private void addDefaultRequestMethod()
	{
		addPrivateMethod("fetchRequest", JavascriptBasicType.ANY)
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "method"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "path"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "headerParams"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "pathParams"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "queryParams"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.OBJECT, "body"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "contentType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "resultType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.ANY, "resultObject"))
			.addBody("path = replacePathParamsInPath(path, pathParams);")
			.addBody("if (queryParams != null) { ")
				.addBody("path = replaceQueryParamsInPath(path, queryParams);", 1)
			.addBody("} ")
			.addBody("var headers = {};\n")
			.addBody("headers['Content-Type'] = contentType;")
			.addBody("if(headerParams != null) {")
				.addBodyWithIndent("for(var headerName in headerParams) {", 1)
					.addBodyWithIndent("headers[headerName] = headerParams[headerName];", 2)
				.addBodyWithIndent("}",1)
			.addBody("}")
			.addBody("var fetchParams = {")
				.addBodyWithIndent("method: method,", 1)
				.addBodyWithIndent("headers: headers,", 1)
				.addBodyWithIndent("mode: 'cors',", 1)
				.addBodyWithIndent("cache: 'default'", 1)
			.addBody("};")
			.addBody("if(body) {")
				.addBodyWithIndent("if(contentType == 'application/json') {", 1)
					.addBodyWithIndent("fetchParams.body = body.toJson(false);", 2)
				.addBodyWithIndent("}", 1)
				.addBodyWithIndent("else {", 1)
					.addBodyWithIndent("fetchParams.body = body;", 2)
				.addBodyWithIndent("}", 1)
			.addBody("}")
			.addBody("return new Promise(function(onFulfill, onReject) {")
				.addBodyWithIndent("fetch(path, fetchParams)", 1)
					.addBodyWithIndent(".then(function(response) {", 2)
						.addBodyWithIndent("if(!response.ok) {", 3)
							.addBodyWithIndent("onReject('Received invalid HTTP status');", 4)
							.addBodyWithIndent("return;", 4)
						.addBodyWithIndent("}", 3)
						.addBodyWithIndent("var receivedContentType = response.headers.get('content-type');", 3)
						.addBodyWithIndent("if(!receivedContentType || receivedContentType.indexOf(resultType) === -1) {", 3)
							.addBodyWithIndent("onReject('Received unexpected content type');", 4)
							.addBodyWithIndent("return;", 4)
						.addBodyWithIndent("}", 3)
						.addBodyWithIndent("if(receivedContentType == 'application/json') {", 3)
							.addBodyWithIndent("response.json().then(function(jsonData) {", 4)
								.addBodyWithIndent("if(resultObject != null) {", 5)
									.addBodyWithIndent("if(resultObject.hasOwnProperty('initFromJson')) {", 6)
										.addBodyWithIndent("onFulfill(resultObject.initFromJson(jsonData));", 7)
									.addBodyWithIndent("} else {", 6)
										.addBodyWithIndent("onFulfill(jsonData);", 7)
									.addBodyWithIndent("}", 6)
								.addBodyWithIndent("}", 5)
								.addBodyWithIndent("else {", 5)
									.addBodyWithIndent("onFulfill(response.json());", 6)
								.addBodyWithIndent("}", 5)
							.addBodyWithIndent("});", 4)
						.addBodyWithIndent("}", 3)
						.addBodyWithIndent("else if(receivedContentType.startsWith('text/')) {", 3)
							.addBodyWithIndent("response.text().then(function(textData) {", 4)
								.addBodyWithIndent("onFulfill(textData);", 5)
							.addBodyWithIndent("});", 4)
						.addBodyWithIndent("}", 3)
						.addBodyWithIndent("else {", 3)
							.addBodyWithIndent("response.blob().then(function(blobData) {", 4)
								.addBodyWithIndent("onFulfill(blobData);", 5)
							.addBodyWithIndent("});", 4)
						.addBodyWithIndent("}", 3)
					.addBodyWithIndent("})", 2)
				.addBodyWithIndent(".catch(function(error) {", 1)
					.addBodyWithIndent("onReject('Request yielded error=' + error);", 2)
				.addBodyWithIndent("});", 1)
			.addBody("});");
	}


	private void addGettersForServiceClasses(RestServiceLayout layout)
	{
		for(ServiceClass serviceClass : layout.getServiceClasses())
		{
			addPublicMethod(StringUtils.uncapitalize(serviceClass.name), new JavascriptType(serviceClass.name))
				.addBody("return %s;", StringUtils.uncapitalize(serviceClass.name));
		}
	}


	private void addGetRequestMethod()
	{
		addPublicMethod("getRequest", JavascriptBasicType.ANY)
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "path"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "headerParams"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "pathParams"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "queryParams"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "contentType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING,"resultType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.ANY, "resultObject"))
			.addBody("return fetchRequest('GET', path, headerParams, pathParams, queryParams, null, contentType, resultType, resultObject);");
	}


	private void addPostRequestMethod()
	{
		addPublicMethod("postRequest", JavascriptBasicType.ANY)
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "path"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "headerParams"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "pathParams"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.OBJECT, "body"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "contentType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "resultType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.ANY, "resultObject"))
			.addBody("return fetchRequest('POST', path, headerParams, pathParams, null, body, contentType, resultType, resultObject);");
	}


	private void addPutRequestMethod()
	{
		addPublicMethod("putRequest", JavascriptBasicType.ANY)
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "path"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "headerParams"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "pathParams"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.OBJECT, "body"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "contentType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "resultType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.ANY, "resultObject"))
			.addBody("return fetchRequest('PUT', path, headerParams, pathParams, null, body, contentType, resultType, resultObject);");
	}


	private void addDeleteRequestMethod()
	{
		addPublicMethod("deleteRequest", JavascriptBasicType.ANY)
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "path"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "headerParams"))
			.addParameter(new JavascriptParameter(JavascriptArrayType.STRING_ARRAY, "pathParams"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "contentType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.STRING, "resultType"))
			.addParameter(new JavascriptParameter(JavascriptBasicType.ANY, "resultObject"))
			.addBody("return fetchRequest('DELETE', path, headerParams, pathParams, null, null, contentType, resultType, resultObject);");
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
