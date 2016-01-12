package com.misternerd.resteasytox.swift.objects;

public class SwiftProperty extends Buildable implements ParameterBuildable
{
	private final boolean isStatic;

	private boolean isFinal;

	private SwiftType type;

	private String name;

	private boolean isOptional;

	private final String defaultValue;

	private final boolean supportObjC;


	public SwiftProperty(boolean isStatic, boolean isFinal, SwiftType type, String name, boolean isOptional, String defaultValue, boolean supportObjC)
	{
		super();
		this.isStatic = isStatic;
		this.isFinal = isFinal;
		this.type = type;
		this.name = name;
		this.isOptional = isOptional;
		this.defaultValue = defaultValue;
		this.supportObjC = supportObjC;
	}


	public String getName() {
		return getName(false);
	}

	public String getName(boolean supportObjC)
	{
		if (supportObjC) {
			return getObjCSaveName();
		} else {
			return name;
		}
	}

	public String getObjCSaveName() {
		switch (name) {
			case "description":
				return "description_";
			default:
				return name;
		}
	}

	public boolean isOptional()
	{
		return isOptional;
	}


	public String lineForConstructor()
	{
		return "self." + getName(supportObjC) + " = " + getName();
	}


	public String lineForUnmarshalling()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("let ").append(name);
		
		
		if (type.isArray()) {
			sb.append(": ");
			type.build(sb);
			sb.append("? = ");
			
			// We need special handling for data and date, because they don't implement Unmarshalling protocol
			if (type.getName().equals(SwiftType.NSDATA) ||
					type.getName().equals(SwiftType.NSDATE)) {
				sb.append(type.getName()).append(".arrayFromJson(json[\"").append(getName()).append("\"])");
			} else {
				sb.append("arrayFromJson(json[\"").append(getName()).append("\"])");
			}
		} else {
			sb.append(" = ").append(type.getName()).append("(json: json[\"").append(getName()).append("\"])");
		}
		
		
		return sb.toString();
	}


	public String lineForMarshalling()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("jsonParameter[\"").append(getName()).append("\"] = ").append(getName(supportObjC));
		
		if (isOptional) {
			sb.append("?");
		}
		
		if (type.isArray()) {
			sb.append(".map{$0");
		}
		
		sb.append(".toJson()");
		
		if (type.isArray()) {
			sb.append("}");
		}
		
		if (isOptional) {
			sb.append(" ?? NSNull()");
		}
		
		return sb.toString();
	}
	
	public void buildParameter(StringBuilder sb) {
		sb.append(getName()).append(": ").append(getName());
	}


	@Override
	public void buildParameterDeclaration(StringBuilder sb)
	{
		if (isFinal)
		{
			sb.append("let ");
		}

		sb.append(getName()).append(": ");

		type.build(sb);

		if (isOptional)
		{
			sb.append("?");
		}

		if (defaultValue != null)
		{
			sb.append(" = ").append(defaultValue);
		}
	}


	@Override
	public void build(StringBuilder sb)
	{
		if (isStatic)
		{
			sb.append("static ");
		}

		if (isFinal)
		{
			sb.append("let ");
		}
		else
		{
			sb.append("var ");
		}

		sb.append(getName(supportObjC)).append(": ");

		type.build(sb);

		if (isOptional)
		{
			sb.append("?");
		}

		if (defaultValue != null)
		{
			sb.append(" = ").append(defaultValue);
		}
	}
}
