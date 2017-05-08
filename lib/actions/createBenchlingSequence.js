var pug = require('pug')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var sbolmeta = require('sbolmeta')

const benchling = require('../benchling')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

exports = module.exports = function(req, res) {

    const { graphUri, uri, designId, share } = getUrisFromReq(req)
    
    fetchSBOLObjectRecursive(uri, graphUri).then((result) => {

        const sbol = result.sbol
        const componentDefinition = result.object

	var meta = sbolmeta.summarizeComponentDefinition(componentDefinition)

	var bSeq = {}

	bSeq.circular = false
	componentDefinition.types.forEach((type) => {
	    if (type.toString() === "http://identifiers.org/so/SO:0000988") {
		bSeq.circular = true
	    }
	})

	bSeq.folder = config.get("defaultBenchlingFolderId")
	bSeq.name = meta.name
	bSeq.description = meta.description
	
	meta.sequences.forEach((sequence, i) => {

	    bSeq.bases = sequence.elements

        })
	// TODO: add aliases, tags

	bSeq.annotations = []
	componentDefinition.sequenceAnnotations.forEach((sa) => {
	    var annotation = {}
	    annotation.name = sa.name?sa.name:sa.displayId
	    annotation.type = 'misc_feature'
	    if (sa.component && sa.component != '') {
		sa.component.definition.roles.forEach((role) => {
		    if (soToGenBank(role.toString())) {
			annotation.type = soToGenBank(role.toString())
		    }
		})
	    } else {
		sa.roles.forEach((role) => {
		    if (soToGenBank(role.toString())) {
			annotation.type = soToGenBank(role.toString())
		    }
		})
	    }
	    sa.ranges.forEach((range) => {
		annotation.start = range.start
		annotation.end = range.end
		if (range.orientation) {
		    annotation.strand = range.orientation.toString().endsWith('inline')?1:-1
		} else {
		    annotation.strand = 0
		}
	    })
	    // TODO: get annotation else use a default based on role, perhaps
	    annotation.color = '#999999'
	    sa.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/benchling#color').forEach((color) => {
		annotation.color = color
	    })
	    bSeq.annotations.push(annotation)
	})
	console.log(JSON.stringify(bSeq.annotations))

	const remoteConfig = config.get('remotes')[config.get("defaultBenchlingInstance")]
	
	return benchling.createSequence(remoteConfig, bSeq).then((result) => {
	    console.log(JSON.stringify(result))
	    res.redirect('/public/' + remoteConfig.id + '/' + remoteConfig.folderPrefix + bSeq.folder + '/current')
	})

    }).catch((err) => {

	console.log(err)
        const locals = {
            config: config.get(),
            section: 'errors',
            user: req.user,
            errors: [ uri + ' Not Found' ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })

    function soToGenBank(soTerm) {
	if (soTerm.endsWith("SO:0001023")) {return "allele";}
	if (soTerm.endsWith("SO:0000140")) {return "attenuator";}
	if (soTerm.endsWith("SO:0001834")) {return "C_region";}
	if (soTerm.endsWith("SO:0000172")) {return "CAAT_signal";}
	if (soTerm.endsWith("SO:0000316")) {return "CDS";}
	//if (soTerm.endsWith("SO:")) {return "conflict";}
	if (soTerm.endsWith("SO:0000297")) {return "D-loop";}
	if (soTerm.endsWith("SO:0000458")) {return "D_segment";}
	if (soTerm.endsWith("SO:0000165")) {return "enhancer";}
	if (soTerm.endsWith("SO:0000147")) {return "exon";}
	if (soTerm.endsWith("SO:0000704")) {return "gene";}
	if (soTerm.endsWith("SO:0000173")) {return "GC_signal";}
	if (soTerm.endsWith("SO:0000723")) {return "iDNA";}
	if (soTerm.endsWith("SO:0000188")) {return "intron";}
	if (soTerm.endsWith("SO:0000470")) {return "J_region";}
	if (soTerm.endsWith("SO:0000286")) {return "LTR";}
	if (soTerm.endsWith("SO:0000419")) {return "mat_peptide";}
	if (soTerm.endsWith("SO:0000409")) {return "misc_binding";}
	if (soTerm.endsWith("SO:0000413")) {return "misc_difference";}
	if (soTerm.endsWith("SO:0000001")) {return "misc_feature";}
	if (soTerm.endsWith("SO:0001645")) {return "misc_marker";}
	if (soTerm.endsWith("SO:0000298")) {return "misc_recomb";}
	if (soTerm.endsWith("SO:0000233")) {return "misc_RNA";}
	if (soTerm.endsWith("SO:0001411")) {return "misc_signal";}
	if (soTerm.endsWith("SO:0005836")) {return "regulatory";}
	if (soTerm.endsWith("SO:0000002")) {return "misc_structure";}
	if (soTerm.endsWith("SO:0000305")) {return "modified_base";}
	if (soTerm.endsWith("SO:0000234")) {return "mRNA";}
	//if (soTerm.endsWith("SO:")) {return "mutation";}
	if (soTerm.endsWith("SO:0001835")) {return "N_region";}
	//if (soTerm.endsWith("SO:")) {return "old_sequence";}
	if (soTerm.endsWith("SO:0000551")) {return "polyA_signal";}
	if (soTerm.endsWith("SO:0000553")) {return "polyA_site";}
	if (soTerm.endsWith("SO:0000185")) {return "precursor_RNA";}
	if (soTerm.endsWith("SO:0000185")) {return "prim_transcript";}
	// NOTE: redundant with line above
	if (soTerm.endsWith("SO:0000112")) {return "primer";}
	if (soTerm.endsWith("SO:0005850")) {return "primer_bind";}
	if (soTerm.endsWith("SO:0000167")) {return "promoter";}
	if (soTerm.endsWith("SO:0000410")) {return "protein_bind";}
	if (soTerm.endsWith("SO:0000139") || soTerm.endsWith("SO:0000552")) {return "RBS";}
	if (soTerm.endsWith("SO:0000296")) {return "rep_origin";}
	if (soTerm.endsWith("SO:0000657")) {return "repeat_region";}
	if (soTerm.endsWith("SO:0000726")) {return "repeat_unit";}
	if (soTerm.endsWith("SO:0000252")) {return "rRNA";}
	if (soTerm.endsWith("SO:0001836")) {return "S_region";}
	if (soTerm.endsWith("SO:0000005")) {return "satellite";}
	if (soTerm.endsWith("SO:0000013")) {return "scRNA";}
	if (soTerm.endsWith("SO:0000418")) {return "sig_peptide";}
	if (soTerm.endsWith("SO:0000274")) {return "snRNA";}
	if (soTerm.endsWith("SO:0000149")) {return "source";}
	if (soTerm.endsWith("SO:0000019")) {return "stem_loop";}
	if (soTerm.endsWith("SO:0000331")) {return "STS";}
	if (soTerm.endsWith("SO:0000174")) {return "TATA_signal";}
	if (soTerm.endsWith("SO:0000141")) {return "terminator";}
	if (soTerm.endsWith("SO:0000725")) {return "transit_peptide";}
	if (soTerm.endsWith("SO:0001054")) {return "transposon";}
	if (soTerm.endsWith("SO:0000253")) {return "tRNA";}
	// if (soTerm.endsWith("SO:")) {return "unsure";}
	if (soTerm.endsWith("SO:0001833")) {return "V_region";}
	if (soTerm.endsWith("SO:0001060")) {return "variation";}
	if (soTerm.endsWith("SO:0000175")) {return "-10_signal";}
	if (soTerm.endsWith("SO:0000176")) {return "-35_signal";}
	if (soTerm.endsWith("SO:0000557")) {return "3'clip";}
	if (soTerm.endsWith("SO:0000205")) {return "3'UTR";}
	if (soTerm.endsWith("SO:0000555")) {return "5'clip";}
	if (soTerm.endsWith("SO:0000204")) {return "5'UTR";}
	/*
	  if (soTerm.endsWith("CDS") || soTerm.endsWith("promoter") || soTerm.endsWith("terminator"))
	  return soTerm);
	  else if (soTerm.endsWith("ribosome_entry_site"))
	  return "RBS            ";
	*/
	return "";
    }

};


