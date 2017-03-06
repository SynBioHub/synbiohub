
const extend = require('xtend')
const config = require('./config')

const split = require('binary-split')

const EOT = '\04'

const spawn = require('child_process').spawn

const javaArgs = [
    '-cp', __dirname + '/../java/target/classes:' + __dirname + '/../java/lib/*',
    'org.synbiohub.Main'
]

console.log(JSON.stringify(javaArgs))

const javaProcess = spawn(config.get('javaPath'), javaArgs)

javaProcess.stderr.pipe(split('\n')).on('data', (data) => {
    console.log('[Java]', data.toString().trim())
})


const responseCallbacks = {}

javaProcess.stdout.pipe(split(EOT)).on('data', (token) => {

    const response = JSON.parse(token.toString())

    const callback = responseCallbacks[response.jobId]
    delete responseCallbacks[response.jobId]

    callback(response)

})

javaProcess.on('close', (exitCode) => {

    throw new Error('Java child process exited with code ' + exitCode)

})



var jobId = 0

function java(jobType, paramObj) {

    const job = extend(paramObj, {
        id: ++ jobId,
        type: jobType
    })

    return new Promise((resolve, reject) => {

        javaProcess.stdin.write(JSON.stringify(job))
        javaProcess.stdin.write(EOT)

        responseCallbacks[job.id] = (response) => {

            if(response.error !== undefined) {
                reject(new Error(response.error))
            } else {
                resolve(response)
            }

        }

    })

}

function init() {

    return java('initialize', {}).then((result) => {

        console.log('Java initialized; JVM ' + result.version)

    })

}


java.init = init

module.exports = java




