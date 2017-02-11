const git = require('simple-git')();

module.exports = function (req, res) {

    git.pull(function (err, update) {
        if (err) {
            console.log("Error: " + err);
            res.status(500).send('Update failed: ' + err);
        } else if (update && update.summary.changes) {
            console.log("restarting!");
            require('child_process').exec('npm restart');
        } else {
            console.log("No changes detected.");
        }
    }).then(function () {
        res.redirect('/');
    });
}
