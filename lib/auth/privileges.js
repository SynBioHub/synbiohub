function validate (priv) {
  return priv
}

/** Permissions -
 * Stored in the database as an integer:
 * 0: Can only read
 * 1: Can modify
 * 2: Can share
 */

function canRead (priv) {
  return priv >= 0
}

function canEdit (priv) {
  return priv >= 1
}

function canShare (priv) {
  return priv >= 2
}

function toText (priv) {
  if (priv === null || priv === undefined) {
    priv = -1
  }

  priv = parseInt(priv)
  switch (priv) {
    case 0:
      return 'Can view'
    case 1:
      return 'Can edit'
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
