const pug = require('pug')
const config = require('../config')

module.exports = function (req, res) {
  let locals = {
    config: config.get(),
    streamId: req.params.id
  }

  res.send(pug.renderFile('templates/layouts/stream.jade', locals))
}
