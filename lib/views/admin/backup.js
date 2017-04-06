
var pug = require('pug')

var sparql = require('../../sparql/sparql')

const config = require('../../config')

const isql = require('../../isql')

const path = require('path')

const fs = require('mz/fs')

const filesize = require('filesize')



const backupDir = path.normalize(__dirname + '/../../../backup')

module.exports = function(req, res) {

    if(req.method === 'POST') {

        post(req, res)

    } else {

        form(req, res)

    }

};

function listBackups() {

    return fs.readdir(backupDir).then((files) => {

        const backupFiles = files.filter((filename) => {

            return filename[0] !== '.'
            
        })

        const backups = {}

        backupFiles.forEach((filename) => {

            const date = filename.split('_')[2]

            if(backups[date] === undefined) {
                backups[date] = [ filename ]
            } else {
                backups[date].push(filename)
            }

        })

        return Promise.all(
            Object.keys(backups).map((backup) => {

                const files = backups[backup]

                const backupInfo = {
                    date: new Date(parseInt(backup)),
                    files: files,
                    prefix: files[0].toString().substring(0, files[0].length - '_.bp'.length)
                }

                return Promise.all(files.map((filename) => {

                    return fs.stat(backupDir + '/' + filename).then((stats) => {

                        return Promise.resolve(stats.size)

                    })

                })).then((sizes) => {

                    var totalSize = 0

                    sizes.forEach((size) => totalSize += size)

                    backupInfo.size = filesize(totalSize)

                    return Promise.resolve(backupInfo)

                })

            })
        )

    })

}

function form(req, res) {

    listBackups().then((backups) => {

        console.log(JSON.stringify(backups))

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


