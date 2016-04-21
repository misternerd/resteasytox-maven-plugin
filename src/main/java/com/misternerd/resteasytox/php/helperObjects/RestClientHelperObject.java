package com.misternerd.resteasytox.php.helperObjects;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.misternerd.resteasytox.php.baseObjects.PhpBasicType;
import com.misternerd.resteasytox.php.baseObjects.PhpClass;
import com.misternerd.resteasytox.php.baseObjects.PhpMethod;
import com.misternerd.resteasytox.php.baseObjects.PhpNamespace;
import com.misternerd.resteasytox.php.baseObjects.PhpParameter;
import com.misternerd.resteasytox.php.baseObjects.PhpType;
import com.misternerd.resteasytox.php.baseObjects.PhpVisibility;

public class RestClientHelperObject extends AbstractHelperObject
{

	private final PhpType loggerType = new PhpType(new PhpNamespace("Psr\\Log"), "LoggerInterface",null, true, true, false);


	public RestClientHelperObject(Path outputPath, PhpNamespace namespace, List<PhpClass> serviceClasses)
	{
		super(outputPath, namespace, "RestClient", null);

		phpClass.addMember(PhpVisibility.PRIVATE, true, PhpBasicType.STRING, "baseUrl", null);
		phpClass.addMember(PhpVisibility.PRIVATE, true, loggerType, "logger", null);
		phpClass.addMember(PhpVisibility.PROTECTED, true, new PhpType(PhpNamespace.ROOT, "JsonMapper", null, true, true, false), "mapper", null);

		PhpMethod initMethod = createInitMethod();
		addServicesToClass(serviceClasses, initMethod);
		addGetMethod();
		addPostMethod();
		createGenerateUrlMethod();
		createJsonToObjectMethod();
	}


	private PhpMethod createInitMethod()
	{
		return phpClass.addMethod(PhpVisibility.PUBLIC, true, "init", null, null)
			.addParameter(new PhpParameter(PhpBasicType.STRING, "baseUrl"))
			.addParameter(new PhpParameter(loggerType, "logger"))

			.addBody("self::$baseUrl = $baseUrl;")
			.addBody("self::$logger = $logger;")
			.addBody("self::$mapper = new \\JsonMapper();")
			.addBody("self::$mapper->bExceptionOnUndefinedProperty = true;");
	}


	private void addServicesToClass(List<PhpClass> serviceClasses, PhpMethod initMethod)
	{
		for(PhpClass serviceClass : serviceClasses)
		{
			String serviceName = StringUtils.uncapitalize(serviceClass.className);

			PhpType type = new PhpType(serviceClass.namespace, serviceClass.className, null, true, true, false);
			phpClass.addTypeImport(type);
			phpClass.addMember(PhpVisibility.PRIVATE, true, type, serviceName, null);
			initMethod.addBody(String.format("self::$%s = new %s(self::$logger);", serviceName, serviceClass.className));

			phpClass.addMethod(PhpVisibility.PUBLIC, true, serviceName, null,
					String.format("return self::$%s;", serviceName));
		}
	}


	private void addGetMethod()
	{
		PhpMethod method = phpClass.addMethod(PhpVisibility.PROTECTED, false, "createGetRequest", null, null);
		method.addParameter(new PhpParameter(PhpBasicType.STRING, "path"));
		method.addParameter(new PhpParameter(PhpBasicType.ARRAY, "pathParams"));
		method.addParameter(new PhpParameter(PhpBasicType.ARRAY, "headerParams"));
		method.addParameter(new PhpParameter(PhpBasicType.STRING, "requestType"));
		method.addParameter(new PhpParameter(PhpBasicType.STRING, "responseType"));

		method.addBody("$url = self::createUrlFromPathAndParams($path, $pathParams);");
		method.addBody("$request = \\Httpful\\Request::get($url)")
			.addBody("\t->contentType($requestType)")
			.addBody("\t->expects($responseType);");

		method.addBody("foreach($headerParams as $paramName => $paramValue)")
			.addBody("{")
				.addBody("\t$request->addHeader($paramName, $paramValue);")
			.addBody("}");

		method.addBody("return $request;");
	}


	private void addPostMethod()
	{
		phpClass.addMethod(PhpVisibility.PROTECTED, false, "createPostRequest", null, null)
			.addParameter(new PhpParameter(PhpBasicType.STRING, "path"))
			.addParameter(new PhpParameter(PhpBasicType.ARRAY, "pathParams"))
			.addParameter(new PhpParameter(PhpBasicType.ARRAY, "headerParams"))
			.addParameter(new PhpParameter(PhpBasicType.STRING, "requestType"))
			.addParameter(new PhpParameter(PhpBasicType.STRING, "responseType"))
			.addParameter(new PhpParameter(PhpBasicType.MIXED, "body", "null"))

			.addBody("$url = self::createUrlFromPathAndParams($path, $pathParams);")

			.addBody("if($body != null)")
			.addBody("{")
			.addBody("\t$body = $body->toJson();")
			.addBody("}")

			.addBody("$request = \\Httpful\\Request::post($url, $body, $requestType)")
			.addBody("\t->neverSerializePayload()")
			.addBody("\t->expects($responseType);")

			.addBody("foreach($headerParams as $paramName => $paramValue)")
			.addBody("{")
				.addBody("\t$request->addHeader($paramName, $paramValue);")
			.addBody("}")

			.addBody("return $request;");
	}


	private void createGenerateUrlMethod()
	{
		PhpMethod method = phpClass.addMethod(PhpVisibility.PRIVATE, false, "createUrlFromPathAndParams", null, null);
		method.addParameter(new PhpParameter(PhpBasicType.STRING, "path"));
		method.addParameter(new PhpParameter(PhpBasicType.ARRAY, "pathParams"));

		method.addBody("$url = self::$baseUrl . $path;");
		method.addBody("if(empty($pathParams))");
		method.addBody("{");
			method.addBody("\treturn $url;");
		method.addBody("}");
		method.addBody("foreach($pathParams as $name => $value)");
		method.addBody("{");
			method.addBody("\t$url = str_replace('{' .$name . '}', $value, $url);");
		method.addBody("}");
		method.addBody("return $url;");
	}


	private void createJsonToObjectMethod()
	{
		PhpMethod method = phpClass.addMethod(PhpVisibility.PROTECTED, false, "mapJsonToObject",
				null, "return self::$mapper->map($json, $object);");
		method.addParameter(new PhpParameter(PhpBasicType.MIXED, "json"));
		method.addParameter(new PhpParameter(PhpBasicType.MIXED, "object"));
	}
}
