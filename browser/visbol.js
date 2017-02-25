
const visbol = require('visbol')
const sbolv = require('visbol/font/sbolv/main')

if(document.getElementById('design')
    && typeof meta !== 'undefined'
    && meta.displayList) {

    var design = new visbol.Design({
        element: document.getElementById('design'),
        font: sbolv
    });

    design.setDisplayList(meta.displayList);

}



