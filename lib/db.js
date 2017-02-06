
const Sequelize = require('sequelize')

const sequelize = new Sequelize('database', 'username', 'password', {
  dialect: 'sqlite',
  pool: {
    max: 5,
    min: 0,
    idle: 10000
  },
  storage: 'synbiohub.sqlite'
})

const User = sequelize.define('user', {
  name: { type: Sequelize.STRING },
  email: { type: Sequelize.STRING },
  affiliation: { type: Sequelize.STRING },
  password: { type: Sequelize.STRING },
  graphUri: { type: Sequelize.STRING },
  isAdmin: { type: Sequelize.BOOLEAN },
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
}, {
  freezeTableName: true
})


const Task = sequelize.define('task', {
  jobId: { type: Sequelize.INTEGER },
  index: { type: Sequelize.INTEGER },
  name: { type: Sequelize.STRING },
  description: { type: Sequelize.STRING },
  params: { type: Sequelize.STRING },
  status: { type: Sequelize.INTEGER },
}, {
  freezeTableName: true
})

module.exports = {
    sequelize: sequelize,
    model: {
        User: User,
        Job: Job,
        Task: Task
    }
}

