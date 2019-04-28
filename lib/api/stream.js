const uuid = require('uuid/v4')

let responses = {}

function serve (req, res) {
  let id = req.params.id

  if (req.method === 'DELETE') {
    if (responses[id]) {
      console.log(`deleting stream ${id}`)
      delete responses[id]
      return 'Deleted.'
    }
  }

  switch (responses[id]) {
    case undefined:
      console.log(`stream ${id} does not exist`)
      res.sendStatus(404)
      return
    case 'waiting...':
      console.log(`stream ${id} not yet completed`)
      res.set('Retry-After', '1').status(503).end()
      return
    default:
      console.log(`completing stream ${id}`)
      res.send(responses[id])
  }
}

function create (promise) {
  let id = uuid()

  responses[id] = 'waiting...'

  promise.then((response) => {
    responses[id] = response
  }).catch((err) => {
    console.log(`Deleting stream ${id} because an error was encountered`)
    delete responses[id]
    console.log(err)
  })

  return id
}

module.exports = {
  serve: serve,
  create: create
}
