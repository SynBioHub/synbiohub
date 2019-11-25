package org.oboparser.obo;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * OBOTerm represent an OBO stanza that begins with the "[Term]" string.
 * 
 * Each term must have (at a minimum) a "name" field.  

 * @author Timothy Danford
 */
public class OBOTerm extends OBOStanza {
	
	private static Pattern relationshipPattern = Pattern.compile(
			"^\\s*" +  			// leading spaces.
			"([^\\s]+)" +   	// typedef name.
			"\\s+" +
			"([^\\s]+)" +		// target term ID.
			"\\s*" +
			"($" + 				// BEGIN either we finish the string here
			"|" +				// ... or
			"(?:!\\s+(.*)" +	// we match a name string, with the form "! [name]" where [name] can have spaces.
			"\\s*$)" +			// trailing spaces.
			")?");				// END either.	
	
	private static Pattern isAPattern = Pattern.compile(
			"^\\s*" +  			// leading spaces
			"([^\\s]+)" +		// parent term ID.
			"\\s*" +			// optional spaces.
			"($" +				// Either we end the string here
			"|" +				// ... or
			"(?:!\\s+(.*)" +	// we finish with a string "! [name]" where [name] can contain spaces.
			"\\s*$)" +			// trailing spaces.
			")?");
	
	private String name;
	
	public OBOTerm() { 
		super("Term");
		name = null;
	}
	
	@Override
	public Object clone() { 
		return super.clone(OBOTerm.class);
	}
	
	@Override
	public String getName() { return name; }
	
	public boolean isObsolete() { 
		return hasValue("is_obsolete") && 
			values("is_obsolete").get(0).getRawString().trim().equals("true");
	}
	
	@Override
	public void addValue(String k, OBOValue v) { 
		super.addValue(k, v);
		
		if(k.equals("name")) { 
			if(name != null) { 
				throw new OBOException(String.format("Term %s given duplicate name %s", 
						name, v.getValue()));
			} else { 
				name = v.getValue();
			}
		}
	}
	
	public String tag() { return String.format("%s ! %s", getId(), name); }
	
	@Override
	public String toString() { return name; }
	
	public String[] relationships() { 
		ArrayList<String> rels = new ArrayList<String>();
		if(hasValue("relationship")) { 
			for(OBOValue value : values("relationship")) { 
				Matcher m = relationshipPattern.matcher(value.getValue());
				if(m.matches()) { 
					rels.add(m.group(1));
				}
			}
		}
		return rels.toArray(new String[0]);
	}
	
	public String[] relationship(String typedef) { 
		ArrayList<String> rels = new ArrayList<String>();
		if(hasValue("relationship")) { 
			for(OBOValue value : values("relationship")) { 
				Matcher m = relationshipPattern.matcher(value.getValue());
				if(m.matches()) { 
					if(m.group(1).equals(typedef)) { 
						rels.add(m.group(2));
					}
				}
			}
		}
		return rels.toArray(new String[0]);		
	}
	
	public String[] isa() { 
		ArrayList<String> isa = new ArrayList<String>();
		if(hasValue("is_a")) { 
			for(OBOValue value : values("is_a")) { 
				Matcher m = isAPattern.matcher(value.getValue());
				if(m.matches()) { 
					isa.add(m.group(1));
				}
			}
		}
		return isa.toArray(new String[0]);
	}	
	
	public String[] intersectionOf() { 
		ArrayList<String> inters = new ArrayList<String>();
		if(hasValue("intersection_of")) { 
			for(OBOValue value : values("intersection_of")) { 
				Matcher relMatcher = relationshipPattern.matcher(value.getValue());
				Matcher isaMatcher = isAPattern.matcher(value.getValue());
				if(!relMatcher.matches() && isaMatcher.matches()) { 
					inters.add(isaMatcher.group(1));
				}
			}
		}
		return inters.toArray(new String[0]);
	}
}
