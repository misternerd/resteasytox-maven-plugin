package com.misternerd.resteasytox;

import java.lang.annotation.Annotation;

public class JaxWsAnnotations
{

	public final Class<? extends Annotation> get;

	public final Class<? extends Annotation> post;

	public final Class<? extends Annotation> path;

	public final Class<? extends Annotation> consumes;

	public final Class<? extends Annotation> produces;

	public final Class<? extends Annotation> headerParam;

	public final Class<? extends Annotation> pathParam;

	public final Class<? extends Annotation> context;


	@SuppressWarnings("unchecked")
	public JaxWsAnnotations(ClassLoader classLoader) throws ClassNotFoundException
	{
		this.get = (Class<? extends Annotation>) classLoader.loadClass("javax.ws.rs.GET");
		this.post = (Class<? extends Annotation>) classLoader.loadClass("javax.ws.rs.POST");
		this.path = (Class<? extends Annotation>) classLoader.loadClass("javax.ws.rs.Path");
		this.consumes = (Class<? extends Annotation>) classLoader.loadClass("javax.ws.rs.Consumes");
		this.produces = (Class<? extends Annotation>) classLoader.loadClass("javax.ws.rs.Produces");
		this.headerParam = (Class<? extends Annotation>) classLoader.loadClass("javax.ws.rs.HeaderParam");
		this.pathParam = (Class<? extends Annotation>) classLoader.loadClass("javax.ws.rs.PathParam");
		this.context = (Class<? extends Annotation>) classLoader.loadClass("javax.ws.rs.core.Context");
	}

}
