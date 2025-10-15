const express = require('express')
const bodyParser = require('body-parser')
const multer = require('multer')
const config = require('./config')
const initSSE = require('./sse').initSSE
const cache = require('./cache')
const setupAppMiddleware = require('./app-middleware')
const cors = require('cors')

/*
var whitelist = ['http://localhost:7777', 'http://localhost:3000']
var corsOptions = {
  origin: function (origin, callback) {
    if (!origin || whitelist.indexOf(origin) !== -1) {
      callback(null, true)
    } else {
      callback(new Error('Not allowed by CORS: ' + origin))
    }
  }
}
*/

const views = {
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
  shared: require('./views/shared'),
  visualization: require('./views/visualization'),
  logo: require('./views/logo'),
  stream: require('./views/stream'),
  sbsearch: require('./views/sbsearch'),
  addToCollection: require('./views/addToCollection'),
  admin: {
    explorer: require('./views/admin/explorer'),
    status: require('./views/admin/status'),
    graphs: require('./views/admin/graphs'),
    sparql: require('./views/admin/sparql'),
    remotes: require('./views/admin/remotes'),
    users: require('./views/admin/users'),
    newUser: require('./views/admin/newUser'),
    jobs: require('./views/admin/jobs'),
    theme: require('./views/admin/theme'),
    //    backup: require('./views/admin/backup'),
    //    backupRestore: require('./views/admin/backupRestore'),
    registries: require('./views/admin/registries'),
    mail: require('./views/admin/mail'),
    log: require('./views/admin/log'),
    listLogs: require('./views/admin/listLogs'),
    plugins: require('./views/admin/plugins')
  }
}

const api = {
  search: require('./api/search'),
  sbol: require('./api/sbol'),
  sbolnr: require('./api/sbolnr'),
  omex: require('./api/omex'),
  persistentIdentity: require('./api/persistentIdentity'),
  summary: require('./api/summary'),
  fasta: require('./api/fasta'),
  genBank: require('./api/genBank'),
  gff3: require('./api/gff3'),
  metadata: require('./api/metadata'),
  autocomplete: require('./api/autocomplete'),
  count: require('./api/count'),
  healthCheck: require('./api/healthCheck'),
  rootCollections: require('./api/rootCollections'),
  subCollections: require('./api/subCollections'),
  download: require('./api/download'),
  datatables: require('./api/datatables'),
  sparql: require('./api/sparql'),
  stream: require('./api/stream'),
  updateWebOfRegistries: require('./api/updateWebOfRegistries'),
  editObject: require('./api/editObject'),
  addObject: require('./api/addObject'),
  removeObject: require('./api/removeObject'),
  attachUrl: require('./api/attachUrl'),
  shareLink: require('./api/shareLink'),
  expose: require('./api/expose').serveExpose,
  admin: {
    sparql: require('./api/admin/sparql')
  }
}

const actions = {
  makePublic: require('./actions/makePublic'),
  copyFromRemote: require('./actions/copyFromRemote'),
  createBenchlingSequence: require('./actions/createBenchlingSequence'),
  createICEPart: require('./actions/createICEPart'),
  removeCollection: require('./actions/removeCollection'),
  resetPassword: require('./actions/resetPassword'),
  setNewPassword: require('./actions/setNewPassword'),
  remove: require('./actions/remove'),
  removeMembership: require('./actions/removeMembership'),
  replace: require('./actions/replace'),
  createImplementation: require('./actions/createImplementation'),
  createTest: require('./actions/createTest'),
  updateMutableDescription: require('./actions/updateMutableDescription'),
  updateMutableNotes: require('./actions/updateMutableNotes'),
  updateMutableSource: require('./actions/updateMutableSource'),
  updateCitations: require('./actions/updateCitations'),
  cancelJob: require('./actions/cancelJob'),
  restartJob: require('./actions/restartJob'),
  upload: require('./actions/upload'),
  createSnapshot: require('./actions/createSnapshot'),
  updateCollectionIcon: require('./actions/updateCollectionIcon'),
  removeOwner: require('./actions/removeOwnedBy'),
  callPlugin: require('./plugins/pluginEndpoints'),
  admin: {
    saveRemote: require('./actions/admin/saveRemote'),
    saveRegistry: require('./actions/admin/saveRegistry'),
    deleteRegistry: require('./actions/admin/deleteRegistry'),
    savePlugin: require('./actions/admin/savePlugin'),
    deletePlugin: require('./actions/admin/deletePlugin'),
    deleteRemote: require('./actions/admin/deleteRemote'),
    updateUser: require('./actions/admin/updateUser'),
    deleteUser: require('./actions/admin/deleteUser'),
    federate: require('./actions/admin/federate'),
    retrieve: require('./actions/admin/retrieveFromWoR'),
    explorerUpdateIndex: require('./actions/admin/explorerUpdateIndex'),
    setAdministratorEmail: require('./actions/admin/updateAdministratorEmail'),
    explorerLog: require('./actions/admin/explorerLog'),
    explorerIndexingLog: require('./actions/admin/explorerIndexingLog')
  }
}

