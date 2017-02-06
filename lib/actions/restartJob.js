
var jobUtils = require('../jobs/job-utils')

module.exports = function(req, res) {

    const jobId = parseInt(req.body.jobId)

    jobUtils.findJobById(jobId).then((job) => {

        if(job.userId != req.user.id) {
            res.status(403).send('that job does not belong to you')
            return
        }

        return jobUtils.restartJob(job).then(() => {

            res.redirect('/jobs')

        })

    })

};


