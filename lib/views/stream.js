const pug = require('pug')
const config = require('../config')
const stream = require('../api/stream')
const uuid = require('uuid/v4')

function download (req, res, content) {
  let data = content.body
  let filename = content.filename

  if (!filename || filename === '') {
    filename = uuid()
  }

  res.attachment(filename)
  res.send(data)
}

function spin (req, res, id) {
  let locals = {
    config: config.get(),
    streamId: id
  }

  res.send(pug.renderFile('templates/layouts/stream.jade', locals))
}

module.exports = function (req, res) {
  let id = req.params.id
  let content = stream.fetch(id)

  if (!content || content.status !== 'resolved') {
    return spin(req, res, id)
  }

  return download(req, res, content)
}
