
var pug = require('pug')

var stack = require('../stack')()
var search = require('../search')

module.exports = function(req, res) {

    var properties = [
        {
            name: 'Name',
            predicate: 'dcterms:title'
        },
        {
            name: 'Role',
            predicate: 'sbol2:role'
        },
        {
            name: 'Type',
            predicate: 'sbol2:type',
            values: [
                {
                    name: 'DNA',
                    value: 'foo'
                },
                {
                    name: 'RNA',
                    value: 'foo'
                },
                {
                    name: 'Protein',
                    value: 'foo'
                }
            ]
        },
        {
            name: 'Encoded By',
            predicate: 'sybiont:encodedBy'
        },
        {
            name: 'Encodes',
            predicate: 'sybiont:encodedBy'
        }

    ]

	var locals = {
        section: 'advanced-search',
        user: req.user,
        properties: properties
    }

    res.send(pug.renderFile('templates/views/advanced-search.jade', locals))
	
};


