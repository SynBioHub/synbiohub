package org.oboparser.obo.annotations.parsers;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import org.oboparser.obo.OBOOntology;
import org.oboparser.obo.OBOTerm;
import org.oboparser.obo.OBOValue;

public class OBOTermCreator {

	private Map<String,Class> created;

	public OBOTermCreator() {
		created = new TreeMap<String,Class>();
	}

	//	/**
	//	 * Extracts the metadata from an OBOTerm object, and converts it into a call to the other createTerm method
	//	 * in this class.
	//	 *
	//	 * Since the is_a relationships in the OBOTerm *also* need to be converted (first) into Class objects representing
	//	 * the superclasses of this term's class, this method will recursively call itself until the entire super-class
	//	 * tree has been created.
	//	 *
	//	 * This means that dangling is_a references in the target OBOTerm (or its super-term hierarchy) will lead to exceptions.
	//	 *
	//	 * @param ontology
	//	 * @param termStanza
	//	 * @return term
	//	 * @throws CannotCompileException
	//	 */
	public Class<?> createTerm(OBOOntology ontology, OBOTerm termStanza) throws CannotCompileException {

		String id = termStanza.getId();
		if(created.containsKey(id)) {
			return created.get(id);
		}

		//System.out.println(String.format("Creating Class: %s", id));

		String name = termStanza.getName();
		String def = termStanza.hasValue("def") ? termStanza.values("def").get(0).getValue() : "";

		List<OBOValue> isaValues = termStanza.values("is_a");
		Class[] is_a = new Class[isaValues != null ? isaValues.size() : 0];
		for(int i = 0; i < is_a.length; i++) {
			OBOValue value = isaValues.get(i);
			String valueStr = value.getValue();
			String isaID = valueStr.split("!")[0].trim();
			OBOTerm isaTerm = (OBOTerm)ontology.getStanza(isaID);
			is_a[i] = createTerm(ontology, isaTerm);
		}

		Pattern relPattern = Pattern.compile("^\\s*([^\\s]+)\\s+(.*)(?:\\s*!\\s*.*)?$");

		List<OBOValue> relValues = termStanza.values("relationship");
		ArrayList<Class> relTypes = new ArrayList<Class>();
		ArrayList<String> relTypedefs = new ArrayList<String>();
		for(int i = 0; relValues != null && i < relValues.size(); i++) {
			OBOValue relValue = relValues.get(i);
			Matcher relMatcher = relPattern.matcher(relValue.getValue());
			if(relMatcher.matches()) {
				String relID = relMatcher.group(2).trim();
				OBOTerm relTerm = (OBOTerm)ontology.getStanza(relID);
				if(relTerm == null) { throw new IllegalArgumentException(relID + " in " + id); }

				relTypes.add(createTerm(ontology, relTerm));
				relTypedefs.add(relMatcher.group(1));
			} else {
				System.err.println(relValue.getValue() + " isn't a relationship");
			}
		}

		Class cc = createTerm(id, name, def, null, is_a, relTypes.toArray(new Class[0]), relTypedefs.toArray(new String[0]));
		//System.out.println("\tDone : " + id);
		return cc;
	}

	private static Charset utf8 = Charset.forName("UTF-8");

	//	/**
	//	 * A hacky attempt to remove all (really, probably most) of the characters from Strings, so that
	//	 * they can be converted into legal Java class/interface/method/field names.
	//	 *
	//	 * This removes a lot of punctuation, and then finally camel-cases the result.
	//	 *
	//	 * There's also a special case about apostrophe (') characters followed by spaces -- since these
	//	 * are often indicative of "primes", we convert them to the word "prime."  This is a behavior that should
	//	 * be optional, since a collective possessive would also fit the same pattern.
	//	 */
	public static String unmangleName(String ccClassName) {

		String original = new String(ccClassName.getBytes(utf8), utf8);

		ccClassName = ccClassName.replaceAll("/", " ");
		ccClassName = ccClassName.replaceAll("-", " ");
		ccClassName = ccClassName.replaceAll("\\(", " ");
		ccClassName = ccClassName.replaceAll("\\)", " ");
		ccClassName = ccClassName.replaceAll(",", " ");
		ccClassName = ccClassName.replaceAll("\\[", " ");
		ccClassName = ccClassName.replaceAll("\\]", " ");
		ccClassName = ccClassName.replaceAll("\\.", " ");
		ccClassName = ccClassName.replaceAll("'\\s", " prime ");
		ccClassName = ccClassName.replaceAll("'", "");
		ccClassName = ccClassName.replaceAll("\"", "");
		//ccClassName = ccClassName.replaceAll("\\\\", "\\\\\\\\");

		ccClassName = OntologyAnnotationParser.camelCase(ccClassName, " ");

		if(ccClassName.indexOf("(") != -1) { throw new IllegalArgumentException(ccClassName); }

		return ccClassName;
	}

	public static boolean isSubclass(Class c1, Class c2) {
		return c2.isAssignableFrom(c1);
	}

