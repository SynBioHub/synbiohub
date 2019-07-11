const uuid = require('uuid/v4')

let responses = {}

function fetch (id) {
  return responses[id]
}

function clear (id) {
  delete responses[id]
}

function prepare (id) {
  responses[id] = 'waiting...'
}

function resolve (id, content) {
  console.log('Setting ' + id)
  responses[id] = content
}

function serve (req, res) {
  let id = req.params.id

  if (req.method === 'DELETE') {
    clear(id)
    return 'Deleted.'
  }

  let content = fetch(id)

  switch (content) {
    case undefined:
      console.error(`stream ${id} does not exist`)
      res.sendStatus(404)
      return
    case 'waiting...':
      console.warn(`stream ${id} not yet completed`)
      res.set('Retry-After', '1').status(503).end()
      return
    default:
      console.log(`completing stream ${id}`)
      res.send(content)
  }
}

function create (promise) {
  let id = uuid()

  prepare(id)

  promise.then((response) => {
    resolve(id, response)
  }).catch((err) => {
    console.error(`Deleting stream ${id} because an error was encountered`)
    clear(id)
    console.error(err)
  })

  return id
}

module.exports = {
  serve: serve,
  create: create,
  fetch: fetch,
  clear: clear,
  prepare: prepare,
  resolve: resolve
}
