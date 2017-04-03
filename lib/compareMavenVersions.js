

// Source: https://gist.github.com/danielflower/3656539
//

/**
 * Compares two Maven version strings and returns value suitable for use in Array.sort.
 * @param {String} v1
 * @param {String} v2
 * @return {Number} Negative number if v1 is older than v2; positive number if v1 is newer than v2; 0 if equal.
 */
module.exports = function (v1, v2) {
    // Strategy: pad each part of the version with zeros so strings are same length, then do string compare.
    // Snapshot version have an extra 0 put on the end, whereas release versions have a 1 on the end
    // e.g. 1.5-SNAPSHOT vs. 1.10.0 => 1.05.0.a vs. 1.10.0.c

    // e.g. 1.5 => [1,5]
    var v1Bits = v1.split(".");
    var v2Bits = v2.split(".");
    var v1OriginalLength = v1Bits.length;
    var v2OriginalLength = v2Bits.length;

    // equalise the number of parts, e.g. [1, 5] => [1,5,0]
    while (v1Bits.length < v2Bits.length) {
        v1Bits.push("0");
    }
    while (v2Bits.length < v1Bits.length) {
        v2Bits.push("0");
    }

    // Change snapshot versions to have an extra ".0" and release versions to have an extra ".1"
    function alterBasedOnSnapshotOrReleaseVersion(bits, potentialTextPosition) {
        if (bits[potentialTextPosition].indexOf("-SNAPSHOT") > 0) {
            bits[potentialTextPosition] = bits[potentialTextPosition].replace("-SNAPSHOT", "");
            bits.push("0");
        } else if (bits[potentialTextPosition].indexOf("-") > 0) {
            bits[potentialTextPosition] = bits[potentialTextPosition].replace(/\-.*/, "");
            bits.push("1");
        } else {
            bits.push("2");
        }
    }
    alterBasedOnSnapshotOrReleaseVersion(v1Bits, v1OriginalLength - 1);
    alterBasedOnSnapshotOrReleaseVersion(v2Bits, v2OriginalLength - 1);

    // pad with zeros, e.g. [1,5,0] => [1,05,0]
    for (var i = 0; i < v1Bits.length; i++) {
        var targetLength = Math.max(v1Bits[i].length, v2Bits[i].length);
        while (v1Bits[i].length < targetLength) {
            v1Bits[i] = "0" + v1Bits[i];
        }
        while (v2Bits[i].length < targetLength) {
            v2Bits[i] = "0" + v2Bits[i];
        }
    }

    // back to normal, e.g. [1,05,0] => 1.05.0
    var transformed1 = v1Bits.join(".");
    var transformed2 = v2Bits.join(".");

    //console.log("v1: " + v1 + "=>" + transformed1 + ", v2: " + v2 + "=>" + transformed2);

    return transformed1.localeCompare(transformed2);
};