	public boolean isConflicting(Class returnType, String methodName, Method extant) {
		return methodName.equals(extant.getName()) && !isSubclass(returnType, extant.getReturnType());
	}

	public Method findConflictingMethod(Class returnType, String methodName, Class cls) {
		for(Method m : cls.getMethods()) {
			if(isConflicting(returnType, methodName, m)) {
				return m;
			}
		}
		return null;
	}

	/**
	 * Returns an acronym string for a camel-cased string.
	 *
	 * The acronym is a string of all upper-case and digit characters in the original string.
	 * It will not begin itself with a digit.
	 *
	 * @param camelCased The argument string -- <em>should</em> be in camel case (via a call to unmangle) but
	 * not required.
	 * @return acronym
	 */
	public String acronym(String camelCased) {
		StringBuilder sb = new StringBuilder();

		/*
		 * The acronym is just a collection of all upper-case or
		 * digit characters from the original string.
		 */
		for(int i = 0; i < camelCased.length(); i++) {
			char c = camelCased.charAt(i);
			if(Character.isUpperCase(c) || (sb.length() > 0 && Character.isDigit(c))) {
				sb.append(c);
			}
		}

		/*
		 * The acronym should not be empty -- but nor should it start with a digit.
		 * Therefore, we walk forward, and add the upper-cased version of the first
		 * non-digit character we can find, if we found no upper-case characters in
		 * the loop above.
		 */
		if(sb.length() == 0) {
			int i = 0;

			while(i < camelCased.length() && Character.isDigit(camelCased.charAt(i))) {
				i++;
			}

			if(i < camelCased.length()) {
				sb.append(Character.toUpperCase(camelCased.charAt(i)));
			}
		}

		return sb.toString();
	}

	public Method findAnyConflictingMethod(Class returnType, String methodName, Class superClass, Class[] interfaces) {
		Method conflict = superClass != null ? findConflictingMethod(returnType, methodName, superClass) : null;
		for(int i = 0; conflict == null && interfaces != null && i < interfaces.length; i++) {
			conflict = findConflictingMethod(returnType, methodName, interfaces[i]);
		}
		return conflict;
	}

	/*
	 * Counts the duplicates of str[i], in the range str[0...i-1]
	 */
	private int countDuplicates(String[] array, int i) {
		int c = 0;
		for(int j = 0; j < i; j++) {
			if(array[j].equals(array[i])) {
				c += 1;
			}
		}
		return c;
	}

	/*
	 * Returns an array where the duplicates have been renamed with numeric suffixes.
	 * So the array
	 *   { "foo", "foo", "bar", "foo" }
	 * would be renamed,
	 *   { "foo", "foo_2", "bar", "foo_3" }
	 */
	private String[] renameDuplicates(String[] str) {
		Integer[] append = new Integer[str.length];
		String[] unduped = new String[str.length];
		for(int i = 0; i < str.length; i++) {
			append[i] = countDuplicates(str, i);
		}

		for(int i = 0; i < str.length; i++) {
			if(append[i] > 0) {
				unduped[i] = String.format("%s_%d", str[i], append[i]);
			} else {
				unduped[i] = str[i];
			}
		}

		return unduped;
	}

	/*
	 * We need to check the superclass and interfaces, to find method names that will not conflict with the
	 * given method name.  This is handled in a type-safe manner (so the method doesn't conflict if the child
	 * class has a method with the same name, but a *narrowed* return type), and we try at add reasonable class-name
	 * prefixes or acronyms where appropriate.
	 */
	private String findNonConflictingName(String className, Class returnType, String methodName, Class superClass, Class[] interfaces) {
		String acronym = acronym(returnType.getSimpleName());
		methodName = String.format("%s_%s", className, methodName);

		Method conflict = findAnyConflictingMethod(returnType, methodName, superClass, interfaces);
		if(conflict != null) {
			methodName = String.format("%s_%s", methodName, acronym);
			conflict = findAnyConflictingMethod(returnType, methodName, superClass, interfaces);
		}

		String countedName = methodName;

		int i = 2;
		while(conflict != null) {
			countedName = String.format("%s_%d", methodName, i++);
			conflict = findAnyConflictingMethod(returnType, countedName, superClass, interfaces);
		}

		return countedName;
	}

	private Class<? extends Object> getArrayType(Class type) {
		return Array.newInstance(type, 0).getClass();
	}

	private String removeSlashes(String str) {
		return str.replaceAll("\\\\", "\\\\\\\\"); // replace single \'s with \\'s.  Gah.
	}

