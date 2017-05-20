var Bluebird = require('bluebird');

module.exports = {
    up: (query, DataTypes) => {
        return Promise.all([
            query.addColumn('user', 'isMember', {
                type: DataTypes.BOOLEAN,
                allowNull: false,
                defaultValue: false
            }),
            query.sequelize.query(`UPDATE "user" SET "isMember" = "isAdmin"`, {raw: true}),
            query.addColumn('user', 'isCurator', {
                type: DataTypes.BOOLEAN,
                allowNull: false,
                defaultValue: false
            }),
            query.sequelize.query(`UPDATE "user" SET "isCurator" = "isAdmin"`, {raw: true})
        ])
    },

    down: (query, DataTypes) => {
        return Promise.all([
            query.removeColumn('user', 'isMember'),
            query.removeColumn('user', 'isCurator')
        ]);
    }
};