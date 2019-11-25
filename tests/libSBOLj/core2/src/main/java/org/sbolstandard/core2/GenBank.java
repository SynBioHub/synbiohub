package org.sbolstandard.core2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

/**
 * This class provides methods for converting GenBank files to and from SBOL 2.0 files.
 * @author Chris Myers
 * @author Ernst Oberortner
 * @version 2.1
 */
class GenBank {

	private static SequenceOntology so = null;

	public static final String GBPREFIX = "genbank";
	public static final String GBNAMESPACE = "http://www.ncbi.nlm.nih.gov/genbank#";
	public static final String LOCUS = "locus";
	public static final String REGION = "region";
	public static final String MOLECULE = "molecule";
	public static final String TOPOLOGY = "topology"; // Only used for backward compatiblity with 2.1.0
	public static final String DIVISION = "division";
	public static final String DATE = "date";
	public static final String GINUMBER = "GInumber";
	public static final String KEYWORDS = "keywords";
	public static final String SOURCE = "source";
	public static final String ORGANISM = "organism";
	public static final String REFERENCE = "reference";
	public static final String NESTEDREFERENCE = "Reference";
	public static final String LABEL = "label";
	public static final String AUTHORS = "authors";
	public static final String TITLE = "title";
	public static final String JOURNAL = "journal";
	public static final String MEDLINE = "medline";
	public static final String PUBMED = "pubmed";
	public static final String COMMENT = "comment";
	public static final String BASECOUNT = "baseCount";
	public static final String FEATURETYPE = "featureType";

	public static final String GBCONVPREFIX = "gbConv";
	public static final String GBCONVNAMESPACE = "http://sbols.org/genBankConversion#";
	public static final String POSITION = "position";
	public static final String STRADLESORIGIN = "stradlesOrigin";
	public static final String STARTLESSTHAN = "startLessThan";
	public static final String ENDGREATERTHAN = "endGreaterThan";
	public static final String SINGLEBASERANGE = "singleBaseRange";
	public static final String MULTIRANGETYPE = "multiRangeType";
	
	// locus line
	protected static final Pattern lp = Pattern.compile("LOCUS\\s+([\\S+\\s]*)\\s+(\\d+)\\s+(bp|BP|aa|AA)\\s{0,4}(([dmsDMS][sS]-)?(\\S+))?\\s*(circular|CIRCULAR|linear|LINEAR)?\\s*(\\S+)?\\s*(\\S+)?$");
	
	private static void writeGenBankLine(Writer w, String line, int margin, int indent) throws IOException {
		if (line.length() < margin) {
			w.write(line+"\n");
		} else {
			String spaces = "";
			for (int i = 0 ; i < indent ; i++) spaces += " ";
			int breakPos = line.substring(0,margin-1).lastIndexOf(" ")+1;
			if (breakPos==0 || breakPos < 0.75*margin) breakPos = margin-1;
			w.write(line.substring(0, breakPos)+"\n");
			int i = breakPos;
			while (i < line.length()) {
				if ((i+(margin-indent)) < line.length()) {
					breakPos = line.substring(i,i+(margin-indent)-1).lastIndexOf(" ")+1;
					if (breakPos==0 || breakPos < 0.65*margin) breakPos = (margin-indent)-1;
					w.write(spaces+line.substring(i,i+breakPos)+"\n");
				} else {
					w.write(spaces+line.substring(i)+"\n");
					breakPos = (margin-indent)-1;
				}
				i+=breakPos;
			}
		}
	}
	
	private static void writeComponentDefinition(ComponentDefinition componentDefinition, Writer w) throws IOException, SBOLConversionException {
		so = new SequenceOntology();
		Sequence seq = null;
		for (Sequence sequence : componentDefinition.getSequences()) {
			if (sequence.getEncoding().equals(Sequence.IUPAC_DNA)||
					sequence.getEncoding().equals(Sequence.IUPAC_RNA)) {
				seq = sequence;
				break;
			}
		}
		if (seq == null) {
			throw new SBOLConversionException("ComponentDefinition " + componentDefinition.getIdentity() +
								" does not have an IUPAC sequence.");
		}
		int size = seq.getElements().length();
		writeHeader(w,componentDefinition,size);
		writeReferences(w,componentDefinition);
		writeComment(w,componentDefinition);
		w.write("FEATURES             Location/Qualifiers\n");
		recurseComponentDefinition(componentDefinition,w,0,true,0);
		w.write("ORIGIN\n");
		writeSequence(w,seq,size);
		w.write("//\n");
	}

	/**
	 * Serializes a given ComponentDefinition and outputs the data from the serialization to the given output stream
	 * in GenBank format.
	 * @param componentDefinition a given ComponentDefinition
	 * @param out the given output file name in GenBank format
	 * @throws IOException input/output operation failed
	 * @throws SBOLConversionException violates conversion limitations
	 */
	private static void write(ComponentDefinition componentDefinition, Writer w) throws IOException, SBOLConversionException {
		writeComponentDefinition(componentDefinition,w);
	}
	
	/**
	 * Serializes a given SBOLDocument and outputs the data from the serialization to the given output stream
	 * in GenBank format.
	 * @param sbolDocument a given SBOLDocument
	 * @param out the given output file name in GenBank format
	 * @throws IOException input/output operation failed
	 * @throws SBOLConversionException violates conversion limitations
	 */
	static void write(SBOLDocument sbolDocument, OutputStream out) throws IOException, SBOLConversionException {
		Writer w = new OutputStreamWriter(out, "UTF-8");
		for (ComponentDefinition componentDefinition : sbolDocument.getRootComponentDefinitions()) {
			write(componentDefinition,w);
		}
		w.close();
	}

