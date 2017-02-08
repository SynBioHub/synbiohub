
function startTask(doc, topLevel) {

    return new Promise((resolve, reject) => {

        console.log('**** starting dummy job')

        setTimeout(() => resolve(doc), 5000)

    })

}

module.exports = startTask


