package org.oboparser.obo.annotations;

import java.lang.annotation.Retention;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Lacks {
	public String rel();
	public Class term();
	public boolean defining() default false;
}
