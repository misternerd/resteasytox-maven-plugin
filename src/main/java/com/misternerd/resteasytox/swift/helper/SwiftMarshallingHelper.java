package com.misternerd.resteasytox.swift.helper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.misternerd.resteasytox.swift.objects.SwiftConstructorMethod;
import com.misternerd.resteasytox.swift.objects.SwiftExtension;
import com.misternerd.resteasytox.swift.objects.SwiftFile;
import com.misternerd.resteasytox.swift.objects.SwiftMethod;
import com.misternerd.resteasytox.swift.objects.SwiftProperty;
import com.misternerd.resteasytox.swift.objects.SwiftProtocol;
import com.misternerd.resteasytox.swift.objects.SwiftType;

public class SwiftMarshallingHelper
{

	public static final String MARSHALLING_PROTOCOL = "Marshalling";
	public static final String UNMARSHALLING_PROTOCOL = "Unmarshalling";

	private static final String MARSHALLING_METHOD = "toJson";


	static public void generateMarshallingHelper(Path outputPath) throws IOException
	{
		final String name = "MarshallingHelper";
		Path filePath = FileHelper.getOrCreateFilePath(outputPath, "helper", name, FileHelper.FILE_EXTENSION_SWIFT);
		SwiftFile swiftFile = new SwiftFile(filePath, name);

		swiftFile.addImport("Foundation");

		swiftFile.addProtocol(createMarshallingProtocol());
		swiftFile.addProtocol(createUnmarshallingProtocol());
		
		swiftFile.addExtension(createMarshallingParameterExtension());
		swiftFile.addExtensions(createBasicExtensions());
		
		swiftFile.addMethod(createUnmarshallingArrayMethod());

		swiftFile.writeToFile();
	}

	
	static private SwiftMethod createUnmarshallingArrayMethod() {

		SwiftMethod method = new SwiftMethod("arrayFromJson<T: Unmarshalling>");
		method.setReturnType("[T]?");
		SwiftProperty parameter = new SwiftProperty(false, false, new SwiftType(SwiftType.ANYOBJECT), "json", true, null);
		method.addParameter(parameter);
		
		method.addBody("guard let json = json as? [AnyObject] else {");
		method.addBody("\treturn nil");
		method.addBody("}");
		method.addBody("return json.flatMap{T(json: $0)}");
		
		return method;
	}

	static private SwiftProtocol createMarshallingProtocol()
	{
		SwiftProtocol marshallingProtocol = new SwiftProtocol(MARSHALLING_PROTOCOL);
		SwiftMethod marshallingMethod = createMarshallingMethod();
		marshallingMethod.setIsDefinition(true);
		marshallingProtocol.addMethod(marshallingMethod);

		return marshallingProtocol;
	}


	static private SwiftProtocol createUnmarshallingProtocol()
	{
		SwiftProtocol unmarshallingProtocol = new SwiftProtocol(UNMARSHALLING_PROTOCOL);
		SwiftMethod unmarshallingMethod = createUnmarshallingMethod();
		unmarshallingMethod.setIsDefinition(true);
		unmarshallingProtocol.addMethod(unmarshallingMethod);

		return unmarshallingProtocol;
	}
	
	static private SwiftExtension createMarshallingParameterExtension() {
		SwiftExtension extension = new SwiftExtension(MARSHALLING_PROTOCOL, null);
		
		SwiftMethod method = new SwiftMethod("parameter");
		method.setReturnType("[String: AnyObject]?");
		method.addBody("return self.toJson() as? [String: AnyObject]");
		extension.addMethod(method);
		
		return extension;
	}


	static public SwiftMethod createUnmarshallingMethod()
	{
		SwiftConstructorMethod unmarshallingMethod = new SwiftConstructorMethod(null, null, false);
		unmarshallingMethod.setOptional(true);
		SwiftProperty parameter = new SwiftProperty(false, false, new SwiftType(SwiftType.ANYOBJECT), "json", true, null);
		unmarshallingMethod.addParameter(parameter);
		return unmarshallingMethod;
	}


	static public SwiftMethod createMarshallingMethod()
	{
		SwiftMethod marshallingMethod = new SwiftMethod(MARSHALLING_METHOD);
		marshallingMethod.setReturnType(SwiftType.ANYOBJECT);
		return marshallingMethod;
	}


	static private List<SwiftExtension> createBasicExtensions()
	{
		List<SwiftExtension> extensions = new ArrayList<>();

		// Unmarshalling
		extensions.add(getUnmarshallingExtension(SwiftType.BOOL));
		extensions.add(getUnmarshallingExtension(SwiftType.STRING));
		extensions.add(getUnmarshallingExtension(SwiftType.INT));
		extensions.add(getUnmarshallingExtension(SwiftType.FLOAT));
		extensions.add(getUnmarshallingExtension(SwiftType.DOUBLE));
		extensions.add(getUnmarshallingNSDataExtension());
		extensions.add(getUnmarshallingNSDateExtension());

		// Marshalling
		extensions.add(getMarshallingExtension(SwiftType.BOOL));
		extensions.add(getMarshallingExtension(SwiftType.STRING));
		extensions.add(getMarshallingExtension(SwiftType.INT));
		extensions.add(getMarshallingExtension(SwiftType.FLOAT));
		extensions.add(getMarshallingExtension(SwiftType.DOUBLE));
		extensions.add(getMarshallingNSDataExtension());
		extensions.add(getMarshallingNSDateExtension());
		
		// Unmarshalling Array
		extensions.add(getUnmarshallingFromArrayExtension(SwiftType.NSDATA));
		extensions.add(getUnmarshallingFromArrayExtension(SwiftType.NSDATE));

		return extensions;
	}


