
const db = require('../db')

const extend = require('xtend')

function createJob(user, tasks) {

    const tasksWithIndexes =
        tasks.map((task, i) => extend(task, { index: i }))

    return db.model.Job.create({

        userId: user.id

    }).then(function createTasks(job) {

        return Promise.all(tasksWithIndexes.map((task) => {

            return db.model.Task.create({

                jobId: job.id,
                index: task.index

            })

        }))

    })



}

module.exports = {

    createJob: createJob

}


