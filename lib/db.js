
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
  resetPasswordLink: { type: Sequelize.STRING }
}, {
  freezeTableName: true
})

module.exports = {
    sequelize: sequelize,
    model: {
        User: User
    }
}