	private static String convertSOtoGenBank(String soTerm) {
		if (soTerm.equals("SO:0001023")) {return String.format("%-15s", "allele");}
		if (soTerm.equals("SO:0000730")) {return String.format("%-15s", "assembly_gap");}
		if (soTerm.equals("SO:0002174")) {return String.format("%-15s", "assembly_gap");}
		if (soTerm.equals("SO:0000140")) {return String.format("%-15s", "attenuator");}
		if (soTerm.equals("SO:0001834")) {return String.format("%-15s", "C_region");}
		if (soTerm.equals("SO:0000172")) {return String.format("%-15s", "CAAT_signal");}
		if (soTerm.equals("SO:0000316")) {return String.format("%-15s", "CDS");}
		if (soTerm.equals("SO:0000577")) {return String.format("%-15s", "centromere");}
		//if (soTerm.equals("SO:")) {return String.format("%-15s", "conflict");}
		if (soTerm.equals("SO:0000297")) {return String.format("%-15s", "D-loop");}
		if (soTerm.equals("SO:0000458")) {return String.format("%-15s", "D_segment");}
		if (soTerm.equals("SO:0000165")) {return String.format("%-15s", "enhancer");}
		if (soTerm.equals("SO:0000147")) {return String.format("%-15s", "exon");}
		if (soTerm.equals("SO:0000730")) {return String.format("%-15s", "gap");} // TODO: alias with assembly_gap
		if (soTerm.equals("SO:0000704")) {return String.format("%-15s", "gene");}
		if (soTerm.equals("SO:0000173")) {return String.format("%-15s", "GC_signal");}
		if (soTerm.equals("SO:0000723")) {return String.format("%-15s", "iDNA");}
		if (soTerm.equals("SO:0000188")) {return String.format("%-15s", "intron");}
		if (soTerm.equals("SO:0000470")) {return String.format("%-15s", "J_region");}
		if (soTerm.equals("SO:0000470")) {return String.format("%-15s", "J_segment");} // TODO: alias with J_region
		if (soTerm.equals("SO:0000286")) {return String.format("%-15s", "LTR");}
		if (soTerm.equals("SO:0000419")) {return String.format("%-15s", "mat_peptide");}
		if (soTerm.equals("SO:0000409")) {return String.format("%-15s", "misc_binding");}
		if (soTerm.equals("SO:0000413")) {return String.format("%-15s", "misc_difference");}
		if (soTerm.equals("SO:0000001")) {return String.format("%-15s", "misc_feature");}
		if (soTerm.equals("SO:0001411")) {return String.format("%-15s", "misc_feature");}
		if (soTerm.equals("SO:0001645")) {return String.format("%-15s", "misc_marker");}
		if (soTerm.equals("SO:0000298")) {return String.format("%-15s", "misc_recomb");}
		if (soTerm.equals("SO:0000233")) {return String.format("%-15s", "misc_RNA");}
		if (soTerm.equals("SO:0000673")) {return String.format("%-15s", "misc_RNA");}
		if (soTerm.equals("SO:0001411")) {return String.format("%-15s", "misc_signal");}
		if (soTerm.equals("SO:0005836")) {return String.format("%-15s", "regulatory");}
		if (soTerm.equals("SO:0000002")) {return String.format("%-15s", "misc_structure");}
		if (soTerm.equals("SO:0001037")) {return String.format("%-15s", "mobile_element");}
		if (soTerm.equals("SO:0000305")) {return String.format("%-15s", "modified_base");}
		if (soTerm.equals("SO:0000234")) {return String.format("%-15s", "mRNA");}
		//if (soTerm.equals("SO:")) {return String.format("%-15s", "mutation");}
		if (soTerm.equals("SO:0001835")) {return String.format("%-15s", "N_region");}
		//if (soTerm.equals("SO:")) {return String.format("%-15s", "old_sequence");}
		if (soTerm.equals("SO:0000655")) {return String.format("%-15s", "ncRNA");}
		if (soTerm.equals("SO:0000178")) {return String.format("%-15s", "operon");}
		if (soTerm.equals("SO:0000724")) {return String.format("%-15s", "oriT");}
		if (soTerm.equals("SO:0000551")) {return String.format("%-15s", "polyA_signal");}
		if (soTerm.equals("SO:0000553")) {return String.format("%-15s", "polyA_site");}
		if (soTerm.equals("SO:0000185")) {return String.format("%-15s", "precursor_RNA");}
		if (soTerm.equals("SO:0000185")) {return String.format("%-15s", "prim_transcript");}
		// NOTE: redundant with line above
		if (soTerm.equals("SO:0000112")) {return String.format("%-15s", "primer");}
		if (soTerm.equals("SO:0005850")) {return String.format("%-15s", "primer_bind");}
		if (soTerm.equals("SO:0000167")) {return String.format("%-15s", "promoter");}
		if (soTerm.equals("SO:0001062")) {return String.format("%-15s", "propeptide");}
		if (soTerm.equals("SO:0000410")) {return String.format("%-15s", "protein_bind");}
		if (soTerm.equals("SO:0000139") || soTerm.equals("SO:0000552")) {return String.format("%-15s", "RBS");}
		if (soTerm.equals("SO:0000296")) {return String.format("%-15s", "rep_origin");}
		if (soTerm.equals("SO:0000657")) {return String.format("%-15s", "repeat_region");}
		if (soTerm.equals("SO:0000726")) {return String.format("%-15s", "repeat_unit");}
		if (soTerm.equals("SO:0000252")) {return String.format("%-15s", "rRNA");}
		if (soTerm.equals("SO:0001836")) {return String.format("%-15s", "S_region");}
		if (soTerm.equals("SO:0000005")) {return String.format("%-15s", "satellite");}
		if (soTerm.equals("SO:0000013")) {return String.format("%-15s", "scRNA");}
		if (soTerm.equals("SO:0000418")) {return String.format("%-15s", "sig_peptide");}
		if (soTerm.equals("SO:0000274")) {return String.format("%-15s", "snRNA");}
		if (soTerm.equals("SO:0000149")) {return String.format("%-15s", "source");}
		if (soTerm.equals("SO:0002206")) {return String.format("%-15s", "source");}
		if (soTerm.equals("SO:0000019")) {return String.format("%-15s", "stem_loop");}
		if (soTerm.equals("SO:0000313")) {return String.format("%-15s", "stem_loop");}
		if (soTerm.equals("SO:0000331")) {return String.format("%-15s", "STS");}
		if (soTerm.equals("SO:0000174")) {return String.format("%-15s", "TATA_signal");}
		if (soTerm.equals("SO:0000624")) {return String.format("%-15s", "telomere");}
		if (soTerm.equals("SO:0000141")) {return String.format("%-15s", "terminator");}
		if (soTerm.equals("SO:0000584")) {return String.format("%-15s", "tmRNA");}
		if (soTerm.equals("SO:0000725")) {return String.format("%-15s", "transit_peptide");}
		if (soTerm.equals("SO:0001054")) {return String.format("%-15s", "transposon");}
		if (soTerm.equals("SO:0000253")) {return String.format("%-15s", "tRNA");}
		if (soTerm.equals("SO:0001086")) {return String.format("%-15s", "unsure");}
		if (soTerm.equals("SO:0001833")) {return String.format("%-15s", "V_region");}
		if (soTerm.equals("SO:0000109")) {return String.format("%-15s", "variation");}
		if (soTerm.equals("SO:0001060")) {return String.format("%-15s", "variation");}
		if (soTerm.equals("SO:0000466")) {return String.format("%-15s", "V_segment");}
		if (soTerm.equals("SO:0000175")) {return String.format("%-15s", "-10_signal");}
		if (soTerm.equals("SO:0000176")) {return String.format("%-15s", "-35_signal");}
		if (soTerm.equals("SO:0000557")) {return String.format("%-15s", "3'clip");}
		if (soTerm.equals("SO:0000205")) {return String.format("%-15s", "3'UTR");}
		if (soTerm.equals("SO:0000555")) {return String.format("%-15s", "5'clip");}
		if (soTerm.equals("SO:0000204")) {return String.format("%-15s", "5'UTR");}
		/*
		if (soTerm.equals("CDS") || soTerm.equals("promoter") || soTerm.equals("terminator"))
			return String.format("%-15s", soTerm);
		else if (soTerm.equals("ribosome_entry_site"))
			return "RBS            ";
		 */
		return "misc_feature   ";
	}

