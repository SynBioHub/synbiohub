
var pug = require('pug')

var retrieveCitations = require('../citations');
var fs = require('fs');

var async = require('async');

var SBOLDocument = require('sboljs')

var extend = require('xtend')

var uuid = require('node-uuid');

var serializeSBOL = require('../serializeSBOL')

var request = require('request')

var config = require('../config')

module.exports = function(req, res) {

    var submissionData = {
        name: req.body.name || '',
        createdBy: req.user,
        type: req.body.type || req.params.type || 'private',
        acs: req.body.acs || '',
        citations: req.body.citations || '', // comma separated pubmed IDs
        description: req.body.description || '',
        keywords: req.body.keywords || '',
        chassis: req.body.chassis || '',
        purpose: req.body.purpose || '',
        file: req.body.file || ''
    }

    if(req.method === 'POST') {

        submitPost(req, res, submissionData)

    } else {

        submitForm(req, res, submissionData, {})

    }
}

function submitForm(req, res, submissionData, locals) {
	
    var submissionID = '';

	locals = extend({
        section: 'submit',
        user: req.user,
        submission: submissionData,
        errors: []
    }, locals)

    res.send(pug.renderFile('templates/views/submit.jade', locals))
}
	

function submitPost(req, res, submissionData) {

    var stack = require('../stack')()

    var submissionFile = '';
    var submissionCitations = [];
    var submissionSBOL = null

    var errors = []

    submissionData.name = submissionData.name.trim()
    submissionData.description = submissionData.description.trim()
    submissionData.purpose = submissionData.purpose.trim()

    if(submissionData.name === '') {
        errors.push('Please enter a name for your submission')
    }

    if(submissionData.description === '') {
        errors.push('Please enter a brief description for your submission')
    }

    if(submissionData.purpose === '') {
        errors.push('Please enter a purpose for your submission')
    }

    if(!req.file) {
        errors.push('An SBOL file is required')
    }

    if(errors.length > 0) {

        return submitForm(req, res, submissionData, {
            errors: errors
        })
    }

    var convertedSBOL

    async.series([

        function lookupCitations(next) {

            console.log('lookupCitations');

            retrieveCitations(submissionData.citations, function(err, citations) {

                submissionCitations = citations;

                console.log('got citations ' + JSON.stringify(submissionCitations));

                next(err);

            });
        },

        function convertSBOL(next) {

            var converterURL = config.get('converterURL')

            request.post({

                url: converterURL,
                body: req.file.buffer.toString('utf8'),

            }, (err, response, body) => {

                if(err)
                    return next(err)

                if(response.statusCode >= 300)
                    return next(new Error(response.statusCode + ' ' + body))

                convertedSBOL = body

                next()
            })
        },

        function loadSBOL(next) {

            console.log('loadSBOL');

            SBOLDocument.loadRDF(convertedSBOL, (err, sbol) => {

                if(err)
                    return next(err)

                submissionSBOL = sbol

                next()

            })

        },

        function saveSubmission(next) {

            console.log('saving...');

            var keywords = submissionData.keywords.split(',')
                .map((keyword) => keyword.trim())
                .filter((keyword) => keyword !== '')

            var collection = submissionSBOL.collection()

            collection.addStringAnnotation('http://synbiohub.org#uploadedBy', req.user.email)

            keywords.forEach((keyword) => {
                collection.addStringAnnotation('http://synbiohub.org#keyword', keyword)
            })

            submissionCitations.forEach((citation) => {
                collection.addStringAnnotation('http://synbiohub.org#citation', citation)
            })

            collection.addStringAnnotation('http://synbiohub.org#chassis', submissionData.chassis)
            collection.addStringAnnotation('http://synbiohub.org#purpose', submissionData.purpose)

            collection.uri = 'http://synbiohub.org#collection-' + uuid.v1()
            collection.name = submissionData.name
            collection.description = submissionData.description

            submissionSBOL.componentDefinitions.forEach((componentDefinition) => {
                collection.addMember(componentDefinition)
            })

            var xml = serializeSBOL(submissionSBOL)

            // 

            var store = req.userStore

            store.upload(xml, function(err, result) {

                console.log(result);

                if(err) {

                    next(err);

                } else {

                    res.redirect('/manage');
                }


            })
        }


    ], function done(err) {

        res.status(500).send(err.stack)
                
    })
}




