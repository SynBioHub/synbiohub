package org.sbolstandard.core2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Methods to convert FASTA to/from SBOL Sequences
 * @author Chris Myers
 * @author Ernst Oberortner
 * @version 2.1
 */
class GFF3 {

	public static final String GFF3NAMESPACE = "http://sbolstandard.org/gff3#";
	public static final String GFF3PREFIX = "gff3";
	public static final String SOURCE = "source";
	public static final String SCORE = "score";
	public static final String PHASE = "phase";
	
	// "look-ahead" line
	private static String nextLine = null;
	private static final int lineWidth = 80;

	/**
	 * Serializes all Sequence in an SBOLDocument to the given output stream in FASTA format.
	 * @param document a given SBOLDocument
	 * @param out the output stream to serialize into
	 * @throws IOException input/output operation failed
	 * @throws SBOLConversionException violates conversion limitations
	 */
	static void write(SBOLDocument document, OutputStream out) throws IOException, SBOLConversionException {
		Writer w = new OutputStreamWriter(out, "UTF-8");
		w.write("##gff-version 3\n");
		for (ComponentDefinition componentDefinition : document.getRootComponentDefinitions()) {
			int start = -1;
			int end = -1;
			for (SequenceAnnotation sa : componentDefinition.getSequenceAnnotations()) {
				for (Location loc : sa.getLocations()) {
					if (loc instanceof Range) {
						Range range = (Range)loc;
						if (start==-1 || (start!=-1 && range.getStart()<start)) {
							start = range.getStart();
						}
						if (end==-1 || (end!=-1 && range.getEnd()>end)){
							end = range.getEnd();
						}
					}
				}
			}
			w.write("##sequence-region " + componentDefinition.getDisplayId() + " " + start + " " + end + "\n");
		}
		for (ComponentDefinition componentDefinition : document.getRootComponentDefinitions()) {
			write(w,componentDefinition,componentDefinition.getDisplayId(),null,0,true,0);
		}
		w.close();
	}
	
	private static void write(Writer w, ComponentDefinition componentDefinition, String id, String parentId, 
			int offset,	boolean inline, int featureEnd) throws IOException, SBOLConversionException {
		if (!componentDefinition.getTypes().contains(ComponentDefinition.DNA_REGION)) {
			throw new SBOLConversionException("GFF 3 is only supported for DNA components.");
		}
		SequenceOntology so = new SequenceOntology();
		for (SequenceAnnotation sa : componentDefinition.getSequenceAnnotations()) {
			w.write(id + "\t");
			Annotation annotation = sa.getAnnotation(new QName(GFF3NAMESPACE,SOURCE,GFF3PREFIX));
			String source = ".";
			if (annotation!=null) {
				source = annotation.getStringValue();
			}
			w.write(source + "\t");
			String type = "sequence_feature";
			if (sa.isSetComponent()) {
				ComponentDefinition comp = sa.getComponentDefinition();
				if (comp!=null) {
					for (URI role : comp.getRoles()) {
						type = so.getName(role);
						if (type != null) {
							break;
						}
					}
				}
			} else {
				for (URI role : sa.getRoles()) {
					type = so.getName(role);
					if (type != null) {
						break;
					}
				}
			}
			w.write(type + "\t");
			for (Location location : sa.getLocations()) {
				if (location instanceof Range) {
					Range range = (Range)location;
					int start = offset+range.getStart();
					int end = offset+range.getEnd();
					if (!inline) {
						int tmpOffset = (featureEnd - (GenBank.getFeatureEnd(sa)+GenBank.getFeatureStart(sa)-1) - offset);
						start = tmpOffset+range.getStart();
						end = tmpOffset+range.getEnd();
					} 
					w.write(start + "\t" + end + "\t");
					annotation = sa.getAnnotation(new QName(GFF3NAMESPACE,SCORE,GFF3PREFIX));
					String score = ".";
					if (annotation!=null) {
						score = annotation.getStringValue();
					}
					w.write(score + "\t");
					if (!range.isSetOrientation()) {
						w.write(".\t");
					} else if (range.getOrientation().equals(OrientationType.INLINE)) {
						w.write("+\t");
					} else if (range.getOrientation().equals(OrientationType.REVERSECOMPLEMENT)) {
						w.write("-\t");
					} else {
						w.write(".\t");
					}
					break;
				}
			}
			annotation = sa.getAnnotation(new QName(GFF3NAMESPACE,PHASE,GFF3PREFIX));
			String phase = "0";
			if (annotation!=null) {
				phase = annotation.getStringValue();
			}
			w.write(phase + "\t");
			String featureId = sa.getDisplayId();
			if (sa.isSetComponent() && sa.getComponentDefinition() != null) {
				featureId = sa.getComponentDefinition().getDisplayId();
			}
			w.write("ID="+featureId);
			if (sa.isSetName()) {
				w.write(";Name="+sa.getName());
			}
			if (parentId!=null) {
				w.write(";Parent="+parentId);
			}
			w.write("\n");
			if (sa.isSetComponent()) {
				ComponentDefinition comp = sa.getComponentDefinition();
				if (comp != null) {
					int newFeatureEnd = featureEnd;
					if (!GenBank.isInlineFeature(sa)) {
						newFeatureEnd = GenBank.getFeatureEnd(sa);
					}
					write(w,comp,id,featureId, offset + GenBank.getFeatureStart(sa)-1,
							!(inline^GenBank.isInlineFeature(sa)), newFeatureEnd);
				}
			}
		}
	}

