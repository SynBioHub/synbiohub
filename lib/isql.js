
const spawn = require('child_process').spawn

const config = require('./config')

const EOT = '\04'

function isql(commands) {

    return new Promise((resolve, reject) => {

        const args = [
        ]


        console.log('Executing isql: ' + commands)

        const isqlProcess = spawn(config.get('triplestore').isql, args)

        commands.forEach((command) => {
            isqlProcess.stdin.write(command)
            isqlProcess.stdin.write('\n')
        })

        isqlProcess.stdin.write('exit;\n')

        var output = []

        isqlProcess.stdout.on('data', (data) => {

            console.log('[isql] ' + data)

            output.push(data)
        })

        isqlProcess.stderr.on('data', (data) => {

            console.log('[isql/stderr] ' + data)

        })

        isqlProcess.on('close', (exitCode) => {

            if(exitCode !== 0) {
                reject(new Error('isql-vt returned exit code ' + exitCode))
                return
            }

            resolve(output.join(''))

        })

    })
}

module.exports = isql

