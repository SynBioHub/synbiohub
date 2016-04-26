
var mongoose = require('mongoose')

var userSchema = new mongoose.Schema({
    name: 'string',
    email: 'string',
    affiliation: 'string',
    password: 'string',
    storeName: 'string',
    isAdmin: 'boolean',
});

var User = mongoose.model('User', userSchema)

module.exports = User


