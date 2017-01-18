
var express = require('express')

var session = require('express-session')
var cookieParser = require('cookie-parser')
var bodyParser = require('body-parser')
var multer = require('multer')

var mongoose = require('mongoose')

var lessCompiler = require('express-less-middleware')()

var config = require('./config')

var User = require('./models/User')

var MongoStore = require('connect-mongo')(session)

var views = {
    index: require('./views/index'),
    about: require('./views/about'),
    login: require('./views/login'),
    logout: require('./views/logout'),
    register: require('./views/register'),
    search: require('./views/search'),
    advancedSearch: require('./views/advancedSearch'),
    submit: require('./views/submit'),
    manage: require('./views/manage'),
    topLevel: require('./views/topLevel'),
    setup: require('./views/setup')
}

var api = {
    search: require('./api/search'),
    sbolTopLevel: require('./api/sbolTopLevel'),
    summary: require('./api/summary'),
    fasta: require('./api/fasta'),
    genBank: require('./api/genBank'),
}

var actions = {
    makePublic: require('./actions/makePublic'),
    removeSubmission: require('./actions/removeSubmission'),
    cloneSubmission: require('./actions/cloneSubmission'),
    accessStack: require('./actions/accessStack')
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

                var stack = require('./stack')()

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

	app.get('/submit/', requireUser, views.submit);
	app.post('/submit/', requireUser, upload.single('file'), views.submit);

	app.get('/manage', requireUser, views.manage);
    
	app.get('/search/:query?', views.search);
	
	app.get('/api/search', api.search);

	app.post('/advancedSearch', views.advancedSearch);
	app.get('/advancedSearch', views.advancedSearch);
	app.get('/advancedSearch/:query?', views.search);

	app.get('/public/:collectionId/:displayId/:version/removeSubmission', requireUser, actions.removeSubmission);
    
        // TODO: not sure why have to drop version for search
	app.get('/public/:collectionId/:displayId/search/:query?', views.search);
	app.get('/public/:collectionId/:displayId/:version/uses', views.search);
	app.get('/public/:collectionId/:displayId/:version/twins', views.search);
	app.get('/public/:collectionId(*)/:displayId/:version/:filename.xml', api.sbolTopLevel);
	app.get('/public/:collectionId(*)/:displayId/:version/:filename.json', api.summary);
	app.get('/public/:collectionId(*)/:displayId/:version/:filename.fasta', api.fasta);
	app.get('/public/:collectionId(*)/:displayId/:version/:filename.gb', api.genBank);
	app.get('/public/:collectionId/:displayId/:version([^\\.]+)', views.topLevel);

	app.get('/user/:userId/:collectionId/:displayId/:version/makePublic', requireUser, actions.makePublic);
	app.get('/user/:userId/:collectionId/:displayId/:version/removeSubmission', requireUser, actions.removeSubmission);
	app.get('/user/:userId/:collectionId/:displayId/:version/cloneSubmission/', requireUser, actions.cloneSubmission);
	app.post('/user/:userId/:collectionId/:displayId/:version/cloneSubmission/', requireUser, upload.single('file'), actions.cloneSubmission);

        // TODO: not sure why have to drop version for search
	app.get('/user/:userId/:collectionId/:displayId/search/:query?', views.search);
	app.get('/user/:userId/:collectionId/:displayId/:version/uses', views.search);
	app.get('/user/:userId/:collectionId/:displayId/:version/twins', views.search);
	app.get('/user/:userId/:collectionId(*)/:displayId/:version/:filename.xml', api.sbolTopLevel);
	app.get('/user/:userId/:collectionId(*)/:displayId/:version/:filename.json', api.summary);
	app.get('/user/:userId/:collectionId(*)/:displayId/:version/:filename.fasta', api.fasta);
	app.get('/user/:userId/:collectionId(*)/:displayId/:version/:filename.gb', api.genBank);
	app.get('/user/:userId/:collectionId/:displayId/:version([^\\.]+)', views.topLevel);


    app.get([
        '/component/count',
        '/store/:store/component/count'
    ], requireUser, actions.accessStack)

    app.get([
        '/component/search/metadata',
        '/store/:store/component/search/metadata'
    ], requireUser, actions.accessStack)

    app.post([
        '/component/search/metadata',
        '/store/:store/component/search/metadata'
    ], requireUser, bodyParser.json(), actions.accessStack)

    app.get([
        '/component/:uri/sbol',
        '/store/:store/component/:uri/sbol'
    ], requireUser, actions.accessStack)

    app.get([
        '/module/count',
        '/store/:store/module/count'
    ], requireUser, actions.accessStack)

    app.get([
        '/module/:uri/sbol',
        '/store/:store/module/:uri/sbol'
    ], requireUser, actions.accessStack)

    app.get([
        '/sequence/count',
        '/store/:store/sequence/count'
    ], requireUser, actions.accessStack)

    app.get([
        '/sequence/:uri/sbol',
        '/store/:store/sequence/:uri/sbol'
    ], requireUser, actions.accessStack)

    app.get([
        '/model/count',
        '/store/:store/model/count'
    ], requireUser, actions.accessStack)

    app.get([
        '/model/:uri/sbol',
        '/store/:store/model/:uri/sbol'
    ], requireUser, actions.accessStack)

    app.get([
        '/genericTopLevel/count',
        '/store/:store/genericTopLevel/count'
    ], requireUser, actions.accessStack)

    app.get([
        '/genericTopLevel/:uri/sbol',
        '/store/:store/genericTopLevel/:uri/sbol'
    ], requireUser, actions.accessStack)

    app.get([
        '/topLevel/count',
        '/store/:store/topLevel/count'
    ], requireUser, actions.accessStack)

    app.get([
        '/topLevel/:uri/sbol',
        '/store/:store/topLevel/:uri/sbol'
    ], requireUser, actions.accessStack)

    app.get([
        '/collection/count',
        '/store/:store/collection/count'
    ], requireUser, actions.accessStack)

    app.get([
        '/collection/:uri/sbol',
        '/store/:store/collection/:uri/sbol'
    ], requireUser, actions.accessStack)

    app.post([
        '/collection/search/sbol',
        '/store/:store/collection/search/sbol'
    ], requireUser, actions.accessStack)

    app.post([
        '/collection/search/metadata',
        '/store/:store/collection/search/metadata'
    ], requireUser, actions.accessStack)

    app.get([
        '/collection/roots',
        '/store/:store/collection/roots'
    ], requireUser, actions.accessStack)

    app.get([
        '/collection/:uri/subCollections',
        '/store/:store/collection/:uri/subCollections'
    ], requireUser, actions.accessStack)

    function requireUser(req, res, next) {

        if(req.user === undefined)
            res.redirect('/login?next=' + encodeURIComponent(req.url))
        else
            next()
    }

    return app
}

module.exports = App

