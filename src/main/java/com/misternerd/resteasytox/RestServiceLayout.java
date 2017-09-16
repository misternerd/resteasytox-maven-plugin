package com.misternerd.resteasytox;

//import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import com.misternerd.resteasytox.base.AbstractDto;
import com.misternerd.resteasytox.base.MethodParameter;
import com.misternerd.resteasytox.base.MethodParameterComparator;
import com.misternerd.resteasytox.base.ServiceClass;
import com.misternerd.resteasytox.base.ServiceMethod;
import com.misternerd.resteasytox.base.ServiceMethod.RequestMethod;
import com.misternerd.resteasytox.base.ServiceMethodComparator;

import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.maven.project.MavenProject;

/**
 * This class reads in all the relevant data via reflections and provides
 * the collected data to the output implementation.
 */
public class RestServiceLayout
{

	private final Log logger;

	public final MavenProject mavenProject;

	public final Metadata metadata;

	private final String javaPackageName;

	private final JaxWsAnnotations jaxWsAnnotations;

	private final List<Class<?>> serviceClassList;

	private final Set<Class<?>> baseClasses = new HashSet<>();

	private final Set<Class<?>> requestClasses = new HashSet<>();

	private final Set<Class<?>> responseClasses = new HashSet<>();

	private final Set<Class<?>> dtoClasses = new HashSet<>();

	private final List<ServiceClass> serviceClasses = new ArrayList<>();

	public final Map<Class<?>, AbstractDto> abstractDtos = new HashMap<>();



	public RestServiceLayout(Log log, MavenProject mavenProject, Metadata metadata, String javaPackageName,
		JaxWsAnnotations annotations, List<Class<?>> serviceClassList)
	{
		this.logger = log;
		this.mavenProject = mavenProject;
		this.metadata = metadata;
		this.javaPackageName = javaPackageName;
		this.jaxWsAnnotations = annotations;
		this.serviceClassList = serviceClassList;
	}


	public Set<Class<?>> getBaseClasses()
	{
		return baseClasses;
	}


	public Set<Class<?>> getRequestClasses()
	{
		return requestClasses;
	}


	public Set<Class<?>> getResponseClasses()
	{
		return responseClasses;
	}


	public Set<Class<?>> getDtoClasses()
	{
		return dtoClasses;
	}


	public List<ServiceClass> getServiceClasses()
	{
		return serviceClasses;
	}
	

	public JaxWsAnnotations getAnnotations()
	{
		return jaxWsAnnotations;
	}


	public void readLayoutFromReflection(boolean printLayout)
			throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException
	{
		readServiceClasses();

		if(printLayout)
		{
			printLayout();
		}
	}


