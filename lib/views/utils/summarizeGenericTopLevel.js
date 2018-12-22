
var namespace = require('./namespace')

const uriToUrl = require('../../uriToUrl')

const shareImages = require('../../shareImages')

var wiky = require('../../wiky/wiky.js');

var URI = require('sboljs').URI

const attachments = require('../../attachments')

var filterAnnotations = require('../../filterAnnotations')

var config = require('../../config')

var sha1 = require('sha1');

function summarizeGenericTopLevel(genericTopLevel,req,sbol,remote,graphUri) {

    if (genericTopLevel instanceof URI)
	return {
            uri: genericTopLevel + '',
	    id: genericTopLevel + ''
	}

    var mutableDescriptionSource = genericTopLevel.getAnnotations(namespace.synbiohub + 'mutableDescription').toString() || '';
    var mutableNotesSource = genericTopLevel.getAnnotations(namespace.synbiohub + 'mutableNotes').toString() || '';
    var sourceSource = genericTopLevel.getAnnotations(namespace.synbiohub + 'mutableProvenance').toString() || '';
    
    return {
        uri: genericTopLevel.uri + '',
	url: url(genericTopLevel,req),
        name: name(genericTopLevel),
        id: id(genericTopLevel),
        pid: pid(genericTopLevel),
        version: version(genericTopLevel),
        wasDerivedFrom: wasDerivedFrom(genericTopLevel,req),
        wasDerivedFroms: wasDerivedFroms(genericTopLevel,req),
        wasGeneratedBy: wasGeneratedBy(genericTopLevel,req),
        wasGeneratedBys: wasGeneratedBys(genericTopLevel,req),
	creator: creator(genericTopLevel),
	created: created(genericTopLevel),
	modified: modified(genericTopLevel),
	mutableDescriptionSource: mutableDescriptionSource,
        mutableDescription: mutableDescription(mutableDescriptionSource,req),
        mutableNotesSource: mutableNotesSource,
        mutableNotes: mutableNotes(mutableNotesSource,req),
        sourceSource: sourceSource,
        source: source(sourceSource,req),
        description: description(genericTopLevel),
        attachments: attachments.getAttachmentsFromTopLevel(sbol, genericTopLevel, req.url.toString().endsWith('/share')),
	comments: comments(genericTopLevel),
        labels: labels(genericTopLevel),
	canEdit: canEdit(genericTopLevel,req,remote),
        annotations: filterAnnotations(req,genericTopLevel.annotations),
	triplestore: graphUri ? 'private' : 'public',
	remote: remote
    }
}

function url(genericTopLevel,req) {
    urlResult = '/' + genericTopLevel.uri.toString().replace(config.get('databasePrefix'),'')
    if (req.url.toString().endsWith('/share')) {
	urlResult += '/' + sha1('synbiohub_' + sha1(genericTopLevel.uri) + config.get('shareLinkSalt')) + '/share'
    }
    return urlResult;
}

function name(genericTopLevel) {

    return genericTopLevel.name || genericTopLevel.displayId

}

function id(genericTopLevel) {

    return genericTopLevel.displayId /* or last fragment of URI? */

}

function pid(genericTopLevel) {

    return genericTopLevel.persistentIdentity

}

function version(genericTopLevel) {

    return genericTopLevel.version

}

function wasDerivedFrom(genericTopLevel,req) {

    if (genericTopLevel.wasDerivedFrom) {
	wasDerivedFromResult = { uri: genericTopLevel.wasDerivedFrom.uri?genericTopLevel.wasDerivedFrom.uri:genericTopLevel.wasDerivedFrom,
				 url: uriToUrl(genericTopLevel.wasDerivedFrom,req)
			       }
    }
    return wasDerivedFromResult

}

function wasDerivedFroms(genericTopLevel,req) {

    if (genericTopLevel.wasDerivedFroms) {
	wasDerivedFromsResult = genericTopLevel.wasDerivedFroms.map((wasDerivedFrom) => {
	    return { uri: wasDerivedFrom.uri?wasDerivedFrom.uri:wasDerivedFrom,
		     url: uriToUrl(wasDerivedFrom,req)
		   }
	})
    }
    return wasDerivedFromsResult

}

function wasGeneratedBy(genericTopLevel,req) {

    if (genericTopLevel.wasGeneratedBy) {
	wasGeneratedByResult = { uri: genericTopLevel.wasGeneratedBy.uri?genericTopLevel.wasGeneratedBy.uri:genericTopLevel.wasGeneratedBy,
			   url: uriToUrl(genericTopLevel.wasGeneratedBy,req)
			 }
    }
    return wasGeneratedByResult

}

function wasGeneratedBys(genericTopLevel,req) {

    if (genericTopLevel.wasGeneratedBys) {
	wasGeneratedBysResult = genericTopLevel.wasGeneratedBys.map((wasGeneratedBy) => {
	    return { uri: wasGeneratedBy.uri?wasGeneratedBy.uri:wasGeneratedBy,
		     url: uriToUrl(wasGeneratedBy,req)
		   }
	})
    }
    return wasGeneratedBysResult

}

function description(genericTopLevel) {

    if(genericTopLevel.description) {
	descriptionResult = wiky.process(genericTopLevel.description, {})
	descriptionResult = descriptionResult.split(';').join('<br/>')
        return descriptionResult
    }
    
    var commentAnnotations = comments(genericTopLevel)

    if(commentAnnotations)
        return commentAnnotations.join('\n')

    return ''
}

function creator(genericTopLevel) {
    
    var creatorStr = genericTopLevel.getAnnotations(namespace.dcterms + 'creator')
    if (creatorStr.toString() === '') {
	creatorStr = genericTopLevel.getAnnotations(namespace.dc + 'creator')
    }
    return {
	description: creatorStr
    }
    
}

function created(genericTopLevel) {
    
    
    var resultStr = genericTopLevel.getAnnotations(namespace.dcterms + 'created')
    return {
	name: resultStr,
	description: resultStr.toString().replace('T',' ').replace('Z','')
    }
}

function modified(genericTopLevel) {
    
    var resultStr = genericTopLevel.getAnnotations(namespace.dcterms + 'modified')
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

function labels(genericTopLevel) {
    
    return genericTopLevel.getAnnotations(namespace.rdfs + 'label')

}

function comments(genericTopLevel) {

    return genericTopLevel.getAnnotations(namespace.rdfs + 'comment')

}

function canEdit(genericTopLevel,req,remote) {

    if(!remote && req.user) {

        const ownedBy = genericTopLevel.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy')
        const userUri = config.get('databasePrefix') + 'user/' + req.user.username
        if(ownedBy && ownedBy.indexOf(userUri) > -1) {

            return true
		
        } 

    }
    return false;
    
}

module.exports = summarizeGenericTopLevel

