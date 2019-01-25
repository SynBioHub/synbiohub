
const Sequelize = require('sequelize')
const Umzug = require('umzug')

const sequelize = new Sequelize('database', 'username', 'password', {
  dialect: 'sqlite',
  pool: {
    max: 5,
    min: 0,
    idle: 10000
  },
  logging: false,
  storage: 'synbiohub.sqlite'
})

const User = sequelize.define('user', {
  name: { type: Sequelize.STRING },
  username: { type: Sequelize.STRING },
  email: { type: Sequelize.STRING },
  affiliation: { type: Sequelize.STRING },
  password: { type: Sequelize.STRING },
  graphUri: { type: Sequelize.STRING },
  isAdmin: { type: Sequelize.BOOLEAN },
  resetPasswordLink: { type: Sequelize.STRING },
  isCurator: { type: Sequelize.BOOLEAN },
  isMember: { type: Sequelize.BOOLEAN }
}, {
  freezeTableName: true
})

const Job = sequelize.define('job', {
  userId: { type: Sequelize.INTEGER },
  name: { type: Sequelize.STRING },
  description: { type: Sequelize.STRING },
  graphUri: { type: Sequelize.STRING },
  inputUri: { type: Sequelize.STRING },
  status: { type: Sequelize.INTEGER },
  currentTaskIndex: { type: Sequelize.INTEGER },
  iteration: { type: Sequelize.INTEGER },
  lastUri: { type: Sequelize.STRING }
}, {
  freezeTableName: true
})

User.hasMany(Job, { foreignKey: 'userId' })
Job.belongsTo(User)

const Task = sequelize.define('task', {
  jobId: { type: Sequelize.INTEGER },
  index: { type: Sequelize.INTEGER },
  name: { type: Sequelize.STRING },
  description: { type: Sequelize.STRING },
  params: { type: Sequelize.STRING },
  status: { type: Sequelize.INTEGER }
}, {
  freezeTableName: true
})

Job.hasMany(Task, { foreignKey: 'jobId' })
Task.belongsTo(Job)

var umzug = new Umzug({
  storage: 'sequelize',
  storageOptions: {
    sequelize: sequelize
  },
  migrations: {
    path: 'migrations',
    pattern: /^\d+[\w-]+\.js$/,
    params: [
      sequelize.getQueryInterface(), // queryInterface
      sequelize.constructor, // DataTypes
      function () {
        throw new Error('Migration tried to use old style "done" callback. Please upgrade to "umzug" and return a promise instead.')
      }
    ]
  }
})

module.exports = {
  umzug: umzug,
  sequelize: sequelize,
  model: {
    User: User,
    Job: Job,
    Task: Task
  }
}
