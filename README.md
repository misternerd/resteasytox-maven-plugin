JAX-RS Code Generator Maven Plugin
==================================

This is a Maven plugin that automatically generates a REST client for other languages, based on a JAX-RS compliant
JavaEE webapp.
The basic idea is this: When you develop a server in JavaEE and would like to access it from different clients, you
usually need to write the REST layer for each client from scratch. E.g. if you have a Java server servicing an
Android, iOS and AngularJS app, you would at least have to create the REST layer for iOS and Javascript
(for Android, you can reuse the DTOs together with Spring Resttemplate).
This is where this plugin comes into play. After compilation of your JAX-RS project, it uses reflection to scan through
the project. It identifies all `@Path` annotations and from there on gathers information about DTO classes involved.
After gathering all information, it generates a REST client for the target platform.


Target Languages
----------------

Currently, this plugin supports PHP and Javascript as a target language. Support for Swift is somewhat planned :)


Usage
-----

* The plugin is currently in no repository yet, so you need to checkout this GIT and build the plugin with `mvn clean install`
	(installing it to your local `.m2` directory)
* Include the plugin in your JAX-RS compatible webapp as a build plugin. In the `pom.xml`, add something like this:
```xml
<build>
	...
	<plugins>
		<plugin>
    	<groupId>com.misternerd</groupId>
			<artifactId>resteasytox-maven-plugin</artifactId>
			<version>1-SNAPSHOT</version>
			<executions>
				<execution>
					<phase>package</phase>
					<goals>
						<goal>convert</goal>
					</goals>
				</execution>
			</executions>
			<configuration>
				<javaPackageName>org.example.package.of.your.rest.project</javaPackageName>
				<printLayout>true</printLayout>
				<serviceClassnames>
					<class>org.example.package.of.your.rest.project.SomeServiceClassWithPathAnnotation1</class>
					<class>org.example.package.of.your.rest.project.SomeServiceClassWithPathAnnotation2</class>
				</serviceClassnames>
				<convertToPhp>true</convertToPhp>
				<phpOutputPath>/tmp/phpOutput</phpOutputPath>
				<phpBaseNamespace>lessons2go\rest</phpBaseNamespace>
				<convertToJavascript>true</convertToJavascript>
				<javascriptOutputPath>/tmp/javascriptOutput</javascriptOutputPath>
			</configuration>
    </plugin>
	</plugins>
</build>
```	
* You need to specify the target languages (e.g. `convertToPhp`, `convertToJavascript`) including an output path. Also, currently you will have to provided ALL your service classes names. These are the classes that you annotate with `@Path`. I hope to work around this requirement in the future.


What Gets Converted?
--------------------

* For each class annotated with `@Path`, a service class is created in the target language;
* For each method in a service class annotated with `@Path`, an function is generated in the service class;
* All of the service methods request and response objects are scanned through recursively, adding the to the target language as well;
* `@HeaderParam`s and `@PathParam`s are added to the method.
		
