const uuid = require('uuid/v4')

let responses = {}

function serve(req, res) {
    let id = req.params.id

    switch (responses[id]) {
        case undefined:
            console.log(`stream ${id} does not exist`)
            res.sendStatus(404)
            return
        case "waiting...":
            console.log(`stream ${id} not yet completed`)
            res.set('Retry-After', '1').status(503).end()
            return
        default:
            console.log(`completing stream ${id}`)
            res.send(responses[id])
            delete responses[id]
            return
    }
}

function create(request) {
    let id = uuid()

    responses[id] = "waiting..."

    request.then((response) => {
        if (response.statusCode == 404) {
            delete responses[id]
        } else {
            responses[id] = response
        }
    })

    return id
}


module.exports = {
    serve: serve,
    create: create
}
