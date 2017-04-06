
var pug = require('pug')

var sparql = require('../../sparql/sparql')

const config = require('../../config')

const isql = require('../../isql')

const path = require('path')

const fs = require('mz/fs')



const backupDir = path.normalize(__dirname + '/../../../backup')

module.exports = function(req, res) {

    if(req.method === 'POST') {

        post(req, res)

    } else {

        form(req, res)

    }

};

function form(req, res) {

    fs.readdir(backupDir).then((files) => {

        const backups = files.filter((filename) => {

            return filename[0] !== '.'
            
        }).map((filename) => {

            return {

                date: new Date(parseInt(filename.split('_')[2])),
                filename: filename

            }

        })

        var locals = {
            config: config.get(),
            section: 'admin',
            adminSection: 'backup',
            user: req.user,
            backups: backups
        }

        res.send(pug.renderFile('templates/views/admin/backup.jade', locals))

    }).catch((err) => {
        res.status(500).send(err.stack)
    })

}


function post(req, res) {

    const prefix = 'sbh_backup_' + new Date().getTime() + '_'

    isql([
        'backup_context_clear();',
        'backup_online(\'' + prefix + '\', 1000, 0, vector(\'' + backupDir + '\'));'
    ]).then((result) => {

        return form(req, res)

    }).catch((err) => {
        res.status(500).send(err.stack)
    })

}


