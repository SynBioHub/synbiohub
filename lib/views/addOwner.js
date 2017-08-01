const pug = require('pug');
const db = require('../db');
const config = require('../config');
const getUrisFromReq = require('../getUrisFromReq');
const addOwnedBy = require('../actions/addOwnedBy');

module.exports = function (req, res) {

    if (req.method === 'POST') {
        post(req, res)
    } else {
        view(req, res)
    }
}

function view(req, res) {
    const {
        graphUri,
        uri,
        designId
    } = getUrisFromReq(req, res);

    db.model.User.findAll().then(users => {
        let locals = {
            config: config.get(),
            user: req.user,
            users: users,
            uri: uri
        }

        res.send(pug.renderFile('templates/views/addOwner.jade', locals))
    })
}

function post(req, res) {
    addOwnedBy(req, res).then(() => {
        res.redirect(req.originalUrl.replace('/addOwner', ''));
    }).catch(err => {
        console.log(err);
    })
}