
const db = require('../db')

const extend = require('xtend/mutable')

const taskHandlers = require('../task/index')

const getSBOL = require('../getSBOL')

const SBOLDocument = require('sboljs')

const Status = {
    QUEUED: 0,
    RUNNING: 1,
    FINISHED: 2,
    CANCELED: 3
}

function createJob(user, graphUri, inputUri, tasks) {

    const tasksWithIndexes =
        tasks.map((task, i) => extend(task, { index: i }))

    return db.model.Job.create({

        userId: user.id,
        name: 'Untitled Job',
        description: '',
        graphUri: graphUri,
        inputUri: inputUri,
        status: Status.QUEUED,
        currentTaskIndex: 0,
        iteration: 0

    }).then(function createTasks(job) {

        return Promise.all(tasksWithIndexes.map((task) => {

            return db.model.Task.create({

                jobId: job.id,
                index: task.index,
                name: task.name,
                description: task.description,
                params: JSON.stringify({}),
                status: Status.QUEUED,

            })

        }))

    })
}

function listJobs(user) {

    return db.model.Job.findAll({

        where: {
            userId: user.id
        }

    }).then((jobs) => {

        return Promise.all(jobs.map((job) => resolveTasks(job)))

    })
}

function resolveTasks(job) {

    return db.model.Task.findAll({

        where: {
            jobId: job.id
        }

    }).then((taskList) => {
        return extend(job, { tasks: taskList })
    })

}

function findJobById(jobId) {

    return db.model.Job.findById(jobId).then((job) => resolveTasks(job))

}

function cancelJob(job) {

    const ops = []

    job.status = Status.CANCELED

    job.tasks.forEach((task) => {
        task.status = Status.CANCELED
        ops.push(task.save())
    })

    ops.push(job.save())

    return Promise.all(ops)

}

function restartJob(job) {

    const ops = []

    job.status = Status.QUEUED

    job.tasks.forEach((task) => {
        task.status = Status.QUEUED
        ops.push(task.save())
    })

    ops.push(job.save())

    return Promise.all(ops).then(() => resumeAllJobs())

}

function setRunningJobsToQueued() {

    return db.sequelize.query('UPDATE job SET status=:queued WHERE status=:running', {
        replacements: {
            queued: Status.QUEUED,
            running: Status.RUNNING,
        }
    }).then(db.sequelize.query('UPDATE task SET status=:queued WHERE status=:running', {
        replacements: {
            queued: Status.QUEUED,
            running: Status.RUNNING,
        }
    }))


}

function resumeAllJobs() {

    return db.model.Job.findAll({

        where: {
            status: Status.QUEUED
        }
    
    }).then((jobs) => {

        console.log('Resuming ' + jobs.length + ' job(s)')

        return jobs.map(startNextTask)

    })

    function startNextTask(job) {

        job.status = Status.RUNNING

        return job.save().then(() => db.model.Task.find({

            where: {
                jobId: job.id,
                index: job.currentTaskIndex
            }

        })).then((task) => {

            task.status = Status.RUNNING

            return task.save()
                       .then(getSBOL(new SBOLDocument(), 'ComponentDefinition', job.graphUri, [ job.inputUri ]))
                       .then((result) => extend(result, { task: task }))

        }).then((result) => {

            const task = result.task
            const sbol = result.sbol
            const object = result.object

            const handler = taskHandlerFromName(task.name)

            if(!handler) {
                throw new Error('no handler for task name ' + task.name)
            }


            return task.save()
                       .then(() => handler.startTask(object))

        }).then((updatedTopLevel) => {

            console.log('job is finished')

            job.status = Status.FINISHED

            return job.save()

        })

    }
}

function taskHandlerFromName(name) {

    for(var i = 0; i < taskHandlers.length; ++ i) {

        if(taskHandlers[i].name === name) {

            return taskHandlers[i]

        }

    }


}

module.exports = {

    createJob: createJob,
    listJobs: listJobs,
    findJobById: findJobById,
    cancelJob: cancelJob,
    restartJob: restartJob,
    Status: Status,
    setRunningJobsToQueued: setRunningJobsToQueued,
    resumeAllJobs: resumeAllJobs

}


