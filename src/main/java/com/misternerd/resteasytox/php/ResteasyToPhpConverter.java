package com.misternerd.resteasytox.php;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import com.misternerd.resteasytox.AbstractResteasyConverter;
import com.misternerd.resteasytox.RestServiceLayout;
import com.misternerd.resteasytox.base.MethodParameter;
import com.misternerd.resteasytox.base.ServiceClass;
import com.misternerd.resteasytox.base.ServiceMethod;
import com.misternerd.resteasytox.base.ServiceMethod.RequestMethod;
import com.misternerd.resteasytox.php.baseObjects.PhpBasicType;
import com.misternerd.resteasytox.php.baseObjects.PhpClass;
import com.misternerd.resteasytox.php.baseObjects.PhpMethod;
import com.misternerd.resteasytox.php.baseObjects.PhpNamespace;
import com.misternerd.resteasytox.php.baseObjects.PhpParameter;
import com.misternerd.resteasytox.php.baseObjects.PhpType;
import com.misternerd.resteasytox.php.baseObjects.PhpVisibility;
import com.misternerd.resteasytox.php.helperObjects.DateHelperObject;
import com.misternerd.resteasytox.php.helperObjects.MetaDataGenerator;
import com.misternerd.resteasytox.php.helperObjects.RestClientHelperObject;



public class ResteasyToPhpConverter extends AbstractResteasyConverter
{

	private final Log logger;

	private final Path sourcePath;

	private final PhpNamespace baseNamespace;

	private final PhpTypeLib typeLib;

	private final List<PhpClass> serviceClasses = new ArrayList<>();


	public ResteasyToPhpConverter(Log logger, Path outputPath, String javaPackageName, RestServiceLayout layout, PhpConverterConfig config)
	{
		super(outputPath, javaPackageName, layout);
		this.logger = logger;
		this.sourcePath = new File(outputPath.toFile(), "src").toPath();
		this.baseNamespace = new PhpNamespace(config.baseNamespace);
		this.typeLib = new PhpTypeLib(baseNamespace, javaPackageName, layout);
	}


	@Override
	public void convert() throws Exception
	{
		setupFolderLayout();
		generateBaseObjects();
		generateHelperObjects();
		generateRequestObjects();
		generateResponseObjects();
		generateDtos();
		generateServiceClasses();
		generateRestClient();
	}


	private void setupFolderLayout() throws IOException
	{
		if(!Files.isDirectory(outputPath))
		{
			Files.createDirectories(outputPath);
		}

		if(Files.isDirectory(sourcePath))
		{
			FileUtils.deleteDirectory(sourcePath.toFile());
		}

		Files.createDirectories(sourcePath);

		new MetaDataGenerator(outputPath, baseNamespace).createFiles();
	}


	private void generateBaseObjects() throws Exception
	{
		for(Class<?> cls : layout.getBaseClasses())
		{
			PhpClass phpClass = new PhpClass(sourcePath, baseNamespace, cls.getSimpleName(), null);

			writePublicClassConstants(cls, phpClass);
			writePrivateAndProtectedFields(cls, phpClass, PhpVisibility.PROTECTED);
			writeEmptyConstructor(phpClass);
			writePublicGettersAndSetters(cls, phpClass);

			phpClass.addMethod(PhpVisibility.PUBLIC, false, "toJson", null, null)
				.addBody("return json_encode($this->getObjectAsArray(get_object_vars($this)));");

			phpClass.addMethod(PhpVisibility.PROTECTED, false, "getObjectAsArray", null, null)
				.addParameter(new PhpParameter(PhpBasicType.MIXED, "vars"))
				.addBody("foreach($vars as $key => $var)")
				.addBody("{")
					.addBody("\tif(is_object($var))")
					.addBody("\t{")
						.addBody("\t\tif(method_exists($var, 'getEnumValue'))")
						.addBody("\t\t{")
							.addBody("\t\t\t$vars[$key] = $var->getEnumValue();")
						.addBody("\t\t}")
						.addBody("\t\telse")
						.addBody("\t\t{")
							.addBody("\t\t\t$vars[$key] = $this->getObjectAsArray(get_object_vars($var));")
						.addBody("\t\t}")
					.addBody("\t}")
				.addBody("}")
				.addBody("return $vars;");

			phpClass.writeToFile();
		}
	}


