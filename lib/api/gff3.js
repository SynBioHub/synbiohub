var pug = require('pug')

const { fetchSBOLObjectRecursive } = require('../fetch/fetch-sbol-object-recursive')

var serializeSBOL = require('../serializeSBOL')

var config = require('../config')

var getUrisFromReq = require('../getUrisFromReq')

const convertToGFF3 = require('../conversion/convert-to-gff3')

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

    console.log('-- converting to gff3')

    return saveTempFile().then((tmpFilename) => {
      return convertToGFF3(tmpFilename, {

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
    if (err) {
      const locals = {
        config: config.get(),
        section: 'errors',
        user: req.user,
        errors: [ uri + ' Not Found' ]
      }

      res.send(pug.renderFile('templates/views/errors/errors.jade', locals))
    }
  })
}
