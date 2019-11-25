package org.oboparser.obo;

import java.io.*;
import java.util.regex.*;

public class OBOParser {
	
	public static void main(String[] args) { 
		try {
			File f = new File(args[0]);
			OBOParser parser = new OBOParser();
			parser.parse(f);
			OBOOntology ontology = parser.getOntology();
			
			System.out.println(String.format("Ontology: \n%s", ontology.toString()));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private OBOOntology currentOntology;
	private OBOStanza currentStanza;
	
	private static Pattern stanzaStart = Pattern.compile("^\\s*\\[([^\\]]+)\\]\\s*$");
	
	public OBOParser() { 
		currentOntology = null;
		currentStanza = null;
	}
	
	public OBOOntology getOntology() { return currentOntology; }
	
	public void parse(Reader r) throws IOException { 
		parse(new BufferedReader(r));
	}
	
	public void parse(BufferedReader br) throws IOException { 
		String line = null;
		
		currentOntology = new OBOOntology();
		currentStanza = currentOntology;

		/*
		while((line = br.readLine()) != null) { 
			handleLine(line); 
		}
		*/
		do { 
			handleLine(line = br.readLine()); 
		} while(line != null);
	}
	
	public void parse(File f) throws IOException { 
		BufferedReader br = new BufferedReader(new FileReader(f));
		parse(br);
		br.close();
	}
	
	private void handleLine(String line) {
		Matcher m = null;
		if(line != null) { 
			line = line.trim(); 
			m = stanzaStart.matcher(line);
		} 
	
		assert currentOntology != null;
		
		if(line == null) { 
			if(currentStanza != null && currentStanza != currentOntology) { 
				currentOntology.addOBOStanza(currentStanza);
				//System.out.println();
			}
			
		} else if (m.matches()) {
			if(currentStanza != null && currentStanza != currentOntology) { 
				currentOntology.addOBOStanza(currentStanza);
				//System.out.println();
			}

			String uline = m.group(1).toLowerCase();
			//System.out.println(line);
			if(uline.equals("term")) { 
				currentStanza = new OBOTerm();
			} else if (uline.equals("typedef")) { 
				currentStanza = new OBOTypedef();
			} else if (uline.equals("individual")) { 
				currentStanza = new OBOIndividual();
			} else { 
				throw new IllegalArgumentException(line);
			}
			
		} else if(line.length() > 0) { 
			//System.out.println(String.format("\t%s", line));
			if (!line.trim().startsWith("!")){ // A line starting with "!" is a comment.
				int idx = line.indexOf(":");
				String key = line.substring(0, idx).trim();
				String valueString = line.substring(idx+1, line.length());
				OBOValue value = new OBOValue(valueString);
				currentStanza.addValue(key, value);
			}
		}
	}
	
}
