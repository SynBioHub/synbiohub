
var express = require('express')

var session = require('express-session')
var cookieParser = require('cookie-parser')
var bodyParser = require('body-parser')
var multer = require('multer')

var lessMiddleware = require('less-middleware')

var config = require('./config')

var SequelizeStore = require('connect-sequelize')(session)

const db = require('./db')



var CountEndpoint = require('./endpoint/CountEndpoint');
var RetrieveSBOLEndpoint = require('./endpoint/RetrieveSBOLEndpoint');
var SearchSBOLEndpoint = require('./endpoint/SearchSBOLEndpoint');
var SearchMetadataEndpoint = require('./endpoint/SearchMetadataEndpoint');
var SearchMetadataCountEndpoint = require('./endpoint/SearchMetadataCountEndpoint');
var SearchSBOLComponentSummaryEndpoint = require('./endpoint/SearchSBOLComponentSummaryEndpoint');
var SearchSBOLComponentSummaryCountEndpoint = require('./endpoint/SearchSBOLComponentSummaryCountEndpoint');
var SPARQLEndpoint = require('./endpoint/SPARQLEndpoint')
var PrefixesEndpoint = require('./endpoint/PrefixesEndpoint')
var MetadataEndpoint = require('./endpoint/MetadataEndpoint')
var UploadEndpoint = require('./endpoint/UploadEndpoint')
var ComponentInteractionsEndpoint = require('./endpoint/ComponentInteractionsEndpoint');
var RootCollectionMetadataEndpoint = require('./endpoint/RootCollectionMetadata');
var SubCollectionsEndpoint = require('./endpoint/SubCollectionsEndpoint');



var cache = require('./cache')

var views = {
    index: require('./views/index'),
    about: require('./views/about'),
    login: require('./views/login'),
    logout: require('./views/logout'),
    register: require('./views/register'),
    resetPassword: require('./views/resetPassword'),
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
    remoteLogin: require('./api/remoteLogin'),
    remoteSubmit: require('./api/remoteSubmit'),
    autocomplete: require('./api/autocomplete'),
}

var actions = {
    makePublic: require('./actions/makePublic'),
    removeSubmission: require('./actions/removeSubmission'),
    cloneSubmission: require('./actions/cloneSubmission'),
    accessStack: require('./actions/accessStack')
}

