(function() {

var describe = function(name, func) {
}

var it = function(name, func) {
}

var expect = function(value) {
    var o = {};
    o["_$value"] = value;
    o["_$not"] = false;
    // 真逆の条件.
    o.not = function() {
        o["_$not"] = true;
        return o;
    }
    // foo === 1 であるか.
    o.toBe = function(v) {
        try {

        }
    };
    // foo === 1 であるか.
    o.toEqual = function(v) {

    }
    // 文字列が正規表現にマッチするか.
    o.toMatch = function(v) {

    }
    // 要素が存在するか.
    o.toExist = function() {

    }
    // 変数が定義済みか.
    o.toBeDefined = function() {

    }
    // 変数が未定義か.
    o.toBeUndefined = function() {

    }
    // 変数がnullか.
    o.toBeNull = function() {

    }
    // 変数が true 相当の値か（true, 1, “a” など）.
    o.toBeTruthy = function() {

    }
    // 変数が false 相当の値か（false, 0, 空文字列など）.
    o.toBeFalsy = function() {

    }
    // 配列が値を含んでいるか.
    o.toContain = function(eq) {

    }
    // foo < 2 であるか.
    o.toBeLessThan = function(n) {

    }
    // foo > 0 であるか.
    o.toBeGreaterThan = function(n) {

    }
    // 小数が有効数字 c ケタで n に等しいか.
    o.toBeCloseTo = function(n, c) {

    }
    // 関数 func が何らかの例外を投げることを期待.
    o.toThrow = function() {

    }
}



})