	private static URI convertGenBanktoSO(String genBankTerm) {
		if (genBankTerm.equals("allele")) {
			return so.getURIbyId("SO:0001023");}
		if (genBankTerm.equals("assembly_gap")) {
			return so.getURIbyId("SO:0000730");}
		if (genBankTerm.equals("attenuator")) {
			return so.getURIbyId("SO:0000140");}
		if (genBankTerm.equals("C_region")) {
			return so.getURIbyId("SO:0001834");}
		if (genBankTerm.equals("CAAT_signal")) {
			return so.getURIbyId("SO:0000172");}
		if (genBankTerm.equals("CDS")) {
			return so.getURIbyId("SO:0000316");}
		if (genBankTerm.equals("centromere")) {
			return so.getURIbyId("SO:0000577");}
		/* if (genBankTerm.equals("conflict")) {
		return so.getURIbyId("SO_");} */
		if (genBankTerm.equals("D-loop")) {
			return so.getURIbyId("SO:0000297");}
		if (genBankTerm.equals("D_segment")) {
			return so.getURIbyId("SO:0000458");}
		if (genBankTerm.equals("enhancer")) {
			return so.getURIbyId("SO:0000165");}
		if (genBankTerm.equals("exon")) {
			return so.getURIbyId("SO:0000147");}
		if (genBankTerm.equals("gap")) {
			return so.getURIbyId("SO:0000730");}
		if (genBankTerm.equals("gene")) {
			return so.getURIbyId("SO:0000704");}
		if (genBankTerm.equals("GC_signal")) {
			return so.getURIbyId("SO:0000173");}
		if (genBankTerm.equals("iDNA")) {
			return so.getURIbyId("SO:0000723");}
		if (genBankTerm.equals("intron")) {
			return so.getURIbyId("SO:0000188");}
		if (genBankTerm.equals("J_region")) {
			return so.getURIbyId("SO:0000470");}
		if (genBankTerm.equals("J_gene_segment")) {
			return so.getURIbyId("SO:0000470");}
		if (genBankTerm.equals("J_segment")) {
			return so.getURIbyId("SO:0000470");}
		if (genBankTerm.equals("LTR")) {
			return so.getURIbyId("SO:0000286");}
		if (genBankTerm.equals("mat_peptide")) {
			return so.getURIbyId("SO:0000419");}
		if (genBankTerm.equals("misc_binding")) {
			return so.getURIbyId("SO:0000409");}
		if (genBankTerm.equals("misc_difference")) {
			return so.getURIbyId("SO:0000413");}
		if (genBankTerm.equals("misc_feature")) {
			return so.getURIbyId("SO:0001411");}
//		return so.getURIbyId("SO:0000001");}
		if (genBankTerm.equals("misc_marker")) {
			return so.getURIbyId("SO:0001645");}
		if (genBankTerm.equals("misc_recomb")) {
			return so.getURIbyId("SO:0000298");}
		if (genBankTerm.equals("misc_RNA")) {
			return so.getURIbyId("SO:0000673");}
//		return so.getURIbyId("SO:0000233");}
		if (genBankTerm.equals("misc_signal")) {
			return so.getURIbyId("SO:0001411");}
		if (genBankTerm.equals("misc_structure")) {
			return so.getURIbyId("SO:0000002");}
		if (genBankTerm.equals("mobile_element")) {
			return so.getURIbyId("SO:0001037");}
		if (genBankTerm.equals("mobile_genetic_element")) {
			return so.getURIbyId("SO:0001037");}
		if (genBankTerm.equals("modified_base")) {
			return so.getURIbyId("SO:0000305");}
		if (genBankTerm.equals("mRNA")) {
			return so.getURIbyId("SO:0000234");}
		/* if (genBankTerm.equals("mutation")) {
		return so.getURIbyId("SO_");} */
		if (genBankTerm.equals("N_region")) {
			return so.getURIbyId("SO:0001835");}
		if (genBankTerm.equals("old_sequence")) {
			return so.getURIbyId("SO:0000413");} // TODO: alias with misc_difference
		if (genBankTerm.equals("ncRNA")) {
			return so.getURIbyId("SO:0000655");}
		if (genBankTerm.equals("operon")) {
			return so.getURIbyId("SO:0000178");}
		if (genBankTerm.equals("oriT")) {
			return so.getURIbyId("SO:0000724");}
		if (genBankTerm.equals("polyA_signal")) {
			return so.getURIbyId("SO:0000551");}
		if (genBankTerm.equals("polyA_site")) {
			return so.getURIbyId("SO:0000553");}
		if (genBankTerm.equals("precursor_RNA")) {
			return so.getURIbyId("SO:0000185");}
		if (genBankTerm.equals("prim_transcript")) {
			return so.getURIbyId("SO:0000185");}
		if (genBankTerm.equals("primer")) {
			return so.getURIbyId("SO:0000112");}
		if (genBankTerm.equals("primer_bind")) {
			return so.getURIbyId("SO:0005850");}
		if (genBankTerm.equals("promoter")) {
			return so.getURIbyId("SO:0000167");}
		if (genBankTerm.equals("promoter")) {
			return so.getURIbyId("SO:0000167");}
		if (genBankTerm.equals("propeptide")) {
			return so.getURIbyId("SO:0001062");}
		if (genBankTerm.equals("RBS")) {
			return so.getURIbyId("SO:0000139");}
		if (genBankTerm.equals("rep_origin")) {
			return so.getURIbyId("SO:0000296");}
		if (genBankTerm.equals("repeat_region")) {
			return so.getURIbyId("SO:0000657");}
		if (genBankTerm.equals("repeat_unit")) {
			return so.getURIbyId("SO:0000726");}
		if (genBankTerm.equals("rRNA")) {
			return so.getURIbyId("SO:0000252");}
		if (genBankTerm.equals("S_region")) {
			return so.getURIbyId("SO:0001836");}
		if (genBankTerm.equals("satellite")) {
			return so.getURIbyId("SO:0000005");}
		if (genBankTerm.equals("scRNA")) {
			return so.getURIbyId("SO:0000013");}
		if (genBankTerm.equals("sig_peptide")) {
			return so.getURIbyId("SO:0000418");}
		if (genBankTerm.equals("snRNA")) {
			return so.getURIbyId("SO:0000274");}
		if (genBankTerm.equals("source")) {
			return so.getURIbyId("SO:0002206");}
//		return so.getURIbyId("SO:0000149");}
		if (genBankTerm.equals("stem_loop")) {
			return so.getURIbyId("SO:0000313");}
		if (genBankTerm.equals("STS")) {
			return so.getURIbyId("SO:0000331");}
		if (genBankTerm.equals("TATA_signal")) {
			return so.getURIbyId("SO:0000174");}
		if (genBankTerm.equals("telomere")) {
			return so.getURIbyId("SO:0000624");}
		if (genBankTerm.equals("terminator")) {
			return so.getURIbyId("SO:0000141");}
		if (genBankTerm.equals("tmRNA")) {
			return so.getURIbyId("SO:0000584");}
		if (genBankTerm.equals("transit_peptide")) {
			return so.getURIbyId("SO:0000725");}
		if (genBankTerm.equals("transposon")) {
			return so.getURIbyId("SO:0001054");}
		if (genBankTerm.equals("tRNA")) {
			return so.getURIbyId("SO:0000253");}
		if (genBankTerm.equals("sequence_uncertainty")) {
			return so.getURIbyId("SO:0001086");}
		if (genBankTerm.equals("unsure")) {
			return so.getURIbyId("SO:0001086");}
		if (genBankTerm.equals("V_region")) {
			return so.getURIbyId("SO:0001833");}
		if (genBankTerm.equals("variation")) {
			return so.getURIbyId("SO:0001060");}
		if (genBankTerm.equals("-10_signal")) {
			return so.getURIbyId("SO:0000175");}
		if (genBankTerm.equals("-35_signal")) {
			return so.getURIbyId("SO:0000176");}
		if (genBankTerm.equals("3'clip")) {
			return so.getURIbyId("SO:0000557");}
		if (genBankTerm.equals("3'UTR")) {
			return so.getURIbyId("SO:0000205");}
		if (genBankTerm.equals("5'clip")) {
			return so.getURIbyId("SO:0000555");}
		if (genBankTerm.equals("5'UTR")) {
			return so.getURIbyId("SO:0000204");}
		if (genBankTerm.equals("regulatory")) {
			return so.getURIbyId("SO:0005836");}
		if (genBankTerm.equals("snoRNA")) {
			return so.getURIbyId("SO:0000275");}
		if (genBankTerm.equals("V_gene_segment")) {
			return so.getURIbyId("SO:0000466");}
		if (genBankTerm.equals("V_segment")) {
			return so.getURIbyId("SO:0000466");}
		return so.getURIbyId("SO:0000110");
		//return null;
		/*
		URI soTerm = so.getURIbyName(genBankTerm);
		if (soTerm==null && genBankTerm.equals("misc_feature")) {
			soTerm = SequenceOntology.ENGINEERED_REGION;
		}
		return soTerm;
		 */
	}

