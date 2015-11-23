package com.misternerd.resteasytox.swift.helper;

import java.io.IOException;
import java.nio.file.Path;

import com.misternerd.resteasytox.swift.objects.SwiftConstructorMethod;
import com.misternerd.resteasytox.swift.objects.SwiftFile;
import com.misternerd.resteasytox.swift.objects.SwiftMethod;
import com.misternerd.resteasytox.swift.objects.SwiftProperty;
import com.misternerd.resteasytox.swift.objects.SwiftProtocol;
import com.misternerd.resteasytox.swift.objects.SwiftType;

public class SwiftMarshallingHelper
{

	static final String MARSHALLING_METHOD = "toJson";
	static final String UNMARSHALLING_METHOD = "fromJson";
	static final String MARSHALLING_PROTOCOL = "Marshalling";
	static final String UNMARSHALLING_PROTOCOL = "Unmarshalling";


	static public void generateMarshallingHelper(Path outputPath) throws IOException
	{
		String name = "MarshallingHelper";
		Path filePath = FileHelper.getOrCreateFilePath(outputPath, "helper", name, FileHelper.FILE_EXTENSION_SWIFT);
		SwiftFile swiftFile = new SwiftFile(filePath, name);

		SwiftProtocol marshallingProtocol = new SwiftProtocol(MARSHALLING_PROTOCOL);
		SwiftMethod marshallingMethod = new SwiftMethod(MARSHALLING_METHOD);
		marshallingMethod.setReturnType(SwiftType.ANYOBJECT);
		marshallingMethod.setIsDefinition(true);
		marshallingProtocol.addMethod(marshallingMethod);
		swiftFile.addProtocol(marshallingProtocol);

		SwiftProtocol unmarshallingProtocol = new SwiftProtocol(UNMARSHALLING_PROTOCOL);
		SwiftConstructorMethod unmarshallingMethod = new SwiftConstructorMethod(null, null, false);
		unmarshallingMethod.setOptional(true);
		unmarshallingMethod.setIsDefinition(true);
		SwiftProperty parameter = new SwiftProperty(false, false, new SwiftType(SwiftType.ANYOBJECT), "json", true, null);
		unmarshallingMethod.addParameter(parameter);
		unmarshallingProtocol.addMethod(unmarshallingMethod);
		swiftFile.addProtocol(unmarshallingProtocol);

		swiftFile.writeToFile();
	}
}
