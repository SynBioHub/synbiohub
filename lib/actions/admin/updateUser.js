
const db = require('../../db')

module.exports = function(req, res) {

    db.model.User.findById(req.body.id).then((user) => {

        user.username = req.body.username
        user.name = req.body.name
        user.email = req.body.email
        user.affiliation = req.body.affiliation
        user.isMember = req.body.isMember
        user.isCurator = req.body.isCurator
        user.isAdmin = req.body.isAdmin

        return user.save()

    }).then(() => {

        res.status(200).send('saved')

    }).catch((err) => {

        res.status(500).send(err.stack)

    })




}

