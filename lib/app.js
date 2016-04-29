
var express = require('express')

var session = require('express-session')
var cookieParser = require('cookie-parser')
var bodyParser = require('body-parser')
var multer = require('multer')

var mongoose = require('mongoose')

var lessCompiler = require('express-less-middleware')()

var config = require('./config')

var User = require('./models/User')

var stack = require('./stack')()

var MongoStore = require('connect-mongo')(session)

var views = {
    index: require('./views/index'),
    about: require('./views/about'),
    login: require('./views/login'),
    logout: require('./views/logout'),
    register: require('./views/register'),
    search: require('./views/search'),
    submit: require('./views/submit'),
    manage: require('./views/manage'),
    component: require('./views/component'),
    collection: require('./views/collection'),
    setup: require('./views/setup')
}

var api = {
    search: require('./api/search'),
    sbolCollection: require('./api/sbolCollection'),
    sbolComponent: require('./api/sbolComponent'),
    summary: require('./api/summary'),
    fasta: require('./api/fasta'),
}

var actions = {
    makePublic: require('./actions/makePublic')
}

function App() {
    
    var app = express()

    app.use(lessCompiler)

    app.use(express.static('public'))

    app.use(cookieParser())

    app.use(session({
        secret: config.get('sessionSecret'),
        resave: false,
        saveUninitialized: false,
        store: new MongoStore({ mongooseConnection: mongoose.connection })
    }))

    app.use(bodyParser.urlencoded({
        extended: true
    }))

    app.use(function(req, res, next) {

        if(req.url !== '/setup' && config.get('firstLaunch') === true) {

            console.log('redirecting')

            res.redirect('/setup')

        } else {

            next()

        }
    })

    app.use(function(req, res, next) {

        var userID = req.session.user

        if(userID !== undefined) {

            User.findById(userID, function(err, user) {

                req.user = user
                req.userStore = stack.getStore(user.storeName)

                next()
            })

        } else {

            next()

        }

    })

    var upload = multer({
        storage: multer.memoryStorage({})
    })

	app.get('/', views.index);
	app.get('/about', views.about);

	app.all('/setup', views.setup);

	app.all('/login', views.login);
	app.all('/logout', views.logout);
	app.all('/register', views.register);

	app.get('/search/:query?', views.search);

	app.get('/submit/', requireUser, views.submit);
	app.post('/submit/', requireUser, upload.single('file'), views.submit);

	app.get('/manage', requireUser, views.manage);
	
	app.get('/api/search', api.search);

	app.get('/collection/:collectionURI([^\\.\\/]+)', views.collection);
	app.get('/collection/:collectionURI.xml', api.sbolCollection);
	app.get('/collection/:collectionURI/makePublic', requireUser, actions.makePublic);

	app.get('/component/:prefix/:designid([^\\.]+)', views.component);
	app.get('/component/:prefix/:designid.xml', api.sbolComponent);
	app.get('/component/:prefix/:designid.json', api.summary);
	app.get('/component/:prefix/:designid.fasta', api.fasta);

	app.get('/component/:designURI([^\\.]+)', views.component);
	app.get('/component/:designURI.xml', api.sbolComponent);
	app.get('/component/:designURI.json', api.summary);
	app.get('/component/:designURI.fasta', api.fasta);

    function requireUser(req, res, next) {

        if(req.user === undefined)
            res.redirect('/login?next=' + encodeURIComponent(req.url))
        else
            next()
    }

    return app
}

module.exports = App

