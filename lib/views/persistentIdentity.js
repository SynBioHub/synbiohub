
var { getVersion } = require('../query/version')

var async = require('async')

var config = require('../config')

var pug = require('pug')

var getUrisFromReq = require('../getUrisFromReq')

var topLevel = require('./topLevel')

module.exports = function (req, res) {
  const { graphUri, uri, designId, url } = getUrisFromReq(req, res)

  getVersion(uri, graphUri).then((result) => {
    res.redirect(url + '/' + result)
  }).catch((err) => {
    topLevel(req, res)
  })
}
