
var pug = require('pug')

var stack = require('../stack')()
var search = require('../search')

var base64 = require('../base64')

module.exports = function(req, res) {

    if(req.query.q) {
        return res.redirect('/search/' + req.query.q);
    }

    var limit = 50

    var criteria = []

    if(req.params.query) {

        criteria.push(
            search.lucene(req.params.query)
        )

    }

    if(req.params.collectionURI) {
	criteria.push(
            '   ?collection a sbol2:Collection .' +
		'   <' + base64.decode(req.params.collectionURI) + '> sbol2:member ?subject .'
	)
    }

    if(req.params.roleURI) {
	criteria.push(
	    '   ?subject sbol2:role ' + '<' + base64.decode(req.params.roleURI) + '> .'
	)
    }

    if(req.params.typeURI) {
	criteria.push(
	    '   ?subject sbol2:type ' + '<' + base64.decode(req.params.typeURI) + '> .'
	)
    }

    if(req.params.igemResultsURI) {
	criteria.push(
	    '   ?subject igem:results ' + '<' + base64.decode(req.params.igemResultsURI) + '> .'
	)
    }

    if(req.params.igemStatusURI) {
	criteria.push(
	    '   ?subject igem:status ' + '<' + base64.decode(req.params.igemStatusURI) + '> .'
	)
    }

    if(req.params.igemPartStatusStr) {
	criteria.push(
	    '   ?subject igem:partStatus ' + '\'' + base64.decode(req.params.igemPartStatusStr) + '\' .'
	)
    }

    if(req.params.creatorStr) {
	criteria.push(
	    '   ?subject dcterms:creator ' + '\'' + base64.decode(req.params.creatorStr) + '\' .'
	)
    }

    if(req.params.componentURI) {
	criteria.push(
            '   ?subject sbol2:sequenceAnnotation ?sa .' +
	    '   ?sa sbol2:component ?comp .' +
	    '       ?comp sbol2:definition <' + base64.decode(req.params.componentURI) + '> .'
	)
    }

    if(req.params.twinURI) {
	criteria.push(
            '   ?subject sbol2:sequence ?seq .' +
	    '   ?seq sbol2:elements ?elements .' +
	    '   <' + base64.decode(req.params.twinURI) + '> a sbol2:ComponentDefinition .' +
	    '   <' + base64.decode(req.params.twinURI) + '> sbol2:sequence ?seq2 .' +
	    '   ?seq2 sbol2:elements ?elements2 .' +
            '   FILTER(?subject != <' + base64.decode(req.params.twinURI) + '> && ?elements = ?elements2)'
	)
    }

    //if(req.user)
        //criteria.createdBy = req.user;

    // type, storeUrl, query, callback

	var locals = {
        section: 'search',
        user: req.user
    }

    stack.getPrefixes((err, prefixes) => {

        if(err) {
            res.status(500).send(err);
            return
        }

        search(stack.getDefaultStore(), prefixes, 'ComponentDefinition', criteria, req.query.offset, limit, (err, count, results) => {

            if(err) {
                res.status(500).send(err);
                return
            }

            locals.numResultsTotal = count

            locals.section = 'search';
            locals.searchQuery = req.params.query;
            locals.searchResults = results
	    if (req.originalUrl.indexOf("/?offset") !== -1) {
		locals.originalUrl = req.originalUrl.substring(0,req.originalUrl.indexOf("/?offset"))
	    } else {
		locals.originalUrl = req.originalUrl
	    }

	    if (req.query.offset) {
		locals.firstResultNum = parseInt(req.query.offset) + 1
		if (count < parseInt(req.query.offset) + results.length) {
		    locals.lastResultNum = count
		} else { 
		    locals.lastResultNum = parseInt(req.query.offset) + results.length
		}
	    } else {
		locals.firstResultNum = 1
		if (count < results.length) {
		    locals.lastResultNum = count
		} else { 
		    locals.lastResultNum = results.length
		}
	    }

            if(results.length === 0)
                locals.firstResultNum = 0

            res.send(pug.renderFile('templates/views/search.jade', locals))
        })
    })
	
};


