

module.exports = {

    name: 'dummy',

    description: 'Clone a ComponentDefinition with a 30 second delay',

    type: 'in-process',

    isCompatible: (topLevel) => {

        return true

    },

    startTask: require('./start')

}

