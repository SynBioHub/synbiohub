

if(process.env.SBH_NO_JAVA) {

	function nop() {
		return Promise.resolve(true)
	}


	nop.init = nop
	nop.shutdown = nop

	module.exports = nop

	return

}

const extend = require('xtend')
const config = require('./config')

const split = require('binary-split')

const EOT = '\04'

const spawn = require('child_process').spawn

const javaArgs = [
    '-jar', 
    __dirname + '/../java/target/SynBioHub-1.3.0-jar-with-dependencies.jar' 
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


var shuttingDown = false

javaProcess.on('close', (exitCode) => {

    if(!shuttingDown) {
        throw new Error('Java child process exited with code ' + exitCode)
    }

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

            //console.log(response)

            if(response.errorLog && response.success) {
                reject(new Error(response.errorLog))
            } else if(response.error) {
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

function shutdown() {

    return new Promise((resolve, reject) => {

        shuttingDown = true

        javaProcess.on('close', (exitCode) => {

            console.log('Java child process exited with code ' + exitCode + ' (requested)')

            resolve()

        })

        java('shutdown', {})

    })
}

java.init = init
java.shutdown = shutdown

module.exports = java