	private static String readGFF3Line(BufferedReader br) throws IOException {
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
	
	private static ComponentDefinition findParent(SBOLDocument doc, String id) {
		for (ComponentDefinition cd : doc.getComponentDefinitions()) {
			if (cd.getSequenceAnnotation(id) != null) {
				return cd;
			}
		}
		return null;
	}
	
	private static int findOffset(SBOLDocument doc,String parent) {
		int offset = 0;
		ComponentDefinition cd = findParent(doc,parent);
		if (cd != null) {
			SequenceAnnotation sa = cd.getSequenceAnnotation(parent);
			for (Location location : sa.getLocations()) {
				if (location instanceof Range) {
					Range range = (Range)location;
					offset = range.getStart() - 1;
				}
			}
			offset += findOffset(doc,cd.getDisplayId());
		}
		return offset;
	}
	
	private static void addSequenceAnnotation(ComponentDefinition cd, String id, String name, String type, 
			String start, String end, String strand, int offset, String source, String score, String phase) 
					throws SBOLConversionException, SBOLValidationException {
		if (id==null) {
			if (name!=null) { 
				id = URIcompliance.fixDisplayId(name);
			} else {
				id = "SequeanceAnnotation"+start;
			}
		}
		SequenceOntology so = new SequenceOntology();
		URI typeURI = so.getURIbyName(type);
		if (typeURI==null) {
			System.out.println("id = " + id + " name = " + name + " type = " + type + " start = "+start+ " end = " + end);
			throw new SBOLConversionException("Type " + type + " is not a valid Sequence Ontology (SO) term");
		}
		int startInt = Integer.parseInt(start) - offset;
		int endInt = Integer.parseInt(end) - offset;
		SequenceAnnotation sa = cd.getSequenceAnnotation(id);
		if (sa == null) {
			if (strand.equals("+")) {
				sa = cd.createSequenceAnnotation(id, "Range", startInt, endInt, OrientationType.INLINE);
			} else if (strand.equals("-")) {
				sa = cd.createSequenceAnnotation(id, "Range", startInt, endInt, OrientationType.REVERSECOMPLEMENT);
			} else {
				sa = cd.createSequenceAnnotation(id, "Range", startInt, endInt);
			}
			sa.setName(name);
			sa.addRole(typeURI);
			sa.createAnnotation(new QName(GFF3NAMESPACE, SOURCE, GFF3PREFIX), source);
			sa.createAnnotation(new QName(GFF3NAMESPACE, SCORE, GFF3PREFIX), score);
			sa.createAnnotation(new QName(GFF3NAMESPACE, PHASE, GFF3PREFIX), phase);
		} else {
			int i = 1;
			while (sa.getLocation("Range"+i)!=null) i++;
			if (strand.equals("+")) {
				sa.addRange("Range"+i, startInt, endInt, OrientationType.INLINE);
			} else if (strand.equals("-")) {
				sa.addRange("Range"+i, startInt, endInt, OrientationType.REVERSECOMPLEMENT);
			} else {
				sa.addRange("Range"+i, startInt, endInt);
			}
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
	 * @throws SBOLConversionException 
	 */
	static void read(SBOLDocument doc,String stringBuffer,String URIprefix,String version,URI encoding) throws SBOLValidationException, IOException, SBOLConversionException
	{
		Set<ComponentDefinition> parentCDs = new HashSet<>();
		// reset the global static variables needed for parsing
		nextLine = null;
		//lineCounter = 0;

		String strLine;
		BufferedReader br = new BufferedReader(new StringReader(stringBuffer));
		
		while ((strLine = readGFF3Line(br)) != null)   {
			strLine = strLine.trim();
			if (strLine.startsWith("##gff-version 3")) {
				// skip
			} else if (strLine.startsWith("##sequence-region")) {
				String [] splits = strLine.split("\\s+");
				if (splits.length < 4) {
					throw new SBOLConversionException("Misformated sequence region, expected 4 columns:\n"+strLine);
				}
				String id = URIcompliance.fixDisplayId(splits[1]);
				doc.createComponentDefinition(id, version, ComponentDefinition.DNA_REGION);
			} else if (!strLine.startsWith("##")) {
				String [] splits = strLine.split("\\t");
				if (splits.length < 9) {
					throw new SBOLConversionException("Misformated annotation, expected 9 columns:\n"+strLine);
				}
				String seqId = URIcompliance.fixDisplayId(splits[0]);
				String source = splits[1];
				String type = splits[2];
				String start = splits[3];
				String end = splits[4];
				String score = splits[5];
				String strand = splits[6];
				String phase = splits[7];
				String attributesCol = splits[8];
				String id = null;
				String name = null;
				int offset = 0;
				ComponentDefinition cd = doc.getComponentDefinition(seqId, version);
				ComponentDefinition parentCD = null;
				if (cd==null) {
					throw new SBOLConversionException("Sequence region missing for sequence " + seqId);
				}
				String [] attributes = attributesCol.split(";");
				parentCDs.clear();
				for (String attribute : attributes) {
					if (attribute.startsWith("ID=")) {
						id = attribute.replace("ID=", "");
					} else if (attribute.startsWith("Name=")) {
						name = attribute.replace("Name=", "");
					} else if (attribute.startsWith("Parent=")) {
						String [] parents = attribute.replace("Parent=", "").split(",");
						for (String parent : parents) {
							parentCD = doc.getComponentDefinition(parent, version);
							if (parentCD == null) {
								cd = findParent(doc,parent);
								SequenceAnnotation sa = cd.getSequenceAnnotation(parent);
								parentCD = doc.createComponentDefinition(parent, version, ComponentDefinition.DNA_REGION);
								parentCD.setRoles(sa.getRoles());
								sa.clearRoles();
								cd.createComponent(parent+"_comp", AccessType.PUBLIC, parentCD.getDisplayId());
								sa.setComponent(parent+"_comp");
							}
							//offset = findOffset(doc,parent);
							parentCDs.add(parentCD);
							//cd = parentCD;
						}
					}
				}
				if (parentCDs.size() > 0) {
					for (ComponentDefinition pCD : parentCDs) {
						offset = findOffset(doc,pCD.getDisplayId());
						addSequenceAnnotation(pCD, id, name, type, start, end, strand, offset, source, score, phase); 
					}
				} else {
					addSequenceAnnotation(cd, id, name, type, start, end, strand, 0, source, score, phase); 
				}
			}
		}
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
