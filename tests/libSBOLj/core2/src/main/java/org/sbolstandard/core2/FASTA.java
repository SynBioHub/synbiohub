package org.sbolstandard.core2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;

/**
 * Methods to convert FASTA to/from SBOL Sequences
 * @author Chris Myers
 * @author Ernst Oberortner
 * @version 2.1
 */
class FASTA {

	// "look-ahead" line
	private static String nextLine = null;
	private static final int lineWidth = 80;

	//private static int lineCounter = 0;
	
	private static void writeFASTALine(Writer w, String line, int margin) throws IOException {
		if (line.length() < margin) {
			w.write(line+"\n");
		} else {
			String spaces = "";
			int breakPos = line.substring(0,margin-1).lastIndexOf(" ")+1;
			if (breakPos==0 || breakPos < 0.75*margin) breakPos = margin-1;
			w.write(line.substring(0, breakPos)+"\n");
			int i = breakPos;
			while (i < line.length()) {
				if ((i+(margin)) < line.length()) {
					breakPos = line.substring(i,i+(margin)-1).lastIndexOf(" ")+1;
					if (breakPos==0 || breakPos < 0.65*margin) breakPos = (margin)-1;
					w.write(spaces+line.substring(i,i+breakPos)+"\n");
				} else {
					w.write(spaces+line.substring(i)+"\n");
					breakPos = (margin)-1;
				}
				i+=breakPos;
			}
		}
	}

	/**
	 * Serializes all Sequence in an SBOLDocument to the given output stream in FASTA format.
	 * @param document a given SBOLDocument
	 * @param out the output stream to serialize into
	 * @throws IOException input/output operation failed
	 * @throws SBOLConversionException violates conversion limitations
	 */
	static void write(SBOLDocument document, OutputStream out) throws IOException, SBOLConversionException {
		Writer w = new OutputStreamWriter(out, "UTF-8");
		for (Sequence sequence : document.getSequences()) {
			write(w,sequence);
		}
		w.close();
	}
	
	private static void write(Writer w, Sequence sequence) throws IOException, SBOLConversionException {
		if (!sequence.getEncoding().equals(Sequence.IUPAC_DNA) &&
				!sequence.getEncoding().equals(Sequence.IUPAC_RNA) &&
				!sequence.getEncoding().equals(Sequence.IUPAC_PROTEIN)) {
			throw new SBOLConversionException("Sequence encoding is not in IUPAC DNA, RNA, or Protein formats.");
		}
		if (sequence.isSetDescription()) {
			w.write("> " + sequence.getDisplayId() + " : " + sequence.getDescription() + "\n");
		} else {
			w.write("> " + sequence.getDisplayId() + "\n");
		}
		writeFASTALine(w,sequence.getElements(),lineWidth);
	}

	private static String readFASTALine(BufferedReader br) throws IOException {
		String newLine = "";

		if (nextLine == null) {
			newLine = br.readLine();
			//lineCounter ++;

			if (newLine == null) return null;
			newLine = newLine.trim();
		} else {
			newLine = nextLine;
		}

		while (true) {
			nextLine = br.readLine();

			if (nextLine==null) return newLine;
			nextLine = nextLine.trim();
			return newLine;
		}
	}
	
	/**
	 * @param doc
	 * @param URIprefix
	 * @param displayId
	 * @param version
	 * @param elements
	 * @param encoding
	 * @return
	 * @throws SBOLValidationException if an SBOL validation rule was violated in {@link SBOLDocument#createSequence(String, String, String, String, URI)}.
	 */
	private static Sequence createSequence(SBOLDocument doc,String URIprefix,String displayId,String version,
			String elements,URI encoding) throws SBOLValidationException {
		try {
			Sequence sequence = doc.createSequence(URIprefix,displayId,version,elements,encoding);
			return sequence;
		} catch (SBOLValidationException e) {
			if (e.getMessage().contains("sbol-10405")) {
				if (encoding.equals(Sequence.IUPAC_DNA)) {
					Sequence sequence = doc.createSequence(URIprefix,displayId,version,elements,Sequence.IUPAC_PROTEIN);
					return sequence;
				} else {
					Sequence sequence = doc.createSequence(URIprefix,displayId,version,elements,Sequence.IUPAC_DNA);
					return sequence;
				}
			}
			throw new SBOLValidationException(e.getMessage());
		}
	}
	