function App() {
    
    var app = express()

	app.use(lessMiddleware('public', { once: true, debug: true }))

    app.use(express.static('public'))

    app.use(cookieParser())

    app.use(session({
        secret: config.get('sessionSecret'),
        resave: false,
        saveUninitialized: false,
        store: new SequelizeStore(db.sequelize, {}, 'Session')
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

            db.model.User.findById(userID).then((user) => {

                console.log('user is')
                console.log(user)

                req.user = user

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
	app.all('/remoteLogin', api.remoteLogin);
	app.all('/logout', views.logout);
	app.all('/register', views.register);
	app.all('/resetPassword/:user', views.resetPassword);

	app.get('/submit/', requireUser, views.submit);
	app.post('/submit/', requireUser, upload.single('file'), views.submit);
	app.post('/remoteSubmit/', /*requireUser,*/ upload.single('file'), api.remoteSubmit);

    app.get('/autocomplete/:query', api.autocomplete)

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
	app.post('/user/:userId/:collectionId/:displayId/:version/makePublic', requireUser, upload.single('file'), actions.makePublic);
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
        '/sparql',
        '/store/:store/sparql'
    ], SPARQLEndpoint)

    app.post([
        '/sparql',
        '/store/:store/sparql'
    ], bodyParser.urlencoded(), SPARQLEndpoint)

    app.get([
        '/prefixes',
    ], PrefixesEndpoint)

    app.get([
        '/component/count',
        '/store/:store/component/count'
    ], CountEndpoint('ComponentDefinition'))

    app.get([
        '/component/:uri/sbol',
        '/store/:store/component/:uri/sbol'
    ], RetrieveSBOLEndpoint('ComponentDefinition'));

    app.get([
        '/component/:uri/interactions',
        '/store/:store/component/:uri/interactions'
    ], ComponentInteractionsEndpoint('ComponentDefinition'));

    app.get([
        '/component/:uri/metadata',
        '/store/:store/component/:uri/metadata'
    ], MetadataEndpoint)

    app.get([
        '/component/:prefix/:uri/sbol',
        '/store/:store/component/:prefix/:uri/sbol'
    ], RetrieveSBOLEndpoint('ComponentDefinition'));

    app.get([
        '/component/:prefix/:uri/interactions',
        '/store/:store/component/:prefix/:uri/interactions'
    ], ComponentInteractionsEndpoint('ComponentDefinition'))

    app.post([
        '/component/search/template',
        '/store/:store/component/search/template'
    ], bodyParser.urlencoded(), SearchSBOLComponentSummaryEndpoint());    

    app.post([
        '/component/count/template',
        '/store/:store/component/count/template'
    ], bodyParser.urlencoded(), SearchSBOLComponentSummaryCountEndpoint());    

    app.get([
        '/module/count',
        '/store/:store/module/count'
    ], CountEndpoint('ModuleDefinition'));

    app.get([
        '/module/search/sbol',
        '/store/:store/module/search/sbol'
    ], SearchSBOLEndpoint('ModuleDefinition'));

    app.get([
        '/module/search/metadata',
        '/store/:store/module/search/metadata'
    ], SearchMetadataEndpoint('ModuleDefinition'));

    app.get([
        '/module/:uri/sbol',
        '/store/:store/module/:uri/sbol'
    ], RetrieveSBOLEndpoint('ModuleDefinition'));

    app.get([
        '/sequence/count',
        '/store/:store/sequence/count'
    ], CountEndpoint('Sequence'));

    app.get([
        '/sequence/search/sbol',
        '/store/:store/sequence/search/sbol'
    ], SearchSBOLEndpoint('Sequence'));

    app.get([
        '/sequence/search/metadata',
        '/store/:store/sequence/search/metadata'
    ], SearchMetadataEndpoint('Sequence'));

    app.get([
        '/sequence/:uri/sbol',
        '/store/:store/sequence/:uri/sbol'
    ], RetrieveSBOLEndpoint('Sequence'));

    app.get([
        '/model/count',
        '/store/:store/model/count'
    ], CountEndpoint('Model'));

    app.get([
        '/model/search/sbol',
        '/store/:store/model/search/sbol'
    ], SearchSBOLEndpoint('Model'));

    app.get([
        '/model/search/metadata',
        '/store/:store/model/search/metadata'
    ], SearchMetadataEndpoint('Model'));

    app.get([
        '/model/:uri/sbol',
        '/store/:store/model/:uri/sbol'
    ], RetrieveSBOLEndpoint('Model'));

    app.get([
        '/genericTopLevel/count',
        '/store/:store/genericTopLevel/count'
    ], CountEndpoint('GenericTopLevel'));

    app.get([
        '/genericTopLevel/search/sbol',
        '/store/:store/genericTopLevel/search/sbol'
    ], SearchSBOLEndpoint('GenericTopLevel'));

    app.get([
        '/genericTopLevel/search/metadata',
        '/store/:store/genericTopLevel/search/metadata'
    ], SearchMetadataEndpoint('GenericTopLevel'));

    app.get([
        '/genericTopLevel/:uri/sbol',
        '/store/:store/genericTopLevel/:uri/sbol'
    ], RetrieveSBOLEndpoint('GenericTopLevel'));

    app.get([
        '/topLevel/count',
        '/store/:store/topLevel/count'
    ], CountEndpoint('TopLevel'));

    app.get([
        '/topLevel/search/sbol',
        '/store/:store/topLevel/search/sbol'
    ], SearchSBOLEndpoint('TopLevel'));

    app.get([
        '/topLevel/search/metadata',
        '/store/:store/topLevel/search/metadata'
    ], SearchMetadataEndpoint('TopLevel'));

    app.get([
        '/topLevel/:uri/sbol',
        '/store/:store/topLevel/:uri/sbol'
    ], RetrieveSBOLEndpoint('TopLevel'));

    app.get([
        '/collection/count',
        '/store/:store/collection/count'
    ], CountEndpoint('Collection'));

    app.get([
        '/collection/:uri/sbol',
        '/store/:store/collection/:uri/sbol'
    ], RetrieveSBOLEndpoint('Collection'));

    app.post([
        '/collection/search/sbol',
        '/store/:store/collection/search/sbol'
    ], SearchSBOLEndpoint('Collection'));

    app.post([
        '/collection/search/metadata',
        '/store/:store/collection/search/metadata'
    ], SearchMetadataEndpoint('Collection'));

    app.get([
        '/collection/roots',
        '/store/:store/collection/roots'
    ], RootCollectionMetadataEndpoint)

    app.get([
        '/collection/:uri/subCollections',
        '/store/:store/collection/:uri/subCollections'
    ], SubCollectionsEndpoint)

    //app.post([
        //'/',
        //'/store/:store'
    //], auth.upload, bodyParser.text({
        //type: '*/*',
        //limit: config.get('uploadLimit')
    //}), UploadEndpoint)




    function requireUser(req, res, next) {

        if(req.user === undefined)
            res.redirect('/login?next=' + encodeURIComponent(req.url))
        else
            next()
    }

    function requireUserOnRead(req, res, next) {

	// TODO: not currently enabled, should add a configuration option
        //if(req.user === undefined)
        //    res.redirect('/login?next=' + encodeURIComponent(req.url))
        //else
        next()
    }

    cache.update()

    return app
}

module.exports = App

