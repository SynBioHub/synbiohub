
function sortArray (obj) {
  if (obj.length === 0) {
    return []
  }

  console.log('======')
  obj.forEach(item => console.log(JSON.stringify(item)))
  console.log('======')

  obj.sort((a, b) => {
    let JSONa = JSON.stringify(a)
    let JSONb = JSON.stringify(b)

    if (JSONa > JSONb) {
      return 1
    } else if (JSONa < JSONb) {
      return -1
    } else {
      return 0
    }
  })

  console.log('======')
  obj.forEach(item => console.log(JSON.stringify(item)))
  console.log('======')

  return obj
}

function sort (obj) {
  if (obj === undefined || obj === null) {
    return null
  }

  if (Array.isArray(obj)) {
    console.log('Object is an array')
    console.log(obj)
    obj = sortArray(obj)
  } else if (typeof obj === 'object') {
    console.log('Object is an object')
    Object.keys(obj).forEach(key => {
      console.log(key)
      obj[key] = sort(obj[key])
    })
  }

  return obj
}

module.exports = sort