	/**
	 * @param doc
	 * @param stringBuffer
	 * @param URIprefix
	 * @param version
	 * @param encoding
	 * @throws SBOLValidationException if an SBOL validation rule was violated in {@link #createSequence(SBOLDocument, String, String, String, String, URI)}.
	 * @throws IOException
	 */
	static void read(SBOLDocument doc,String stringBuffer,String URIprefix,String displayId,String version,URI encoding) throws SBOLValidationException, IOException
	{
		// reset the global static variables needed for parsing
		nextLine = null;
		//lineCounter = 0;

		String strLine;
		StringBuilder sbSequence = new StringBuilder();
		String elements = null;
		String description = "";
		boolean sequenceMode = false;
		BufferedReader br = new BufferedReader(new StringReader(stringBuffer));

		while ((strLine = readFASTALine(br)) != null)   {
			strLine = strLine.trim();

			if (strLine.startsWith(">")) {
				if (sequenceMode) {
					sequenceMode = false;
					if (displayId == null || displayId.equals("")) {
						if (description.contains(":")) {
							displayId = description.substring(0, description.indexOf(":")).trim();
							description = description.substring(description.indexOf(":")+1).trim();
						} else {
							displayId = description;
						}
					}
					displayId = URIcompliance.fixDisplayId(displayId);
					Sequence sequence = createSequence(doc,URIprefix,displayId,version,sbSequence.toString(),encoding);
					sequence.setDescription(description);
					displayId = "";
					description = "";
					sbSequence = new StringBuilder();
				}
				description += strLine.replaceFirst(">", "").trim();
			} else if (strLine.startsWith(";")) {
				if (sequenceMode) {
					sequenceMode = false;
					if (displayId == null || displayId.equals("")) {
						if (description.contains(":")) {
							displayId = description.substring(0, description.indexOf(":")).trim();
							description = description.substring(description.indexOf(":")+1).trim();
						} else {
							displayId = description;
						}
					}
					displayId = URIcompliance.fixDisplayId(displayId);
					Sequence sequence = createSequence(doc,URIprefix,displayId,version,sbSequence.toString(),encoding);
					sequence.setDescription(description);
					displayId = "";
					description = "";
					sbSequence = new StringBuilder();
				}
				description += strLine.replaceFirst(";", "").trim();
			} else {
				sequenceMode = true;
				if(elements == null) { elements = new String(""); }
				String[] strSplit = strLine.split(" ");
				for (int i = 0; i < strSplit.length; i++) {
					sbSequence.append(strSplit[i]);
				}
			}
		}
		if (description.contains(":")) {
			if (displayId == null || displayId.equals("")) {
				displayId = description.substring(0, description.indexOf(":")).trim();
			}	
			description = description.substring(description.indexOf(":")+1).trim();
		} else {
			if (displayId == null || displayId.equals("")) {
				displayId = description;
			}	
		}
		displayId = URIcompliance.fixDisplayId(displayId);
		Sequence sequence = createSequence(doc,URIprefix,displayId,version,sbSequence.toString(),encoding);
		sequence.setDescription(description);
		br.close();
	}
	
//	/**
//	 * The read method imports all sequences (represented in FASTA format), stores 
//	 * them in an SBOLDocument object, and returns the SBOLDocument object.
//	 * 
//	 * @param in  ... the input stream that contains the sequences in FASTA format
//	 * @param URIPrefix ... the URI prefix of the sequences
//	 * @param displayId
//	 * @param version ... the version of the sequences
//	 * @param encoding ... the encoding of the sequences (i.e. DNA, RNA, or Protein)
//	 * 
//	 * @return an SBOLDocument object that contains the imported FASTA sequences as SBOL Sequence objects
//	 * @throws SBOLConversionException 
//	 * 
//	 * @throws IOException
//	 * @throws SBOLValidationException
//	 */
//	public static SBOLDocument read(InputStream in,String URIPrefix,String displayId,String version,URI encoding) throws SBOLConversionException, SBOLValidationException, IOException 
//	{
//		
//		/*
//		 * EO: it's unclear how we map the FASTA description to SBOL displayID/description? 
//		 * Shouldn't we just use the FASTA description as both displayID and description?
//		 */
//		
//		SBOLDocument doc = new SBOLDocument();
//		doc.setCreateDefaults(true);
//
//		// check that the caller provided a valid URIprefix
//		if (URIPrefix==null) {
//			throw new SBOLConversionException("No URI prefix has been provided.");
//		}
//		
//		// if the URIprefix is valid, than we set it in the document 
//		doc.setDefaultURIprefix(URIPrefix);
//		
//		// parse the stream's content
//		read(doc,in,URIPrefix,displayId,version,encoding);
//		
//		// lastly, return the SBOLDocument object that contains 
//		// all sequences represented as SBOL objects
//		return doc;
//	}

	
//	/**
//	 * Takes in the given FASTA file and converts the file to an SBOLDocument.
//	 *
//	 * @param file the given FASTA filename
//	 * @param URIprefix the URI prefix used for generated Sequence objects
//	 * @param displayId the base displayId to use for generated Sequence objects (null will use description as id)
//	 * @param version the verison used for generated Sequence objects
//	 * @param encoding the encoding assumed for generated Sequence objects
//	 * @return the converted SBOLDocument instance
//	 * @throws SBOLConversionException violates conversion limitations
//	 * @throws SBOLValidationException violates sbol validation rule
//	 * @throws IOException input/output operation failed
//	 */
//	public static SBOLDocument read(File file,String URIprefix,String displayId,String version,URI encoding) 
//			throws IOException, SBOLConversionException, SBOLValidationException
//	{
//		FileInputStream stream     = new FileInputStream(file);
//		BufferedInputStream buffer = new BufferedInputStream(stream);
//		return read(buffer,URIprefix,displayId,version,encoding);
//	}

//	/**
//	 * Takes in the given FASTA filename and converts the file to an SBOLDocument.
//	 *
//	 * @param fileName the given FASTA filename
//	 * @param URIprefix the URI prefix used for generated Sequence objects
//	 * @param displayId the base displayId to use for generated Sequence objects (null will use description as id)
//	 * @param version the verison used for generated Sequence objects
//	 * @param encoding the encoding assumed for generated Sequence objects
//	 * @return the converted SBOLDocument
//	 * @throws SBOLConversionException violates conversion limitations
//	 * @throws SBOLValidationException violates sbol validation rule
//	 * @throws IOException input/output operation failed
//	 */
//	public static SBOLDocument read(String fileName,String URIprefix,String displayId,String version,URI encoding) 
//			throws IOException, SBOLConversionException, SBOLValidationException
//	{
//		return read(new File(fileName),URIprefix,displayId,version,encoding);
//	}


//	public static void main(String[] args) throws SBOLConversionException, IOException, SBOLValidationException {
//		SBOLDocument doc = read("/Users/myers/Downloads/sample.fasta","http://dummy.org","dummy","",Sequence.IUPAC_DNA);
//		//doc.write(System.out);
//		write(doc, System.out);
//	}
}
