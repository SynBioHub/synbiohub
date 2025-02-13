const {v4: uuidv4 } = require('uuid')

let responses = {}

function fetch (id) {
  if (!responses[id]) {
    console.error('Stream ' + id + ' status is not found')
  }
  return responses[id]
}

function clear (id) {
  console.log('clear ' + id)
  responses[id] = {
    status: 'cleared'
  }
}

function prepare (id, plugin) {
  console.log('prepare ' + id)
  responses[id] = {
    status: 'first access',
    plugin: plugin
  }

  plugin.evaluate().then(result => {
    if (!result) {
      responses[id].status = 'gone'
    }
  }).catch(err => {
    console.error(err)
    responses[id].status = 'gone'
  })
}

function resolve (id) {
  console.log('resolve ' + id)
  let plugin = responses[id].plugin
  responses[id].status = 'waiting...'

  plugin.run().then((response) => {
    console.log('Setting ' + id)
    responses[id].body = response.body
    responses[id].filename = response.filename
    responses[id].status = 'resolved'
  }).catch((err) => {
    console.error(`Deleting stream ${id} because an error was encountered`)
    clear(id)
    console.error(err)
  })
}

function serve (req, res) {
  let id = req.params.id

  if (req.method === 'DELETE') {
    clear(id)
    return 'Cleared.'
  }

  let content = fetch(id) || { status: 'gone' }

  switch (content.status) {
    case 'gone':
      console.error(`stream ${id} does not exist`)
      res.sendStatus(404)
      return
    case 'first access':
      console.warn(`resolving stream ${id}`)
      resolve(id)
      res.set('Retry-After', '1').status(503).end()
      return
    case 'waiting...':
      console.warn(`stream ${id} not yet completed`)
      res.set('Retry-After', '1').status(503).end()
      return
    case 'resolved':
      console.log(`completing stream ${id}`)
      res.send(content.body)
      return
    case 'cleared':
      console.log(`cleared stream ${id}`)
      res.sendStatus(410)
      return
    default:
      console.error(`Stream ${id} is in bad state`)
      res.sendStatus(500)
  }
}

function create (plugin) {
  let id = uuidv4()
  console.log('creating stream ' + id)
  prepare(id, plugin)

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
