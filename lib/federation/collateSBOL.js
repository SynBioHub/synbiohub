
var async = require('async')

var SBOLDocument = require('sboljs')

const getRdfSerializeAttribs = require('../getRdfSerializeAttribs')

function collateSBOL(documents, res) {

    res.header('content-type', 'application/rdf+xml')

    if(documents.length === 0) {

        /* no results?
         */

        return res.status(404).send('No results to collate')
    }

    //if(documents.length === 1) {

        /* only one result; no collation required
         */

        //return res.send(documents[0].body)
    //}

    var collatedSBOL = new SBOLDocument()

    async.eachSeries(documents, (document, next) => {

        var done = false

        collatedSBOL.loadRDF(document.body, (err) => {

            if(!done) {
                next(err)
                done = true
            }

        })

    }, (err) => {

        const xml = collatedSBOL.serializeXML(getRdfSerializeAttribs())

        res.send(xml)
    })

}

module.exports = collateSBOL

