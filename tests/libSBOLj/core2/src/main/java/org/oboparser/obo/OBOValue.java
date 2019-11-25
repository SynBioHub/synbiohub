package org.oboparser.obo;

import java.util.*;
import java.util.regex.*;

public class OBOValue {
	
	public static void main(String[] args) { 
		System.out.println(new OBOValue("foo").toFullString());
		System.out.println(new OBOValue("foo !bar").toFullString());
		System.out.println(new OBOValue("foo ! bar").toFullString());		
		System.out.println(new OBOValue("foo {a=b}").toFullString());		
		System.out.println(new OBOValue("foo { a=b, c=d}").toFullString());		
		System.out.println(new OBOValue("foo { a=b, c=d} ! bar").toFullString());		
		System.out.println(new OBOValue("\"foo\" { a=b, c=d} ! bar").toFullString());		
		System.out.println(new OBOValue("\"foo { a=b, c=d}\" ! bar").toFullString());		
	}
	
	private static Pattern commentPattern = Pattern.compile(
			"^\\s*((?:\"[^\"]+\")?[^!]*)" +
			"\\s*" + 
			"(?:!(.*))?\\s*$");
	private static Pattern modifierPattern = Pattern.compile(
			"^([^\\{]+)\\s*(?:\\{(.*)\\})?\\s*$");

	private String rawString;
	private String comment;
	private String value;
	private String[] modifiers;
	
	public OBOValue(String raw) { 
		rawString = raw;
		Matcher m = commentPattern.matcher(raw);
		if(!m.matches()) { 
			throw new IllegalArgumentException("\"" + raw + "\"");
		}
		
		value = m.group(1);
		if(m.groupCount() > 1 && m.group(2) != null) { 
			comment = m.group(2);
		}
		
		Matcher mm = modifierPattern.matcher(value);
		if(mm.matches() && mm.groupCount() > 1 && mm.group(2) != null) { 
			value = mm.group(1);
			modifiers = parseModifiers(mm.group(2));
		} else { 
			modifiers = new String[0];
		}
	}
	
	public OBOValue(String v, String c, String... mds) { 
		value = v;
		comment = c;
		modifiers = mds.clone();
		rawString = toFullString();
	}
	
	public String toFullString() { 
		StringBuilder sb = new StringBuilder(String.format("%s", value));
		sb.append(" {");
		int midx = 0;
		for(String m : modifiers) { 
			if(midx > 0) { sb.append(","); }
			sb.append(String.format("%s", m));
			midx += 1;
		}
		sb.append("}");
		sb.append(String.format(" ! %s", comment));
		return sb.toString();
	}
	
	public String getRawString() { return rawString; }
	public String getValue() { return value; }
	public String getComment() { return comment; }
	public String[] getModifiers() { return modifiers; }
	
	@Override
	public int hashCode() { return rawString.hashCode(); }
	
	@Override
	public boolean equals(Object o) { 
		if(!(o instanceof OBOValue)) { return false; }
		OBOValue v = (OBOValue)o;
		return v.rawString.equals(rawString);
	}
	
	@Override
	public String toString() { return rawString; }
	
 	private static String[] parseModifiers(String mod) { 
 		assert mod != null;
		Set<Integer> commaPoints = new TreeSet<Integer>();
		int s = 0;
		commaPoints.add(-1);
		while(s < mod.length() && (s = mod.indexOf(",", s)) != -1) {
			if(s > 0 && mod.charAt(s-1) != '\\') { 
				commaPoints.add(s);
			}
			s += 1;
		}
		commaPoints.add(mod.length());
		
		Integer[] cpts = commaPoints.toArray(new Integer[0]);
		String[] modarray = new String[cpts.length-1];
		for(int i = 0; i < cpts.length - 1; i++) { 
			modarray[i] = mod.substring(cpts[i]+1, cpts[i+1]);
		}
		return modarray;
	}
}
