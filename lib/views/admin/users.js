
const pug = require('pug')

const sparql = require('../../sparql/sparql')

const db = require('../../db')

const config = require('../../config')

module.exports = function(req, res) {

    db.model.User.findAll().then((users) => {

        var locals = {
            config: config.get(),
            section: 'admin',
            adminSection: 'users',
            user: req.user,
            users: users,
            canSendEmail: config.get('mail').sendgridApiKey != ""
        }

        res.send(pug.renderFile('templates/views/admin/users.jade', locals))

    }).catch((err) => {

        res.status(500).send(err.stack)

    })


};
