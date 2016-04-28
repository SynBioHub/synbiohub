
var pug = require('pug')

var stack = require('../stack')()
var search = require('../search')

module.exports = function(req, res) {

    if(req.query.q) {
        return res.redirect('/search/' + req.query.q);
    }

    var limit = 50

    var criteria = []

    if(req.params.query) {

        criteria.push(
            search.lucene('dcterms:title', req.params.query)
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

            locals.firstResultNum = 1
            locals.lastResultNum = results.length

            if(results.length === 0)
                locals.firstResultNum = 0

            res.send(pug.renderFile('templates/views/search.jade', locals))
        })
    })
	
};


