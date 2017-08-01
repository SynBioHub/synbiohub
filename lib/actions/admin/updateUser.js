
const db = require('../../db')
const config = require('../../config')

module.exports = function(req, res) {

    db.model.User.findById(req.body.id).then((user) => {

        user.username = req.body.username
        user.name = req.body.name
        user.email = req.body.email
        user.affiliation = req.body.affiliation
        user.isMember = req.body.isMember
        user.isCurator = req.body.isCurator
        user.isAdmin = req.body.isAdmin

        if(req.body.isMaintainer) {
            config.set('administratorEmail', req.body.email);
        }

        return user.save()

    }).then(() => {

        res.status(200).send('saved')

    }).catch((err) => {

        res.status(500).send(err.stack)

    })




}

