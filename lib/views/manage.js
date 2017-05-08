
var pug = require('pug')

var search = require('../search')

var async = require('async')

var config = require('../config')

module.exports = function(req, res) {

    var locals = {
        config: config.get(),
        section: 'manage',
        privateSubmissions: [],
        publicSubmissions: [],
        user: req.user
    }

    function submissionIsPublic(submission) {

        for(var i = 0; i < locals.publicSubmissions.length; ++ i)
            if(locals.publicSubmissions[i].id === submission.id)
                return true;
    }

    /*
    var criteria = [
        '?collection a sbol2:Collection .',
        '?collection synbiohub:uploadedBy "' + req.user.email + '" .',
        '?collection sbol2:member ?subject .'
    ].join('\n')*/

    var userCriteria = '{ ?subject synbiohub:uploadedBy "' + req.user.email + '" } UNION { ?subject sbh:ownedBy <' + config.get('databasePrefix') + 'user/' + req.user.username + '> } .'
    if (req.user.isAdmin) {
	userCriteria = '';
    }

    var criteria = [
	'?subject a sbol2:Collection . ' + userCriteria +
	'FILTER NOT EXISTS { ?otherCollection sbol2:member ?subject }'
    ].join('\n')

    var foundURIs = {}

    Promise.all([

        search(null, criteria, undefined, undefined).then((searchRes) => {

            const results = searchRes.results

            locals.publicSubmissions = results.map((result) => {

                result.triplestore = 'public'

                foundURIs[result.uri] = true

                return result

            })

        }),

        search(req.user.graphUri, criteria, undefined, undefined).then((searchRes) => {

            const results = searchRes.results

            locals.privateSubmissions = results.filter((result) => {
                
                return !foundURIs[result.uri]
                
            }).map((result) => {

                result.triplestore = 'private'

                return result

            })

        })

    ]).then(function renderPage(next) {

        locals.removePublicEnabled = config.get('removePublicEnabled')

        res.send(pug.renderFile('templates/views/manage.jade', locals))

    })



};



