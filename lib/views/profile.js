const config = require('../config')
const db = require('../db')
const pug = require('pug')
const sha1 = require('sha1')

module.exports = function(req, res) {
    
    if(req.method == "POST") {
        update(req, res);
    } else {
        display(req, res);
    }
}

function update(req, res) {

    db.model.User.findById(req.user.id).then(user => {
        if(req.body.name)
            user.name = req.body.name

        if(req.body.affiliation)
            user.affiliation = req.body.affiliation

        if(req.body.email)
            user.email = req.body.email

        if(req.body.password1 && req.body.password2) {
            if(req.body.password1 === req.body.password2)
                user.password = sha1(config.get('passwordSalt') + sha1(req.body.password1))
        }

        return user.save()
    }).then(() => {
        res.redirect("/profile")
    })
}


function display(req, res) {
    const user = req.user

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
}