function App () {
  const app = express()

  setupAppMiddleware(app)

  // use cors
  app.use(cors())
  app.set('trust proxy', true)

  const uploadToMemory = multer({
    storage: multer.memoryStorage({})
  })

  initSSE(app)

  // Data integration experimental endpoints created by James McLaughlin
  if (config.get('experimental').dataIntegration) {
    app.get('/jobs', requireUser, views.jobs)
    app.post('/actions/job/cancel', requireUser, actions.cancelJob)
    app.post('/actions/job/restart', requireUser, actions.restartJob)
    app.get('/admin/jobs', requireAdmin, views.admin.jobs)
    app.all('/user/:userId/:collectionId/:displayId/:version([^\\.]+)/integrate', requireUser, views.dataIntegration)
    app.all('/public/:collectionId/:displayId/:version([^\\.]+)/integrate', views.dataIntegration)
    app.get('/user/:userId/:collectionId/:displayId/:version/createImplementation', requireUser, actions.createImplementation)
    app.get('/user/:userId/:collectionId/:displayId/:version/createTest', requireUser, actions.createTest)
    // TODO: need to decide if createSnapshot is functional and should be kept or not
    app.get('/public/:collectionId/:displayId/:version/createSnapshot', actions.createSnapshot)
  }

  app.get('/', views.index)
  app.get('/about', views.about)

  if (config.get('firstLaunch')) {
    app.get('/setup', views.setup)
    app.post('/setup', uploadToMemory.single('logo'), views.setup)
  }

  app.all('/browse', requirePublicLogin, views.browse)

  function forceNoHTML (req, res, next) {
    // User Endpoints
    req.forceNoHTML = true

    next()
  }

  // User Endpoints
  app.all('/register', allowPublicSignup, views.register)
  app.all('/login', views.login)
  app.post('/remoteLogin', forceNoHTML, views.login)
  app.all('/logout', views.logout)
  app.all('/resetPassword/token/:token', actions.resetPassword)
  app.all('/resetPassword', views.resetPassword)
  app.post('/setNewPassword', actions.setNewPassword)
  app.all('/profile', requireUser, views.profile)

  // Misc. Endpoints
  app.get('/logo*', views.logo)
  app.get('/autocomplete/:query', requirePublicLogin, api.autocomplete)
  app.get('/api/datatables', requirePublicLogin, bodyParser.urlencoded({ extended: true }), api.datatables)

  // Plugin Endpoints
  app.get('/stream/:id', requirePublicLogin, views.stream)
  app.get('/api/stream/:id', requirePublicLogin, api.stream.serve)
  app.delete('/api/stream/:id', requirePublicLogin, api.stream.serve)
  app.post('/callPlugin', requirePublicLogin, actions.callPlugin)

  // Edit Mutable Fields Endpoints
  app.post('/updateMutableDescription', requireUser, actions.updateMutableDescription)
  app.post('/updateMutableNotes', requireUser, actions.updateMutableNotes)
  app.post('/updateMutableSource', requireUser, actions.updateMutableSource)
  app.post('/updateCitations', requireUser, actions.updateCitations)

  app.post('/public/:collectionId/:displayId/:version/edit/:field', requireAdmin, api.editObject)
  app.post('/public/:collectionId/:displayId/:version/add/:field', requireAdmin, api.addObject)
  app.post('/public/:collectionId/:displayId/:version/remove/:field', requireAdmin, api.removeObject)

  app.post('/user/:userId/:collectionId/:displayId/:version/edit/:field', requireUser, api.editObject)
  app.post('/user/:userId/:collectionId/:displayId/:version/add/:field', requireUser, api.addObject)
  app.post('/user/:userId/:collectionId/:displayId/:version/remove/:field', requireUser, api.removeObject)

  // Submission Endpoints
  app.get('/submit/', requireUser, views.submit)
  app.post('/submit/', requireUser, views.submit)
  app.post('/remoteSubmit/', forceNoHTML, requirePublicLogin, /* requireUser, */ views.submit) // Deprecated

  // Administration Endpoints
  app.get('/admin', requireAdmin, views.admin.status)
  app.get('/admin/graphs', requireAdmin, views.admin.graphs)
  app.get('/admin/log', requireAdmin, views.admin.log)
  app.get('/admin/listLogs', requireAdmin, views.admin.listLogs)
  app.get('/admin/virtuoso', requirePublicLogin, api.healthCheck)

  app.get('/admin/mail', requireAdmin, views.admin.mail)
  app.post('/admin/mail', requireAdmin, bodyParser.urlencoded({ extended: true }), views.admin.mail)

  app.get('/admin/plugins', views.admin.plugins)
  app.post('/admin/savePlugin', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.savePlugin)
  app.post('/admin/deletePlugin', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.deletePlugin)

  app.get('/admin/registries', views.admin.registries)
  app.post('/admin/saveRegistry', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.saveRegistry)
  app.post('/admin/deleteRegistry', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.deleteRegistry)
  app.post(
    '/admin/setAdministratorEmail',
    requireAdmin,
    bodyParser.urlencoded({ extended: true }),
    actions.admin.setAdministratorEmail
  )
  app.post('/admin/retrieveFromWebOfRegistries', requireAdmin, actions.admin.retrieve)
  app.post('/admin/federate', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.federate)

  // This endpoint is used by Web-of-Registries to update SynBioHub's list of registries
  app.post('/updateWebOfRegistries', bodyParser.json(), api.updateWebOfRegistries)

  app.get('/admin/remotes', requireAdmin, views.admin.remotes)
  app.post('/admin/saveRemote', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.saveRemote)
  app.post('/admin/deleteRemote', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.deleteRemote)

  app.get('/admin/explorer', requireAdmin, views.admin.explorer)
  app.post('/admin/explorer', requireAdmin, bodyParser.urlencoded({ extended: true }), views.admin.explorer)
  app.post('/admin/explorerUpdateIndex', requireAdmin, actions.admin.explorerUpdateIndex)
  app.all('/admin/explorerLog', requireAdmin, actions.admin.explorerLog)
  app.all('/admin/explorerIndexingLog', requireAdmin, actions.admin.explorerIndexingLog)

  app.get('/admin/sparql', requireAdmin, sparqlAdmin)
  app.post('/admin/sparql', requireAdmin, bodyParser.urlencoded({ extended: true }), sparqlAdmin)

  app.get('/admin/theme', views.admin.theme)
  app.post('/admin/theme', requireAdmin, uploadToMemory.single('logo'), views.admin.theme)

  app.get('/admin/users', requirePublicLogin, views.admin.users)
  app.post('/admin/users', requireAdmin, views.admin.users)
  app.get('/admin/newUser', requireAdmin, views.admin.newUser)
  app.post('/admin/newUser', requireAdmin, bodyParser.urlencoded({ extended: true }), views.admin.newUser)
  app.post('/admin/updateUser', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.updateUser)
  app.post('/admin/deleteUser', requireAdmin, bodyParser.urlencoded({ extended: true }), actions.admin.deleteUser)

  // Search Endpoints
  app.get('/search/:query?', requirePublicLogin, views.search)
  app.get('/searchCount/:query?', requirePublicLogin, views.search)
  app.get('/remoteSearch/:query?', forceNoHTML, requirePublicLogin, views.search) /// DEPRECATED, use /search

  app.get('/sbsearch', requirePublicLogin, views.sbsearch)
  app.post('/sbsearch', requirePublicLogin, views.sbsearch)

  app.get('/advancedSearch', requirePublicLogin, views.advancedSearch)
  app.post('/advancedSearch', requirePublicLogin, views.advancedSearch)
  app.post('/createCollection', requirePublicLogin, views.advancedSearch)

  app.get('/:type/count', requirePublicLogin, api.count)
  app.get('/rootCollections', requirePublicLogin, api.rootCollections)

  app.get('/manage', requireUser, views.manage)
  app.get('/shared', requireUser, views.shared)

  app.get('/public/:collectionId/:displayId/:version/subCollections', requirePublicLogin, api.subCollections)
  app.get('/public/:collectionId/:displayId/:version/twins', requirePublicLogin, views.search)
  app.get('/public/:collectionId/:displayId/:version/uses', requirePublicLogin, views.search)
  app.get('/public/:collectionId/:displayId/:version/similar', requirePublicLogin, views.search)

  app.get('/user/:userId/:collectionId/:displayId/:version/subCollections', requirePublicLogin, api.subCollections)
  app.get('/user/:userId/:collectionId/:displayId/:version/twins', requirePublicLogin, views.search)
  app.get('/user/:userId/:collectionId/:displayId/:version/uses', requirePublicLogin, views.search)
  app.get('/user/:userId/:collectionId/:displayId/:version/similar', requirePublicLogin, views.search)

  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/subCollections', requirePublicLogin, api.subCollections)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/twins', requirePublicLogin, views.search)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/uses', requirePublicLogin, views.search)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/similar', requirePublicLogin, views.search)

  app.get('/sparql', requirePublicLogin, sparql)
  app.post('/sparql', requirePublicLogin, bodyParser.urlencoded({ extended: true }), sparql)

  // Manage Submissions Endpoints
  app.post(
    '/public/:collectionId/:displayId/:version/icon',
    requireUser,
    uploadToMemory.single('collectionIcon'),
    actions.updateCollectionIcon
  )

  app.get('/public/:collectionId/:displayId/:version/removeCollection', requireAdmin, actions.removeCollection)
  app.get('/public/:collectionId/:displayId/:version/remove', requireAdmin, actions.remove)
  app.post('/public/:collectionId/:displayId/:version/removeMembership', requireAdmin, actions.removeMembership)

  app.get('/public/:collectionId/:displayId/:version/addToCollection', requireUser, views.addToCollection)
  app.post('/public/:collectionId/:displayId/:version/addToCollection', requireUser, views.addToCollection)

  app.get('/user/:userId/:collectionId/:displayId/:version/removeCollection', requireUser, actions.removeCollection)
  app.get('/user/:userId/:collectionId/:displayId/:version/addToCollection', requireUser, views.addToCollection)
  app.post('/user/:userId/:collectionId/:displayId/:version/addToCollection', requireUser, views.addToCollection)
  app.get('/user/:userId/:collectionId/:displayId/:version/remove', requireUser, actions.remove)
  app.post('/user/:userId/:collectionId/:displayId/:version/removeMembership', requireUser, actions.removeMembership)
  app.get('/user/:userId/:collectionId/:displayId/:version/replace', requireUser, actions.replace)
  app.get('/user/:userId/:collectionId/:displayId/:version/makePublic', requireUser, actions.makePublic)
  app.post(
    '/user/:userId/:collectionId/:displayId/:version/makePublic',
    requireUser,
    uploadToMemory.single('file'),
    actions.makePublic
  )

  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/removeCollection', requireUser, actions.removeCollection)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/addToCollection', requireUser, views.addToCollection)
  app.post('/user/:userId/:collectionId/:displayId/:version/:hash/share/addToCollection', requireUser, views.addToCollection)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/remove', requirePublicLogin, actions.remove)
  app.post('/user/:userId/:collectionId/:displayId/:version/:hash/share/removeMembership', requirePublicLogin, actions.removeMembership)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/replace', requirePublicLogin, actions.replace)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/makePublic', requirePublicLogin, actions.makePublic)
  app.post(
    '/user/:userId/:collectionId/:displayId/:version/:hash/share/makePublic',
    requirePublicLogin,
    uploadToMemory.single('file'),
    actions.makePublic
  )

  // Remote ICE/Benchling endpoints
  app.get('/public/:collectionId/:displayId/:version/copyFromRemote', requireUser, actions.copyFromRemote)
  app.post(
    '/public/:collectionId/:displayId/:version/copyFromRemote',
    requireUser,
    uploadToMemory.single('file'),
    actions.copyFromRemote
  )

  app.get('/public/:collectionId/:displayId/:version/createBenchlingSequence', requireUser, actions.createBenchlingSequence)
  app.post(
    '/public/:collectionId/:displayId/:version/createBenchlingSequence',
    requireUser,
    uploadToMemory.single('file'),
    actions.createBenchlingSequence
  )
  app.get('/public/:collectionId/:displayId/:version/createICEPart', requireUser, actions.createICEPart)
  app.post(
    '/public/:collectionId/:displayId/:version/createICEPart',
    requireUser,
    uploadToMemory.single('file'),
    actions.createICEPart
  )

  app.get(
    '/user/:userId/:collectionId/:displayId/:version/createBenchlingSequence',
    requireUser,
    actions.createBenchlingSequence
  )
  app.post(
    '/user/:userId/:collectionId/:displayId/:version/createBenchlingSequence',
    requireUser,
    uploadToMemory.single('file'),
    actions.createBenchlingSequence
  )
  app.get('/user/:userId/:collectionId/:displayId/:version/createICEPart', requireUser, actions.createICEPart)
  app.post(
    '/user/:userId/:collectionId/:displayId/:version/createICEPart',
    requireUser,
    uploadToMemory.single('file'),
    actions.createICEPart
  )

  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/createBenchlingSequence', requirePublicLogin, actions.createBenchlingSequence)
  app.post(
    '/user/:userId/:collectionId/:displayId/:version/:hash/share/createBenchlingSequence',
    requirePublicLogin,
    uploadToMemory.single('file'),
    actions.createBenchlingSequence
  )
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/createICEPart', requirePublicLogin, actions.createICEPart)
  app.post(
    '/user/:userId/:collectionId/:displayId/:version/:hash/share/createICEPart',
    requirePublicLogin,
    uploadToMemory.single('file'),
    actions.createICEPart
  )

  // Update Permissions Endpoints
  app.get('/public/:collectionId/:displayId/:version/addOwner', requireUser, views.addOwner)
  app.post('/public/:collectionId/:displayId/:version/addOwner', requireUser, views.addOwner)
  app.post('/public/:collectionId/:displayId/:version/removeOwner/:username', requireUser, actions.removeOwner)

  app.get('/user/:userId/:collectionId/:displayId/:version/addOwner', requireUser, views.addOwner)
  app.post('/user/:userId/:collectionId/:displayId/:version/addOwner', requireUser, views.addOwner)
  app.post('/user/:userId/:collectionId/:displayId/:version/removeOwner/:username', requireUser, actions.removeOwner)
  app.get('/user/:userId/:collectionId/:displayId/:version/shareLink', requireUser, api.shareLink)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/shareLink', requirePublicLogin, api.shareLink)

  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/addOwner', requirePublicLogin, views.addOwner)
  app.post('/user/:userId/:collectionId/:displayId/:version/:hash/share/addOwner', requirePublicLogin, views.addOwner)
  app.post('/user/:userId/:collectionId/:displayId/:version/:hash/share/removeOwner/:username', requirePublicLogin, actions.removeOwner)

  // Attachment Endpoints
  app.post('/public/:collectionId/:displayId/:version/attach', requireUser, actions.upload)
  app.post('/public/:collectionId/:displayId/:version/attachUrl', requireUser, api.attachUrl)
  app.get('/public/:collectionId/:displayId/:version/download', requirePublicLogin, api.download)

  app.post('/user/:userId/:collectionId/:displayId/:version/attach', requireUser, actions.upload)
  app.post('/user/:userId/:collectionId/:displayId/:version/attachUrl', requireUser, api.attachUrl)
  app.get('/user/:userId/:collectionId/:displayId/:version/download', requireUser, api.download)

  app.post('/user/:userId/:collectionId/:displayId/:version/:hash/share/attach', requirePublicLogin, actions.upload)
  app.post('/user/:userId/:collectionId/:displayId/:version/:hash/share/attachUrl', requirePublicLogin, api.attachUrl)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/download', requirePublicLogin, api.download)

  // Download Endpoints
  app.get('/public/:collectionId/:displayId/sbol', requirePublicLogin, api.persistentIdentity)
  app.get('/public/:collectionId/:displayId/sbolnr', requirePublicLogin, api.persistentIdentity)
  app.get('/user/:userId/:collectionId/:displayId/sbol', requirePublicLogin, api.persistentIdentity)
  app.get('/user/:userId/:collectionId/:displayId/sbolnr', requirePublicLogin, api.persistentIdentity)

  app.get('/public/:collectionId/:displayId/:version/sbol', requirePublicLogin, api.sbol)
  app.get('/public/:collectionId/:displayId/:version/sbolnr', requirePublicLogin, api.sbolnr)
  app.get('/public/:collectionId/:displayId/:version/omex', requirePublicLogin, api.omex)
  app.get('/public/:collectionId/:displayId/:version/summary', requirePublicLogin, api.summary)
  app.get('/public/:collectionId/:displayId/:version/fasta', requirePublicLogin, api.fasta)
  app.get('/public/:collectionId/:displayId/:version/gb', requirePublicLogin, api.genBank)
  app.get('/public/:collectionId/:displayId/:version/gff', requirePublicLogin, api.gff3)
  app.get('/public/:collectionId/:displayId/:version/metadata', requirePublicLogin, api.metadata)

  app.get('/user/:userId/:collectionId/:displayId/:version/sbol', requirePublicLogin, api.sbol)
  app.get('/user/:userId/:collectionId/:displayId/:version/sbolnr', requirePublicLogin, api.sbolnr)
  app.get('/user/:userId/:collectionId/:displayId/:version/omex', requirePublicLogin, api.omex)
  app.get('/user/:userId/:collectionId/:displayId/:version/summary', requirePublicLogin, api.summary)
  app.get('/user/:userId/:collectionId/:displayId/:version/fasta', requirePublicLogin, api.fasta)
  app.get('/user/:userId/:collectionId/:displayId/:version/gb', requirePublicLogin, api.genBank)
  app.get('/user/:userId/:collectionId/:displayId/:version/gff', requirePublicLogin, api.gff3)
  app.get('/user/:userId/:collectionId/:displayId/:version/metadata', requirePublicLogin, api.metadata)

  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/sbol', api.sbol)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/sbolnr', requirePublicLogin, api.sbolnr)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/omex', requirePublicLogin, api.omex)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/summary', requirePublicLogin, api.summary)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/fasta', requirePublicLogin, api.fasta)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/gb', requirePublicLogin, api.genBank)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/gff', requirePublicLogin, api.gff3)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/metadata', requirePublicLogin, api.metadata)

  app.get('/public/:collectionId/:displayId/:version/visualization', requirePublicLogin, views.visualization)
  app.get('/user/:userId/:collectionId/:displayId/:version/visualization', requirePublicLogin, views.visualization)
  app.get('/user/:userId/:collectionId/:displayId/:version/:hash/share/visualization', requirePublicLogin, views.visualization)

  // View/Download Endpoints
  app.get('/public/:collectionId/:displayId', requirePublicLogin, views.persistentIdentity)
  app.get('/user/:userId/:collectionId/:displayId', requirePublicLogin, views.persistentIdentity)

  app.get('/user/:userId/:collectionId/:displayId(*)/:version/:hash/share/full', requirePublicLogin, views.topLevel)
  app.get('/user/:userId/:collectionId/:displayId(*)/:version/:hash/share', requirePublicLogin, views.topLevel)
  app.post('/user/:userId/:collectionId/:displayId(*)/:version/:hash/share/full', requirePublicLogin, views.topLevel)
  app.post('/user/:userId/:collectionId/:displayId(*)/:version/:hash/share', requirePublicLogin, views.topLevel)

  app.get('/user/:userId/:collectionId/:displayId(*)/:version/:hash/share/sparql', requirePublicLogin, sparql)
  app.post('/user/:userId/:collectionId/:displayId(*)/:version/:hash/share/sparql', requirePublicLogin, sparql)

  app.get('/user/:userId/:collectionId/:displayId(*)/:version/full', requirePublicLogin, views.topLevel)
  app.get('/user/:userId/:collectionId/:displayId(*)/:version', requirePublicLogin, views.topLevel)
  app.post('/user/:userId/:collectionId/:displayId(*)/:version/full', requirePublicLogin, views.topLevel)
  app.post('/user/:userId/:collectionId/:displayId(*)/:version', requirePublicLogin, views.topLevel)

  app.get('/public/:collectionId/:displayId(*)/:version/full', requirePublicLogin, views.topLevel)
  app.get('/public/:collectionId/:displayId(*)/:version', requirePublicLogin, views.topLevel)
  app.post('/public/:collectionId/:displayId(*)/:version/full', requirePublicLogin, views.topLevel)
  app.post('/public/:collectionId/:displayId(*)/:version', requirePublicLogin, views.topLevel)

  app.get('/expose/:id', api.expose)

  app.post('/corruptLog', function (req, res) {
    // For testing purposes only
    for (let i = 0; i < 10000; i++) {
      console.log('Corrupting log file as requested')
    }
    res.status(200).send('Log file corrupted')
  })

  function sparql (req, res) {
    // jena sends accept: */* and then complains when we send HTML
    // back. so only send html if the literal string text/html is present
    // in the accept header.

    let accept = req.header('accept')
    if (accept && accept.indexOf('text/html') !== -1) {
      views.sparql(req, res)
    } else {
      api.sparql(req, res)
    }
  }

  function sparqlAdmin (req, res) {
    // jena sends accept: */* and then complains when we send HTML
    // back. so only send html if the literal string text/html is present
    // in the accept header.

    let accept = req.header('accept')
    if (accept && accept.indexOf('text/html') !== -1) {
      views.admin.sparql(req, res)
    } else {
      api.admin.sparql(req, res)
    }
  }

  function allowPublicSignup (req, res, next) {
    if (!config.get('allowPublicSignup')) {
      if (!req.accepts('text/html')) {
        res.status(401).send('Public signup is not allowed')
      } else {
        res.redirect('/')
      }
    } else {
      next()
    }
  }

  function requirePublicLogin (req, res, next) {
    if (config.get('requireLogin') && !req.user) {
      if (!req.accepts('text/html')) {
        res.status(401).send('Login required')
      } else {
        res.redirect('/login?next=' + encodeURIComponent(req.url))
      }
    } else {
      next()
    }
  }

  function requireUser (req, res, next) {
    if (!req.user) {
      if (!req.accepts('text/html')) {
        res.status(401).send('Login required')
      } else {
        res.redirect('/login?next=' + encodeURIComponent(req.url))
      }
    } else {
      next()
    }
  }

  function requireAdmin (req, res, next) {
    if (!req.user || !req.user.isAdmin) {
      if (!req.accepts('text/html')) {
        res.status(401).send('Administrator login required')
      } else {
        res.redirect('/login?next=' + encodeURIComponent(req.url))
      }
    } else {
      next()
    }
  }

  if (config.get('prewarmSearch')) {
    cache.update()
  }

  return app
}

module.exports = App
