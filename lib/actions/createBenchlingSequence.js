var pug = require('pug')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var sbolmeta = require('sbolmeta')

const benchling = require('../benchling')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

exports = module.exports = function(req, res) {

    var benchlingRemote = config.get("defaultBenchlingInstance")
    console.log(benchlingRemote)

    if (!benchlingRemote) {
	if(req.method === 'GET') {

	    var benchlingRemotes = []
	    Object.keys(config.get('remotes')).filter(function(e){return config.get('remotes')[e].type ==='benchling'}).forEach((remote) => {
		benchlingRemotes.push(config.get('remotes')[remote])
	    })
	
	    if (benchlingRemotes.length > 1) {
		locals = {
		    config: config.get(),
		    section: 'createBenchlingPart',
		    user: req.user,
		    benchlingRemotes: benchlingRemotes,
		    submission: {}
		}
		res.send(pug.renderFile('templates/views/selectBenchlingRemote.jade', locals))
		return
	    }

	    if (benchlingRemotes.length === 0) {
		const locals = {
		    config: config.get(),
		    section: 'errors',
		    user: req.user,
		    errors: [ 'No Benchling remote instances configured' ]
		}
		res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
		return
	    }

	    if (benchlingRemotes.length === 1) {
		benchlingRemote = benchlingRemotes[0].id
	    }

	} else {
	    benchlingRemote = req.body.benchlingRemote
	}
    }

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

	bSeq.folder = config.get('remotes')[benchlingRemote].defaultFolderId
	bSeq.name = meta.name
	bSeq.description = meta.description
	bSeq.aliases = []
	bSeq.aliases.push(componentDefinition.uri.toString())
	
	meta.sequences.forEach((sequence, i) => {

	    bSeq.bases = sequence.elements

        })
	// TODO: add aliases, tags

	bSeq.annotations = []
	flattenSequenceAnnotations(componentDefinition).forEach((sa) => {
	    var annotation = {}
	    annotation.name = sa.name?sa.name:sa.displayId
	    annotation.type = 'misc_feature'
	    annotation.color = '#999999'
	    if (sa.component && sa.component != '') {
		sa.component.definition.roles.forEach((role) => {
		    if (sa.component.definition.name != '') {
			annotation.name = sa.component.definition.name
		    }
		    if (soToGenBank(role.toString())) {
			annotation.type = soToGenBank(role.toString())
			annotation.color = soToColor(role.toString())
		    }
		})
	    } else {
		sa.roles.forEach((role) => {
		    if (soToGenBank(role.toString())) {
			annotation.type = soToGenBank(role.toString())
			annotation.color = soToColor(role.toString())
		    }
		})
	    }
	    sa.ranges.forEach((range) => {
		annotation.start = range.start
		annotation.end = range.end
		if (range.orientation && range.orientation != '') {
		    annotation.strand = range.orientation.toString().endsWith('reverseComplement')?-1:1
		} else {
		    // TODO: should be 0, but benchling seems to convert 0 to -1
		    annotation.strand = 1
		}
	    })
	    sa.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/benchling#color').forEach((color) => {
		annotation.color = color
	    })
	    bSeq.annotations.push(annotation)
	})

	const remoteConfig = config.get('remotes')[benchlingRemote]
	
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
            errors: [ err ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))

    })

	function flattenSequenceAnnotations(cd) {
	    var annotations = []
	    cd.sequenceAnnotations.forEach((sa) => {
		if (sa.component && sa.component != '') {
		    if (sa.component.definition.components.length > 0) {
			var start
			sa.ranges.forEach((range) => {
			    start = range.start
			})
			results = flattenSequenceAnnotations(sa.component.definition)
			results.forEach((result) => {
			    result.ranges.forEach((range) => {
				range.start = range.start + start - 1
				range.end = range.end + start - 1
			    })
			    annotations.push(result)
			})
		    } else {
			annotations.push(sa)
		    }
		} else {
		    annotations.push(sa)
		}
	    })
	    return annotations
	}

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

    function soToColor(soTerm) {
	if (soTerm.endsWith("SO:0000316")) {return "#779ecb";} //"CDS";}
	if (soTerm.endsWith("SO:0000234")) {return "#03c03c";} //"mRNA";}
	if (soTerm.endsWith("SO:0000185")) {return "#03c03c";} //"precursor_RNA";}
	if (soTerm.endsWith("SO:0000167")) {return "#03c03c";} //"promoter";}
	if (soTerm.endsWith("SO:0000139") || soTerm.endsWith("SO:0000552")) {return "#966FD6";} //"RBS";}
	if (soTerm.endsWith("SO:0000252")) {return "#03c03c";} //"rRNA";}
	if (soTerm.endsWith("SO:0000013")) {return "#03c03c";} //"scRNA";}
	if (soTerm.endsWith("SO:0000274")) {return "#03c03c";} //"snRNA";}
	if (soTerm.endsWith("SO:0000253")) {return "#03c03c";} //"tRNA";}


	if (soTerm.endsWith("SO:0001023")) {return "#000033";} //"allele";}
	if (soTerm.endsWith("SO:0000140")) {return "#000066";} //"attenuator";}
	if (soTerm.endsWith("SO:0001834")) {return "#000099";} //"C_region";}
	if (soTerm.endsWith("SO:0000172")) {return "#0000CC";} //"CAAT_signal";}
	if (soTerm.endsWith("SO:0000704")) {return "#0000FF";} //"gene";}
	if (soTerm.endsWith("SO:0000297")) {return "#003300";} //"D-loop";}
	if (soTerm.endsWith("SO:0000458")) {return "#006600";} //"D_segment";}
	if (soTerm.endsWith("SO:0000165")) {return "#009900";} //"enhancer";}
	if (soTerm.endsWith("SO:0000147")) {return "#00CC00";} //"exon";}
	if (soTerm.endsWith("SO:0000173")) {return "#00FF00";} //"GC_signal";}
	if (soTerm.endsWith("SO:0000723")) {return "#330000";} //"iDNA";}
	if (soTerm.endsWith("SO:0000188")) {return "#660000";} //"intron";}
	if (soTerm.endsWith("SO:0000470")) {return "#990000";} //"J_region";}
	if (soTerm.endsWith("SO:0000286")) {return "#CC0000";} //"LTR";}
	if (soTerm.endsWith("SO:0000419")) {return "#FF0000";} //"mat_peptide";}
	if (soTerm.endsWith("SO:0000409")) {return "#FF0033";} //"misc_binding";}
	if (soTerm.endsWith("SO:0000413")) {return "#FF0066";} //"misc_difference";}
	if (soTerm.endsWith("SO:0000001")) {return "#FF0099";} //"misc_feature";}
	if (soTerm.endsWith("SO:0001645")) {return "#FF00CC";} //"misc_marker";}
	if (soTerm.endsWith("SO:0000298")) {return "#FF00FF";} //"misc_recomb";}
	if (soTerm.endsWith("SO:0000233")) {return "#FF3300";} //"misc_RNA";}
	if (soTerm.endsWith("SO:0001411")) {return "#FF6600";} //"misc_signal";}
	if (soTerm.endsWith("SO:0005836")) {return "#FF9900";} //"regulatory";}
	if (soTerm.endsWith("SO:0000002")) {return "#FFCC00";} //"misc_structure";}
	if (soTerm.endsWith("SO:0000305")) {return "#FFFF00";} //"modified_base";}
	if (soTerm.endsWith("SO:0001835")) {return "#FFFF33";} //"N_region";}
	if (soTerm.endsWith("SO:0000551")) {return "#FFFF66";} //"polyA_signal";}
	if (soTerm.endsWith("SO:0000553")) {return "#FFFF99";} //"polyA_site";}
	if (soTerm.endsWith("SO:0000185")) {return "#FFFFCC";} //"prim_transcript";}
	// NOTE: redundant with line above
	if (soTerm.endsWith("SO:0000112")) {return "#FFFFFF";} //"primer";}
	if (soTerm.endsWith("SO:0005850")) {return "#CC0033";} //"primer_bind";}
	if (soTerm.endsWith("SO:0000410")) {return "#CC0066";} //"protein_bind";}
	if (soTerm.endsWith("SO:0000296")) {return "#CC0099";} //"rep_origin";}
	if (soTerm.endsWith("SO:0000657")) {return "#CC00CC";} //"repeat_region";}
	if (soTerm.endsWith("SO:0000726")) {return "#CC00FF";} //"repeat_unit";}
	if (soTerm.endsWith("SO:0001836")) {return "#CC3300";} //"S_region";}
	if (soTerm.endsWith("SO:0000005")) {return "#CC6600";} //"satellite";}
	if (soTerm.endsWith("SO:0000418")) {return "#CC9900";} //"sig_peptide";}
	if (soTerm.endsWith("SO:0000149")) {return "#CCCC00";} //"source";}
	if (soTerm.endsWith("SO:0000019")) {return "#CCFF00";} //"stem_loop";}
	if (soTerm.endsWith("SO:0000331")) {return "#CCFF33";} //"STS";}
	if (soTerm.endsWith("SO:0000174")) {return "#CCFF66";} //"TATA_signal";}
	if (soTerm.endsWith("SO:0000141")) {return "#CCFF99";} //"terminator";}
	if (soTerm.endsWith("SO:0000725")) {return "#CCFFCC";} //"transit_peptide";}
	if (soTerm.endsWith("SO:0001054")) {return "#CCFFFF";} //"transposon";}
	if (soTerm.endsWith("SO:0001833")) {return "#990033";} //"V_region";}
	if (soTerm.endsWith("SO:0001060")) {return "#990066";} //"variation";}
	if (soTerm.endsWith("SO:0000175")) {return "#990099";} //"-10_signal";}
	if (soTerm.endsWith("SO:0000176")) {return "#9900CC";} //"-35_signal";}
	if (soTerm.endsWith("SO:0000557")) {return "#9900FF";} //"3'clip";}
	if (soTerm.endsWith("SO:0000205")) {return "#993300";} //"3'UTR";}
	if (soTerm.endsWith("SO:0000555")) {return "#996600";} //"5'clip";}
	if (soTerm.endsWith("SO:0000204")) {return "#999900";} //"5'UTR";}
	//if (soTerm.endsWith("SO:")) {return "#999999";} //"conflict";}
	//if (soTerm.endsWith("SO:")) {return "#999999";} //"mutation";}
	//if (soTerm.endsWith("SO:")) {return "#999999";} //"old_sequence";}
	// if (soTerm.endsWith("SO:")) {return "#999999";} //"unsure";}
	/*
	  if (soTerm.endsWith("CDS") || soTerm.endsWith("promoter") || soTerm.endsWith("terminator"))
	  return soTerm);
	  else if (soTerm.endsWith("ribosome_entry_site"))
	  return "RBS            ";
	*/
	return "#999999";
    }


};


