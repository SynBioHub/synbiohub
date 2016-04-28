
var glyphMapping = {

    'sbolv-promoter': 'promoter',
    'sbolv-cds': 'cds',
    'sbolv-user-defined': 'user-defined',
    'sbolv-terminator': 'terminator',
    'sbolv-res': 'res'
};

var glyphNames = Object.keys(glyphMapping);

var glyphFilenames = glyphNames.map(function(glyphName) {

    return './' + glyphName;

});

define(glyphFilenames, function() {

    var glyphs = Array.prototype.slice.call(arguments, 0);

    var font = {};

    for(var i = 0; i < glyphs.length; ++ i)
        font[glyphMapping[glyphNames[i]]] = glyphs[i];

    return font;
});