	private static void writeHeader(Writer w,ComponentDefinition componentDefinition,int size) throws SBOLConversionException, IOException {
		String locus = componentDefinition.getDisplayId().substring(0, 
				componentDefinition.getDisplayId().length()>15?15:componentDefinition.getDisplayId().length());
		Annotation annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE,LOCUS,GBPREFIX));
		if (annotation!=null) {
			locus = annotation.getStringValue();
		}
		String type = null;
		for (URI typeURI : componentDefinition.getTypes()) {
			if (typeURI.equals(ComponentDefinition.RNA_REGION)) {
				type = "RNA";
				break;
			} else if (typeURI.equals(ComponentDefinition.DNA_REGION)) {
				type = "DNA";
			}
		}
		if (type == null) {
			throw new SBOLConversionException("ComponentDefinition " + componentDefinition.getIdentity() +
							" is not DNA or RNA type.");
		}
		annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE,MOLECULE,GBPREFIX));
		if (annotation!=null) {
			type = annotation.getStringValue();
		}
		String linCirc = "linear";
		// Below only needed for backwards compatibility with 2.1.0 converter.
		annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE,TOPOLOGY,GBPREFIX));
		if (annotation!=null) {
			linCirc = annotation.getStringValue();
		}
		if (componentDefinition.containsType(SequenceOntology.CIRCULAR)) {
			linCirc = "circular";
		}
		if (componentDefinition.containsType(SequenceOntology.LINEAR)) {
			linCirc = "linear";
		}
		String division = "   "; //UNK";
		annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE,DIVISION,GBPREFIX));
		if (annotation!=null) {
			division = annotation.getStringValue();
		}
		DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
		Date dateobj = new Date();
		String date = df.format(dateobj);
		annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE,DATE,GBPREFIX));
		if (annotation!=null) {
			date = annotation.getStringValue();
		}
		String locusLine = "LOCUS       " + String.format("%-16s", locus) + " " +
				String.format("%11s", ""+size) + " bp " + "   " + String.format("%-6s", type) + "  " +
				String.format("%-8s", linCirc) + " " + division + " " + date + "\n";
		w.write(locusLine);
		if (componentDefinition.isSetDescription()) {
			writeGenBankLine(w,"DEFINITION  " + componentDefinition.getDescription(),80,12);
		}
		String region = "";
		annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE,REGION,GBPREFIX));
		if (annotation!=null) {
			region = annotation.getStringValue();
			w.write("ACCESSION   " + componentDefinition.getDisplayId() + " REGION: " + region + "\n");
		} else {
			w.write("ACCESSION   " + componentDefinition.getDisplayId() + "\n");
		}
		if (componentDefinition.isSetVersion()) {
			String giNumber = "";
			annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE,GINUMBER,GBPREFIX));
			if (annotation!=null) {
				giNumber = annotation.getStringValue();
			}
			w.write("VERSION     " + componentDefinition.getDisplayId() + "." +
					componentDefinition.getVersion() + "  " + giNumber + "\n");
		}
		annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE,KEYWORDS,GBPREFIX));
		if (annotation!=null) {
			w.write("KEYWORDS    " + annotation.getStringValue() + "\n");
		}
		annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE,SOURCE,GBPREFIX));
		if (annotation!=null) {
			w.write("SOURCE      " + annotation.getStringValue() + "\n");
		}
		annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE,ORGANISM,GBPREFIX));
		if (annotation!=null) {
			writeGenBankLine(w,"  ORGANISM  " + annotation.getStringValue(),80,12);
		}
	}

	private static void writeReferences(Writer w,ComponentDefinition componentDefinition) throws IOException {
		for (Annotation a : componentDefinition.getAnnotations()) {
			if (a.getQName().equals(new QName(GBNAMESPACE,REFERENCE,GBPREFIX))) {
				String label = null;
				String authors = null;
				String title = null;
				String journal = null;
				String medline = null;
				String pubmed = null;
				for (Annotation ref : a.getAnnotations()) {
					if (ref.getQName().equals(new QName(GBNAMESPACE,LABEL,GBPREFIX))) {
						label = ref.getStringValue();
					} else if (ref.getQName().equals(new QName(GBNAMESPACE,AUTHORS,GBPREFIX))) {
						authors = ref.getStringValue();
					} else if (ref.getQName().equals(new QName(GBNAMESPACE,TITLE,GBPREFIX))) {
						title = ref.getStringValue();
					} else if (ref.getQName().equals(new QName(GBNAMESPACE,JOURNAL,GBPREFIX))) {
						journal = ref.getStringValue();
					} else if (ref.getQName().equals(new QName(GBNAMESPACE,MEDLINE,GBPREFIX))) {
						medline = ref.getStringValue();
					} else if (ref.getQName().equals(new QName(GBNAMESPACE,PUBMED,GBPREFIX))) {
						pubmed = ref.getStringValue();
					}
				}
				if (label != null) {
					writeGenBankLine(w,"REFERENCE   " + label,80,12);
					if (authors != null) {
						writeGenBankLine(w,"  AUTHORS   " + authors,80,12);
					}
					if (title != null) {
						writeGenBankLine(w,"  TITLE     " + title,80,12);
					}
					if (journal != null) {
						writeGenBankLine(w,"  JOURNAL   " + journal,80,12);
					}
					if (medline != null) {
						writeGenBankLine(w,"   MEDLINE  " + medline,80,12);
					}
					if (pubmed != null) {
						writeGenBankLine(w,"   PUBMED   " + pubmed,80,12);
					}
				}
			}
		}
	}

	private static void writeComment(Writer w,ComponentDefinition componentDefinition) throws IOException {
		Annotation annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE,COMMENT,GBPREFIX));
		if (annotation != null) {
			String[] comments = annotation.getStringValue().split("\n ");
			for (String comment : comments) {
				w.write("COMMENT     " + comment+"\n");
			}
		}
	}
