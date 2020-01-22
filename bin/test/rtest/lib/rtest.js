(function() {

var _root = [];                 // root.
var _now = _root;               // 現在の格納条件.
var _nowDescribe = null;        // 現在のテスト記載枠
var _nowIt = null;              // 現在のテスト項目.

// RTestをクリア.
var _clearRTest = function() {
    _root = [];
    _now = _root;
    _nowDescribe = null;
    _nowIt = null;
}

// RTestの条件を取得.
var _getRTest = function() {
    var ret = _root;
    _clearRTest();
    return ret;
}

// テスト記載枠.
var describe = function(name, func) {
    var inner = [];
    var beforeDescribe = _nowDescribe;
    _nowDescribe = {
        type: 0,            // descript.
        name: name,         // 名前.
        inner: inner        // 枠内の内容.
    };
    _now.push(_nowDescribe);
    var before = _now;
    _now = inner;
    func();
    _nowDescribe = beforeDescribe;
    _now = before ;
}

// テスト項目.
var it = function(name, func) {
    if(_nowDescribe == null) {
        throw new Error("describe is not set.");
    }
    var inner = [];
    _nowIt = {
        type: 1,            // it.
        name: name,         // 名前.
        inner: inner        // expectの処理結果.
    };
    _now.push(_nowIt);
    var before = _now;
    _now = inner;
    func();
    _nowIt = null;
    _now = before ;
}

// expactの判別結果をセット.
var _result = function(name, notFlg, successFlg) {
    _now.push({
        name: notFlg ? "not." + name : name,
        success: (successFlg && !notFlg) || (!successFlg && notFlg) ? 1 : 0,
        error: (!successFlg && !notFlg) || (successFlg && notFlg) ? 0 : 1
    });
}

// expactの例外結果をセット.
var _error = function(name, notFlg) {
    _now.push({
        name: notFlg ? "not." + name : name,
        success: 0,
        error: 1
    });
}

// テスト評価.
var expect = function(value) {
    if(_nowIt == null) {
        throw new Error("it is not set.");
    }
    var o = {};
    o["_$not"] = false;
    // not.
    o.not = function() {
        o["_$not"] = !o["_$not"];
        return o;
    }
    // foo === 1 であるか.
    o.toBe = function(v) {
        try {
            if(v === value) {
                _result("toBe", o["_$not"], true);
            } else {
                _result("toBe", o["_$not"], false);
            }
        } catch(e) {
            _error("toBe", o["_$not"]);
        }
        return o;
    };
    // foo == 1 であるか.
    o.toEqual = function(v) {
        try {
            if(v == value) {
                _result("toEqual", o["_$not"], true);
            } else {
                _result("toEqual", o["_$not"], false);
            }
        } catch(e) {
            _error("toEqual", o["_$not"]);
        }
    }
    // 文字列が正規表現にマッチするか.
    o.toMatch = function(v) {
        try {
            if(v.mache("" + value) != null) {
                _result("toMatch", o["_$not"], true);
            } else {
                _result("toMatch", o["_$not"], false);
            }
        } catch(e) {
            _error("toMatch", o["_$not"]);
        }
    }
    // 要素が存在するか.
    o.toExist = function() {
        try {
            if(value != null && value != undefined) {
                _result("toExist", o["_$not"], true);
            } else {
                _result("toExist", o["_$not"], false);
            }
        } catch(e) {
            _error("toExist", o["_$not"]);
        }
    }
    // 変数が定義済みか.
    o.toBeDefined = function() {
        try {
            if(value != undefined) {
                _result("toBeDefined", o["_$not"], true);
            } else {
                _result("toBeDefined", o["_$not"], false);
            }
        } catch(e) {
            _error("toBeDefined", o["_$not"]);
        }
    }
    // 変数が未定義か.
    o.toBeUndefined = function() {
        try {
            if(value == undefined) {
                _result("toBeUndefined", o["_$not"], true);
            } else {
                _result("toBeUndefined", o["_$not"], false);
            }
        } catch(e) {
            _error("toBeUndefined", o["_$not"]);
        }
    }
    // 変数がnullか.
    o.toBeNull = function() {
        try {
            if(value == null) {
                _result("toBeNull", o["_$not"], true);
            } else {
                _result("toBeNull", o["_$not"], false);
            }
        } catch(e) {
            _error("toBeNull", o["_$not"]);
        }
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
    return o;
}



})