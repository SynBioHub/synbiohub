package org.oboparser.obo.annotations.parsers;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.oboparser.obo.annotations.Relates;
import org.oboparser.obo.annotations.Term;

public class OntologyAnnotationParser {

	public static String unCamelCase(String str) {
		return unCamelCase(str, " ");
	}

	public static String camelCase(String str, String spacer) {
		StringBuilder sb = new StringBuilder();
		String[] array = str.split(spacer);
		for(String a : array) {
			if(a.length() > 0) {
				String cca = Character.toUpperCase(a.charAt(0)) + a.substring(1, a.length());
				sb.append(cca);
			}
		}
		return sb.toString();
	}

	public static String unCamelCase(String str, String spacer) {
		Set<Integer> wordBoundaries = new TreeSet<Integer>();
		if(str.length() > 0) {
			StringBuilder sb =new StringBuilder();
			wordBoundaries.add(0);
			for(int i = 1; i < str.length(); i++) {
				if(Character.isUpperCase(str.charAt(i)) &&
						!Character.isUpperCase(str.charAt(i-1))) {

					wordBoundaries.add(i);
				}
			}

			wordBoundaries.add(str.length());
			Integer[] array = wordBoundaries.toArray(new Integer[0]);
			for(int i = 0; i < array.length-1; i++) {
				String word = str.substring(array[i], array[i+1]).toLowerCase();
				if(sb.length() > 0) { sb.append(spacer); }
				sb.append(word);
			}

			return sb.toString();

		} else {
			return str;
		}
	}

	public static boolean isSubclass(Class c1, Class c2) {
		return c2.isAssignableFrom(c1);
	}

	//	/**
	//	 *
	//	 * @param cls
	//	 * @param fieldName
	//	 * @param type
	//	 * @return A static, public field with the given name.
	//	 * @throws NoSuchFieldException
	//	 */
	protected Field findPublicField(Class cls, String fieldName, Class type) throws NoSuchFieldException {
		Field field = cls.getField(fieldName);
		int mod = field.getModifiers();
		if(!Modifier.isPublic(mod) || !Modifier.isStatic(mod)) {
			throw new NoSuchFieldException(fieldName);
		}
		if(!isSubclass(field.getType(), type)) {
			throw new NoSuchFieldException(field.getType().getCanonicalName());
		}
		return field;
	}

	//	/**
	//	 *
	//	 * @param cls
	//	 * @param methodName
	//	 * @param type
	//	 * @return A static, public accessor method with the given name.
	//	 * @throws NoSuchMethodException
	//	 */
	protected Method findPublicAccessor(Class cls, String methodName, Class type) throws NoSuchMethodException {
		Method method = cls.getDeclaredMethod(methodName);
		int mod = method.getModifiers();
		if(!Modifier.isPublic(mod) || !Modifier.isStatic(mod)) {
			throw new NoSuchMethodException(methodName);
		}
		if(!isSubclass(method.getReturnType(), type)) {
			throw new NoSuchMethodException(method.getReturnType().getCanonicalName());
		}
		return method;
	}

