package org.sbolstandard.core2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Signals that an SBOL validation rule violation has occurred. 
 * 
 * @author Zhen Zhang
 * @author Chris Myers
 * @version 2.1
 */

public class SBOLValidationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String ruleBegin = "^\\[(\\w+)\\]\\s*$";
	private static String ruleId = "^id:\\s(sbol-\\d+)\\s*$";
	private static String ruleCondition = "^condition:\\s(\\w+\\s*\\w*)\\s*$";
	private static String ruleDescriptionBegin = "^description:\\s(.+)\\s*$";
	private static String ruleDescriptionBody = "^(?!reference:)(.+)$";//"^\\b(?!reference)\\b(?!:)(.+)$";
	// (?!...) is negative lookahead.
	//	Word boundary \b is equivalent to (?<=\w)(?!\w)|(?=\w)(?<!\w).It means that a position that's preceded by a word 
	//	character and not followed by one, or a position that's followed by a word character and not 
	//	preceded by one.
	private static String ruleReference = "^\\breference\\b:\\s(.+)";
	private static SBOLValidationRule currentRule;
	private static Map<String, SBOLValidationRule> validationRules;

	/**
	 * Creates a new exception instance with the given message and objects causing the problem.
	 * @param message
	 * @param objects
	 */
	SBOLValidationException(String message, Identified ... objects) {
		this(message, Arrays.asList(objects));
	}

	/**
	 * Creates a new exception instance with the given message and objects causing the problem.
	 * @param message
	 * @param objects
	 */
	SBOLValidationException(String message, URI identity) {
		super(formatMessage(message, identity));
	}

	/**
	 * Creates a new exception instance with the given message and objects causing the problem.
	 * 
	 * @param message
	 * @param objects
	 */
	private SBOLValidationException(String message, java.util.Collection<? extends Identified> objects) {
		super(formatMessage(message, objects));
	}

	/**
	 * Creates a new exception instance with the given message and objects causing the problem.
	 * 
	 * @param message
	 * @param cause
	 * @param objects
	 */
	SBOLValidationException(String message, Throwable cause, Identified ... objects) {
		this(message, cause, Arrays.asList(objects));
	}
	
	private SBOLValidationException(String message, Throwable cause, java.util.Collection<? extends Identified> objects) {
		super(formatMessage(message, objects),cause);
	}

	/**
	 * Creates a new exception instance with the given cause but no specific objects for the problem.
	 * 
	 * @param cause
	 */	
	SBOLValidationException(Throwable cause) {
		super(cause);
	}
	
	private static String formatMessage(String message, URI identity) {
		final StringBuilder sb = new StringBuilder(message);
		if (message.startsWith("sbol-")) {
			if (validationRules == null) {
				validationRules = new LinkedHashMap<String, SBOLValidationRule>();
				InputStreamReader f = new InputStreamReader(SBOLValidationRule.class.
						getResourceAsStream("/validation/rules2p3p0.txt"));
				try {					
					parse(new BufferedReader(f));
					//writeRulesToXML("rules.xml");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//printAllRules();
			String key = message.trim();
			SBOLValidationRule rule = validationRules.get(key);
			if (rule == null) {
				throw new RuntimeException("Rule ID does not exist.");
			}
			sb.append(":\0 " +rule.getCondition()+ ":\0\n"+ rule.getDescription() + "\n\0");
			sb.append("Reference: SBOL Version 2.3.0 " + rule.getReference() + "\n");
			sb.append(":\0 " + identity.toString());
		}
		else {		
			sb.append(":\0 " + identity.toString());
		}
		sb.append("\0");
		return sb.toString();
	}

	private static String formatMessage(String message, java.util.Collection<? extends Identified> objects) {
		final StringBuilder sb = new StringBuilder(message);
		if (message.startsWith("sbol-")) {
			if (validationRules == null) {
				validationRules = new LinkedHashMap<String, SBOLValidationRule>();
				InputStreamReader f = new InputStreamReader(SBOLValidationRule.class.
						getResourceAsStream("/validation/rules2p3p0.txt"));
				try {					
					parse(new BufferedReader(f));
					//writeRulesToXML("rules.xml");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//printAllRules();
			String key = message.trim();
			SBOLValidationRule rule = validationRules.get(key);
			sb.append(":\0 ");
			if (rule != null) {
				sb.append(rule.getCondition()+":\0\n");
				sb.append(rule.getDescription());
				sb.append("\n\0Reference: SBOL Version 2.3.0 "+rule.getReference());
			} 
			if (!objects.isEmpty()) {
				sb.append("\n:\0 ");
				boolean first = true;
				for (Identified obj : objects) {
					if (first) {
						first = false;
					}
					else {
						sb.append(", ");
					}
					if (obj.getIdentity() != null) {
						sb.append(obj.getIdentity());
					}
				}
			} else {
				sb.append("\0");
			}
			sb.append("\0");
		}
		else {		
			//final StringBuilder sb = new StringBuilder(message);
			if (!objects.isEmpty()) {
				sb.append(":\0 ");
				boolean first = true;
				for (Identified obj : objects) {
					if (first) {
						first = false;
					}
					else {
						sb.append(", ");
					}
					if (obj.getIdentity() != null) {
						sb.append(obj.getIdentity());
					}
				}
			} else {
				sb.append("\0");
			}
			sb.append("\0");
			//return sb.toString();		
		}
		return sb.toString();
	}
	
// TODO: Complete this method to write validation rules in XML format.
	static void writeRulesToXML(String filename) {
		File outputFile;
		FileWriter outputFW;
		BufferedWriter outputBW = null;
		try
		{
			outputFile = new File(filename);
			//System.out.println(outputFile.getAbsolutePath());
			//System.out.println(filename);
			outputFW = new FileWriter(outputFile);
			outputBW = new BufferedWriter(outputFW);//for OS to keep track of what you have written
		}
		catch(IOException e){
					
		}
		String eol = System.getProperty("line.separator");
		String rule = "";
		rule = rule + "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + eol;
		rule = rule + "<rules>" + eol;
		try {
			outputBW.write(rule);		
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String key : validationRules.keySet()) {
			rule = rule + "<id>"+ key +"</id>" + eol;
			rule = rule + "<category>" + validationRules.get(key).getRuleClass() +"</category>" + eol;
			rule = rule + "<condition>" + validationRules.get(key).getCondition() +"</condition>" + eol;
			rule = rule + "<description>" + validationRules.get(key).getDescription() +"</description>" + eol;
			rule = rule + "<reference>" + validationRules.get(key).getReference() +"</reference>" + eol;
			
		}
		rule = rule + "</rules>";
		System.out.println(rule);
		try {
			outputBW.write(rule);		
			outputBW.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	
	}

	/**
	 * @param br
	 * @throws IOException
	 */
	private static void parse(BufferedReader br) throws IOException {		
		String line;
		String ruleDescription = "";
		while((line = br.readLine())!= null) {
			//System.out.println(line);
			// WARNING: DO NOT modify the ordering of the if-else clause below!
			if (line.matches(ruleBegin)) {
				Matcher mRuleBegin = Pattern.compile(ruleBegin).matcher(line);
				if (mRuleBegin != null && mRuleBegin.matches()) { // need to call matches method in order to call the group method.
					currentRule = new SBOLValidationRule(mRuleBegin.group(1));
					//System.out.println("currentRule.ruleClass: " + currentRule.getRuleClass());
					ruleDescription = "";
				}
			}
			else if (line.matches(ruleId)) {
				Matcher mRuleId = Pattern.compile(ruleId).matcher(line);
				if (mRuleId != null && mRuleId.matches()) { // need to call matches method in order to call the group method.
					currentRule.setId(mRuleId.group(1));
					//System.out.println("currentRule.ruleId: " + currentRule.getId());
				}
			}
			else if (line.matches(ruleCondition)) {
				Matcher mRuleCondition = Pattern.compile(ruleCondition).matcher(line);
				if (mRuleCondition != null && mRuleCondition.matches()) { // need to call matches method in order to call the group method.
					currentRule.setCondition(mRuleCondition.group(1));
					//System.out.println("currentRule.ruleCondition: " + currentRule.getCondition());
				}
			}
			else if (line.matches(ruleDescriptionBegin)) {
				Matcher mRuleDescription = Pattern.compile(ruleDescriptionBegin).matcher(line);
				if (mRuleDescription != null && mRuleDescription.matches()) { // need to call matches method in order to call the group method.					
					ruleDescription = mRuleDescription.group(1);
					//System.out.println("currentRule.ruleDescriptionBegin: " + mRuleDescription.group(1));
				}
			}
			else if (line.matches(ruleDescriptionBody)) { // WARNING: Do NOT move this if clause to other places.
				Matcher mRuleDescriptionBody = Pattern.compile(ruleDescriptionBody).matcher(line);
				if (mRuleDescriptionBody != null && mRuleDescriptionBody.matches()) { // need to call matches method in order to call the group method.
					ruleDescription = ruleDescription.trim() + " " + mRuleDescriptionBody.group(1);
					//System.out.println("currentRule.ruleDescriptionBody: " + mRuleDescriptionBody.group(1));
				}
			}
			else if (line.matches(ruleReference)) {
				currentRule.setDescription(ruleDescription);
				Matcher mRuleReference = Pattern.compile(ruleReference).matcher(line);
				if (mRuleReference != null && mRuleReference.matches()) { // need to call matches method in order to call the group method.
					//System.out.println("currentRule.ruleReference: " + mRuleReference.group(1));
					currentRule.setReference(mRuleReference.group(1));
					validationRules.put(currentRule.getId(), currentRule);
					currentRule = null;
				}		
			}
		}
		br.close();
	}
	
	/*
	 * Return the validation rule number of the exception
	 * @return the validation rule number of the exception
	 */
	String getRule() {
		if (!getMessage().startsWith("sbol-")) return "";
		return getMessage().split(":")[0];
	}

	// TODO: unfinished
	static void printAllRules() {
		for (String key : validationRules.keySet()) {
			System.out.println(validationRules.get(key));
		}
	}

}

