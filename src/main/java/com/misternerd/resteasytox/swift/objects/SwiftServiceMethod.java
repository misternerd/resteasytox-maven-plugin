package com.misternerd.resteasytox.swift.objects;

import java.util.Set;

import com.misternerd.resteasytox.base.MethodParameter;
import com.misternerd.resteasytox.base.ServiceMethod;
import com.misternerd.resteasytox.swift.helper.SwiftTypeHelper;

public class SwiftServiceMethod extends SwiftMethod
{

	private final ServiceMethod serviceMethod;


	public SwiftServiceMethod(ServiceMethod serviceMethod)
	{
		super(serviceMethod.name);
		this.serviceMethod = serviceMethod;
		setStatic(true);
	}


	@Override
	public void build(StringBuilder sb, int indent)
	{
		// We need to generate parameter and body before the method is build.
		for (MethodParameter parameter : serviceMethod.pathParams)
		{
			SwiftProperty swiftParameter = new SwiftProperty(false, false, new SwiftType(SwiftType.STRING), parameter.name, false, null);
			addParameter(swiftParameter);
		}

		if (serviceMethod.returnType != null)
		{
			SwiftProperty response = new SwiftProperty(false, false, SwiftTypeHelper.getSwiftTypeFromClass(serviceMethod.returnType), "response", true, null);
			SwiftProperty error = new SwiftProperty(false, false, SwiftTypeHelper.getSwiftTypeFromString("NSError"), "error", true, null);
			
			SwiftMethod closure = new SwiftMethod("callback");
			closure.addParameter(response);
			closure.addParameter(error);
			addParameter(closure);
		}

		String path = replacePathParams(serviceMethod.path, serviceMethod.pathParams);
		String parameters = serviceMethod.bodyParam == null ? "nil" : "request.parameters()";
		addBody("manager");
		addBody("\t.request(.%s, baseUrl + \"%s\", parameters: %s, encoding: .JSON)", serviceMethod.httpMethod, path, parameters);
		addBody("\t.responseObject { (response: " + SwiftTypeHelper.getSwiftTypeFromClass(serviceMethod.returnType).getName() + "?, error) -> Void in ", serviceMethod.bodyParam);
		addBody("\t\tself.checkResponse(response)");
		addBody("\t\tcallback(response: response, error: error)");
		addBody("\t}");

		super.build(sb, indent);
	}


	private String replacePathParams(String path, Set<MethodParameter> pathParams)
	{
		String newPath = path;
		for (MethodParameter methodParameter : pathParams)
		{
			newPath = path.replace("{" + methodParameter.name + "}", "\\(" + methodParameter.name + ")");
		}
		return newPath;
	}

}