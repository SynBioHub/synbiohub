var namespace = require('./namespace')

function summarizeRoles(sbolObject) {
 
    return sbolObject.roles.map((uri) => {

        uri = '' + uri

        for(var i = 0; i < namespace.so.length; ++ i) {

            var prefix = namespace.so[i]

            if(uri.indexOf(prefix) === 0) {

                var soTerm = uri.slice(prefix.length).split('_').join(':')

                var sequenceOntology = require('../ontologies/sequence-ontology')

                return {
                    uri: uri,
                    term: soTerm,
                    description: sequenceOntology[soTerm]
                }
            }
        }

        for(var i = 0; i < namespace.go.length; ++ i) {

            var prefix = namespace.go[i]

            if(uri.indexOf(prefix) === 0) {

                var goTerm = uri.slice(prefix.length).split('_').join(':')

                var geneOntology = require('../ontologies/gene-ontology')

                return {
                    uri: uri,
                    term: goTerm,
                    description: geneOntology[goTerm]
                }
            }
        }

	var igemPrefix = 'http://wiki.synbiohub.org/wiki/Terms/igem#partType/'

        if(!uri.term && uri.indexOf(igemPrefix) === 0) {

            return {
                uri: uri,
                term: uri.slice(igemPrefix.length)
            }

        }

	if(!uri.term && uri.lastIndexOf('#')>=0 && uri.lastIndexOf('#')+1 < uri.length) {
            return {
		uri: uri,
		term: uri.slice(uri.lastIndexOf('#')+1)
	    }
	}

	if(!uri.term && uri.lastIndexOf('/')>=0 && uri.lastIndexOf('/')+1 < uri.length) {
            return {
		uri: uri,
		term: uri.slice(uri.lastIndexOf('/')+1)
	    }
	}
	
        return {
            uri: uri
        }
    })
}

module.exports = summarizeRoles





