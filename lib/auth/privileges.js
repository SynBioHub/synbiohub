
/** Permissions -
 * Stored in the database as an integer:
 * 0: No permissions
 * 1: Can read
 * 2: Can edit
 * 3: Can share
 */

function validate (priv) {
  if (priv < 0 || priv > 3) {
    throw new Error('Invalid privilege value!')
  }

  return priv
}

function canRead (priv) {
  return priv > 0
}

function canEdit (priv) {
  return priv > 1
}

function canShare (priv) {
  return priv > 2
}

function toText (priv) {
  if (priv === null || priv === undefined) {
    priv = -1
  }

  priv = parseInt(priv)
  switch (priv) {
    case 1:
      return 'Can view'
    case 2:
      return 'Can edit'
    case 3:
      return 'Can share'
    default:
      return 'Unknown privilege'
  }
}

module.exports = {
  validate: validate,
  canRead: canRead,
  canEdit: canEdit,
  canShare: canShare,
  toText: toText
}
