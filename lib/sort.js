const fastSort = require('fast-sort')

function sortArray (obj) {
  if (obj.length === 0) {
    return []
  }

  obj.forEach(item => {
    sort(item)
  })

  let keys = Object.keys(obj[0]).sort()

  fastSort(obj).asc(...keys)

  return obj
}

function sort (obj) {
  if (obj === undefined || obj === null) {
    return null
  }

  if (Array.isArray(obj)) {
    obj = sortArray(obj)
  } else if (typeof obj === 'object') {
    Object.keys(obj).forEach(key => {
      obj[key] = sort(obj[key])
    })
  }

  return obj
}

module.exports = sort
