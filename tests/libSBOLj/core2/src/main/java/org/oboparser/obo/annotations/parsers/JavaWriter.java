package org.oboparser.obo.annotations.parsers;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaWriter {

	private static String tabString = "\t";

	public static final Integer CLASS = 0;
	public static final Integer INTERFACE = 1;
	public static final Integer METHOD = 2;

	private Set<Class> usedClasses;

	private PrintWriter writer;
	private LinkedList<Integer> blockTypes;

	public JavaWriter(PrintWriter w) {
		writer = w;
		blockTypes = new LinkedList<Integer>();
		usedClasses = new TreeSet<Class>(new ClassNameComparator());
	}

	private String annotationValueString(Object value) {
		if(value instanceof Class) {
			Class cls = (Class)value;
			useClasses(cls);
			return String.format("%s.class", cls.getSimpleName());
		} else if (value instanceof String) {
			String str = (String)value;
			Pattern p = Pattern.compile("(?:^|[^\\\\])(\")");
			Matcher m = p.matcher(str);
			while(m.matches()) {
				int start = m.start(1), end = m.end(1);
				str = str.substring(0, start) + "\\\"" + str.substring(end, str.length());
				m = p.matcher(str);
			}
			str = str.replaceAll("\\\\:", ":");
			return String.format("\"%s\"", str);
		} else {
			return String.valueOf(value);
		}
	}

	//	/**
	//	 * Annotations are represented in two ways: either as a "vanilla" Class object, or as an object
	//	 * that implements the annotation class and contains all the annotation values.  These need to be rendered
	//	 * in one of the two ways.
	//	 *
	//	 * @param ann
	//	 * @return rendered annotation
	//	 */
	public String renderAnnotation(Object ann) {
		if(ann instanceof Class) {
			Class annClass = (Class)ann;
			useClasses(annClass);
			return String.format("@" + annClass.getSimpleName());

		} else if (ann instanceof Annotation) {
			Annotation annotation = (Annotation)ann;
			Class annClass = annotation.annotationType();
			useClasses(annClass);

			StringBuilder sb = new StringBuilder();
			String base = String.format("@" + annClass.getSimpleName());

			String defaultValue = null;

			for(Method m : annClass.getDeclaredMethods()) {
				Class type = m.getReturnType();
				String mName = m.getName();
				int mod = m.getModifiers();
				if(Modifier.isPublic(mod) && Modifier.isAbstract(mod) && m.getParameterTypes().length == 0) {
					if(sb.length() > 0) { sb.append(", "); }
					try {
						String value = annotationValueString(m.invoke(ann));

						if(mName.equals("value")) {
							defaultValue = value;
						} else {
							sb.append(String.format("%s=%s", mName, value));
						}

						useClasses(type);

					} catch (IllegalAccessException e) {
						throw new IllegalArgumentException(String.valueOf(ann));
					} catch (InvocationTargetException e) {
						throw new IllegalArgumentException(String.valueOf(ann));
					}
				} else {
					System.err.println(String.format("Method %s has modifiers %d and param length %d",
							mName, mod, m.getParameterTypes().length));
				}
			}

			if(defaultValue != null) {
				if(sb.length() == 0) {
					sb.append(defaultValue);
				} else {
					sb.append(String.format(", value=%s", defaultValue));
				}
			}

			if(sb.length() > 0) {
				return String.format("%s(%s)", base, sb.toString());
			} else {
				return base;
			}
		} else {
			throw new IllegalArgumentException(String.format("Unsupported annotation class: %s", ann.getClass().getCanonicalName()));
		}
	}

	private static class ClassNameComparator implements Comparator<Class> {
		@Override
		public int compare(Class c1, Class c2) {
			return c1.getCanonicalName().compareTo(c2.getCanonicalName());
		}
	}

	public void useClasses(Class... cs) {
		if(cs != null) {
			for(Class c : cs) {
				if(c != null && !c.getCanonicalName().equals(String.format("java.lang.%s", c.getSimpleName()))) {
					usedClasses.add(c);
				}
			}
		}
	}

	public String getImports() {
		StringBuilder sb = new StringBuilder();
		for(Class c : usedClasses) {
			sb.append(String.format("import %s;\n", c.getCanonicalName()));
		}
		return sb.toString();
	}

	private boolean printIfPublic(int mod) {
		boolean present = Modifier.isPublic(mod);
		if(present) { writer.print("public "); }
		return present;
	}

	private boolean printIfPrivate(int mod) {
		boolean present = Modifier.isPrivate(mod);
		if(present) { writer.print("private "); }
		return present;
	}

	private boolean printIfProtected(int mod) {
		boolean present = Modifier.isProtected(mod);
		if(present) { writer.print("protected "); }
		return present;
	}

	private boolean printIfAccess(int mod) {
		return printIfPublic(mod) || printIfPrivate(mod) || printIfProtected(mod);
	}

	public String indent() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < blockTypes.size(); i++) {
			sb.append(tabString);
		}
		return sb.toString();
	}

	private boolean printIfStatic(int mod) {
		boolean present = Modifier.isStatic(mod);
		if(present) { writer.print("static "); }
		return present;
	}

	private void doIndent() {
		writer.print(indent());
	}

	public void beginInterface(int modifiers, String interfaceName, Class[] interfaces, Object... annotations) {

		writer.println();

		if(annotations != null && annotations.length > 0) {
			for(Object annotation : annotations) {
				writer.println(String.format("%s%s", indent(), renderAnnotation(annotation)));
			}
		}

		doIndent();
		printIfAccess(modifiers);
		printIfStatic(modifiers);
		writer.print("interface " + interfaceName + " ");

		if(interfaces != null && interfaces.length != 0) {
			writer.print("extends ");
			for(int i = 0; i < interfaces.length; i++) {
				if(i > 0) { writer.print(", "); }
				writer.print(interfaces[i].getSimpleName());
			}
			writer.print(" ");
		}

		writer.println("{");
		blockTypes.addFirst(INTERFACE);

		useClasses(interfaces);
	}

	public void endInterface() {
		endWithType(INTERFACE);
	}

	public void beginClass(int modifiers, String className, Class superClass, Class[] interfaces, Object... annotations) {

		writer.println();

		if(annotations != null && annotations.length > 0) {
			for(Object annotation : annotations) {
				writer.println(String.format("%s%s", indent(), renderAnnotation(annotation)));
			}
		}

		doIndent();
		printIfAccess(modifiers);
		printIfStatic(modifiers);
		writer.print("class " + className + " ");

		if(superClass != null) {
			writer.print(" extends " + superClass.getSimpleName() + " ");
		}

		if(interfaces != null && interfaces.length != 0) {
			writer.print("implements ");
			for(int i = 0; i < interfaces.length; i++) {
				if(i > 0) { writer.print(", "); }
				writer.print(interfaces[i].getSimpleName());
			}
			writer.print(" ");
		}

		writer.println("{");
		blockTypes.addFirst(CLASS);

		useClasses(superClass);
		useClasses(interfaces);
	}

	public void endClass() {
		endWithType(CLASS);
	}

	private static boolean isSubclass(Class c1, Class c2) {
		return c2.isAssignableFrom(c1);
	}

	public void field(int modifiers, Class type, String name, String init, Class... annotations) {

		if(annotations != null && annotations.length > 0) {
			writer.println();
			for(Class annotation : annotations) {
				writer.println(String.format("%s@%s", indent(), annotation.getSimpleName()));
			}
			useClasses(annotations);
		}

		doIndent();
		printIfAccess(modifiers);
		printIfStatic(modifiers);

		writer.print(type.getSimpleName() + " ");
		writer.print(name);

		if(init != null) {
			if(isSubclass(type, String.class)) {
				writer.print(String.format(" = \"%s\";", init.replace("\"", "\\\"")));
			} else {
				writer.print(String.format(" = %s;", init));
			}
		}

		writer.println();

		useClasses(type);
	}

	private void printMethodLine(int modifiers, Class returnType, String methodName, Class[] argTypes, String[] argNames, Class[] throwsList, Object... annotations) {

		writer.println();

		for(Object annotation : annotations) {
			writer.println(String.format("%s%s", indent(), renderAnnotation(annotation)));
		}

		doIndent();
		printIfAccess(modifiers);
		printIfStatic(modifiers);
		writer.print(returnType.getSimpleName() + " " + methodName);
		writer.print("(");

		if(argTypes != null && argTypes.length > 0) {
			if(argNames == null || argNames.length != argTypes.length) {
				throw new IllegalArgumentException();
			}

			for(int i = 0; i < argNames.length; i++) {
				if(i > 0) { writer.print(", "); }
				writer.print(String.format("%s %s", argTypes[i].getSimpleName(), argNames[i]));
			}
		}

		writer.print(")");

		if(throwsList != null && throwsList.length > 0) {
			writer.print(" throws ");
			for(int i = 0; i < throwsList.length; i++) {
				if(i > 0) { writer.print(", "); }
				writer.print(throwsList[i].getSimpleName());
			}
		}

		useClasses(returnType);
		useClasses(argTypes);
		useClasses(throwsList);
	}

	public void methodDeclaration(int modifiers, Class returnType, String methodName, Class[] argTypes, String[] argNames, Class[] throwsList, Object... annotations) {
		printMethodLine(modifiers, returnType, methodName, argTypes, argNames, throwsList, annotations);
		writer.println(";");
	}

	public void beginMethod(int modifiers, Class returnType, String methodName, Class[] argTypes, String[] argNames, Class[] throwsList, Object... annotations) {
		printMethodLine(modifiers, returnType, methodName, argTypes, argNames, throwsList, annotations);
		writer.println(" {");
		blockTypes.addFirst(METHOD);
	}

	private void endWithType(Integer type) {
		if(blockTypes.isEmpty() || !blockTypes.removeFirst().equals(type)) {
			throw new IllegalStateException();
		}
		doIndent();
		writer.println("}");
	}

	public void endMethod() {
		endWithType(METHOD);
	}
}
