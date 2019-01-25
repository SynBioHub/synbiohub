var pug = require('pug')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var serializeSBOL = require('../serializeSBOL')

var request = require('request')

var SBOLDocument = require('sboljs')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

const convertToGenBank = require('../conversion/convert-to-genbank')

const tmp = require('tmp-promise')

var fs = require('mz/fs')

module.exports = function (req, res) {
  const { graphUri, uri, designId, share } = getUrisFromReq(req, res)

  var sbol
  var componentDefinition

  function saveTempFile () {
    return tmp.tmpName().then((tmpFilename) => {
      return fs.writeFile(tmpFilename, serializeSBOL(sbol)).then(() => {
        return Promise.resolve(tmpFilename)
      })
    })
  }

  fetchSBOLObjectRecursive(uri, graphUri).then((result) => {
    sbol = result.sbol
    componentDefinition = result.object

    console.log('-- converting to genbank')

    return saveTempFile().then((tmpFilename) => {
      return convertToGenBank(tmpFilename, {

      }).then((result) => {
        const { success, log, errorLog, resultFilename } = result

        if (!success) {
          const locals = {
            config: config.get(),
            section: 'invalid',
            user: req.user,
            errors: [ errorLog ]
          }

          return fs.unlink(tmpFilename).then(() => {
            res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))
          })
        } else {
          return fs.unlink(tmpFilename).then(() => {
            res.header('content-type', 'text/plain').send(log)
          })
          // res.header('content-type', 'text/plain').send(log);
        }
      })
    })
  }).catch((err) => {
    const locals = {
      config: config.get(),
      section: 'errors',
      user: req.user,
      errors: [ uri + ' Not Found' ]
    }

    res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
  })
}
