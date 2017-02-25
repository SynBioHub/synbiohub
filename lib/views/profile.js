
const config = require('../config')
const db = require('../db')
const pug = require('pug')

module.exports = function(req, res) {

    const username = req.params.userId

    db.model.User.findOne({
        where: {
            username: username
        }
    }).then((user) => {

        if(!user) {
            res.status(404).send('user not found')
            return
        }


        const locals = {
            config: config.get(),
            section: 'profile',
            user: user
        }

        res.send(pug.renderFile('templates/views/profile.jade', locals))    

    }).catch((err) => {

        res.send(err.stack)

    })



}