	private void readServiceClasses() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		for (Class<?> cls : serviceClassList)
		{
			List<ServiceMethod> methods = new ArrayList<>();

			for (Method method : cls.getMethods())
			{
				if (method.getAnnotation(jaxWsAnnotations.path) == null)
				{
					continue;
				}

				RequestMethod requestMethod = getRequestMethodFromAnnotations(method);

				Class<?> returnType = getServiceMethodReturnType(method);
				String path = getValueForJaxWsAnnotation(method.getAnnotation(jaxWsAnnotations.path));
				String requestContentType = getFirstValueForJaxWsAnnotationArray(method.getAnnotation(jaxWsAnnotations.consumes));
				String responseContentType = getFirstValueForJaxWsAnnotationArray(method.getAnnotation(jaxWsAnnotations.produces));
				SortedSet<MethodParameter> headerParams = new TreeSet<>(new MethodParameterComparator());
				SortedSet<MethodParameter> pathParams = new TreeSet<>(new MethodParameterComparator());
				SortedSet<MethodParameter> bodyParams = new TreeSet<>(new MethodParameterComparator());
				SortedSet<MethodParameter> queryParams = new TreeSet<>(new MethodParameterComparator());

				readParamsFromMethod(method, headerParams, pathParams, bodyParams, queryParams);

				MethodParameter bodyParam = (!bodyParams.isEmpty()) ? bodyParams.iterator().next() : null;

				methods.add(new ServiceMethod(method.getName(), path, requestMethod, requestContentType,
						responseContentType, headerParams, pathParams, bodyParam, queryParams, returnType));
			}

			Collections.sort(methods, new ServiceMethodComparator());

			if (!methods.isEmpty())
			{
				serviceClasses.add(new ServiceClass(cls.getSimpleName(), getValueForJaxWsAnnotation(cls.getAnnotation(jaxWsAnnotations.path)), methods));
			}
		}
	}


	private RequestMethod getRequestMethodFromAnnotations(Method method)
	{
		if (method.getAnnotation(jaxWsAnnotations.post) != null)
		{
			return RequestMethod.POST;
		}
		else if(method.getAnnotation(jaxWsAnnotations.put) != null)
		{
			return RequestMethod.PUT;
		}
		else if(method.getAnnotation(jaxWsAnnotations.delete) != null)
		{
			return RequestMethod.DELETE;
		}

		return RequestMethod.GET;
	}


	private void readParamsFromMethod(Method method, Set<MethodParameter> headerParams, Set<MethodParameter> pathParams, Set<MethodParameter> bodyParams, Set<MethodParameter> queryParams)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Class<?>[] parameterTypes = method.getParameterTypes();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();

		parameterLoop:
		for(int i = 0; i < parameterTypes.length; i++)
		{
			Annotation[] annotations = parameterAnnotations[i];

			for(Annotation annotation : annotations)
			{
				if(jaxWsAnnotations.pathParam.equals(annotation.annotationType()))
				{
					pathParams.add(new MethodParameter(getValueForJaxWsAnnotation(annotation), parameterTypes[i]));
					continue parameterLoop;
				}
				else if(jaxWsAnnotations.headerParam.equals(annotation.annotationType()))
				{
					headerParams.add(new MethodParameter(getValueForJaxWsAnnotation(annotation), parameterTypes[i]));
					continue parameterLoop;
				}
				else if(jaxWsAnnotations.context.equals(annotation.annotationType()))
				{
					logger.debug("Context parameter will be discarded for method=" + method.getName());
					continue parameterLoop;
				}
				else if(jaxWsAnnotations.queryParam.equals(annotation.annotationType()))
				{
					queryParams.add(new MethodParameter(getValueForJaxWsAnnotation(annotation), parameterTypes[i]));
					logger.debug("Query parameter will be discarded for method=" + method.getName());
					continue parameterLoop;
				}
			}

			String paramName = StringUtils.uncapitalize(parameterTypes[i].getSimpleName());

			if(!bodyParams.isEmpty())
			{
				throw new IllegalStateException("Cannot have more than one body param for paramName=" + paramName);
			}

			bodyParams.add(new MethodParameter(paramName, getServiceMethodParamType(parameterTypes[i])));
		}
	}


	private Class<?> getServiceMethodParamType(Class<?> type)
	{
		if (isCustomType(type) && !requestClasses.contains(type))
		{
			requestClasses.add(type);
			searchDtoTypeInMembers(type);
			extractBaseclassFromType(type);
		}

		return type;
	}


	private Class<?> getServiceMethodReturnType(Method method)
	{
		Class<?> type = method.getReturnType();

		if (isCustomType(type) && !responseClasses.contains(type))
		{
			responseClasses.add(type);
			searchDtoTypeInMembers(type);
			extractBaseclassFromType(type);
		}

		return type;
	}


	private boolean isCustomType(Class<?> type)
	{
		return type != null
			&& !type.isPrimitive()
			&& type.getPackage() != null
			&& type.getPackage().getName().startsWith(javaPackageName);
	}


	private void searchDtoTypeInMembers(Class<?> cls)
	{
		fieldLoop:
		for(Field field : cls.getDeclaredFields())
		{
			Class<?> type = field.getType();

			try
			{
				ParameterizedType parameterized = (ParameterizedType) field.getGenericType();
				Type[] types = parameterized.getActualTypeArguments();

				if (List.class.isAssignableFrom(type) && types.length == 1)
				{
					addDtoTypeIfApplicable((Class<?>) types[0]);
					continue;
				}

				if(Map.class.isAssignableFrom(type) && types.length == 2)
				{
					addDtoTypeIfApplicable((Class<?>) types[0]);
					addDtoTypeIfApplicable((Class<?>) types[1]);
					continue;
				}
			}
			catch(ClassCastException ignored)
			{
			}

			addDtoTypeIfApplicable(type);
		}
	}


	private void addDtoTypeIfApplicable(Class<?> type)
	{
		if(isCustomType(type) && !dtoClasses.contains(type))
		{
			dtoClasses.add(type);
			extractBaseclassFromType(type);
			searchDtoTypeInMembers(type);

			if(Modifier.isAbstract(type.getModifiers()))
			{
				extractSubclassesForAbstractDto(type);
			}
		}
	}


	private void extractSubclassesForAbstractDto(Class<?> abstractClass)
	{
		try
		{
			logger.debug("Found abstract class: " + abstractClass + ", will search for jackson subtypes");

			Annotation typeInfoAnnotation = abstractClass.getAnnotation(jaxWsAnnotations.jsonTypeInfo);
			Annotation subtypesAnnotation = abstractClass.getAnnotation(jaxWsAnnotations.jsonSubTypes);

			if(subtypesAnnotation == null || typeInfoAnnotation == null)
			{
				logger.warn("Either subtypes or type info are null, will use abstract implementation for class=" + abstractClass);
				return;
			}

			Map<String, Class<?>> implementingClasses = new HashMap<>();
			Method valueMethod = subtypesAnnotation.annotationType().getMethod("value");
			Object[] result = (Object[]) valueMethod.invoke(subtypesAnnotation);

			for(Object typeAnnotation : result)
			{
				valueMethod = typeAnnotation.getClass().getMethod("value");
				Class<?> subClass = (Class<?>) valueMethod.invoke(typeAnnotation);
				valueMethod = typeAnnotation.getClass().getMethod("name");
				String name = (String) valueMethod.invoke(typeAnnotation);

				implementingClasses.put(name, subClass);
				addDtoTypeIfApplicable(subClass);
			}

			valueMethod = typeInfoAnnotation.annotationType().getMethod("property");
			String typeInfo = (String) valueMethod.invoke(typeInfoAnnotation);

			abstractDtos.put(abstractClass, new AbstractDto(abstractClass, implementingClasses, typeInfo));
		}
		catch(Throwable tr)
		{
			logger.warn("Failed to extract subclasses from abstract class:" + tr);
		}
	}


	private void extractBaseclassFromType(Class<?> type)
	{
		Class<?> baseclass = type.getSuperclass();

		if(baseclass != null && !baseClasses.contains(type) &&!Object.class.equals(type.getSuperclass())
				&& baseclass.getPackage().getName().startsWith(javaPackageName))
		{
			baseClasses.add(baseclass);
		}
	}


	private String getValueForJaxWsAnnotation(Annotation annotation)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Method valueMethod = annotation.annotationType().getMethod("value");
		return valueMethod.invoke(annotation).toString();
	}


	private String getFirstValueForJaxWsAnnotationArray(Annotation annotation)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Method valueMethod = annotation.annotationType().getMethod("value");
		String[] result = (String[]) valueMethod.invoke(annotation);
		return result[0];
	}


	private void printLayout()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("Generated REST layout is as follows:\nBASE CLASSES\n============");
		baseClasses.forEach( cls -> sb.append("\n\t").append(cls.getSimpleName()));

		sb.append("\n\nSERVICE CLASSES\n===============");
		serviceClasses.forEach( cls -> sb.append("\n\t").append(cls.name));

		sb.append("\n\nREQUEST CLASSES\n===============");
		requestClasses.forEach( cls -> sb.append("\n\t").append(cls.getSimpleName()));

		sb.append("\n\nRESPONSE CLASSES\n================");
		responseClasses.forEach( cls -> sb.append("\n\t").append(cls.getSimpleName()));

		sb.append("\n\nDTO CLASSES\n===========");
		dtoClasses.forEach( cls -> sb.append("\n\t").append(cls.getSimpleName()));

		logger.info(sb.append("\n\n").toString());
	}
}
