

export const generateHexId = function (length=32) {
    var raw = [];
    var hex = '0123456789ABCDEF';
    for (var i = 0; i < length; i++) {
        raw[i] = hex.charAt([Math.floor(Math.random() * 0x10)]);
    }
    return raw.join('');
}
