
const db = require('../../db')

module.exports = function(req, res) {

    db.model.User.findById(req.body.id).then((user) => {

        user.destroy()

    }).then(() => {

        res.status(200).send('saved')

    }).catch((err) => {

        res.status(500).send(err.stack)

    })




}