	private void generateHelperObjects() throws Exception
	{
		new DateHelperObject(sourcePath, baseNamespace).write();
	}


	private void generateRequestObjects() throws Exception
	{

		for (Class<?> cls : layout.getRequestClasses())
		{
			PhpClass phpClass = new PhpClass(sourcePath, getNamespaceForClass(cls), cls.getSimpleName(), new PhpType(baseNamespace, "AbstractRequest", null, true, true));

			writePublicClassConstants(cls, phpClass);
			writePrivateAndProtectedFields(cls, phpClass, PhpVisibility.PROTECTED);
			writeEmptyConstructor(phpClass);
			writeFullFeaturedConstructor(cls, phpClass, true);
			writePublicGettersAndSetters(cls, phpClass);

			phpClass.writeToFile();
		}
	}


	private void generateResponseObjects() throws Exception
	{
		for (Class<?> cls : layout.getResponseClasses())
		{
			PhpClass phpClass = new PhpClass(sourcePath, getNamespaceForClass(cls), cls.getSimpleName(), new PhpType(baseNamespace, "GenericResponse", null, true, true));

			writePublicClassConstants(cls, phpClass);
			writePrivateAndProtectedFields(cls, phpClass, PhpVisibility.PROTECTED);
			writeEmptyConstructor(phpClass);
			writeFullFeaturedConstructor(cls, phpClass, true);
			writePublicGettersAndSetters(cls, phpClass);

			phpClass.writeToFile();
		}
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void generateDtos() throws Exception
	{
		PhpNamespace namespace = new PhpNamespace(baseNamespace, "dto");

		for (Class<?> cls : layout.getDtoClasses())
		{
			Class<?> superclass = cls.getSuperclass();
			PhpType superType = null;

			if(superclass != null && layout.getDtoClasses().contains(superclass))
			{
				superType = new PhpType(namespace, superclass.getSimpleName(), null, true, true);
			}

			PhpClass phpClass = new PhpClass(sourcePath, namespace, cls.getSimpleName(), superType);

			List<Field> enumConstants = getEnumConstants(cls);

			if(!enumConstants.isEmpty())
			{
				Class<? extends Enum> enumClass = (Class<? extends Enum>) cls;

				for(Field field : enumConstants)
				{
					phpClass.addConstant(field.getName(), Enum.valueOf(enumClass, field.getName()).toString());
				}

				phpClass.addMember(PhpVisibility.PRIVATE, PhpBasicType.STRING, "enumValue", null);

				phpClass
					.addMethod(PhpVisibility.PUBLIC, false, "__construct", null, null)
					.addParameter(new PhpParameter(PhpBasicType.STRING, "_enumValue"))
					.addBody("$this->enumValue = $_enumValue;");

				phpClass.addMethod(PhpVisibility.PUBLIC, true, "create", null, null)
					.addParameter(new PhpParameter(PhpBasicType.STRING, "enumValue"))
					.addBody(String.format("$instance = new %s();", cls.getSimpleName()))
					.addBody("$instance->enumValue = $enumValue;")
					.addBody("return $instance;");

				phpClass.addMethod(PhpVisibility.PUBLIC, false, "getEnumValue", null, "return $this->enumValue;");


				phpClass.addMethod(PhpVisibility.PUBLIC, false, "toJson", null, "return json_encode($this->value);");
			}
			else
			{
				writePublicClassConstants(cls, phpClass);
				writePrivateAndProtectedFields(cls, phpClass, PhpVisibility.PUBLIC);
				writeEmptyConstructor(phpClass);
				writeFullFeaturedConstructor(cls, phpClass, false);
				writePublicGettersAndSetters(cls, phpClass);
			}

			phpClass.writeToFile();
		}
	}


	private PhpNamespace getNamespaceForClass(Class<? extends Object> cls)
	{
		String addedNamespace = cls.getPackage().getName().replace(javaPackageName + ".", "").replace(".", "\\");

		if(addedNamespace.contains("."))
		{
			logger.error("Tried to create invalid namespace: " + addedNamespace);
			throw new IllegalArgumentException(addedNamespace);
		}

		return new PhpNamespace(baseNamespace, addedNamespace);
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writePublicClassConstants(Class<? extends Object> cls, PhpClass phpClass) throws Exception
	{
		List<Field> constants = getPublicClassConstants(cls);

		for (Field field : constants)
		{
			try
			{
				phpClass.addConstant(field.getName(), field.getInt(null));
			}
			catch(Throwable tr)
			{
				phpClass.addConstant(field.getName(), field.get(null).toString());
			}
		}

		constants = getEnumConstants(cls);

		if(!constants.isEmpty())
		{
			Class<? extends Enum> enumClass = (Class<? extends Enum>) cls;

			for(Field field : constants)
			{
				phpClass.addConstant(field.getName(), Enum.valueOf(enumClass, field.getName()).toString());
			}
		}
	}


	private void writePrivateAndProtectedFields(Class<? extends Object> cls, PhpClass phpClass, PhpVisibility visibility)
	{
		List<Field> fields = getPrivateAndProtectedMemberVariables(cls, false);

		for (Field field : fields)
		{
			phpClass.addMember(visibility, typeLib.getPhpType(field), field.getName(), null);
		}
	}


	private void writeEmptyConstructor(PhpClass phpClass)
	{
		phpClass.addMethod(PhpVisibility.PUBLIC, false, "__construct", null, null);
	}


	private void writeFullFeaturedConstructor(Class<? extends Object> cls, PhpClass phpClass, boolean withSuperclass)
	{
		PhpMethod phpMethod = phpClass.addMethod(PhpVisibility.PUBLIC, true, "create", null, "$instance = new self();");

		if(withSuperclass)
		{
			for (Field field : getPrivateAndProtectedMemberVariables(cls.getSuperclass(), false))
			{
				String name = field.getName();
				phpMethod.addParameter(new PhpParameter(typeLib.getPhpType(field), name));
				phpMethod.addBody(String.format("$instance->%s = $%s;", name, name));
			}
		}

		for (Field field : getPrivateAndProtectedMemberVariables(cls, false))
		{
			String name = field.getName();
			phpMethod.addParameter(new PhpParameter(typeLib.getPhpType(field.getType()), name));
			phpMethod.addBody(String.format("$instance->%s = $%s;", name, name));
		}

		phpMethod.addBody("return $instance;");
	}


	private void writePublicGettersAndSetters(Class<? extends Object> cls, PhpClass phpClass)
	{
		List<Field> fields = getPrivateAndProtectedMemberVariables(cls, false);

		for(Field field : fields)
		{
			String name = field.getName();
			phpClass.addGetter(name);
			phpClass.addSetter(name);
		}
	}


	private void generateServiceClasses() throws IOException
	{
		PhpNamespace namespace = new PhpNamespace(baseNamespace, "service");

		for (ServiceClass serviceClass : layout.getServiceClasses())
		{
			PhpClass phpClass = new PhpClass(sourcePath, namespace, serviceClass.name, new PhpType(baseNamespace, "RestClient", null, true, true));
			writeServiceClassHeader(phpClass, serviceClass.name, serviceClass.path);

			for(ServiceMethod method : serviceClass.methods)
			{
				writeServiceMethod(phpClass, method);
			}

			phpClass.writeToFile();
			serviceClasses.add(phpClass);
		}
	}


	private void writeServiceClassHeader(PhpClass phpClass, String className, String servicePath)
	{
		PhpType loggerType = new PhpType(new PhpNamespace("Psr\\Log"), "LoggerInterface", null, true, true);
		phpClass.addTypeImport(loggerType);
		phpClass.addTypeImport(new PhpType(baseNamespace, "RestClient", null, true, true));
		phpClass.addConstant("PATH", servicePath);
		phpClass.addMember(PhpVisibility.PRIVATE, loggerType, "logger", null);
		phpClass
			.addMethod(PhpVisibility.PUBLIC, false, "__construct", null, "$this->logger = $logger;")
			.addParameter(new PhpParameter(loggerType, "logger"));
	}


	private void writeServiceMethod(PhpClass phpClass, ServiceMethod method)
	{
		Set<PhpParameter> headerParams = convertMethodParmsToPhpParams(method.headerParams);
		Set<PhpParameter> pathParams = convertMethodParmsToPhpParams(method.pathParams);

		Set<PhpParameter> allParams = new LinkedHashSet<>();
		allParams.addAll(headerParams);
		allParams.addAll(pathParams);

		if(method.bodyParam != null)
		{
			allParams.add(convertMethodParamToPhpParam(method.bodyParam));
		}

		PhpMethod phpMethod = phpClass.addMethod(PhpVisibility.PUBLIC, false, method.name, allParams, null);
		phpMethod.addBody(createLogLineForMethodAndParams(method.name, allParams));
		String returnType = method.returnType.getSimpleName();

		if(layout.getResponseClasses().contains(method.returnType))
		{
			phpClass.addTypeImport(typeLib.getPhpType(method.returnType));
		}

		phpMethod.addBody("$path = self::PATH . \"" + method.path + "\";");
		writeRestCallToBody(method, phpMethod, pathParams, headerParams);
		phpMethod.addBody("return $this->mapJsonToObject($response->body, new " + returnType + "());");

		if(method.returnType != null)
		{
			phpMethod.setReturnType(typeLib.getPhpType(method.returnType));
		}
	}


	private Set<PhpParameter> convertMethodParmsToPhpParams(Set<MethodParameter> params)
	{
		return params.stream().map(this::convertMethodParamToPhpParam).collect(Collectors.toSet());
	}


	private PhpParameter convertMethodParamToPhpParam(MethodParameter methodParam)
	{
		return new PhpParameter(typeLib.getPhpType(methodParam.type), methodParam.name);
	}


	private void writeRestCallToBody(ServiceMethod method, PhpMethod phpMethod, Set<PhpParameter> pathParams, Set<PhpParameter> headerParams)
	{
		phpMethod.addBody("$pathParams = [");

		for(PhpParameter param : pathParams)
		{
			phpMethod.addBody(String.format("\t'%s' => $%s,", param.originalName, param.originalName));
		}

		phpMethod.addBody("];");

		phpMethod.addBody("$headerParams = [");

		for(PhpParameter param : headerParams)
		{
			phpMethod.addBody(String.format("\t'%s' => $%s,", param.originalName, param.originalName));
		}

		phpMethod.addBody("];");

		if(method.httpMethod == RequestMethod.GET)
		{
			phpMethod.addBody(String.format("$response = $this->createGetRequest($path, $pathParams, $headerParams, '%s', '%s')->send();",
					method.requestContentType, method.responseContentType));
		}
		else
		{
			if(method.bodyParam != null && layout.getRequestClasses().contains(method.bodyParam.type))
			{
				phpMethod.addBody(String.format("$response = $this->createPostRequest($path, $pathParams, $headerParams, '%s', '%s', $%s)->send();",
						method.requestContentType, method.responseContentType, StringUtils.uncapitalize(method.bodyParam.name)));
			}
			else
			{
				phpMethod.addBody(String.format("$response = $this->createPostRequest($path, $pathParams, $headerParams, '%s', '%s', null)->send();",
						method.requestContentType, method.responseContentType));
			}
		}
	}


	private String createLogLineForMethodAndParams(String methodName, Set<PhpParameter> params)
	{
		StringBuilder log = new StringBuilder();

		log.append("$this->logger->debug('" + methodName + "(");

		log.append(params.stream()
				.map(param -> String.format("%s={%s}", param.originalName, param.adaptedName))
				.collect(Collectors.joining(", ")));

		log.append(")', [");

		log.append(params.stream()
				.map(param -> String.format("'%s' => $%s", param.adaptedName, param.originalName))
				.collect(Collectors.joining(", ")));

		log.append("]);");

		return log.toString();
	}


	private void generateRestClient() throws IOException
	{
		RestClientHelperObject restClient = new RestClientHelperObject(sourcePath, baseNamespace, serviceClasses);
		restClient.write();
	}

}
