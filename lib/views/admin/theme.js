
const pug = require('pug')

const config = require('../../config')

const theme = require('../../theme')

module.exports = function(req, res) {

    if(req.method === 'POST') {

        post(req, res)

    } else {

        form(req, res)

    }

}


function form(req, res) {

    const currentTheme = theme.getCurrentTheme()

    const themeParameters = currentTheme.parameters.map((parameter) => {

        return {
            name: parameter.name,
            variable: parameter.variable,
            value: theme.getParameterValue(parameter)
        }

    })

	const locals = {
        config: config.get(),
        section: 'admin',
        adminSection: 'theme',
        user: req.user,
        currentTheme: currentTheme,
        themeParameters: themeParameters
    }
	
    res.send(pug.renderFile('templates/views/admin/theme.jade', locals))
}

function post(req, res) {

    const currentTheme = theme.getCurrentTheme()

    const newParameters = {}

    currentTheme.parameters.map((parameter) => {

        const newParameter = req.body[parameter.variable]

        if(newParameter !== undefined) {

            if(newParameter !== parameter['default']) {

                newParameters[parameter.variable] = newParameter

            }

        }

    })

    const localConfig = config.getLocal()

    const themeName = theme.getCurrentThemeName()

    if(localConfig.themeParameters === undefined)
        localConfig.themeParameters = {}

    localConfig.themeParameters[themeName] = newParameters 

    config.set(localConfig)

    theme.setCurrentThemeFromConfig().then(() =>
        form(req, res)
    ).catch((err) => {
        res.status(500).send(err.stack)
    })
}



