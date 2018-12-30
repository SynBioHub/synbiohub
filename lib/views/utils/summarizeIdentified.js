
var namespace = require('./namespace')

const uriToUrl = require('../../uriToUrl')

const shareImages = require('../../shareImages')

var wiky = require('../../wiky/wiky.js');

var URI = require('sboljs').URI

var filterAnnotations = require('../../filterAnnotations')

var config = require('../../config')

var sha1 = require('sha1');

function summarizeIdentified(topLevel,req,sbol,remote,graphUri) {

    if (topLevel instanceof URI)
	return {
            uri: topLevel + '',
	    id: topLevel + ''
	}

    var mutableDescriptionSource = topLevel.getAnnotations(namespace.synbiohub + 'mutableDescription').toString() || '';
    var mutableNotesSource = topLevel.getAnnotations(namespace.synbiohub + 'mutableNotes').toString() || '';
    var sourceSource = topLevel.getAnnotations(namespace.synbiohub + 'mutableProvenance').toString() || '';
    
    return {
        uri: topLevel.uri + '',
	url: url(topLevel,req),
        name: name(topLevel),
        id: id(topLevel),
        pid: pid(topLevel),
        version: version(topLevel),
        wasDerivedFrom: wasDerivedFrom(topLevel,req),
        wasDerivedFroms: wasDerivedFroms(topLevel,req),
        wasGeneratedBy: wasGeneratedBy(topLevel,req),
        wasGeneratedBys: wasGeneratedBys(topLevel,req),
	creator: creator(topLevel),
	created: created(topLevel),
	modified: modified(topLevel),
	mutableDescriptionSource: mutableDescriptionSource,
        mutableDescription: mutableDescription(mutableDescriptionSource,req),
        mutableNotesSource: mutableNotesSource,
        mutableNotes: mutableNotes(mutableNotesSource,req),
        sourceSource: sourceSource,
        source: source(sourceSource,req),
        description: description(topLevel),
	comments: comments(topLevel),
        labels: labels(topLevel),
	canEdit: canEdit(topLevel,req,remote),
        annotations: filterAnnotations(req,topLevel.annotations),
	triplestore: graphUri ? 'private' : 'public',
	remote: remote
    }
}

function url(topLevel,req) {
    urlResult = '/' + topLevel.uri.toString().replace(config.get('databasePrefix'),'')
    if (req.url.toString().endsWith('/share')) {
	urlResult += '/' + sha1('synbiohub_' + sha1(topLevel.uri) + config.get('shareLinkSalt')) + '/share'
    }
    return urlResult;
}

function name(topLevel) {

    return topLevel.name || topLevel.displayId

}

function id(topLevel) {

    return topLevel.displayId /* or last fragment of URI? */

}

function pid(topLevel) {

    return topLevel.persistentIdentity

}

function version(topLevel) {

    return topLevel.version

}

function wasDerivedFrom(topLevel,req) {

    if (topLevel.wasDerivedFrom) {
	wasDerivedFromResult = { uri: topLevel.wasDerivedFrom.uri?topLevel.wasDerivedFrom.uri:topLevel.wasDerivedFrom,
				 url: uriToUrl(topLevel.wasDerivedFrom,req)
			       }
    }
    return wasDerivedFromResult

}

function wasDerivedFroms(topLevel,req) {

    if (topLevel.wasDerivedFroms) {
	wasDerivedFromsResult = topLevel.wasDerivedFroms.map((wasDerivedFrom) => {
	    return { uri: wasDerivedFrom.uri?wasDerivedFrom.uri:wasDerivedFrom,
		     url: uriToUrl(wasDerivedFrom,req)
		   }
	})
    }
    return wasDerivedFromsResult

}

function wasGeneratedBy(topLevel,req) {

    if (topLevel.wasGeneratedBy) {
	wasGeneratedByResult = { uri: topLevel.wasGeneratedBy.uri?topLevel.wasGeneratedBy.uri:topLevel.wasGeneratedBy,
			   url: uriToUrl(topLevel.wasGeneratedBy,req)
			 }
    }
    return wasGeneratedByResult

}

function wasGeneratedBys(topLevel,req) {

    if (topLevel.wasGeneratedBys) {
	wasGeneratedBysResult = topLevel.wasGeneratedBys.map((wasGeneratedBy) => {
	    return { uri: wasGeneratedBy.uri?wasGeneratedBy.uri:wasGeneratedBy,
		     url: uriToUrl(wasGeneratedBy,req)
		   }
	})
    }
    return wasGeneratedBysResult

}

function description(topLevel) {

    if(topLevel.description) {
	descriptionResult = wiky.process(topLevel.description, {})
	descriptionResult = descriptionResult.split(';').join('<br/>')
        return descriptionResult
    }
    
    var commentAnnotations = comments(topLevel)

    if(commentAnnotations)
        return commentAnnotations.join('\n')

    return ''
}

function creator(topLevel) {
    
    var creatorStr = topLevel.getAnnotations(namespace.dcterms + 'creator')
    if (creatorStr.toString() === '') {
	creatorStr = topLevel.getAnnotations(namespace.dc + 'creator')
    }
    return {
	description: creatorStr
    }
    
}

function created(topLevel) {
    
    
    var resultStr = topLevel.getAnnotations(namespace.dcterms + 'created')
    return {
	name: resultStr,
	description: resultStr.toString().replace('T',' ').replace('Z','')
    }
}

function modified(topLevel) {
    
    var resultStr = topLevel.getAnnotations(namespace.dcterms + 'modified')
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

function labels(topLevel) {
    
    return topLevel.getAnnotations(namespace.rdfs + 'label')

}

function comments(topLevel) {

    return topLevel.getAnnotations(namespace.rdfs + 'comment')

}

function canEdit(topLevel,req,remote) {

    if(!remote && req.user) {

        const ownedBy = topLevel.getAnnotations('http://wiki.synbiohub.org/wiki/Terms/synbiohub#ownedBy')
        const userUri = config.get('databasePrefix') + 'user/' + req.user.username
        if(ownedBy && ownedBy.indexOf(userUri) > -1) {

            return true
		
        } 

    }
    return false;
    
}

module.exports = summarizeIdentified

