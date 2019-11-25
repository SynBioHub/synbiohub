package org.oboparser.obo.annotations.parsers;

import java.lang.reflect.*;

public class OBOAnnotationParser extends OntologyAnnotationParser {
	
	public String name(Class value) {
		try { 
			return getClassValue(value, getTermField(value, "name"), String.class);
		} catch(OBOTermValueException e) { 
			return unCamelCase(value.getSimpleName());
		}
	}

	public String def(Class value) { 
		return getClassValue(value, getTermField(value, "def"), String.class);
	}
	
	public String id(Class value) { 
		return getClassValue(value, getTermField(value, "id"), String.class);
	} 
	
	public String[] comments(Class value) { 
		return getClassValueSet(value, getTermField(value, "comment"), String.class);
	}
	
	public String oboTag(Class cls) { 
		return String.format("%s ! %s", id(cls), name(cls));
	}
	
	public String stanza(Class cls) { 
		if(!isTerm(cls)) { 
			throw new IllegalArgumentException(cls.getCanonicalName());
		}
		StringBuilder sb = new StringBuilder();
		
		sb.append("[Term]\n");
		sb.append(String.format("id: %s\n", id(cls)));
		sb.append(String.format("name: %s\n", name(cls)));
		sb.append(String.format("def: \"%s\"\n", def(cls)));

		for(String comment : comments(cls)) { 
			sb.append(String.format("comment: \"%s\"\n", comment));
		}
		
		for(Class superClass : findImmediateSuperClasses(cls)) { 
			sb.append(String.format("is_a: %s\n", oboTag(superClass)));
		}
		
		for(Method m : findImmediateRelations(cls)) { 
			sb.append(String.format("relationship: %s %s\n", 
					relationProperty(m),
					oboTag(relationType(m))));
		}
		
		return sb.toString();
	}
}
