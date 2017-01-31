
var sparql = require('../sparql/sparql');

var spoSearchPredicates = {

        'name': {
            predicate: 'dcterms:title',
            type: 'string',
            
        },

        'description': {
            predicate: 'dcterms:description',
            type: 'string'
        },

        'role': {
            predicate: 'sbol2:role',
            type: 'uri'
        },

        /*'type': {
            predicate: 'a',
            type: 'uri'
        }*/

        'type': {
            predicate: 'sbol2:type',
            type: 'uri'
        },
};

var opsSearchPredicates = {

    'collection': {
        predicate: 'sbol2:member',
    },
};

function constructQuery(type, criteria) {

    if(Array.isArray(criteria) !== true)
        return '';

    return criteria.map(function(opts) {

        var property = spoSearchPredicates[opts.key];

        if(property === undefined) {

            property = opsSearchPredicates[opts.key]

            if(property === undefined) {
                return '';
            }

            return sparql.escapeIRI(opts.value) + ' ' + property.predicate + ' ?' + type + ' .'
        }

        if(property.type === 'uri') {

            return '?' + type + ' ' + property.predicate
                    + ' ' + sparql.escapeIRI(opts.value) + ' .';

        } else if(property.type === 'string') {

            return sparql.escape(
                '?' + type + ' ' + property.predicate + ' ?p .' +
                '?p bif:contains "%L" .',
            opts.value
            )

        }

    }).join(' ');
}

module.exports = constructQuery;

