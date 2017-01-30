
const npool = require('npool')

const config = require('./config')

const threadpool = npool.createThreadPool(parseInt(config.get('threadPoolSize')))

