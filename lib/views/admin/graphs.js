
var pug = require('pug')

var sparql = require('../../sparql/sparql')

module.exports = function(req, res) {

    const query = [
        'SELECT DISTINCT ?graph WHERE {',
            'GRAPH ?graph { ?s ?a ?t }',
        '}'
    ].join('\n')

    sparql.queryJson(query, null).then((results) => {

        const graphs = results.map((result) => result.graph)

        return Promise.all(
            graphs.map((graph) => graphInfo(graph))
        )

    }).then((graphs) => {

        var locals = {
            section: 'admin',
            adminSection: 'graphs',
            user: req.user,
            graphs: graphs
        }

        res.send(pug.renderFile('templates/views/admin/graphs.jade', locals))

    }).catch((err) => {

        res.status(500).send(err.stack)

    })

    function graphInfo(graphUri) {

        const countTriplesQuery =  [
            'SELECT (COUNT(*) as ?count) WHERE {',
                '?s ?p ?o .',
            '}'
        ].join('\n')

        return sparql.queryJson(countTriplesQuery, graphUri).then((results) => {

            return Promise.resolve({
                graphUri: graphUri,
                numTriples: results[0].count
            })

        })

    }

};
