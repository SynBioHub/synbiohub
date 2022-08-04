
const config = require('../config')

module.exports = function (req, res) {
  return res.status(200).send(config.get('plugins'))
}
