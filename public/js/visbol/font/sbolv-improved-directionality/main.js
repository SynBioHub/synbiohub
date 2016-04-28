
define([

    'sbolv',
    './half-cds',
    './marked-terminator'

], function(sbolv, halfCDS, markedTerminator) {

    var font = {};

    Object.keys(sbolv).forEach(function(key) {
        font[key] = sbolv[key];
    });

    font['cds'] = halfCDS;
    font['terminator'] = markedTerminator;


});


