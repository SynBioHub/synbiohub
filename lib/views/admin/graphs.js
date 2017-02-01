
var pug = require('pug')

var sparql = require('../../sparql/sparql')

module.exports = function(req, res) {

    const query = [
        'SELECT DISTINCT ?graph WHERE {',
            'GRAPH ?graph { ?s ?a ?t }',
        '}'
    ].join('\n')

    sparql.queryJson(query, null).then((results) => {

        var locals = {
            section: 'admin',
            adminSection: 'graphs',
            user: req.user,
            graphs: results.map((result) => result.graph)
        }

        res.send(pug.renderFile('templates/views/admin/graphs.jade', locals))

    }).catch((err) => {

        res.status(500).send(err.stack)

    })

};
