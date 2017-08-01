
var express = require('express')

var session = require('express-session')
var cookieParser = require('cookie-parser')
var bodyParser = require('body-parser')
var multer = require('multer')

var lessMiddleware = require('less-middleware')
var browserifyMiddleware = require('browserify-middleware')
var UglifyJS = require('uglify-es')

var config = require('./config')

var SequelizeStore = require('connect-sequelize')(session)

const db = require('./db')

const initSSE = require('./sse').initSSE

var cache = require('./cache')

var views = {
    index: require('./views/index'),
    about: require('./views/about'),
    browse: require('./views/browse'),
    login: require('./views/login'),
    logout: require('./views/logout'),
    register: require('./views/register'),
    resetPassword: require('./views/resetPassword'),
    profile: require('./views/profile'),
    search: require('./views/search'),
    advancedSearch: require('./views/advancedSearch'),
    submit: require('./views/submit'),
    manage: require('./views/manage'),
    topLevel: require('./views/topLevel'),
    persistentIdentity: require('./views/persistentIdentity'),
    setup: require('./views/setup'),
    dataIntegration: require('./views/dataIntegration'),
    jobs: require('./views/jobs'),
    sparql: require('./views/sparql'),
    addOwner: require('./views/addOwner'),
    admin: {
        general: require('./views/admin/general'),
        status: require('./views/admin/status'),
        graphs: require('./views/admin/graphs'),
        sparql: require('./views/admin/sparql'),
        remotes: require('./views/admin/remotes'),
        users: require('./views/admin/users'),
        newUser: require('./views/admin/newUser'),
        update: require('./views/admin/update'),
        jobs: require('./views/admin/jobs'),
        theme: require('./views/admin/theme'),
        backup: require('./views/admin/backup'),
        backupRestore: require('./views/admin/backupRestore'),
        registries: require('./views/admin/registries'),
        mail: require('./views/admin/mail')
    }
}

var api = {
    search: require('./api/search'),
    sbol: require('./api/sbol'),
    persistentIdentity: require('./api/persistentIdentity'),
    summary: require('./api/summary'),
    fasta: require('./api/fasta'),
    genBank: require('./api/genBank'),
    autocomplete: require('./api/autocomplete'),
    count: require('./api/count'),
    rootCollections: require('./api/rootCollections'),
    subCollections: require('./api/subCollections'),
    download: require('./api/download'),
    datatables: require('./api/datatables'),
    sparql: require('./api/sparql'),
    updateWebOfRegistries: require('./api/updateWebOfRegistries')
}

var actions = {
    makePublic: require('./actions/makePublic'),
    copyFromRemote: require('./actions/copyFromRemote'),
    createBenchlingSequence: require('./actions/createBenchlingSequence'),
    createICEPart: require('./actions/createICEPart'),
    removeSubmission: require('./actions/removeSubmission'),
    cloneSubmission: require('./actions/cloneSubmission'),
    resetPassword: require('./actions/resetPassword'),
    setNewPassword: require('./actions/setNewPassword'),
    remove: require('./actions/remove'),
    updateMutableDescription: require('./actions/updateMutableDescription'),
    updateMutableNotes: require('./actions/updateMutableNotes'),
    updateMutableSource: require('./actions/updateMutableSource'),
    updateCitations: require('./actions/updateCitations'),
    cancelJob: require('./actions/cancelJob'),
    restartJob: require('./actions/restartJob'),
    upload: require('./actions/upload'),
    createSnapshot: require('./actions/createSnapshot'),
    updateCollectionIcon: require('./actions/updateCollectionIcon'),
    admin: {
        saveRemote: require('./actions/admin/saveRemote'),
        saveRegistry: require('./actions/admin/saveRegistry'),
        deleteRegistry: require('./actions/admin/deleteRegistry'),
        deleteRemote: require('./actions/admin/deleteRemote'),
        updateUser: require('./actions/admin/updateUser'),
        deleteUser: require('./actions/admin/deleteUser'),
        federate: require('./actions/admin/federate'),
        retrieve: require('./actions/admin/retrieveFromWoR')
    }
}

