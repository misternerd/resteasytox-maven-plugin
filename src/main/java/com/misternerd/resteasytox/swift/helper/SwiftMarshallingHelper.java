package com.misternerd.resteasytox.swift.helper;

import java.io.IOException;
import java.nio.file.Path;

import com.misternerd.resteasytox.swift.objects.SwiftFile;
import com.misternerd.resteasytox.swift.objects.SwiftMethod;
import com.misternerd.resteasytox.swift.objects.SwiftProtocol;
import com.misternerd.resteasytox.swift.objects.SwiftType;

public class SwiftMarshallingHelper
{

	static final String MARSHALLING_METHOD = "toJson";


	static public void generateMarshallingHelper(Path outputPath) throws IOException
	{
		String name = "MarshallingHelper";
		Path filePath = FileHelper.getOrCreateFilePath(outputPath, "helper", name, FileHelper.FILE_EXTENSION_SWIFT);

		SwiftProtocol marshallingProtocol = new SwiftProtocol("Marshalling");
		SwiftMethod marshallingMethod = new SwiftMethod(MARSHALLING_METHOD);
		marshallingMethod.setReturnType(SwiftType.ANYOBJECT);
		marshallingMethod.setIsDefinition(true);
		marshallingProtocol.addMethod(marshallingMethod);

		SwiftFile swiftFile = new SwiftFile(filePath, name);
		swiftFile.addProtocol(marshallingProtocol);

		swiftFile.writeToFile();
	}

}
