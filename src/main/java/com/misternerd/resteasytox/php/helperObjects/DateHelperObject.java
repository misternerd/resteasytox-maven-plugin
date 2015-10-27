package com.misternerd.resteasytox.php.helperObjects;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.misternerd.resteasytox.php.baseObjects.PhpBasicType;
import com.misternerd.resteasytox.php.baseObjects.PhpMethod;
import com.misternerd.resteasytox.php.baseObjects.PhpNamespace;
import com.misternerd.resteasytox.php.baseObjects.PhpParameter;
import com.misternerd.resteasytox.php.baseObjects.PhpType;
import com.misternerd.resteasytox.php.baseObjects.PhpVisibility;


public class DateHelperObject extends AbstractHelperObject
{

	public DateHelperObject(Path outputPath, PhpNamespace namespace)
	{
		super(outputPath, new PhpNamespace(namespace, "dto"), "Date", new PhpType(null, "DateTime", null, true, true));

		Set<PhpParameter> parameters = new HashSet<>();
		parameters.add(new PhpParameter(PhpBasicType.INT, "javaTimestamp"));

		PhpMethod method = phpClass.addMethod(PhpVisibility.PUBLIC, false, "__construct", parameters, null);
		method.addBody("parent::__construct();");
		method.addBody("$this->setTimestamp($javaTimestamp / 1000);");
	}

}
