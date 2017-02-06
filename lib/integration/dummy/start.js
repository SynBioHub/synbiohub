
function startTask(topLevel) {

    return new Promise((resolve, reject) => {

        setTimeout(() => resolve(topLevel), 15000)

    })

}

module.exports = startTask


