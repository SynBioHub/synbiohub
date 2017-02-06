
function startTask(topLevel) {

    return new Promise((resolve, reject) => {

        console.log('**** starting dummy job')

        setTimeout(() => resolve(topLevel), 5000)

    })

}

module.exports = startTask


