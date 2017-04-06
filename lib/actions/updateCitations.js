
const pug = require('pug')

const sparql = require('../sparql/sparql')

const loadTemplate = require('../loadTemplate')

const getUrisFromReq = require('../getUrisFromReq')

const config = require('../config')

const getGraphUriFromTopLevelUri = require('../getGraphUriFromTopLevelUri')

const wiky = require('../wiky/wiky')

var retrieveCitations = require('../citations')

const getOwnedBy = require('../query/ownedBy')

module.exports = function(req, res) {

    const uri = req.body.uri

    const graphUri = getGraphUriFromTopLevelUri(uri,req.user)

    const citations = req.body.value

    var d = new Date();
    var modified = d.toISOString()
    modified = modified.substring(0,modified.indexOf('.'))

    var citationsSparql = ''
    if (citations.trim() != '') {
	citationsSparql = '<' + uri + '> obo:OBI_0001617 ' + citations.replace(/,/g,'; obo:OBI_0001617 ') + ' .'
    } 

    const updateQuery = loadTemplate('./sparql/UpdateCitations.sparql', {
        topLevel: uri,
	citations: citationsSparql,
	modified: JSON.stringify(modified)
    })

    getOwnedBy(uri, graphUri).then((ownedBy) => {

        if(ownedBy.indexOf(config.get('databasePrefix') + 'user/' + req.user.username) === -1) {
            res.status(401).send('not authorized to edit this submission')
            return
        }

        return sparql.updateQuery(updateQuery, graphUri).then((result) => {

            var templateParams = {
                uri: uri
            }

            var getCitationsQuery = loadTemplate('sparql/GetCitations.sparql', templateParams)

            return sparql.queryJson(getCitationsQuery, graphUri).then((results) => {

                citationsQ = results

            }).then(() => {

                return  retrieveCitations(citationsQ).then((resolvedCitations) => {

                    submissionCitations = resolvedCitations;

                    console.log('got citations ' + JSON.stringify(submissionCitations));

                }).then(() => {

                    const locals = {
                        config: config.get(),
                        src: citations,
                        submissionCitations: submissionCitations,
                        canEdit: true
                    }

                    res.send(pug.renderFile('templates/partials/citations.jade', locals))
                })
            })
        })
    })

}



