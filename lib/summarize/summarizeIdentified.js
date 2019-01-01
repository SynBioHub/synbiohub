
var namespace = require('./namespace')

const uriToUrl = require('../uriToUrl')

const shareImages = require('../shareImages')

var wiky = require('../wiky/wiky.js');

var filterAnnotations = require('../filterAnnotations')

var config = require('../config')

var sha1 = require('sha1');

var uriToMeta = require('../uriToMeta')
var URI = require('sboljs').URI

function summarizeIdentified(identified,req,sbol,remote,graphUri) {

    if (identified instanceof URI) {
	return uriToMeta(identified)
    }

    var mutableDescriptionSource = identified.getAnnotations(namespace.synbiohub + 'mutableDescription').toString() || '';
    var mutableNotesSource = identified.getAnnotations(namespace.synbiohub + 'mutableNotes').toString() || '';
    var sourceSource = identified.getAnnotations(namespace.synbiohub + 'mutableProvenance').toString() || '';
    
    return {
        uri: identified.uri + '',
	url: uriToUrl(identified,req).toString(),
        name: name(identified),
        id: id(identified),
        pid: pid(identified),
        version: version(identified),
        wasDerivedFrom: wasDerivedFrom(identified,req),
        wasDerivedFroms: wasDerivedFroms(identified,req),
        wasGeneratedBy: wasGeneratedBy(identified,req),
        wasGeneratedBys: wasGeneratedBys(identified,req),
	creator: creator(identified),
	created: created(identified),
	modified: modified(identified),
	mutableDescriptionSource: mutableDescriptionSource,
        mutableDescription: mutableDescription(mutableDescriptionSource,req),
        mutableNotesSource: mutableNotesSource,
        mutableNotes: mutableNotes(mutableNotesSource,req),
        sourceSource: sourceSource,
        source: source(sourceSource,req),
        description: description(identified),
	comments: comments(identified),
        labels: labels(identified),
	canEdit: canEdit(identified,req,remote),
        annotations: filterAnnotations(req,identified.annotations),
	triplestore: graphUri ? 'private' : 'public',
	graphUri: graphUri,
	remote: remote
    }
}

function name(identified) {

    return identified.name || identified.displayId

}

function id(identified) {

    return identified.displayId /* or last fragment of URI? */

}

function pid(identified) {

    return identified.persistentIdentity

}

function version(identified) {

    return identified.version

}

function wasDerivedFrom(identified,req) {

    if (identified.wasDerivedFrom) {
	wasDerivedFromResult = { uri: identified.wasDerivedFrom.uri?identified.wasDerivedFrom.uri:identified.wasDerivedFrom,
				 url: uriToUrl(identified.wasDerivedFrom,req)
			       }
    }
    return wasDerivedFromResult

}

function wasDerivedFroms(identified,req) {

    if (identified.wasDerivedFroms) {
	wasDerivedFromsResult = identified.wasDerivedFroms.map((wasDerivedFrom) => {
	    return { uri: wasDerivedFrom.uri?wasDerivedFrom.uri:wasDerivedFrom,
		     url: uriToUrl(wasDerivedFrom,req)
		   }
	})
    }
    return wasDerivedFromsResult

}

function wasGeneratedBy(identified,req) {

    if (identified.wasGeneratedBy) {
	wasGeneratedByResult = { uri: identified.wasGeneratedBy.uri?identified.wasGeneratedBy.uri:identified.wasGeneratedBy,
			   url: uriToUrl(identified.wasGeneratedBy,req)
			 }
    }
    return wasGeneratedByResult

}

function wasGeneratedBys(identified,req) {

    if (identified.wasGeneratedBys) {
	wasGeneratedBysResult = identified.wasGeneratedBys.map((wasGeneratedBy) => {
	    return { uri: wasGeneratedBy.uri?wasGeneratedBy.uri:wasGeneratedBy,
		     url: uriToUrl(wasGeneratedBy,req)
		   }
	})
    }
    return wasGeneratedBysResult

}

function description(identified) {

    if(identified.description) {
	descriptionResult = wiky.process(identified.description, {})
	descriptionResult = descriptionResult.split(';').join('<br/>')
        return descriptionResult
    }
    
    var commentAnnotations = comments(identified)

    if(commentAnnotations)
        return commentAnnotations.join('\n')

    return ''
}

function creator(identified) {
    
    var creatorStr = identified.getAnnotations(namespace.dcterms + 'creator')
    if (creatorStr.toString() === '') {
	creatorStr = identified.getAnnotations(namespace.dc + 'creator')
    }
    return {
	description: creatorStr
    }
    
}

function created(identified) {
    
    var resultStr = identified.getAnnotations(namespace.dcterms + 'created')
    if (resultStr) {
	resultStr = resultStr.toString().split('Z')[0]
    }
    return {
	name: resultStr,
	description: resultStr.toString().replace('T',' ').replace('Z','')
    }
}

function modified(identified) {
    
    var resultStr = identified.getAnnotations(namespace.dcterms + 'modified')
    if (resultStr) {
	resultStr = resultStr.toString().split('Z')[0]
    }
    return {
	name: resultStr,
	description: resultStr.toString().replace('T',' ').replace('Z','')
    }
}

function mutableDescription(mutableDescriptionSource,req) {

    var mutableDescriptionResult = ''
    if (mutableDescriptionSource != '') {
	mutableDescriptionResult = shareImages(req,mutableDescriptionSource)
        mutableDescriptionResult = wiky.process(mutableDescriptionSource, {})
    }
    return mutableDescriptionResult

}

function mutableNotes(mutableNotesSource,req) {

    var mutableNotesResult = ''
    if (mutableNotesSource != '') {
	mutableNotesResult = shareImages(req,mutableNotesSource)
	mutableNotesResult = wiky.process(mutableNotesSource, {})
    }
    return mutableNotesResult

}

function source(sourceSource,req) {

    var sourceResult = ''
    if (sourceSource != '') {
	sourceResult = shareImages(req,sourceSource)
        sourceResult = wiky.process(sourceSource, {})
    }
    return sourceResult

}

function labels(identified) {
    
    return identified.getAnnotations(namespace.rdfs + 'label')

}

function comments(identified) {

    return identified.getAnnotations(namespace.rdfs + 'comment')

}

function canEdit(identified,req,remote) {

    if(!remote && req.user) {

        const ownedBy = identified.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy')
        const userUri = config.get('databasePrefix') + 'user/' + req.user.username
        if(ownedBy && ownedBy.indexOf(userUri) > -1) {

            return true
		
        } 

    }
    return false;
    
}

module.exports = summarizeIdentified