	//	/**
	//	 * Dynamically generates a Class file, for an interface that represents an OBO term with the given fields.
	//	 *
	//	 * The OBO term is uniquely identified by the id, and if a term with this id has been converted into a Class
	//	 * file already, then that Class file (cached within the OBOTermCreator object) will be returned immediately.
	//	 *
	//	 * @param id
	//	 * @param name
	//	 * @param def
	//	 * @param comments
	//	 * @param is_a
	//	 * @param relTypes
	//	 * @param relTypedefs
	//	 * @return term
	//	 * @throws CannotCompileException
	//	 */
	public Class createTerm(
			String id,
			String name,
			String def,
			String[] comments,
			Class[] is_a,
			Class[] relTypes,
			String[] relTypedefs) throws CannotCompileException {

		String ccClassName = unmangleName(name);

		if(created.containsKey(id)) {
			return created.get(id);
		}

		if(created.containsKey(ccClassName)) {
			return created.get(ccClassName);
		}

		OBOAnnotationParser obo = new OBOAnnotationParser();

		ClassPool cp = ClassPool.getDefault();

		CtClass stringClass = null, stringArrayClass = null;
		try {
			stringClass = cp.get("java.lang.String");
			stringArrayClass = cp.get("java.lang.String[]");
		} catch (NotFoundException e) {
			throw new IllegalStateException(e);
		}

		CtClass cc = cp.makeInterface(ccClassName);
		cc.setModifiers(javassist.Modifier.INTERFACE | javassist.Modifier.PUBLIC);

		ClassFile ccFile = cc.getClassFile();
		ConstPool constpool = ccFile.getConstPool();

		Annotation termAnnotation = new Annotation("org.sc.obo.annotations.Term", constpool);

		CtField idField = new CtField(stringClass, "id", cc);
		idField.setModifiers(javassist.Modifier.PUBLIC | javassist.Modifier.STATIC | javassist.Modifier.FINAL);

		CtField nameField = new CtField(stringClass, "name", cc);
		nameField.setModifiers(javassist.Modifier.PUBLIC | javassist.Modifier.STATIC | javassist.Modifier.FINAL);

		CtField defField = new CtField(stringClass, "def", cc);
		defField.setModifiers(javassist.Modifier.PUBLIC | javassist.Modifier.STATIC | javassist.Modifier.FINAL);

		cc.addField(idField, CtField.Initializer.constant(removeSlashes(id)));
		cc.addField(nameField, CtField.Initializer.constant(removeSlashes(name)));
		cc.addField(defField, CtField.Initializer.constant(removeSlashes(def)));

		if(is_a != null) {
			for(Class superClass : is_a) {
				if(!obo.isTerm(superClass)) {
					throw new IllegalArgumentException(superClass.getCanonicalName());
				}
				try {
					CtClass superCtClass = cp.get(superClass.getCanonicalName());
					cc.addInterface(superCtClass);

				} catch (NotFoundException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}

		/*
		 * Finally, we convert all the relTypes/relTypedefs into methods.
		 *
		 * The main trick here is to do this in a way that the method names don't clash!
		 *
		 * We want to rename each method to a form that's unique to this class as well -- the reason is that
		 * Class A and Class B can have incompatible methods with the same name, which is fine since neither of
		 * them is a superclass (super-interface, whatevs) of the other.
		 *
		 * However, as soon as we define a Class C that extends both interfaces A & B, we have a problem -- suddenly
		 * we inherit both methods, but with incompatible types.
		 *
		 * So we need to mangle the names of A and B's classes, so that they are (a) descriptive, but (b) don't
		 * clash with other class's method names.  This leads to ugly code generation, but ... it works.
		 */
		if(relTypes != null && relTypedefs != null) {
			if(relTypes.length != relTypedefs.length) { throw new IllegalArgumentException(); }

			String[] nonDups = renameDuplicates(relTypedefs);

			for(int i = 0; i < relTypes.length; i++) {
				try {
					if(relTypes[i] == null) {
						throw new IllegalArgumentException(id + " " + Arrays.asList(relTypes));
					}

					Class arrayType = relTypes[i].isArray() ? relTypes[i] : getArrayType(relTypes[i]);

					String typeName = arrayType.getCanonicalName();
					String methodName = findNonConflictingName(ccClassName, arrayType, nonDups[i], null, is_a);

					CtClass relTypeClass = cp.get(typeName);

					CtMethod relMethod = new CtMethod(relTypeClass, methodName, new CtClass[]{}, cc);
					relMethod.setModifiers(Modifier.PUBLIC | Modifier.ABSTRACT);

					// We need to create this with the *original* typedef name,
					// not the new (non-clashing) method name.  That way, we can recover the original
					// name of the property from the (mangled) method name.
					Annotation relAnnotation = new Annotation("org.sc.obo.annotations.Relates", constpool);
					relAnnotation.addMemberValue("value", new StringMemberValue(relTypedefs[i], ccFile.getConstPool()));

					AnnotationsAttribute annotations =
							new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);

					annotations.addAnnotation(relAnnotation);
					relMethod.getMethodInfo().addAttribute(annotations);

					cc.addMethod(relMethod);

				} catch (NotFoundException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}

		AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		attr.addAnnotation(termAnnotation);
		ccFile.addAttribute(attr);

		Class c = cc.toClass();

		created.put(id, c);
		created.put(ccClassName, c);

		return c;
	}
}
