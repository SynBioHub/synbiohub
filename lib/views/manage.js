
var pug = require('pug')

var search = require('../search')

var async = require('async')

module.exports = function(req, res) {

    var stack = require('../stack')()

    var locals = {
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

    var criteria = [
        '?subject synbiohub:uploadedBy "' + req.user.email + '" .'
    ].join('\n')

    var foundURIs = {}

    stack.getPrefixes((err, prefixes) => {

        if(err) {
            res.status(500).send(err);
            return
        }

        async.series([

            function retrievePublicSubmissions(next) {

                search(stack.getDefaultStore(), prefixes, criteria, undefined, undefined, (err, count, results) => {

                    locals.publicSubmissions = results.map((result) => {

                        result.triplestore = 'public'

                        foundURIs[result.uri] = true

                        return result

                    })

                    next()

                })

            },

            function retrievePrivateSubmissions(next) {

                search(req.userStore, prefixes, criteria, undefined, undefined, (err, count, results) => {

                    locals.privateSubmissions = results.filter((result) => {
                        
                        return !foundURIs[result.uri]
                        
                    }).map((result) => {

                        result.triplestore = 'private'

                        return result

                    })

                    next()

                })
            },
            
            function renderPage(next) {

                res.send(pug.renderFile('templates/views/manage.jade', locals))

            }
        ])

    })

};



