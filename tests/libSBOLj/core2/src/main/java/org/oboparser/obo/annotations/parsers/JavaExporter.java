package org.oboparser.obo.annotations.parsers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.oboparser.obo.annotations.Relates;
import org.oboparser.obo.annotations.Term;

public class JavaExporter extends OBOAnnotationParser implements Exporter {
	
	@Override
	public String export(Class cls) {
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		JavaWriter java = new JavaWriter(pw);
		
		java.beginInterface(0, cls.getSimpleName(), cls.getInterfaces(), Term.class);
		
		java.field(Modifier.PUBLIC | Modifier.STATIC, String.class, "id", id(cls));
		java.field(Modifier.PUBLIC | Modifier.STATIC, String.class, "name", name(cls));
		java.field(Modifier.PUBLIC | Modifier.STATIC, String.class, "def", def(cls));
		
		for(Method method : findImmediateRelations(cls)) {
			java.methodDeclaration(method.getModifiers(), method.getReturnType(), method.getName(),
					null, null, null, new MethodRelates(method));
		}
		
		java.endInterface();
		
		//return String.format("%s\n%s", java.getImports(), writer.toString());
		return writer.toString();
	} 
	
	private class MethodRelates implements Relates {
		
		private Method method;
	
		public MethodRelates(Method m) { 
			method = m;
		}
		
		@Override
		public String value() {
			if(method.isAnnotationPresent(Relates.class)) { 
				return method.getAnnotation(Relates.class).value();
			} else { 
				return method.getName();
			}
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return Relates.class;
		} 
	}
}