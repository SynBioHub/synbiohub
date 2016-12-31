
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
    genBank: require('./api/genBank'),
}

var actions = {
    makePublic: require('./actions/makePublic'),
    removeSubmission: require('./actions/removeSubmission')
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

	app.post('/advancedSearch', views.advancedSearch);
	app.get('/advancedSearch', views.advancedSearch);
	app.get('/advancedSearch/:query?', views.search);

	app.get('/search/collection/:collectionURI', views.search);
	app.get('/search/role/:roleURI', views.search);
	app.get('/search/type/:typeURI', views.search);
	app.get('/search/igemResults/:igemResultsURI', views.search);
	app.get('/search/igemStatus/:igemStatusURI', views.search);
	app.get('/search/igemPartStatus/:igemPartStatusStr', views.search);
	app.get('/search/creator/:creatorStr', views.search);
	app.get('/search/:query?', views.search);
	app.get('/search/uses/:componentURI', views.search);
	app.get('/search/twins/:twinURI', views.search);

        app.get('/collection/search/:query?', views.search);
	app.get('/collection/:collectionURI/search/:query?', views.search);

	app.get('/submit/', requireUser, views.submit);
	app.post('/submit/', requireUser, upload.single('file'), views.submit);

	app.get('/manage', requireUser, views.manage);
	
	app.get('/api/search', api.search);

	app.get('/collection/:collectionURI([^\\.\\/]+)', views.collection);
	app.get('/collection/:collectionURI.xml', api.sbolCollection);
	app.get('/collection/:collectionURI/makePublic', requireUser, actions.makePublic);
	app.get('/collection/:collectionURI/removeSubmission', requireUser, actions.removeSubmission);

        app.get('/component/search/:query?', views.search);

	app.get('/component/:prefix/:designid/search/:query?', views.search);
	app.get('/component/:prefix/:designid([^\\.]+)', views.component);
	app.get('/component/:prefix/:designid.xml', api.sbolComponent);
	app.get('/component/:prefix/:designid.json', api.summary);
	app.get('/component/:prefix/:designid.fasta', api.fasta);
	app.get('/component/:prefix/:designid.gb', api.genBank);

        app.get('/component/:designURI/search/:query?', views.search);
	app.get('/component/:designURI([^\\.]+)', views.component);
	app.get('/component/:designURI.xml', api.sbolComponent);
	app.get('/component/:designURI.json', api.summary);
	app.get('/component/:designURI.fasta', api.fasta);
	app.get('/component/:designURI.gb', api.genBank);
    
        // TODO: not sure why have to drop version for search
	app.get('/public/:collectionId/:displayId/search/:query?', views.search);
	app.get('/public/:collectionId/:displayId/:version([^\\.]+)', views.component);
	app.get('/public/:collectionId/:displayId/:version.xml', api.sbolComponent);
	app.get('/public/:collectionId/:displayId/:version.json', api.summary);
	app.get('/public/:collectionId/:displayId/:version.fasta', api.fasta);
	app.get('/public/:collectionId/:displayId/:version.gb', api.genBank);

        // TODO: not sure why have to drop version for search
	app.get('/user/:userId/:collectionId/:displayId/search/:query?', views.search);
	app.get('/user/:userId/:collectionId/:displayId/:version([^\\.]+)', views.component);
	app.get('/user/:userId/:collectionId/:displayId/:version.xml', api.sbolComponent);
	app.get('/user/:userId/:collectionId/:displayId/:version.json', api.summary);
	app.get('/user/:userId/:collectionId/:displayId/:version.fasta', api.fasta);
	app.get('/user/:userId/:collectionId/:displayId/:version.gb', api.genBank);

    function requireUser(req, res, next) {

        if(req.user === undefined)
            res.redirect('/login?next=' + encodeURIComponent(req.url))
        else
            next()
    }

    return app
}

module.exports = App

