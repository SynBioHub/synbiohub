package org.sbolstandard.core2;

/**
 * Represents an SBOL validation rule.
 * 
 * @author Zhen Zhang
 * @version 2.1
 */
class SBOLValidationRule {

	private String ruleClass;
	private String id;
	private String condition;
	private String description;
	private String reference;

	SBOLValidationRule(String ruleClass) {
		this.ruleClass = ruleClass;
		id = null;
		condition = null;
		description = null;
	}

	String getRuleClass() {
		return ruleClass;
	}

	String getId() {
		return id;
	}

	void setId(String id) {
		this.id = id;
	}

	String getCondition() {
		return condition;
	}

	void setCondition(String condition) {
		if (condition.equals("strong required")) {
			this.condition = "Strong Validation Error";
		} else if (condition.equals("weak required")) {
			this.condition = "Weak Validation Error";
		} else if (condition.equals("recommended")) {
			this.condition = "Best Practice Warning";
		} else if (condition.equals("compliance")) {
			this.condition = "URI Compliance Warning";
		} else {
			this.condition = condition;
		}
	}

	String getDescription() {
		return description;
	}

	void setDescription(String description) {
		this.description = description;
	}
	
	void setReference(String ref) {
		this.reference = ref;
	}

	String getReference() {
		return reference;
	}
	
	@Override
	public String toString() {
		return "model class: " + ruleClass + "\n" 
			+  "id: " + this.id + "\n"
			+  "condition: " + this.condition + "\n"
			+  "description: " + this.description + "\n"
			+  "reference: " + this.reference+ "\n";
	}
}
