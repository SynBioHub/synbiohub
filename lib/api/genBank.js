var pug = require('pug')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

const convertToGenBank = require('../conversion/convert-to-genbank')

const tmp = require('tmp-promise')

var fs = require('mz/fs')

module.exports = function (req, res) {
  const { graphUri, uri } = getUrisFromReq(req, res)

  var sbol

  function saveTempFile () {
    return tmp.tmpName().then((tmpFilename) => {
      return fs.writeFile(tmpFilename, serializeSBOL(sbol)).then(() => {
        return Promise.resolve(tmpFilename)
      })
    })
  }

  fetchSBOLObjectRecursive(uri, graphUri).then((result) => {
    sbol = result.sbol

    console.log('-- converting to genbank')

    return saveTempFile().then((tmpFilename) => {
      return convertToGenBank(tmpFilename, {

      }).then((result) => {
        const { success, log, errorLog } = result

        if (!success) {
          const locals = {
            config: config.get(),
            section: 'invalid',
            user: req.user,
            errors: [ errorLog ]
          }

          return fs.unlink(tmpFilename).then(() => {
            if (!req.accepts('text/html')) {
              return res.status(422).send(errorLog)
            } else {
              res.send(pug.renderFile('templates/views/errors/invalid.jade', locals))
            }
          })
        } else {
          return fs.unlink(tmpFilename).then(() => {
            res.status(200)
            res.header('Content-Disposition', 'attachment; filename="' + req.params.displayId + '.gb"')
            res.header('Access-Control-Expose-Headers', 'Content-Disposition')
            res.header('content-type', 'text/plain').send(log)
          })
          // res.header('content-type', 'text/plain').send(log);
        }
      })
    })
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
