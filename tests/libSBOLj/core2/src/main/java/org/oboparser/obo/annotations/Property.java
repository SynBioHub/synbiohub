package org.oboparser.obo.annotations;

import java.lang.annotation.Retention;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)

public @interface Property {
	public String id() default "id";
	public String name() default "name";
	public String def() default "def";
	public String[] chain() default {};
}