	static private SwiftExtension getMarshallingExtension(String type)
	{

		List<String> protocol = new ArrayList<>();
		protocol.add(MARSHALLING_PROTOCOL);
		SwiftExtension extension = new SwiftExtension(type, protocol);

		SwiftMethod marshallingMethod = createMarshallingMethod();

		marshallingMethod.addBody("return self");

		extension.addMethod(marshallingMethod);

		return extension;
	}


	static private SwiftExtension getUnmarshallingExtension(String type)
	{
		List<String> protocol = new ArrayList<>();
		protocol.add(UNMARSHALLING_PROTOCOL);
		SwiftExtension extension = new SwiftExtension(type, protocol);

		SwiftMethod unmarshallingMethod = createUnmarshallingMethod();

		unmarshallingMethod.addBody("guard let json = json as? %s else {", type);
		unmarshallingMethod.addBody("\treturn nil");
		unmarshallingMethod.addBody("}");
		unmarshallingMethod.addBody("self.init(json)");

		extension.addMethod(unmarshallingMethod);

		return extension;
	}
	

	static private SwiftExtension getMarshallingNSDataExtension()
	{
		List<String> protocol = new ArrayList<>();
		protocol.add(MARSHALLING_PROTOCOL);
		SwiftExtension extension = new SwiftExtension(SwiftType.NSDATA, protocol);

		SwiftMethod marshallingMethod = createMarshallingMethod();

		marshallingMethod.addBody("return self.base64EncodedStringWithOptions(.Encoding64CharacterLineLength)");

		extension.addMethod(marshallingMethod);

		return extension;
	}


	static private SwiftExtension getMarshallingNSDateExtension()
	{
		List<String> protocol = new ArrayList<>();
		protocol.add(MARSHALLING_PROTOCOL);
		SwiftExtension extension = new SwiftExtension(SwiftType.NSDATE, protocol);

		SwiftMethod marshallingMethod = createMarshallingMethod();

		marshallingMethod.addBody("return Int(self.timeIntervalSince1970 * 1000) // unix to java");

		extension.addMethod(marshallingMethod);

		return extension;
	}


	static private SwiftExtension getUnmarshallingNSDataExtension()
	{
		SwiftExtension extension = new SwiftExtension(SwiftType.NSDATA, null);

		SwiftMethod unmarshallingMethod = createUnmarshallingMethod();
		unmarshallingMethod.setConvenience(true);

		unmarshallingMethod.addBody("guard let json = json as? String else {");
		unmarshallingMethod.addBody("\treturn nil");
		unmarshallingMethod.addBody("}");
		unmarshallingMethod.addBody("if let data = NSData(base64EncodedString:json, options: .IgnoreUnknownCharacters) {");
		unmarshallingMethod.addBody("\tself.init(data: data)");
		unmarshallingMethod.addBody("} else {");
		unmarshallingMethod.addBody("\treturn nil");
		unmarshallingMethod.addBody("}");

		extension.addMethod(unmarshallingMethod);

		return extension;
	}


	static private SwiftExtension getUnmarshallingFromArrayExtension(String type)
	{
		SwiftExtension extension = new SwiftExtension(type, null);

		SwiftMethod method = new SwiftMethod("arrayFromJson");
		method.setStatic(true);
		method.setReturnType(String.format("[%s]?", type));
		
		SwiftProperty parameter = new SwiftProperty(false, false, new SwiftType(SwiftType.ANYOBJECT), "json", true, null);
		method.addParameter(parameter);
		
		method.addBody("guard let json = json as? [AnyObject] else {");
		method.addBody("\treturn nil");
		method.addBody("}");
		method.addBody("return json.flatMap{%s(json: $0)}", type);
		
		extension.addMethod(method);

		return extension;
	}


	static private SwiftExtension getUnmarshallingNSDateExtension()
	{
		SwiftExtension extension = new SwiftExtension(SwiftType.NSDATE, null);

		SwiftMethod unmarshallingMethod = createUnmarshallingMethod();
		unmarshallingMethod.setConvenience(true);

		unmarshallingMethod.addBody("guard let json = json as? NSTimeInterval else {");
		unmarshallingMethod.addBody("\treturn nil");
		unmarshallingMethod.addBody("}");
		unmarshallingMethod.addBody("\tself.init(timeIntervalSince1970: json / 1000) // java to unix");

		extension.addMethod(unmarshallingMethod);

		return extension;
	}
}
