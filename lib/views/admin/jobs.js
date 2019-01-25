
const pug = require('pug')

const sparql = require('../../sparql/sparql')

const jobUtils = require('../../jobs/job-utils')

const db = require('../../db')

const config = require('../../config')

module.exports = function (req, res) {
  db.model.Job.findAll({
    include: [
      { model: db.model.User },
      { model: db.model.Task }
    ]
  }).then((jobs) => {
    var locals = {
      config: config.get(),
      section: 'admin',
      adminSection: 'jobs',
      user: req.user,
      jobs: jobs
    }

    res.send(pug.renderFile('templates/views/admin/jobs.jade', locals))
  }).catch((err) => {
    res.status(500).send(err.stack)
  })
}
