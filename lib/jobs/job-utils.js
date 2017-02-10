
const db = require('../db')

const extend = require('xtend/mutable')

const taskHandlers = require('../task/index')

const getSBOL = require('../getSBOL')

const SBOLDocument = require('sboljs')

const convertAndValidateSbol = require('../convert-validate')

const config = require('../config')

const sparql = require('../sparql/sparql')

const serializeSBOL = require('../serializeSBOL')

const sse = require('../sse')

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

    }).then(() => resumeAllJobs)
}

function listJobs(user) {

    return db.model.Job.findAll({

        where: {
            userId: user.id
        },

        include: [
            { model: db.model.Task }
        ]

    })}

function findJobById(jobId) {

    return db.model.Job.find({
        where: {
            id: jobId
        },
        include: [
            { model: db.model.User },
            { model: db.model.Task }
        ]
    })

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

    return createJob(job.user, job.graphUri, job.inputUri, job.tasks)
                .then(() => resumeAllJobs())

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
        },

        include: [
            { model: db.model.User },
            { model: db.model.Task }
        ]
    
    }).then((jobs) => {

        console.log('Resuming ' + jobs.length + ' job(s)')

        return jobs.map(startNextTask)

    })

    function startNextTask(job) {

        var nextTask
        var object

        job.status = Status.RUNNING

        return job.save().then(() => db.model.Task.find({

            where: {
                jobId: job.id,
                index: job.currentTaskIndex
            }

        })).then((task) => {

            if(!task) {

                console.log('no next task; job is finished')

                job.status = Status.FINISHED

                return job.save().then(() => {

                    sse.push('jobs', 'update')

                })

            } else {

                nextTask = task

                task.status = Status.RUNNING

                return task.save()
                            .then(() => getSBOL(new SBOLDocument(), 'ComponentDefinition', job.graphUri, [ job.inputUri ]))
                            .then((result) => extend(result, { task: task }))
                            .then((result) => {

                                const task = result.task
                                const sbol = result.sbol
                                object = result.object

                                const handler = taskHandlerFromName(task.name)

                                if(!handler) {
                                    throw new Error('no handler for task name ' + task.name)
                                }


                                return task.save()
                                           .then(() => handler.startTask(sbol, object))

                            }).then((newSbolDoc) => {

                                const newUri = taskOutputUri(job.user, job.id, object.displayId, job.currentTaskIndex, job.iteration)

                                job.lastUri = newUri

                                ++ job.currentTaskIndex

                                return job.save().then(
                                    () => convertAndValidateSbol(newSbolDoc, config.get('triplestore').defaultGraph + '/user/' + encodeURIComponent(job.user.email) + '/job' + job.id, '1')
                                )

                            }).then((newSbolConverted) => {

                                console.log('uploading sbol...')

                                const collection = newSbolConverted.collection()

                                collection.name = 'Job ' + job.id + ' task ' + task.index + ' iteration ' + job.iteration
                                collection.description = 'Output of data integration task'
                                collection.displayId = 'task_output_collection'
                                collection.persistentIdentity = job.lastUri + '/' + collection.displayId
                                collection.version = '1'
                                collection.uri = collection.persistentIdentity + '/' + collection.version
                                collection.addStringAnnotation('http://synbiohub.org#uploadedBy', job.user.email)
                                collection.addStringAnnotation('http://purl.org/dc/terms/creator', job.user.name)
                                collection.addDateAnnotation('http://purl.org/dc/terms/created', new Date().toISOString())

                                newSbolConverted.componentDefinitions.forEach((componentDefinition) => {
                                    collection.addMember(componentDefinition)
                                })

                                newSbolConverted.moduleDefinitions.forEach((moduleDefinition) => {
                                    collection.addMember(moduleDefinition)
                                })

                                newSbolConverted.models.forEach((model) => {
                                    collection.addMember(model)
                                })

                                newSbolConverted.sequences.forEach((sequence) => {
                                    collection.addMember(sequence)
                                })

                                newSbolConverted.genericTopLevels.forEach((genericTopLevel) => {
                                    collection.addMember(genericTopLevel)
                                })

                                newSbolConverted.collections.forEach((subCollection) => {
                                    if (collection.uri!=subCollection.uri)
                                        collection.addMember(subCollection)
                                })

                                return sparql.upload(job.user.graphUri, serializeSBOL(newSbolConverted), 'application/rdf+xml')

                            }).then(() => {

                                startNextTask(job)

                            })
            }


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

function taskOutputUri(user, jobId, displayId, taskIndex, iterationNum) {

    //http://synbiohub.org/user/foo@bar.com/job1/BBa_R0010_task1_iteration1
    //
    return [
        config.get('databasePrefix'),
        'user/', encodeURIComponent(user.email),
        '/job', jobId, '/',
        displayId, '_task', taskIndex, '_', iterationNum + '/1'
    ].join('')

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