browserifyMiddleware.settings({
    mode: 'production',
    cache: '1 day',
    // debug: false,
    // minify: true,
    // precompile: true,
    postcompile: function(source) {
        console.log("Compiled!")
        return UglifyJS.minify(source).code
    },
})

function App() {

    var app = express()

    app.get('/bundle.js', browserifyMiddleware(__dirname + '/../browser/synbiohub.js'))


    app.use(lessMiddleware('public', { /*once: true*/ }))

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

    app.use(bodyParser.json())

    app.use(function (req, res, next) {

        if (req.url !== '/setup' && config.get('firstLaunch') === true) {

            console.log('redirecting')

            res.redirect('/setup')

        } else {

            next()

        }
    })

    app.use(function (req, res, next) {

        var userID = req.session.user

        if (userID !== undefined) {

            db.model.User.findById(userID).then((user) => {

                req.user = user

                next()
            })

        } else {

            next()

        }

    })

    var uploadToMemory = multer({
        storage: multer.memoryStorage({})
    })

    initSSE(app)

    if (config.get('experimental').dataIntegration) {
        app.get('/jobs', requireUser, views.jobs)
        app.post('/actions/job/cancel', requireUser, actions.cancelJob)
        app.post('/actions/job/restart', requireUser, actions.restartJob)
        app.get('/admin/jobs', requireAdmin, views.admin.jobs);
        app.all('/user/:userId/:collectionId/:displayId/:version([^\\.]+)/integrate', requireUser, views.dataIntegration);
        app.all('/public/:collectionId/:displayId/:version([^\\.]+)/integrate', views.dataIntegration);
    }

    app.get('/', views.index);
    app.get('/about', views.about);

    if (config.get('firstLaunch')) {
        app.get('/setup', views.setup);
        app.post('/setup', uploadToMemory.single('logo'), views.setup);
    }

    app.all('/browse', views.browse);


    function forceNoHTML(req, res, next) {

        req.forceNoHTML = true

        next()

    }

    app.all('/login', views.login);
    app.post('/remoteLogin', forceNoHTML, views.login); 
    app.all('/logout', views.logout);
    app.all('/register', views.register);
    app.all('/resetPassword/token/:token', actions.resetPassword);
    app.all('/resetPassword', views.resetPassword);
    app.post('/setNewPassword', actions.setNewPassword);
    app.post('/updateMutableDescription', requireUser, actions.updateMutableDescription);
    app.post('/updateMutableNotes', requireUser, actions.updateMutableNotes);
    app.post('/updateMutableSource', requireUser, actions.updateMutableSource);
    app.post('/updateCitations', requireUser, actions.updateCitations);

    app.get('/submit/', requireUser, views.submit);
    app.post('/submit/', requireUser, views.submit);
    app.post('/remoteSubmit/', forceNoHTML, /*requireUser,*/ views.submit); 

    app.get('/autocomplete/:query', api.autocomplete)
    app.get('/manage', requireUser, views.manage);

    app.get('/api/datatables', bodyParser.urlencoded({ extended: true }), api.datatables)

    app.get('/admin', requireAdmin, views.admin.status);
    app.get('/admin/search/:query?', views.search);
    app.get('/admin/graphs', requireAdmin, views.admin.graphs);
    app.get('/admin/sparql', requireAdmin, views.admin.sparql);
    app.get('/admin/remotes', requireAdmin, views.admin.remotes);
    app.get('/admin/users', requireAdmin, views.admin.users);
    app.get('/admin/newUser', requireAdmin, views.admin.newUser);
    app.get('/admin/update', requireAdmin, views.admin.update);
    app.get('/admin/theme', requireAdmin, views.admin.theme);
    app.post('/admin/theme', requireAdmin, uploadToMemory.single('logo'), views.admin.theme);
    app.get('/admin/general', requireAdmin, views.admin.general);
    app.post('/admin/general', requireAdmin, bodyParser.urlencoded({ extended: true }), views.admin.general);
    app.get('/admin/backup', requireAdmin, views.admin.backup);
    app.get('/admin/registries', requireAdmin, views.admin.registries);
    app.post('/admin/backup', requireAdmin, bodyParser.urlencoded({ extended: true }), views.admin.backup);
    app.post('/admin/backup/restore/:prefix', requireAdmin, bodyParser.urlencoded({ extended: true }), views.admin.backupRestore);
    app.post('/admin/newUser', requireAdmin, bodyParser.urlencoded({ extended: true }), views.admin.newUser);
    app.post('/admin/updateUser', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.updateUser);
    app.post('/admin/deleteUser', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.deleteUser);
    app.post('/admin/deleteRemote', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.deleteRemote);
    app.post('/admin/saveRemote', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.saveRemote);
    app.post('/admin/saveRegistry', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.saveRegistry);
    app.post('/admin/deleteRegistry', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.deleteRegistry);
    app.get('/admin/mail', requireAdmin, views.admin.mail);
    app.post('/admin/mail', requireAdmin, bodyParser.urlencoded({ extended: true }), views.admin.mail);

    app.post('/updateWebOfRegistries', bodyParser.json(), api.updateWebOfRegistries);
    app.post('/admin/federate', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.federate);
    app.post('/admin/retrieveFromWebOfRegistries', requireAdmin, actions.admin.retrieve);

    app.get('/search/:query?', views.search);
    app.get('/searchCount/:query?', views.search);
    app.get('/remoteSearch/:query?', forceNoHTML, views.search); /// DEPRECATED, use /search
    app.get('/advancedSearch', views.advancedSearch);
    app.post('/advancedSearch', views.advancedSearch);
    app.get('/advancedSearch/:query?', views.search);

    app.get('/createCollection', views.advancedSearch);
    app.post('/createCollection', views.advancedSearch);
    app.get('/createCollection/:query?', views.search);

    app.get('/:type/count', api.count)
    app.get('/rootCollections', api.rootCollections)

    app.get('/public/:collectionId/:displayId/:version/removeSubmission', requireAdmin, actions.removeSubmission);
    app.get('/public/:collectionId/:displayId/:version/subCollections', api.subCollections);
    app.post('/public/:collectionId/:displayId/:version/attach', requireUser, actions.upload);


    app.get('/public/:collectionId/:displayId/:version/createSnapshot', actions.createSnapshot);

    app.get('/public/:collectionId/:displayId', views.persistentIdentity);
    app.get('/public/:collectionId/:displayId/sbol', api.persistentIdentity);

    app.get('/public/:collectionId/:displayId/:version/copyFromRemote', requireUser, actions.copyFromRemote);
    app.post('/public/:collectionId/:displayId/:version/copyFromRemote', requireUser, uploadToMemory.single('file'), actions.copyFromRemote);

    app.get('/public/:collectionId/:displayId/:version/createBenchlingSequence', requireUser, actions.createBenchlingSequence);
    app.post('/public/:collectionId/:displayId/:version/createBenchlingSequence', requireUser, uploadToMemory.single('file'), actions.createBenchlingSequence);

    app.get('/public/:collectionId/:displayId/:version/createICEPart', requireUser, actions.createICEPart);
    app.post('/public/:collectionId/:displayId/:version/createICEPart', requireUser, uploadToMemory.single('file'), actions.createICEPart);

    app.get('/public/:collectionId/:displayId/search/:query?', views.search);
    app.get('/public/:collectionId/:displayId/:version/search/:query?', views.search);
    app.get('/public/:collectionId/:displayId/:version/uses', views.search);
    app.get('/public/:collectionId/:displayId/:version/twins', views.search);
    app.get('/public/:collectionId/:displayId/:version/download', api.download);
    app.get('/public/:collectionId/:displayId/:version/sbol', api.sbol);
    app.get('/public/:collectionId/:displayId/:version/:filename.xml', api.sbol);
    app.get('/public/:collectionId/:displayId/:version/:filename.json', api.summary);
    app.get('/public/:collectionId/:displayId/:version/:filename.fasta', api.fasta);
    app.get('/public/:collectionId/:displayId/:version/:filename.gb', api.genBank);
    app.get('/public/:collectionId/:displayId/:version/:query?', views.topLevel);
    app.get('/public/:collectionId/:displayId/:version([^\\.]+)', views.topLevel);

    app.post('/public/:collectionId/:displayId/:version/icon', requireUser, uploadToMemory.single('collectionIcon'), actions.updateCollectionIcon);

    app.get('/user/:userId/:collectionId/:displayId/:version/makePublic', requireUser, actions.makePublic);
    app.post('/user/:userId/:collectionId/:displayId/:version/makePublic', requireUser, uploadToMemory.single('file'), actions.makePublic);

    app.get('/user/:userId/:collectionId/:displayId/:version/addOwner', requireUser, views.addOwner);
    app.post('/user/:userId/:collectionId/:displayId/:version/addOwner', requireUser, views.addOwner);

    app.get('/user/:userId/:collectionId/:displayId/:version/createBenchlingSequence', requireUser, actions.createBenchlingSequence);
    app.post('/user/:userId/:collectionId/:displayId/:version/createBenchlingSequence', requireUser, uploadToMemory.single('file'), actions.createBenchlingSequence);

    app.get('/user/:userId/:collectionId/:displayId/:version/createICEPart', requireUser, actions.createICEPart);
    app.post('/user/:userId/:collectionId/:displayId/:version/createICEPart', requireUser, uploadToMemory.single('file'), actions.createICEPart);

    app.get('/user/:userId/:collectionId/:displayId/:version/removeSubmission', requireUser, actions.removeSubmission);
    app.get('/user/:userId/:collectionId/:displayId/:version/cloneSubmission/', requireUser, actions.cloneSubmission);
    app.post('/user/:userId/:collectionId/:displayId/:version/cloneSubmission/', requireUser, uploadToMemory.single('file'), actions.cloneSubmission);
    app.post('/user/:userId/:collectionId/:displayId/:version/attach', requireUser, actions.upload);
    app.get('/user/:userId/:collectionId/:displayId/:version/remove', requireUser, actions.remove);

    app.get('/user/:userId/:collectionId/:displayId/:version/:hash/search/:query?', views.search);
    app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/:filename.xml', api.sbol);
    app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/download', api.download);
    app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/sbol', api.sbol);
    app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/:filename.json', api.summary);
    app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/:filename.fasta', api.fasta);
    app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/:filename.gb', api.genBank);
    app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share', views.topLevel);
    app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/remove', actions.remove);

    app.get('/user/:userId/:collectionId/:displayId/search/:query?', views.search);
    app.get('/user/:userId/:collectionId/:displayId/:version/search/:query?', views.search);
    app.get('/user/:userId/:collectionId/:displayId/search/:query?', views.search);
    app.get('/user/:userId/:collectionId/:displayId/:version/uses', views.search);
    app.get('/user/:userId/:collectionId/:displayId/:version/twins', views.search);
    app.get('/user/:userId/:collectionId/:displayId/:version/download', requireUser, api.download);
    app.get('/user/:userId/:collectionId/:displayId/:version/sbol', api.sbol);
    app.get('/user/:userId/:collectionId/:displayId/:version/:filename.xml', api.sbol);
    app.get('/user/:userId/:collectionId/:displayId/:version/:filename.json', api.summary);
    app.get('/user/:userId/:collectionId/:displayId/:version/:filename.fasta', api.fasta);
    app.get('/user/:userId/:collectionId/:displayId/:version/:filename.gb', api.genBank);
    app.get('/user/:userId/:collectionId/:displayId/:version/:query?', views.topLevel);
    app.get('/user/:userId/:collectionId/:displayId/:version([^\\.]+)', views.topLevel);
    app.get('/user/:userId/:collectionId/:displayId', views.persistentIdentity);

    app.get('/sparql', sparql)
    app.post('/sparql', bodyParser.urlencoded({ extended: true }), sparql)

    function sparql(req, res) {

        // jena sends accept: */* and then complains when we send HTML
        // back. so only send html if the literal string text/html is present
        // in the accept header.
        //
        if (req.header('accept').indexOf('text/html') !== -1) {

            views.sparql(req, res)

        } else {

            api.sparql(req, res)

        }

    }

    function requireUser(req, res, next) {

        if (req.user === undefined)
            res.redirect('/login?next=' + encodeURIComponent(req.url))
        else
            next()
    }

    function requireAdmin(req, res, next) {

        if (req.user === undefined || !req.user.isAdmin)
            res.redirect('/login?next=' + encodeURIComponent(req.url))
        else
            next()
    }

    cache.update()

    return app
}

module.exports = App

