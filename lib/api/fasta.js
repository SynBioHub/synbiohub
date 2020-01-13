
const { getType } = require('../query/type')

var config = require('../config')

// var fastaCollection = require('./fastaCollection')

var fastaComponentDefinition = require('./fastaComponentDefinition')
var fastaCollection = require('./fastaCollection')

// var fastaModule = require('./fastaModule')

var fastaSequence = require('./fastaSequence')

var pug = require('pug')

var getUrisFromReq = require('../getUrisFromReq')

module.exports = function (req, res) {
  const { graphUri, uri } = getUrisFromReq(req, res)

  getType(uri, graphUri).then((result) => {
    if (result && result === 'http://sbols.org/v2#ComponentDefinition') {
      fastaComponentDefinition(req, res)
    } else if (result && result === 'http://sbols.org/v2#Sequence') {
      fastaSequence(req, res)
    } else if (result && result === 'http://sbols.org/v2#Collection') {
      fastaCollection(req, res)
    } else {
      if (!req.accepts('text/html')) {
        return res.status(422).send(uri + ' is a ' + result +
                                    ' FASTA conversion not supported for this type.')
      } else {
        var locals = {
          config: config.get(),
          section: 'errors',
          user: req.user,
          errors: [ uri + ' is a ' + result + '.',
            'FASTA conversion not supported for this type.' ]
        }
        res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
      }
    }
  }).catch((err) => {
    if (!req.accepts('text/html')) {
      return res.status(404).send(uri + ' not found')
    } else {
      var locals = {
        config: config.get(),
        section: 'errors',
        user: req.user,
        errors: [ err.stack ]
      }
      res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
    }
  })
}
