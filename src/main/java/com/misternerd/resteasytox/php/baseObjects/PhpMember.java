package com.misternerd.resteasytox.php.baseObjects;


public class PhpMember extends AbstractBaseObject
{

	public final PhpVisibility visibility;

	public final boolean isStatic;

	public final PhpType type;

	public final String name;

	private String value;


	public PhpMember(PhpVisibility visibility, boolean isStatic, PhpType type, String name, String value)
	{
		this.visibility = visibility;
		this.isStatic = isStatic;
		this.type = type;
		this.name = name;
		this.value = (value != null) ? "'" + value + "'" : null;
	}


	@Override
	public void build(StringBuilder sb, int indentSize)
	{
		String indent = "\n" + getIndent(indentSize);

		sb.append("\n").append(indent)
			.append("/**")
			.append(indent).append(" * @var ");

		if(type != null && type.addToTypeComment)
		{
			if(type.namespace != null)
			{
				sb.append(type.namespace.toAbsoluteNamespace(true)).append("\\");
			}

			sb.append(type.name);

			if(type.suffix != null)
			{
				sb.append(type.suffix);
			}

			if(type.nullable)
			{
				sb.append("|null");
			}
		}
		else
		{
			sb.append("mixed");
		}

		sb.append(indent).append(" */\n").append(getIndent(indentSize));

		if (visibility != null)
		{
			sb.append(visibility.toString()).append(" ");
		}

		if(isStatic)
		{
			sb.append("static ");
		}

		sb.append("$").append(name);

		if (value != null)
		{
			sb.append(" = ").append(value);
		}

		sb.append(";");
	}

}
