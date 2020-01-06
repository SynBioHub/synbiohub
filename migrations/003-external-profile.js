module.exports = {
  up: async (migration, DataTypes) => {
    await migration.createTable('user_external_profile', {
      id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
      },
      userId: {
        type: DataTypes.INTEGER,
        allowNull: false,
        references: {
          model: 'user',
          key: 'id'
        },
        onDelete: 'CASCADE',
        onUpdate: 'CASCADE'
      },
      profileId: {
        type: DataTypes.STRING,
        allowNull: false
      },
      profileName: {
        type: DataTypes.STRING,
        allowNull: false
      }
    })

    await migration.addIndex('user_external_profile', ['userId', 'profileId', 'profileName'], {
      indexName: 'user_external_profile_user_id_profile_id_profile_name',
      indicesType: 'UNIQUE'
    })
  },

  down: (query) => {
    return query.dropTable('user_external_profile')
  }
}
