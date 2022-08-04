
const config = require('../config')

module.exports = function (req, res) {
  const category = req.query.category

  if (!category) {
    return res.status(200).send(config.get('plugins'))
  } else {
    return res.status(200).send(config.get('plugins')[category])
  }
}
