
var requirejs = window.require

requirejs.config({
    packages: [
        { 
            name: 'visbol',
            location: '/js/visbol/lib'
        },
        { 
            name: 'visbol-font',
            location: '/js/visbol/font'
        }
    ]
});

requirejs([

    'visbol',
    'visbol-font',
    'visbol-font!/js/visbol/font/sbolv',

], function(visbol, visbolFont, sbolv) {

    if(document.getElementById('design')
        && typeof meta !== 'undefined'
        && meta.displayList) {

        var design = new visbol.Design({
            element: document.getElementById('design'),
            font: visbolFont.get('sbolv')
        });

        design.setDisplayList(meta.displayList);

    }

});

