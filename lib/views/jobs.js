
const pug = require('pug')

const jobUtils = require('../jobs/job-utils')

module.exports = function(req, res) {

    jobUtils.listJobs(req.user).then((jobs) => {

        const statusToString = {
            [jobUtils.Status.QUEUED]: 'Queued',
            [jobUtils.Status.RUNNING]: 'Running',
            [jobUtils.Status.CANCELED]: 'Canceled',
            [jobUtils.Status.FINISHED]: 'Finished',
        }

        const statusToRowClass = {
            [jobUtils.Status.QUEUED]: 'warning',
            [jobUtils.Status.RUNNING]: 'info',
            [jobUtils.Status.CANCELED]: 'danger',
            [jobUtils.Status.FINISHED]: 'success',
        }

        const locals = {
            section: 'jobs',
            user: req.user,
            jobs: jobs.sort((a, b) => b.createdAt - a.createdAt),
            Status: jobUtils.Status,
            statusToString: statusToString,
            statusToRowClass: statusToRowClass
        }
        
        res.send(pug.renderFile('templates/views/jobs.jade', locals))

    })
	
};
