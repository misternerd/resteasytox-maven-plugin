package com.misternerd.resteasytox;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.misternerd.resteasytox.javascript.ResteasyToJavascriptConverter;
import com.misternerd.resteasytox.php.PhpConverterConfig;
import com.misternerd.resteasytox.php.ResteasyToPhpConverter;
import com.misternerd.resteasytox.swift.ResteasyToSwiftConverter;

@Mojo(name = "convert", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class ResttoxMojo extends AbstractMojo
{

	@Parameter(property = "javaPackageName", required = true)
	private String javaPackageName;

	@Parameter(property = "printLayout", defaultValue = "false", required = false)
	private boolean printLayout;

	@Parameter(property = "serviceClassnames", required = true)
	private List<String> serviceClassnames;

	@Parameter(property = "additionalDtoClassnames", required = false)
	private List<String> additionalDtoClassnames = new ArrayList<>();

	@Parameter(property = "convertToPhp", defaultValue = "false")
	private boolean convertToPhp;

	@Parameter(property = "phpOutputPath", defaultValue = "/tmp/php")
	private String phpOutputPath;

	@Parameter(property = "phpBaseNamespace", defaultValue = "\\")
	private String phpBaseNamespace;

	@Parameter(property = "convertToJavascript", defaultValue = "false")
	private boolean convertToJavascript;

	@Parameter(property = "javascriptOutputPath", defaultValue = "/tmp/javascript")
	private String javascriptOutputPath;

	@Parameter(property = "convertToSwift", defaultValue = "false")
	private boolean convertToSwift;

	@Parameter(property = "swiftOutputPath", defaultValue = "/tmp/swift")
	private String swiftOutputPath;

	@Component
	private MavenProject project;

	private final Log logger;


	public ResttoxMojo()
	{
		super();
		this.logger = getLog();
	}


	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		try
		{
			if (!convertToPhp && !convertToJavascript && !convertToSwift)
			{
				logger.debug("No target language specified, no conversion started");
				return;
			}

			URLClassLoader classLoader = createClassloaderFromCompiledElements();
			JaxWsAnnotations annotations = new JaxWsAnnotations(classLoader);
			List<Class<?>> serviceClasses = loadServicesClasses(classLoader);

			RestServiceLayout serviceLayout = new RestServiceLayout(logger, javaPackageName, annotations, serviceClasses);
			loadAdditionalDtoClasses(classLoader, serviceLayout);
			serviceLayout.readLayoutFromReflection(printLayout);

			if (convertToPhp)
			{
				logger.debug("Converting REST API to PHP with target dir = " + phpOutputPath);
				Path outputPath = verifyOrCreatePath(phpOutputPath);
				PhpConverterConfig config = new PhpConverterConfig(phpBaseNamespace);
				ResteasyToPhpConverter converter = new ResteasyToPhpConverter(logger, outputPath, javaPackageName, serviceLayout, config);
				converter.convert();
			}

			if (convertToJavascript)
			{
				logger.debug("Converting REST API to Javascript with target dir = " + javascriptOutputPath);
				Path outputPath = verifyOrCreatePath(javascriptOutputPath);
				ResteasyToJavascriptConverter converter = new ResteasyToJavascriptConverter(outputPath, javaPackageName, serviceLayout);
				converter.convert();
			}

			if (convertToSwift)
			{
				logger.debug("Converting REST API to Swift with target dir = " + swiftOutputPath);
				Path outputPath = verifyOrCreatePath(swiftOutputPath);
				ResteasyToSwiftConverter converter = new ResteasyToSwiftConverter(outputPath, javaPackageName, serviceLayout);
				converter.convert();
			}
		}
		catch (MojoExecutionException | MojoFailureException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			logger.error("Conversion process failed:", e);
			throw new MojoFailureException("Rest-To-X conversion process failed");
		}
	}


	private URLClassLoader createClassloaderFromCompiledElements() throws MojoExecutionException
	{
		try
		{
			Set<URL> classpathUrls = new HashSet<>();

			for (String element : project.getCompileClasspathElements())
			{
				try
				{
					URL classpathElement = new File(element).toURI().toURL();
					classpathUrls.add(classpathElement);
				}
				catch (MalformedURLException e)
				{
					throw new MojoExecutionException(element + " is an invalid classpath element", e);
				}
			}

			return new URLClassLoader(classpathUrls.toArray(new URL[0]));

		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Dependency resolution failed", e);
		}
	}


	private List<Class<?>> loadServicesClasses(URLClassLoader classLoader) throws Exception
	{
		List<Class<?>> serviceClasses = new ArrayList<>(serviceClassnames.size());

		for (String serviceClassname : serviceClassnames)
		{
			Class<?> loadedClass = classLoader.loadClass(serviceClassname);
			logger.debug("Loading service class=" + serviceClassname);
			serviceClasses.add(loadedClass);
		}

		return serviceClasses;
	}


	private void loadAdditionalDtoClasses(URLClassLoader classLoader, RestServiceLayout serviceLayout) throws ClassNotFoundException
	{
		for (String clsName : additionalDtoClassnames)
		{
			serviceLayout.getDtoClasses().add(classLoader.loadClass(clsName));
		}
	}


	private Path verifyOrCreatePath(String pathName) throws IOException
	{
		Path path = Paths.get(pathName);

		if (!Files.isDirectory(path))
		{
			Files.createDirectories(path);
		}

		return path;
	}

}