//	
//	private static String startStr(Range range,int offset) {
//		if (range.getAnnotation(new QName(GBNAMESPACE,STARTLESSTHAN,GBPREFIX))!=null) {
//			return "<"+(offset+range.getStart());
//		}
//		return ""+(offset+range.getStart());
//	}
//	
//	private static String rangeType(Range range) {
//		if (range.getAnnotation(new QName(GBNAMESPACE,SINGLEBASERANGE,GBPREFIX))!=null) {
//			return ".";
//		}
//		return "..";
//	}
//	
//	private static String endStr(Range range,int offset) {
//		if (range.getAnnotation(new QName(GBNAMESPACE,ENDGREATERTHAN,GBPREFIX))!=null) {
//			return ">"+(offset+range.getEnd());
//		}
//		return ""+(offset+range.getEnd());
//	}
//	
	private static String locationStr(Location location,int offset,boolean complement,Location location2) throws SBOLConversionException {
		int start; 
		int end;
		String locationStr = "";
		boolean isCut = false;
		if (location instanceof Range) {
			Range range = (Range)location;
			start = offset+range.getStart();
			end = offset+range.getEnd();
		} else if (location instanceof Cut) {
			Cut cut = (Cut)location;
			start = offset+cut.getAt();
			end = offset+cut.getAt()+1;
			isCut = true;
		} else {
			throw new SBOLConversionException("Location "+location.getIdentity()+" is not range or cut.");
		}
		if (location2!=null) {
			if (location2 instanceof Range) {
				Range range = (Range)location2;
				end = offset+range.getEnd();
			} else if (location2 instanceof Cut) {
				Cut cut = (Cut)location2;
				end = offset+cut.getAt()+1;
			} 			
		}
		if (complement) {
			locationStr += "complement(";
		}
		if (location.getAnnotation(new QName(GBCONVNAMESPACE,STARTLESSTHAN,GBCONVPREFIX))!=null) {
			locationStr += "<";
		}
		locationStr += start;
		if (isCut) {
			locationStr += "^";
		} else if (location.getAnnotation(new QName(GBCONVNAMESPACE,SINGLEBASERANGE,GBCONVPREFIX))!=null) {
			locationStr += ".";
		} else {
			locationStr += "..";
		}
		if (location.getAnnotation(new QName(GBCONVNAMESPACE,ENDGREATERTHAN,GBCONVPREFIX))!=null) {
			locationStr += ">";
		}
		locationStr += end;
		if (complement) {
			locationStr += ")";
		}
		return locationStr;
	}
	
	private static boolean stradlesOrigin(SequenceAnnotation sa) {
		Annotation annotation = sa.getAnnotation(new QName(GBCONVNAMESPACE,STRADLESORIGIN,GBCONVPREFIX));
		if (annotation!=null) {
			return true;
		}
		return false;
	}

	private static void writeFeature(Writer w,SequenceAnnotation sa,String role,int offset,boolean inline) 
			throws IOException, SBOLConversionException {
		if (sa.getPreciseLocations().size()==0) {
			throw new SBOLConversionException("SequenceAnnotation "+sa.getIdentity()+" has no range/cut locations.");
		} else if (sa.getPreciseLocations().size()==1) {
			Location loc = sa.getPreciseLocations().iterator().next();
			boolean locReverse = false;
			if (loc.isSetOrientation()) {
				locReverse = loc.getOrientation().equals(OrientationType.REVERSECOMPLEMENT);
			}
			w.write("     " + role + " " + locationStr(loc,offset,
					((inline && locReverse)||
					(!inline && !locReverse)),null)+"\n");
		} else if (stradlesOrigin(sa)) {
			Location loc = sa.getLocation("range0");
			Location loc2 = sa.getLocation("range1");
			boolean locReverse = false;
			if (loc.isSetOrientation()) {
				locReverse = loc.getOrientation().equals(OrientationType.REVERSECOMPLEMENT);
			}
			w.write("     " + role + " " + locationStr(loc,offset,
					((inline && locReverse)||
					(!inline && !locReverse)),loc2)+"\n");			
		} else {
			String multiType = "join";
			Annotation annotation = sa.getAnnotation(new QName(GBNAMESPACE,MULTIRANGETYPE,GBCONVPREFIX));
			if (annotation!=null) {
				multiType = annotation.getStringValue();
			}
			String rangeStr = "     " + role + " " + multiType + "(";
			boolean first = true;
			for (Location loc : sa.getSortedLocations()) {
				if (!first) rangeStr += ",";
				else first = false;
				boolean locReverse = false;
				if (loc.isSetOrientation()) {
					locReverse = loc.getOrientation().equals(OrientationType.REVERSECOMPLEMENT);
				}
				rangeStr += locationStr(loc,offset,
						((inline && locReverse)||(!inline && !locReverse)),null);
			}
			rangeStr += ")";
			writeGenBankLine(w,rangeStr,80,21);
		}
		boolean foundLabel = false;
		for (Annotation a : sa.getAnnotations()) {
			if (a.getQName().getLocalPart().equals("multiRangeType")) continue;
			if (a.getQName().getLocalPart().equals("label")) foundLabel = true;
			if (a.getQName().getLocalPart().equals("organism")) foundLabel = true;
			if (a.getQName().getLocalPart().equals("Apeinfo_label")) foundLabel = true;
			if (a.getQName().getLocalPart().equals("product")) foundLabel = true;
			if (a.getQName().getLocalPart().equals("gene")) foundLabel = true;
			if (a.getQName().getLocalPart().equals("note")) foundLabel = true;
			if (a.isStringValue()) {
				try {
					int aInt = Integer.parseInt(a.getStringValue());
					writeGenBankLine(w,"                     /"+
							a.getQName().getLocalPart()+"="+
							aInt,80,21);
				} catch (NumberFormatException e) {
					writeGenBankLine(w,"                     /"+
							a.getQName().getLocalPart()+"="+
							"\"" + a.getStringValue() + "\"",80,21);
				}
			} else if (a.isIntegerValue()) {
				writeGenBankLine(w,"                     /"+
						a.getQName().getLocalPart()+"="+
						a.getIntegerValue(),80,21);
			}
		}
		if (!foundLabel && sa.isSetName()) {
			writeGenBankLine(w,"                     /label="+ sa.getName(),80,21);
		}
	}

	private static void writeSequence(Writer w,Sequence sequence,int size) throws IOException {
		for (int i = 0; i < size; i+=60) {
			String padded = String.format("%9s", "" + (i+1));
			w.write(padded);
			for (int j = i; j < size && j < i + 60; j+=10) {
				if (j+10 < size) {
					w.write(" " + sequence.getElements().substring(j,j+10));
				} else {
					w.write(" " + sequence.getElements().substring(j));
				}
			}
			w.write("\n");
		}
	}
	
	static int getFeatureStart(SequenceAnnotation sa) {
		int featureStart = Integer.MAX_VALUE;
		for (Location location : sa.getPreciseLocations()) {
			if (location instanceof Range) {
				Range range = (Range)location;
				if (range.getStart() < featureStart) {
					featureStart = range.getStart();
				} 
			} else if (location instanceof Cut) {
				Cut cut = (Cut)location;
				if (cut.getAt() < featureStart) {
					featureStart = cut.getAt();
				}
			}
		}
		if (featureStart==Integer.MAX_VALUE) return 1;
		return featureStart;
	}
	
	
	static int getFeatureEnd(SequenceAnnotation sa) {
		int featureEnd = 0;
		for (Location location : sa.getPreciseLocations()) {
			if (location instanceof Range) {
				Range range = (Range)location;
				if (range.getEnd() > featureEnd) {
					featureEnd = range.getEnd();
				} 
			} else if (location instanceof Cut) {
				Cut cut = (Cut)location;
				if (cut.getAt() < featureEnd) {
					featureEnd = cut.getAt();
				}
			}
		}
		//if (featureEnd==Integer.MAX_VALUE) return 1;
		return featureEnd;
	}
	
	// TODO: assumes any complement then entirely complemented, need to fix
	static boolean isInlineFeature(SequenceAnnotation sa) {
		boolean inlineFeature = true;
		for (Location location : sa.getPreciseLocations()) {
			if (location.isSetOrientation() && location.getOrientation().equals(OrientationType.REVERSECOMPLEMENT)) {
				inlineFeature = false;
			}
		}
		return inlineFeature;		
	}

	private static void recurseComponentDefinition(ComponentDefinition componentDefinition, Writer w, int offset,
			boolean inline, int featureEnd) throws IOException, SBOLConversionException {
		for (SequenceAnnotation sa : componentDefinition.getSortedSequenceAnnotationsByDisplayId()) {
			String role = "misc_feature   ";
			Component comp = sa.getComponent();
			if (comp != null) {
				ComponentDefinition compDef = comp.getDefinition();
				if (compDef != null) {
					for (URI roleURI : compDef.getRoles()) {
						String soRole = so.getId(roleURI);
						if (soRole != null) {
							if (soRole=="SO:0000110" && sa.isSetName()) {
								Annotation annotation = sa.getAnnotation(new QName(GBCONVNAMESPACE,FEATURETYPE,GBCONVPREFIX));
								if (annotation!=null) {
									role = annotation.getStringValue();
									for (int i = role.length(); i < 15; i++) {
										role += " ";
									}
								} 
							} else {
								role = convertSOtoGenBank(soRole);
							}
							break;
						}
					}
					int newFeatureEnd = featureEnd;
					if (!isInlineFeature(sa)) {
						newFeatureEnd = getFeatureEnd(sa);
					}
					recurseComponentDefinition(compDef, w, offset + getFeatureStart(sa)-1,
							!(inline^isInlineFeature(sa)),newFeatureEnd);
				}
			} else {
				for (URI roleURI : sa.getRoles()) {
					String soRole = so.getId(roleURI);
					if (soRole != null) {
						if (soRole.equals("SO:0000110") && sa.isSetName()) {
							Annotation annotation = sa.getAnnotation(new QName(GBCONVNAMESPACE,FEATURETYPE,GBCONVPREFIX));
							if (annotation!=null) {
								role = annotation.getStringValue();
								for (int i = role.length(); i < 15; i++) {
									role += " ";
								}
							} 
						} else {
							role = convertSOtoGenBank(soRole);
						}
						break;
					}
				}				
			}
			if (!inline) {
				writeFeature(w,sa,role,(featureEnd - (getFeatureEnd(sa)+getFeatureStart(sa)-1) - offset),inline);
				
			} else {
				writeFeature(w,sa,role,offset,inline);
			}
		}
	}

	// "look-ahead" line
	private static String nextLine = null;

	private static boolean featureMode = false;
	private static boolean originMode = false;

	//private static int lineCounter = 0;

	private static String readGenBankLine(BufferedReader br) throws IOException {
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

			if (featureMode) {
				if (nextLine.startsWith("/")) {
					return newLine;
				}

				String[] strSplit = nextLine.split("\\s+");
				URI role = convertGenBanktoSO(strSplit[0]);

				if (role!=null) return newLine;
			}

			if (originMode) return newLine;
			if (nextLine.startsWith("DEFINITION")) return newLine;
			if (nextLine.startsWith("ACCESSION")) return newLine;
			if (nextLine.startsWith("VERSION")) return newLine;
			if (nextLine.startsWith("KEYWORDS")) return newLine;
			if (nextLine.startsWith("SOURCE")) return newLine;
			if (nextLine.startsWith("ORGANISM")) return newLine;
			if (nextLine.startsWith("REFERENCE")) return newLine;
			if (nextLine.startsWith("COMMENT")) return newLine;
			if (nextLine.startsWith("AUTHORS")) return newLine;
			if (nextLine.startsWith("TITLE")) return newLine;
			if (nextLine.startsWith("JOURNAL")) return newLine;
			if (nextLine.startsWith("MEDLINE")) return newLine;
			if (nextLine.startsWith("PUBMED")) return newLine;
			if (nextLine.startsWith("BASE COUNT")) return newLine;

			if (nextLine.startsWith("FEATURES")) {
				featureMode = true;
				return newLine;
			}
			if (nextLine.startsWith("ORIGIN")) {
				originMode = true;
				return newLine;
			}
			if (featureMode) {
				if (newLine.contains(" ") || nextLine.contains(" ")) {
					newLine += " " + nextLine;
				} else {
					newLine += nextLine;
				}
			} else {
				newLine += " " + nextLine;
			}
			//lineCounter++;
		}
	}

	/**
	 * @param doc
	 * @param topCD
	 * @param type
	 * @param elements
	 * @param version
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following methods:
	 * <ul>
	 * <li>{@link SBOLDocument#createSequence(String, String, String, URI)}, or</li>
	 * <li>{@link ComponentDefinition#addSequence(Sequence)}.</li>
	 * </ul>
	 */
	private static void createSubComponentDefinitions(SBOLDocument doc,ComponentDefinition topCD,URI type,String elements,String version) throws SBOLValidationException {
		for (SequenceAnnotation sa : topCD.getSequenceAnnotations()) {
			if (!sa.isSetComponent()) continue;
			Range range = (Range)sa.getLocation("range");
			if (range!=null) {
				String subElements = elements.substring(range.getStart()-1,range.getEnd()).toLowerCase();
				if (range.getOrientation().equals(OrientationType.REVERSECOMPLEMENT)) {
					subElements = Sequence.reverseComplement(subElements,type);
				}
				ComponentDefinition subCompDef = sa.getComponent().getDefinition();
				String compDefId = subCompDef.getDisplayId();
				Sequence subSequence = doc.createSequence(compDefId+"_seq", version, subElements, Sequence.IUPAC_DNA);
				subCompDef.addSequence(subSequence);
			}
		}
	}
	
	private static String fixTag(String tag) {
		tag = tag.replaceAll("[^a-zA-Z0-9_\\-]", "_");
		tag = tag.replace(" ", "_");
		if (Character.isDigit(tag.charAt(0))|| tag.charAt(0)=='-') {
			tag = "_" + tag;
		}
		return tag;
	}

	/**
	 * @param doc
	 * @param stringBuffer
	 * @param URIPrefix
	 * @throws IOException
	 * @throws SBOLConversionException
	 * @throws SBOLValidationException if an SBOL validation rule violation occurred in any of the following methods:
	 * <ul>
	 * <li>{@link SBOLDocument#createComponentDefinition(String, String, URI)},</li>
	 * <li>{@link Identified#setAnnotations(List)},</li>
	 * <li>{@link SequenceAnnotation#addAnnotation(Annotation)},</li>
	 * <li>{@link ComponentDefinition#createSequenceAnnotation(String, String, int, int, OrientationType)},</li>
	 * <li>{@link SequenceAnnotation#setComponent(String)},</li>
	 * <li>{@link SequenceAnnotation#addRange(String, int, int, OrientationType)},</li>
	 * <li>{@link Range#addAnnotation(Annotation)},</li>
	 * <li>{@link ComponentDefinition#createSequenceAnnotation(String, String, int, OrientationType)},</li>
	 * <li>{@link SBOLDocument#createSequence(String, String, String, URI)}, </li>
	 * <li>{@link ComponentDefinition#addSequence(Sequence)}, or </li>
	 * <li>{@link #createSubComponentDefinitions(SBOLDocument, ComponentDefinition, URI, String, String)}.</li>
	 * </ul>
	 */
	static void read(SBOLDocument doc,String stringBuffer,String URIPrefix,String displayId,String defaultVersion) throws IOException, SBOLConversionException, SBOLValidationException {
		so = new SequenceOntology();

		// reset the global static variables needed for parsing
		//lineCounter = 0;

		doc.addNamespace(URI.create(GBNAMESPACE), GBPREFIX);
		doc.addNamespace(URI.create(GBCONVNAMESPACE), GBCONVPREFIX);
		BufferedReader br = new BufferedReader(new StringReader(stringBuffer));
		String strLine;
		int featureCnt = 0;
		int refCnt = 0;
		nextLine = null;
		String labelType = "";
		URI lastRole = null;
		while (true) {
			boolean cont = false;
			String id = displayId;
			String accession = "";
			String version = defaultVersion;
			featureMode = false;
			originMode = false;
			StringBuilder sbSequence = new StringBuilder();
			String elements = null;
			String description = "";
			String comment = "";
			URI type = ComponentDefinition.DNA_REGION;
			ComponentDefinition topCD = null;
			List<Annotation> annotations = new ArrayList<Annotation>();
			List<Annotation> nestedAnnotations = null;
			Annotation annotation = null;
			boolean circular = false;
			int baseCount = 0;
			while ((strLine = readGenBankLine(br)) != null)   {
				strLine = strLine.trim();
				// LOCUS line
				// Example:
				// LOCUS       AF123456                1510 bp    mRNA    linear   VRT 12-APR-2012
				if (strLine.startsWith("LOCUS")) {
					Matcher m = lp.matcher(strLine.trim());
					if (m.matches()) {
//						System.out.println(strLine);
//						System.out.println("1 = " + m.group(1));
//						System.out.println("2 = " + m.group(2));
//						System.out.println("3 = " + m.group(3));
//						System.out.println("4 = " + m.group(4));
//						System.out.println("5 = " + m.group(5));
//						System.out.println("6 = " + m.group(6));
//						System.out.println("7 = " + m.group(7));
//						System.out.println("8 = " + m.group(8));
//						System.out.println("9 = " + m.group(9));
					} else {
						System.out.println(strLine.trim());
						throw new SBOLConversionException("Error: bad locus line");
					}

					// ID of the sequence
					if (id == null || id.equals("")) {
						id = m.group(1).trim();
						annotation = new Annotation(new QName(GBNAMESPACE, LOCUS, GBPREFIX), id);
						id = URIcompliance.fixDisplayId(id);
						annotations.add(annotation);
					} 
					
					// Base count of the sequence
					try {
						baseCount = Integer.parseInt(m.group(2));
					} catch (NumberFormatException e) {
						throw new SBOLConversionException("Error: bad sequence length");
					}
						
					// type of sequence
					String seqType = m.group(4);
					if (seqType.toUpperCase().contains("RNA")) {
						type = ComponentDefinition.RNA_REGION;
					}
					annotation = new Annotation(new QName(GBNAMESPACE, MOLECULE, GBPREFIX), seqType);
					annotations.add(annotation);
					
					String topology = m.group(7);

					// linear vs. circular construct
					if (topology.startsWith("linear") || topology.startsWith("circular")) {
						if (topology.startsWith("circular")) circular = true;
						//annotation = new Annotation(new QName(GBNAMESPACE, TOPOLOGY, GBPREFIX), strSplit[i]);
					}

					String division = null;
					String date = null;
					if (m.group(8) != null && m.group(9) != null) {
						division = m.group(8);
						date = m.group(9);
					} else if (m.group(8) != null) {
						date = m.group(8);
					} else if (m.group(9) != null) {
						date = m.group(9);
					} 
					if (division != null) {
						annotation = new Annotation(new QName(GBNAMESPACE, DIVISION, GBPREFIX), division);
						annotations.add(annotation);
					}
					if (date != null) {
						annotation = new Annotation(new QName(GBNAMESPACE, DATE, GBPREFIX), date);
						annotations.add(annotation);
					}

				} else if (strLine.startsWith("DEFINITION")) {
					description = strLine.replaceFirst("DEFINITION  ", "");
				} else if (strLine.startsWith("ACCESSION")) {
					String[] strSplit = strLine.split("\\s+");
					if (strSplit.length > 1) {
						accession = strSplit[1];
						if (accession.length()>1) {
							id = accession;
							id = URIcompliance.fixDisplayId(id);
						}
					}
					if (strSplit.length > 3) {
						if (strSplit[2].equals("REGION:")) {
							annotation = new Annotation(new QName(GBNAMESPACE, REGION, GBPREFIX), strSplit[3]);
							annotations.add(annotation);
						}
					}
				} else if (strLine.startsWith("VERSION")) {
					String[] strSplit = strLine.split("\\s+");
					//id = URIcompliance.fixDisplayId(id);
					if (strSplit.length > 1) {
						if (!accession.equals(strSplit[1])) {
							if (strSplit[1].split("\\.").length > 1) {
								version = strSplit[1].split("\\.")[strSplit[1].split("\\.").length-1];
							}	
							if (strSplit[1].split("\\.").length > 0) {
								String vId = strSplit[1].split("\\.")[0];
								if (!accession.equals(vId)) {
									throw new SBOLConversionException("Warning: id in version does not match id in accession");
								}
							}
						}
					}
					//id = id.replaceAll("\\.", "_");
					if (strSplit.length > 2) {
						annotation = new Annotation(new QName(GBNAMESPACE,GINUMBER,GBPREFIX),strSplit[2]);
						annotations.add(annotation);
					}
				} else if (strLine.startsWith("KEYWORDS")) {
					String annotationStr = strLine.replace("KEYWORDS", "").trim();
					annotation = new Annotation(new QName(GBNAMESPACE,KEYWORDS,GBPREFIX), annotationStr);
					annotations.add(annotation);
				} else if (strLine.startsWith("SOURCE")) {
					String annotationStr = strLine.replace("SOURCE", "").trim();
					annotation = new Annotation(new QName(GBNAMESPACE,SOURCE,GBPREFIX), annotationStr);
					annotations.add(annotation);
				} else if (strLine.startsWith("ORGANISM")) {
					String annotationStr = strLine.replace("ORGANISM", "").trim();
					annotation = new Annotation(new QName(GBNAMESPACE,ORGANISM,GBPREFIX), annotationStr);
					annotations.add(annotation);
				} else if (strLine.startsWith("REFERENCE")) {
					String annotationStr = strLine.replace("REFERENCE", "").trim();
					nestedAnnotations = new ArrayList<Annotation>();
					Annotation labelAnnotation = new Annotation(new QName(GBNAMESPACE,LABEL,GBPREFIX), annotationStr);
					nestedAnnotations.add(labelAnnotation);
					URI nestedURI = URI.create(URIPrefix+id+"/reference"+refCnt);
					refCnt++;
					annotation = new Annotation(new QName(GBNAMESPACE,REFERENCE,GBPREFIX),
							new QName(GBNAMESPACE,NESTEDREFERENCE,GBPREFIX),nestedURI,nestedAnnotations);
					annotations.add(annotation);
				} else if (strLine.startsWith("AUTHORS")) {
					String annotationStr = strLine.replace("AUTHORS", "").trim();
					Annotation nestedAnnotation = new Annotation(new QName(GBNAMESPACE,AUTHORS,GBPREFIX), annotationStr);
					nestedAnnotations.add(nestedAnnotation);
					annotation.setAnnotations(nestedAnnotations);
				} else if (strLine.startsWith("TITLE")) {
					String annotationStr = strLine.replace("TITLE", "").trim();
					Annotation nestedAnnotation = new Annotation(new QName(GBNAMESPACE,TITLE,GBPREFIX), annotationStr);
					nestedAnnotations.add(nestedAnnotation);
					annotation.setAnnotations(nestedAnnotations);
				} else if (strLine.startsWith("JOURNAL")) {
					String annotationStr = strLine.replace("JOURNAL", "").trim();
					Annotation nestedAnnotation = new Annotation(new QName(GBNAMESPACE,JOURNAL,GBPREFIX), annotationStr);
					nestedAnnotations.add(nestedAnnotation);
					annotation.setAnnotations(nestedAnnotations);
				} else if (strLine.startsWith("MEDLINE")) {
					String annotationStr = strLine.replace("MEDLINE", "").trim();
					Annotation nestedAnnotation = new Annotation(new QName(GBNAMESPACE,MEDLINE,GBPREFIX), annotationStr);
					nestedAnnotations.add(nestedAnnotation);
					annotation.setAnnotations(nestedAnnotations);
				} else if (strLine.startsWith("PUBMED")) {
					String annotationStr = strLine.replace("PUBMED", "").trim();
					Annotation nestedAnnotation = new Annotation(new QName(GBNAMESPACE,PUBMED,GBPREFIX), annotationStr);
					nestedAnnotations.add(nestedAnnotation);
					annotation.setAnnotations(nestedAnnotations);
					Annotation pubMedAnnotation = new Annotation(new QName("http://purl.obolibrary.org/obo/", "OBI_0001617", "obo"), annotationStr);
					annotations.add(pubMedAnnotation);
				} else if (strLine.startsWith("COMMENT")) {
					String annotationStr = strLine.replace("COMMENT     ", "");
					if (!comment.equals("")) {
						comment += "\n ";
					} 
					comment += annotationStr;
				} else if (strLine.startsWith("BASE COUNT")) {
					String annotationStr = strLine.replace("BASE COUNT", "").trim();
					annotation = new Annotation(new QName(GBNAMESPACE,BASECOUNT,GBPREFIX), annotationStr);
					annotations.add(annotation);

					// sequence features
				} else if (strLine.startsWith("FEATURE")) {
					if (!comment.equals("")) {
						annotation = new Annotation(new QName(GBNAMESPACE,COMMENT,GBPREFIX), comment);
						annotations.add(annotation);
					}
					
					topCD = doc.createComponentDefinition(id, version, type);
					if (circular) {
						topCD.addType(SequenceOntology.CIRCULAR);
					} else {
						topCD.addType(SequenceOntology.LINEAR);
					}
					topCD.addRole(SequenceOntology.ENGINEERED_REGION);
					if (!"".equals(description)) {
						topCD.setDescription(description);
					}
					topCD.setAnnotations(annotations);

					// tell the parser that we're in the "FEATURE" section
					featureMode = true;

				} else if (strLine.startsWith("ORIGIN")) {
					// switch from feature to origin mode
					originMode = true;
					featureMode = false;

				} else {

					/*---------------------
					 * FEATURE MODE
					 *---------------------*/
					if (featureMode) {


						// parse the labels of a feature
						if (strLine.startsWith("/")) {


							// per default, we assume that every label
							// has a key only
							String tag = strLine.replace("/","").trim();
							String value = "";

							// now, we check if the key has a value too
							// i.e. /<key>=<value>
							if((-1) != strLine.indexOf('=')) {
								String[] splitStr = strLine.split("=");
								tag = splitStr[0].replace("/","");
								value = splitStr[1];
							}

							// here, we just read the next lines until we find the closing double-quota
							StringBuilder sbValue = new StringBuilder();
							sbValue.append(value);

							// multi-line string value
							if(value.startsWith("\"") && !value.endsWith("\"")) {
								while(true) {
									strLine = readGenBankLine(br).trim();
									sbValue.append(strLine);
									if (value.contains(" ") || strLine.contains(" ")) {
										value += " " + strLine;
									} else {
										value += strLine;
									}
									if(strLine.endsWith("\"")) {
										break;
									}
								}

							}


							// a Genbank feature label is mapped to an SBOL SequenceAnnotation
							SequenceAnnotation sa = topCD.getSequenceAnnotation("annotation" + (featureCnt - 1));
							if(sa != null) {
								if (tag.equals("Apeinfo_label")) {
									sa.setName(value.replace("\"", ""));
									labelType = "Apeinfo_label";
								} else if (tag.equals("label")) {
									if (!labelType.equals("Apeinfo_label")) {
										sa.setName(value.replace("\"", ""));
										labelType = "label";
									}
								} else if (tag.equals("product")) {
									if (!labelType.equals("Apeinfo_label")&&
											!labelType.equals("label")) {
										sa.setName(value.replace("\"", ""));
										labelType = "product";
									}
								} else if (tag.equals("gene")) {
									if (!labelType.equals("Apeinfo_label")&&
											!labelType.equals("label")&&
											!labelType.equals("product")) {
										sa.setName(value.replace("\"", ""));
										labelType = "gene";
									}
								} else if (tag.equals("note")) {
									if (!labelType.equals("Apeinfo_label")&&
											!labelType.equals("label")&&
											!labelType.equals("product")&&
											!labelType.equals("gene")) {
										sa.setName(value.replace("\"", ""));
										labelType = "note";
									}
								} else if (tag.equals("organism")) {
									if (!labelType.equals("Apeinfo_label")&&
											!labelType.equals("label")&&
											!labelType.equals("product")&&
											!labelType.equals("gene")&&
											!labelType.equals("note")) {
										sa.setName(value.replace("\"", ""));
										labelType = "organism";
									}
								}
								tag = fixTag(tag);
								if (value.startsWith("\"")) {
									value = value.replaceAll("\"", "");
									annotation = new Annotation(new QName(GBCONVNAMESPACE,tag,GBCONVPREFIX),value);
								} else {
									annotation = new Annotation(new QName(GBCONVNAMESPACE,tag,GBCONVPREFIX),value);
									// TODO: does not work because integer type of annotation is lost on serialization
									//annotation = new Annotation(new QName(GBNAMESPACE,tag,GBPREFIX),Integer.parseInt(value));
								}
								sa.addAnnotation(annotation);
							}

							// start of a new feature
						} else {

							strLine = strLine.replace(", ",",");
							String[] strSplit = strLine.split("\\s+");
							String featureType = strSplit[0];

							// a Genbank feature is mapped to a SBOL role
							// documented by an SO term
							URI role = convertGenBanktoSO(featureType);
//							ComponentDefinition feature =
//									doc.createComponentDefinition("feature"+featureCnt, version, type);
//							feature.addRole(role);

							String range = strSplit[1];
							boolean outerComplement = false;
							OrientationType orientation = OrientationType.INLINE;
							if (range.startsWith("complement")) {
								outerComplement = true;
								orientation = OrientationType.REVERSECOMPLEMENT;
								range = range.replace("complement(", "").replace(")","");
								if ((range.startsWith("join")) || (range.startsWith("order"))) range += ")";
							}
							
							if (range.startsWith("join")||range.startsWith("order")) {
								String multiType = "join";
								if (range.startsWith("order")) {
									multiType = "order";
								}
								while (!range.endsWith(")")) {
									strLine = readGenBankLine(br).trim();
//									System.out.println("Multi:"+strLine);
									range += strLine;
								}
								range = range.replace("join(", "").replace(")","");
								range = range.replace("order(", "").replace(")","");
								String[] ranges = range.split(",");
								int rangeCnt = 0;
								SequenceAnnotation sa =  null;
								for (String r : ranges) {
									orientation = OrientationType.INLINE;
									if (r.startsWith("complement")||outerComplement) {
										orientation = OrientationType.REVERSECOMPLEMENT;
										r = r.replace("complement(", "").replace(")","");
									}
									boolean startLessThan=false;
									boolean endGreaterThan=false;
									if (r.contains("<")) {
										startLessThan=true;
										r = r.replace("<","");
									}
									if (r.contains(">")) {
										endGreaterThan=true;
										r = r.replace(">", "");
									}
									boolean singleBaseRange = false;
									String[] rangeSplit = null;
									if (range.contains(".") && !range.contains("..")) {
										rangeSplit = r.split("\\.");
										singleBaseRange = true;
									} else {
										rangeSplit = r.split("\\.\\.");
									}
									int start = 0;
									if (rangeSplit.length > 0) {
										start = Integer.parseInt(rangeSplit[0]);
									}
									int end = start;
									if (rangeSplit.length > 1) {
										end = Integer.parseInt(rangeSplit[1]);
									}
									Range newRange = null;
									if (rangeCnt==0) {
										sa = topCD.createSequenceAnnotation("annotation"+featureCnt,"range"+rangeCnt,
												start,end,orientation);
										//sa.setComponent("feature"+featureCnt);
										sa.setName(featureType);
										sa.addRole(role);
										annotation = new Annotation(new QName(GBCONVNAMESPACE,MULTIRANGETYPE,GBCONVPREFIX),multiType);
										sa.addAnnotation(annotation);
										if (role.equals(SequenceOntology.SEQUENCE_FEATURE)) {
											annotation = new Annotation(new QName(GBCONVNAMESPACE,FEATURETYPE,GBCONVPREFIX),featureType);
											sa.addAnnotation(annotation);
										}
										newRange = (Range)sa.getLocation("range"+rangeCnt);
									} else if (sa != null) {
										newRange = sa.addRange("range"+rangeCnt, start, end, orientation);
									}
									if (outerComplement) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,POSITION,GBCONVPREFIX),"position"+((ranges.length-1)-rangeCnt));
									} else {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,POSITION,GBCONVPREFIX),"position"+rangeCnt);
									}
									newRange.addAnnotation(annotation);
									if (startLessThan) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,STARTLESSTHAN,GBCONVPREFIX),"true");
										newRange.addAnnotation(annotation);
									}
									if (endGreaterThan) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,ENDGREATERTHAN,GBCONVPREFIX),"true");
										newRange.addAnnotation(annotation);
									}
									if (singleBaseRange) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,SINGLEBASERANGE,GBCONVPREFIX),"true");
										newRange.addAnnotation(annotation);
									}
									rangeCnt++;
								}
							} else if (range.contains("^")) {
								String[] rangeSplit = range.split("\\^");
								int at = Integer.parseInt(rangeSplit[0]);
								SequenceAnnotation sa =
										topCD.createSequenceAnnotation("annotation"+featureCnt,"cut",at,orientation);
								//sa.setComponent("feature"+featureCnt);
								sa.addRole(role);
								sa.setName(featureType);
								if (role.equals(SequenceOntology.SEQUENCE_FEATURE)) {
									annotation = new Annotation(new QName(GBCONVNAMESPACE,FEATURETYPE,GBCONVPREFIX),featureType);
									sa.addAnnotation(annotation);
								}
							} else {
								boolean startLessThan=false;
								boolean endGreaterThan=false;
								if (range.contains("<")) {
									startLessThan=true;
									range = range.replace("<","");
								}
								if (range.contains(">")) {
									endGreaterThan=true;
									range = range.replace(">", "");
								}
								boolean singleBaseRange = false;
								String[] rangeSplit = null;
								if (range.contains(".") && !range.contains("..")) {
									rangeSplit = range.split("\\.");
									singleBaseRange = true;
								} else {
									rangeSplit = range.split("\\.\\.");
								}
								int start = Integer.parseInt(rangeSplit[0]);
								int end = Integer.parseInt(rangeSplit[0]);
								if (rangeSplit.length > 1) {
									end = Integer.parseInt(rangeSplit[1]);
								}
								if (start > end && circular) {
									SequenceAnnotation sa =
											topCD.createSequenceAnnotation("annotation"+featureCnt,"range0",start,baseCount,orientation);
									//sa.setComponent("feature"+featureCnt);
									sa.addRole(role);
									sa.setName(featureType);
									annotation = new Annotation(new QName(GBCONVNAMESPACE,STRADLESORIGIN,GBCONVPREFIX),"true");
									sa.addAnnotation(annotation);
									if (role.equals(SequenceOntology.SEQUENCE_FEATURE)) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,FEATURETYPE,GBCONVPREFIX),featureType);
										sa.addAnnotation(annotation);
									}
									Range newRange = (Range)sa.getLocation("range0");
									if (startLessThan) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,STARTLESSTHAN,GBCONVPREFIX),"true");
										newRange.addAnnotation(annotation);
									}
									if (singleBaseRange) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,SINGLEBASERANGE,GBCONVPREFIX),"true");
										newRange.addAnnotation(annotation);
									}
									newRange = sa.addRange("range1", 1, end, orientation);
									if (singleBaseRange) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,SINGLEBASERANGE,GBCONVPREFIX),"true");
										newRange.addAnnotation(annotation);
									}
									if (endGreaterThan) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,ENDGREATERTHAN,GBCONVPREFIX),"true");
										newRange.addAnnotation(annotation);
									}
								} else {
									SequenceAnnotation sa =
											topCD.createSequenceAnnotation("annotation"+featureCnt,"range",start,end,orientation);
									//sa.setComponent("feature"+featureCnt);
									sa.addRole(role);
									sa.setName(featureType);
									if (role.equals(SequenceOntology.SEQUENCE_FEATURE)) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,FEATURETYPE,GBCONVPREFIX),featureType);
										sa.addAnnotation(annotation);
									}
									Range newRange = (Range)sa.getLocation("range");
									if (startLessThan) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,STARTLESSTHAN,GBCONVPREFIX),"true");
										newRange.addAnnotation(annotation);
									}
									if (endGreaterThan) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,ENDGREATERTHAN,GBCONVPREFIX),"true");
										newRange.addAnnotation(annotation);
									}
									if (singleBaseRange) {
										annotation = new Annotation(new QName(GBCONVNAMESPACE,SINGLEBASERANGE,GBCONVPREFIX),"true");
										newRange.addAnnotation(annotation);
									}
								}
							}
							labelType = "";
							lastRole = role;
							featureCnt++;

						}


						/*---------------------
						 * SEQUENCE MODE
						 *---------------------*/
					} else if (originMode) {
						if (featureCnt==1) {
							topCD.clearRoles();
							topCD.addRole(lastRole);
						}
						if(elements == null) { elements = new String(""); }
						if (strLine.startsWith("//")) {
							cont = true;
							break;
						}
						String[] strSplit = strLine.split(" ");
						for (int i = 1; i < strSplit.length; i++) {
							sbSequence.append(strSplit[i]);
						}
					}
					cont = false;
				}
			}
			if (topCD!=null) {
				//throw new SBOLConversionException("Invalid GenBank file.");
				Sequence sequence = doc.createSequence(id+"_seq", version, sbSequence.toString(), Sequence.IUPAC_DNA);
				topCD.addSequence(sequence);
				createSubComponentDefinitions(doc,topCD,type,sbSequence.toString(),version);
			}
			if (!cont) break;
		}
		br.close();
	}
}
