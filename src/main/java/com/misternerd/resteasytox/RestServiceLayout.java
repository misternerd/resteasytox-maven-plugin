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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import com.misternerd.resteasytox.base.AbstractDto;
import com.misternerd.resteasytox.base.MethodParameter;
import com.misternerd.resteasytox.base.ServiceClass;
import com.misternerd.resteasytox.base.ServiceMethod;
import com.misternerd.resteasytox.base.ServiceMethod.RequestMethod;

/**
 * This class reads in all the relevant data via reflections and provides
 * the collected data to the output implementation.
 */
public class RestServiceLayout
{

	private final Log logger;

	private final String javaPackageName;

	private final JaxWsAnnotations jaxWsAnnotations;

	private final List<Class<?>> serviceClassList;

	private final Set<Class<?>> baseClasses = new HashSet<>();

	private final Set<Class<?>> requestClasses = new HashSet<>();

	private final Set<Class<?>> responseClasses = new HashSet<>();

	private final Set<Class<?>> dtoClasses = new HashSet<>();

	private final List<ServiceClass> serviceClasses = new ArrayList<>();

	public final Map<Class<?>, AbstractDto> abstractDtos = new HashMap<>();


	public RestServiceLayout(Log log, String javaPackageName, JaxWsAnnotations annotations, List<Class<?>> serviceClassList)
	{
		this.logger = log;
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

				RequestMethod requestMethod = RequestMethod.GET;

				if (method.getAnnotation(jaxWsAnnotations.post) != null)
				{
					requestMethod = RequestMethod.POST;
				}

				Class<?> returnType = getServiceMethodReturnType(method);
				String path = getValueForJaxWsAnnotation(method.getAnnotation(jaxWsAnnotations.path));
				String requestContentType = getFirstValueForJaxWsAnnotationArray(method.getAnnotation(jaxWsAnnotations.consumes));
				String responseContentType = getFirstValueForJaxWsAnnotationArray(method.getAnnotation(jaxWsAnnotations.produces));
				Set<MethodParameter> headerParams = new HashSet<>();
				Set<MethodParameter> pathParams = new HashSet<>();
				Set<MethodParameter> bodyParams = new HashSet<>();

				readParamsFromMethod(method, headerParams, pathParams, bodyParams);

				MethodParameter bodyParam = (!bodyParams.isEmpty()) ? bodyParams.iterator().next() : null;

				methods.add(new ServiceMethod(method.getName(), path, requestMethod, requestContentType,
						responseContentType, headerParams, pathParams, bodyParam, returnType));
			}

			if (!methods.isEmpty())
			{
				serviceClasses.add(new ServiceClass(cls.getSimpleName(), getValueForJaxWsAnnotation(cls.getAnnotation(jaxWsAnnotations.path)), methods));
			}
		}
	}


	private void readParamsFromMethod(Method method, Set<MethodParameter> headerParams, Set<MethodParameter> pathParams, Set<MethodParameter> bodyParams)
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
		if(type != null
				&& !type.isPrimitive()
				&& type.getPackage() != null
				&& type.getPackage().getName().startsWith(javaPackageName))
		{
			return true;
		}
		else
		{
//			logger.debug("This is not a DTO: " + type.getSimpleName());
			return false;
		}
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
					continue fieldLoop;
				}

				if(Map.class.isAssignableFrom(type) && types.length == 2)
				{
					addDtoTypeIfApplicable((Class<?>) types[0]);
					addDtoTypeIfApplicable((Class<?>) types[1]);
					continue fieldLoop;
				}
			}
			catch(ClassCastException e)
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


	public void printLayout()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("Generated REST layout is as follows:\nBASE CLASSES\n============");
		baseClasses.stream().forEach( cls -> sb.append("\n\t").append(cls.getSimpleName()));

		sb.append("\n\nSERVICE CLASSES\n===============");
		serviceClasses.stream().forEach( cls -> sb.append("\n\t").append(cls.name));

		sb.append("\n\nREQUEST CLASSES\n===============");
		requestClasses.stream().forEach( cls -> sb.append("\n\t").append(cls.getSimpleName()));

		sb.append("\n\nRESPONSE CLASSES\n================");
		responseClasses.stream().forEach( cls -> sb.append("\n\t").append(cls.getSimpleName()));

		sb.append("\n\nDTO CLASSES\n===========");
		dtoClasses.stream().forEach( cls -> sb.append("\n\t").append(cls.getSimpleName()));

		logger.info(sb.append("\n\n").toString());
	}
}
