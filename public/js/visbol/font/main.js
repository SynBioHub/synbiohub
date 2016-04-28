
define([],
        
function() {

    var fonts = {};

    function loadFont(name, req, onload, config) {

        var baseUrl = name;
        
        $.getJSON(baseUrl + '/font.json', function(fontSpec) {

            var font = {
                base: fontSpec.base || null
            };

            fontSpec.glyphs.forEach(function(glyphName) {

                req([ baseUrl + '/' + glyphName + '.js' ], function(glyph) {

                    font[glyphName] = glyph;

                    if(Object.keys(font).length === fontSpec.glyphs.length) {
                        
                        fonts[fontSpec.name] = font;

                        onload(font);

                    }
                    
                });

            });
        });
    }

    function getFont(name) {

        var font = fonts[name];

        if(typeof font.base === 'string') {

            var base = getFont(font.base);

            Object.keys(base).forEach(function(glyph) {

                if(font[glyph] === undefined)
                    font[glyph] = base[glyph];

            });

            delete font.base;
        }

        return font;
    }

    return {
        
        load: loadFont,
        get: getFont

    };

});



