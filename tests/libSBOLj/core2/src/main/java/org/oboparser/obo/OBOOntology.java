package org.oboparser.obo;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class OBOOntology extends OBOStanza {

	private Map<String,OBOStanza> stanzas;

	public OBOOntology() {
		super("ontology");
		stanzas = new TreeMap<String,OBOStanza>();
	}

	public void addOBOStanza(OBOStanza s) {
		assert s != null;
		String id = s.getId();
		if(id == null || stanzas.containsKey(id)) {
			throw new IllegalArgumentException(String.format("ID: \"%s\"", id));
		}
		stanzas.put(id, s);
	}

	public void add(OBOOntology ont) {
		super.add(ont);
		for(String key : ont.stanzas.keySet()) {
			if(!stanzas.containsKey(key)) {
				stanzas.put(key, (OBOStanza)ont.stanzas.get(key).clone());
			} else {
				stanzas.get(key).add(ont.stanzas.get(key));
			}
		}
	}

	@Override
	public void print(PrintWriter w) {
		for(String id : stanzas.keySet()) {
			stanzas.get(id).print(w);
		}
	}

	public Collection<OBOStanza> getStanzas() {
		return stanzas.values();
	}

	public OBOStanza getStanza(String id) {
		return stanzas.get(id);
	}

	//	/**
	//	 * Recursively retrieve the "is_a" relation to check if the child {@code OBOStanza} is a descendant of the parent {@code OBOStanza}.
	//	 * @param child
	//	 * @param terminateParent
	//	 * @return {@code true} if the child is a descendant of the terminateParent; {@code false} otherwise.
	//	 */
	public boolean isDescendantOf(OBOStanza child, OBOStanza terminateParent) {
		boolean result = false;
		if (child.hasValue("replaced_by")) {
			for (OBOValue newChildId : child.values("replaced_by")) {
				// child.values("replaced_by") should always return a list with a single element.
				//System.out.println(child.getId() + " is replaced by " + newChildId.getValue().trim());
				child = this.getStanza(newChildId.getValue().trim());
			}
		}
		if (!child.hasValue("is_a")) { // Reached a root OBOStanza but no match
			return false;
		}
		else {
			for (OBOValue immediateParentInfo : child.values("is_a")) {
				String immediateParentId = immediateParentInfo.getValue().trim(); // getValue returns id without any comments. Comments started with "!" in the OBO file.
				//System.out.println("is_a = " + immediateParentInfo);
				//System.out.println(child.getId() + " is_a: " + immediateParentInfo.getValue());
				//System.out.println("anotherStanza_id = " + terminateParent.id());
				if (immediateParentId.equals(terminateParent.getId().trim())) {
					return true;
				}
				else {
					OBOStanza immediateParent = this.getStanza(immediateParentId);
					result = result || isDescendantOf(immediateParent, terminateParent);
					if (result == true) break;
				}
			}
			return result;
		}
	}

	public Set<String> getDescendantsOf(OBOStanza terminateParent) {
		Set<String> result = new HashSet<String>();
		for (OBOStanza child : getStanzas()) {
			if (!child.hasValue("is_a")) { 
				continue;
			}
			for (OBOValue immediateParentInfo : child.values("is_a")) {
				String immediateParentId = immediateParentInfo.getValue().trim(); 
				if (immediateParentId.equals(terminateParent.getId().trim())) {
					result.add(child.getId());
					result.addAll(getDescendantsOf(child));
				}
			}
		}
		return result;
	}
}
