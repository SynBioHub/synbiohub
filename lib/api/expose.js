const {uuid: uuidv4 } = require('uuid')
const exposeLifetime = 10 * 60 * 1000 // 10 minutes

let exposes = {}

function createExpose (filename) {
  let id = uuid()
  exposes[id] = filename

  // Delete expose after lifetime expires
  setTimeout(() => { delete exposes[id] }, exposeLifetime)
  return id
}

function serveExpose (req, res) {
  let id = req.params.id
  let filename = exposes[id]

  if (!filename) {
    res.sendStatus(404).end()
    return
  }

  res.sendFile(filename)
}

module.exports = {
  createExpose: createExpose,
  serveExpose: serveExpose
}