	protected <T> T[] getClassValueSet(Class cls, String name, Class<T> type) {
		T[] array = (T[])Array.newInstance(type, 0);
		Class arrayType = array.getClass();
		try {
			Field field = findPublicField(cls, name, arrayType);
			return (T[])field.get(cls);

		} catch (NoSuchFieldException e) {
			try {
				Method method = findPublicAccessor(cls, name, arrayType);
				return (T[])method.invoke(cls);

			} catch (NoSuchMethodException e1) {
				return array;

			} catch (InvocationTargetException e1) {
				throw new IllegalArgumentException(e1);
			} catch (IllegalAccessException e1) {
				throw new IllegalArgumentException(e1);
			}
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected <T> T getClassValue(Class cls, String name, Class<T> type) {
		try {
			Field field = findPublicField(cls, name, type);
			return (T)field.get(cls);

		} catch (NoSuchFieldException e) {
			try {
				Method method = findPublicAccessor(cls, name, type);
				return (T)method.invoke(cls);

			} catch (NoSuchMethodException e1) {
				throw new OBOTermValueException(name);

			} catch (InvocationTargetException e1) {
				throw new IllegalArgumentException(e1);
			} catch (IllegalAccessException e1) {
				throw new IllegalArgumentException(e1);
			}
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected String getTermField(Class cls, String fieldName) {
		if(!cls.isAnnotationPresent(org.oboparser.obo.annotations.Term.class)) {
			throw new IllegalArgumentException("Annotations for " + cls.getCanonicalName() + ": " + Arrays.asList(cls.getAnnotations()));
		}
		Term termAnnotation = (Term)cls.getAnnotation(org.oboparser.obo.annotations.Term.class);

		try {
			Method termAccessor = termAnnotation.getClass().getMethod(fieldName);
			if(!isSubclass(termAccessor.getReturnType(), String.class)) {
				throw new IllegalArgumentException(fieldName);
			}

			return (String)termAccessor.invoke(termAnnotation);

		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Method[] findImmediateRelations(Class cls) {
		ArrayList<Method> ms = new ArrayList<Method>();
		for(Method m : cls.getDeclaredMethods()) {
			if(m.isAnnotationPresent(Relates.class)) {
				ms.add(m);
			}
		}
		return ms.toArray(new Method[0]);
	}

	public Method[] findRelations(Class cls, String typedef) {
		ArrayList<Method> ms = new ArrayList<Method>();
		for(Method m : cls.getMethods()) {
			if(m.isAnnotationPresent(Relates.class)) {
				Relates rel = m.getAnnotation(Relates.class);
				if(rel.value().equals(typedef)) {
					ms.add(m);
				}
			}
		}
		return ms.toArray(new Method[0]);
	}

	public Method[] findAllRelations(Class cls) {
		ArrayList<Method> ms = new ArrayList<Method>();
		for(Method m : cls.getMethods()) {
			if(m.isAnnotationPresent(Relates.class)) {
				ms.add(m);
			}
		}
		return ms.toArray(new Method[0]);
	}

	public String relationProperty(Method m) {
		if(m.isAnnotationPresent(Relates.class)) {
			Relates rel = m.getAnnotation(Relates.class);
			String typedef = rel.value();
			if(typedef == null || typedef.length() == 0) {
				typedef = unCamelCase(m.getName(), "_");
			}
			return typedef;
		} else {
			throw new IllegalArgumentException(String.format("Method %s in class %s has annotations %s",
					m.getName(), m.getDeclaringClass().getSimpleName(),
					Arrays.asList(m.getAnnotations())));
		}
	}

	public Class relationType(Method m) {
		if(m.isAnnotationPresent(Relates.class)) {
			Relates rel = m.getAnnotation(Relates.class);
			Class type = m.getReturnType();
			if(type.isArray()) {
				type = type.getComponentType();
			}

			if(!isTerm(type)) {
				throw new IllegalArgumentException(String.format("Type class %s has annotations %s",
						type.getSimpleName(),
						Arrays.asList(type.getAnnotations())));
			}

			return type;

		} else {
			throw new IllegalArgumentException(String.format("Method %s in class %s has annotations %s",
					m.getName(), m.getDeclaringClass().getSimpleName(),
					Arrays.asList(m.getAnnotations())));
		}
	}

	public Class[] findImmediateSuperClasses(Class cls) {
		Set<Class> supers = new LinkedHashSet<Class>();
		if(isTerm(cls)) {
			Class superClass = cls.getSuperclass();
			if(superClass != null && isTerm(superClass)) {
				supers.add(superClass);
			}

			Class[] interfaces = cls.getInterfaces();
			for(int i = 0; i < interfaces.length; i++) {
				if(isTerm(interfaces[i])) {
					supers.add(interfaces[i]);
				}
			}
		}
		return supers.toArray(new Class[0]);
	}

	public Class[] findAllSuperClasses(Class cls) {
		Set<Class> supers = new LinkedHashSet<Class>();
		if(isTerm(cls)) {
			supers.add(cls);
			for(Class superClass : findImmediateSuperClasses(cls)) {
				supers.addAll(Arrays.asList(findAllSuperClasses(superClass)));
			}
		}
		return supers.toArray(new Class[0]);
	}

	public boolean isTerm(Class t) {
		return t.isAnnotationPresent(org.oboparser.obo.annotations.Term.class);
	}

	private boolean isRelationship(Method m) {
		return m.isAnnotationPresent(org.oboparser.obo.annotations.Relates.class);
	}


}
