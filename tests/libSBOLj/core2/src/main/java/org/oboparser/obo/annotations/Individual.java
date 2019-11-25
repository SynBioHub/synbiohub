package org.oboparser.obo.annotations;

import java.lang.annotation.Retention;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Individual {
	public Class value() default Object.class;
	public Class[] types() default {};
}
