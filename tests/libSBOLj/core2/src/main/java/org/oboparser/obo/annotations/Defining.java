package org.oboparser.obo.annotations;

import java.lang.annotation.Retention;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Defining {
	public Class value() default Object.class;
	public Class[] is_a() default {};
}
