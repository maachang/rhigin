// rhigin test 用コアモジュール.
//
(function() {
// テスト結果内容.
//
// [describe]
//   |
//   +-- type = 0 describeの場合はタイプは[0]
//   |
//   +-- name     describeの名前.
//   |
//   +-- time     describeの実行時間(msec).
//   |
//   +-- inner    describe内の describe や it などが格納される.
//
// [it]
//   |
//   +-- type = 1 itの場合はタイプは[1]
//   |
//   +-- name     itの名前.
//   |
//   +-- time     itの実行時間(msec).
//   |
//   +-- inner    it内のexpect が格納される.
//
// [expect]
//   |
//   +-- type = 2 expectの場合はタイプは[2]
//   |
//   +-- name     expectの処理名.
//   |
//   +-- success  処理結果成功の場合は[1]
//   |
//   +-- error    処理結果失敗の場合は[1]

var _root = [];                 // root.
var _eachList = [];             // beforeEach, afterEach のリスト.
var _now = _root;               // 現在の格納条件.
var _nowDescribe = null;        // 現在のテスト記載枠
var _nowEach = null;            // 現在のbeforeEach, afterEach のリスト.
var _nowIt = null;              // 現在のテスト項目.

// RTestをクリア.
var _clearRTest = function() {
    _root = [];
    _eachList = [];
    _now = _root;
    _nowDescribe = null;
    _nowEach = null;
    _nowIt = null;
}

// 乱数発生装置.
var __r = (function() {
    var a = 123456789;
    var b = 362436069;
    var c = 521288629;
    var d = 88675123;
    var s = Date.now();
    a=s=1812433253*(s^(s>>30))+1;
    b=s=1812433253*(s^(s>>30))+2;
    c=s=1812433253*(s^(s>>30))+3;
    d=s=1812433253*(s^(s>>30))+4;
    return function() {
        var t=a;
        var r=t;
        t = ( t << 11 );
        t = ( t ^ r );
        r = t;
        r = ( r >> 8 );
        t = ( t ^ r );
        r = b;
        a = r;
        r = c;
        b = r;
        r = d;
        c = r;
        t = ( t ^ r );
        r = ( r >> 19 );
        r = ( r ^ t );
        d = r;
        return r;
    };
})();

// 一意のコードを作成する.
var _createCode = function(len, no) {
    var i, n;
    var ret = [];
    var len4 = len / 4;
    var lenEtc = len % 4;
    var r = __r;
    for(i = 0; i < len4; i ++) {
        n = r();
        ret.push(n & 0x000000ff);
        ret.push((n & 0x0000ff00) >> 8);
        ret.push((n & 0x00ff0000) >> 16);
        ret.push(((n & 0xff000000) >> 24) & 0x000000ff);
    }
    for(i = 0; i < lenEtc; i ++) {
        ret.push(r() & 0x000000ff);
    }
    ret.push(no & 0x000000ff);
    return btoa(ret);
}

// タイプヘッダ.
var _typeHead = "_$type@";

// ランダム文字列の長さ.
var _RAND_LEN = 64;

// any判別.
var _any = _typeHead + _createCode(_RAND_LEN, 100);

// データタイプ判別用.
var types = {
    "undefined": _typeHead + _createCode(_RAND_LEN, 0),    // データタイプが undefined の場合.
    "null": _typeHead + _createCode(_RAND_LEN, 1),         // データタイプが null の場合.
    "string": _typeHead + _createCode(_RAND_LEN, 2),       // データタイプが 文字 の場合.
    "integer": _typeHead + _createCode(_RAND_LEN, 3),      // データタイプが 整数 の場合.
    "float": _typeHead + _createCode(_RAND_LEN, 4),        // データタイプが 浮動小数点 の場合.
    "number": _typeHead + _createCode(_RAND_LEN, 5),       // データタイプが 数字 の場合.
    "boolean": _typeHead + _createCode(_RAND_LEN, 6),      // データタイプが boolean の場合.
    "date": _typeHead + _createCode(_RAND_LEN, 7),         // データタイプが 日付オブジェクト の場合.
    "function": _typeHead + _createCode(_RAND_LEN, 8),     // データタイプが function の場合.
    "array": _typeHead + _createCode(_RAND_LEN, 9),        // データタイプが 配列 の場合.
    "object": _typeHead + _createCode(_RAND_LEN, 10),      // データタイプが オブジェクト の場合.
    "any": function(object) {                              // データタイプが オブジェクト型チェック の場合.
        return [_any, object];
    }
};

// タイプ判別.
var _checkType = function(value, check) {
    if(typeof(check) != "string" || check.indexOf(_typeHead) != 0) {
        // types.any の場合.
        if(typeof(check) == "array" && check.length == 2 && check[0] == _any) {
            var c = check[1];
            // オブジェクトの型比較.
            return value == c || value instanceof c;
        }
        // タイプ判別失敗の場合は、nullを返却.
        return null;
    }
    switch(check) {
        case types["undefined"]:
            return typeof(value) == "undefined";
        case types["null"]:
            return value == null;
    }
    if(value != null && typeof(value) != "undefined") {
        switch(check) {
            case types["string"]:
                return typeof(value) == "string";
            case types["integer"]:
                if(typeof(value) == "number") {
                    return ("" + value).indexOf(".") == -1;
                }
                break;
            case types["float"]:
                if(typeof(value) == "number") {
                    return ("" + value).indexOf(".") != -1;
                }
                break;
            case types["number"]:
                return typeof(value) == "number";
            case types["boolean"]:
                return typeof(value) == "boolean";
            case types["date"]:
                return typeof(value) == "object" &&
                    (value instanceof Date || value instanceof JDate);
            case types["array"]:
                return typeof(value) == "object" && value instanceof List;
            case types["object"]:
                return typeof(value) == "object";
        }
    }
    return false;
}

// expectの1つのパラメータを表示.
var _viewParam = function(v) {
    if(v == null) {
        return "null";
    } else if(v == undefined) {
        return "undefined";
    }
    var t = typeof(v);
    if(t == "number" || t == "boolean") {
        return "" + v;
    } else if(t == "string") {
        if(v.indexOf(_typeHead) == 0) {
            if(v == types.undefined) {
                return "types undefined";
            } else if(v == types.null) {
                return "types null";
            } else if(v == types.string) {
                return "types string";
            } else if(v == types.integer) {
                return "types integer";
            } else if(v == types.float) {
                return "types float";
            } else if(v == types.number) {
                return "types number";
            } else if(v == types.boolean) {
                return "types boolean";
            } else if(v == types.date) {
                return "types date";
            } else if(v == types.function) {
                return "types function";
            } else if(v == types.array) {
                return "types array";
            } else if(v == types.object) {
                return "types object";
            } else {
                return "types unknown";
            }
        }
        return "'" + v + "'";
    } else if(t == "object") {
        if(v instanceof Array) {
            if(v.length == 2 && v[0] == _any) {
                return "types any(" + v[1] + ")";
            }
            return "array(" + v.length + ")";
        } else if(v instanceof Error) {
            return "exception " + v;
        }
        return "object";
    } else if(t == "function") {
        return "function";
    }
}

// expectのパラメータを表示.
var _viewParams = function(v) {
    var ret = "";
    var len = v.length;
    for(var i = 3; i < len; i ++) {
        if(i != 3) {
            ret += ", ";
        }
        ret += _viewParam(v[i]);
    }
    return ret;
}

// expectの判別結果をセット.
var _result = function(name, notFlg, successFlg) {
    _now.push({
        // expect.
        type: 2,
        name: (notFlg ? "not." + name : name) + "(" + _viewParams(arguments) + ")",
        success: (successFlg && !notFlg) || (!successFlg && notFlg) ? 1 : 0,
        error: (!successFlg && !notFlg) || (successFlg && notFlg) ? 1 : 0
    });
}

// expectの例外結果をセット.
var _error = function(name, notFlg) {
    _now.push({
        // expect.
        type: 2,
        name: (notFlg ? "not." + name : name),
        success: 0,
        error: 1
    });
}

// beforeEachを実行.
var _beforeEach = function() {
    var list = _nowEach;
    var len = list.length;
    for(var i = 0; i < len; i ++) {
        list[i].before();
    }
}

// afterEachを実行.
var _afterEach = function() {
    var list = _nowEach;
    var len = list.length;
    for(var i = 0; i < len; i ++) {
        list[i].after();
    }
}

// RTestのテスト結果を取得.
var result = function() {
    var ret = _root;
    _clearRTest();
    return ret;
}

// テスト記載枠.
var describe = function(name, func) {
    // 新しいdescribeを登録.
    var beforeDescribe = _nowDescribe;
    var inner = [];
    _nowDescribe = {
        type: 0,            // descript.
        name: name,         // 名前.
        time: 0,            // 実行時間.
        inner: inner        // 枠内の内容.
    };
    _now.push(_nowDescribe);
    var before = _now;
    _now = inner;
    // 新しいeachをセット.
    var each = {before: function() {}, after: function() {}}; 
    var befEach = _nowEach; _nowEach = each;
    _eachList.push(each);
    // describeを実行.
    var startTime = Date.now();
    func();
    _nowDescribe.time = parseInt(Date.now() - startTime);
    // 今回のeachを廃棄.
    var p = _eachList.indexOf(each);
    if(p != -1) {
        _eachList.splice(p, 1);
    }
    _nowEach = befEach;
    // 前回のdescribeに戻す.
    _nowDescribe = beforeDescribe;
    _now = before ;
}

// itテストの前に実行.
var beforeEach = function(func) {
    if(typeof(func) != "function") {
        throw new Error("The content set in 'beforeEach' is not a function.")
    }
    if(_nowEach == null) {
        throw new Error("'describe' is not set.");
    }
    // 今回のdescribeに登録.
    _nowEach.before = func;
}

// itテストの後に実行.
var afterEach = function(func) {
    if(typeof(func) != "function") {
        throw new Error("The content set in 'afterEach' is not a function.")
    }
    if(_nowEach == null) {
        throw new Error("'describe' is not set.");
    }
    // 今回のdescribeに登録.
    _nowEach.after = func;
}

// テスト項目.
var it = function(name, func) {
    if(_nowDescribe == null) {
        throw new Error("'describe' is not set.");
    } else if(_nowIt != null) {
        throw new Error("'it' cannot enclose 'it' in 'it'.");
    }
    var inner = [];
    _nowIt = {
        type: 1,            // it.
        name: name,         // 名前.
        time: 0,            // 実行時間.
        inner: inner        // expectの処理結果.
    };
    _now.push(_nowIt);
    var before = _now;
    _now = inner;
    var startTime = Date.now();
    var errType = 0;
    try {
        errType = 0;
        _beforeEach();          // 定義されているbeforeEachを実行.
        errType = 1;
        func();
        errType = 2;
        _afterEach();           // 定義されているafterEachを実行.
    } catch(e) {
        // エラーの結果をセット.
        _result(
            ["beforeEach","it","afterEach"][errType] +
            ":exception", false, false, e);
    }
    _nowIt.time = parseInt(Date.now() - startTime);
    _nowIt = null;
    _now = before ;
}

// オブジェクトの完全一致チェック.
var _equal = function(src, v) {
    if(src == v) {
        return true;
    }
    if(typeof(src) == "object" && typeof(v) == "object") {
        if(src instanceof Array) {
            if(v instanceof Array) {
                return _eqArray(src, v);
            }
        } else if((src instanceof Date || src instanceof JDate) &&
            (v instanceof Date || v instanceof JDate)) {
            return src.getTime() == v.getTime();
        } else {
            return _eqObject(src, v);
        }
    }
    return false;
}
var _eqObject = function(src, v) {
    var cnt = 0;
    for(var k in src) {
        if(!_equal(src[k], v[k])) {
            return false;
        }
        cnt ++;
    }
    var vCnt = 0;
    for(var k in v) {
        vCnt ++;
    }
    return cnt == vCnt;
}
var _eqArray = function(src, v) {
    var len = src.length;
    if(len != v.length) {
        return false;
    }
    for(var i = 0; i < len; i ++) {
        if(!_equal(src[i], v[i])) {
            return false;
        }
    }
    return true;
}

// テスト評価.
var expect = function(value) {
    if(_nowIt == null) {
        throw new Error("it 'is' not set.");
    }
    var o = {};
    o["_$not"] = false;
    // not.
    o.not = function() {
        o["_$not"] = !o["_$not"];
        return o;
    }
    // value === v であるか.
    o.toBe = function(v) {
        try {
            var typeCheck = _checkType(value, v);
            if(typeCheck != null) {
                if(typeCheck) {
                    _result("toEqual", o["_$not"], true, value, v);
                } else {
                    _result("toEqual", o["_$not"], false, value, v);
                }
            } else {
                if(v === value) {
                    _result("toBe", o["_$not"], true, value, v);
                } else {
                    _result("toBe", o["_$not"], false, value, v);
                }
            }
        } catch(e) {
            _error("toBe", o["_$not"]);
        }
        return o;
    };
    // value の中身と v が完全一致であるか.
    o.toEqual = function(v) {
        try {
            var typeCheck = _checkType(value, v);
            if(typeCheck != null) {
                if(typeCheck) {
                    _result("toEqual", o["_$not"], true, value, v);
                } else {
                    _result("toEqual", o["_$not"], false, value, v);
                }
            } else {
                if(_equal(v, value)) {
                    _result("toEqual", o["_$not"], true, value, v);
                } else {
                    _result("toEqual", o["_$not"], false, value, v);
                }
            }
        } catch(e) {
            _error("toEqual", o["_$not"]);
        }
        return o;
    }
    o.toEquals = o.toEqual;
    // 文字列が正規表現にマッチするか.
    o.toMatch = function(v) {
        try {
            if(v.mache("" + value) != null) {
                _result("toMatch", o["_$not"], true, value, v);
            } else {
                _result("toMatch", o["_$not"], false, value, v);
            }
        } catch(e) {
            _error("toMatch", o["_$not"]);
        }
        return o;
    }
    // valueが存在するか.
    o.toExist = function() {
        try {
            if(value != null && typeof(value) != "undefined") {
                _result("toExist", o["_$not"], true, value);
            } else {
                _result("toExist", o["_$not"], false, value);
            }
        } catch(e) {
            _error("toExist", o["_$not"]);
        }
        return o;
    }
    // valueが定義済みか.
    o.toBeDefined = function() {
        try {
            if(typeof(value) != "undefined") {
                _result("toBeDefined", o["_$not"], true, value);
            } else {
                _result("toBeDefined", o["_$not"], false, value);
            }
        } catch(e) {
            _error("toBeDefined", o["_$not"]);
        }
        return o;
    }
    // valueが未定義か.
    o.toBeUndefined = function() {
        try {
            if(typeof(value) != "undefined") {
                _result("toBeUndefined", o["_$not"], true, value);
            } else {
                _result("toBeUndefined", o["_$not"], false, value);
            }
        } catch(e) {
            _error("toBeUndefined", o["_$not"]);
        }
        return o;
    }
    // valueがnullか.
    o.toBeNull = function() {
        try {
            if(value == null) {
                _result("toBeNull", o["_$not"], true, value);
            } else {
                _result("toBeNull", o["_$not"], false, value);
            }
        } catch(e) {
            _error("toBeNull", o["_$not"]);
        }
        return o;
    }
    // valueが false 相当の値か（false, 0, 空文字列など）.
    o.toBeFalsy = function() {
        try {
            if(!!!value) {
                _result("toBeFalsy", o["_$not"], true, value);
            } else {
                _result("toBeFalsy", o["_$not"], false, value);
            }
        } catch(e) {
            _error("toBeFalsy", o["_$not"]);
        }
        return o;
    }
    // valueが true 相当の値か（true, 1, “a” など）.
    o.toBeTruthy = function() {
        try {
            if(!!value) {
                _result("toBeTruthy", o["_$not"], true, value);
            } else {
                _result("toBeTruthy", o["_$not"], false, value);
            }
        } catch(e) {
            _error("toBeTruthy", o["_$not"]);
        }
        return o;
    }
    // valueが false か.
    o.toBeFalse = function() {
        try {
            if(value === false) {
                _result("toBeFalsy", o["_$not"], true, value);
            } else {
                _result("toBeFalsy", o["_$not"], false, value);
            }
        } catch(e) {
            _error("toBeFalsy", o["_$not"]);
        }
        return o;
    }
    // valueが true か.
    o.toBeTrue = function() {
        try {
            if(value === true) {
                _result("toBeTruthy", o["_$not"], true, value);
            } else {
                _result("toBeTruthy", o["_$not"], false, value);
            }
        } catch(e) {
            _error("toBeTruthy", o["_$not"]);
        }
        return o;
    }
    // valueが NaN か.
    o.toBeNaN = function() {
        try {
            if(isNaN(value)) {
                _result("toBeFalsy", o["_$not"], true, value);
            } else {
                _result("toBeFalsy", o["_$not"], false, value);
            }
        } catch(e) {
            _error("toBeFalsy", o["_$not"]);
        }
        return o;
    }
    // value が配列で、指定された値を含んでいるか.
    o.toContain = function(eq) {
        try {
            var res;
            if(value == null || value == undefined) {
                res = -1;
            } else if(value instanceof Array) {
                res = value.indexOf(eq);
            } else {
                res = -1;
            }
            if(res != -1) {
                _result("toContain", o["_$not"], true, value, eq);
            } else {
                _result("toContain", o["_$not"], false, value, eq);
            }
        } catch(e) {
            _error("toContain", o["_$not"]);
        }
        return o;
    }
    // value < n であるか.
    o.toBeLessThan = function(n) {
        try {
            if(value < n) {
                _result("toBeLessThan", o["_$not"], true, value, n);
            } else {
                _result("toBeLessThan", o["_$not"], false, value, n);
            }
        } catch(e) {
            _error("toBeLessThan", o["_$not"]);
        }
        return o;
    }
    // value > n であるか.
    o.toBeGreaterThan = function(n) {
        try {
            if(value > n) {
                _result("toBeLessThan", o["_$not"], true, value, n);
            } else {
                _result("toBeLessThan", o["_$not"], false, value, n);
            }
        } catch(e) {
            _error("toBeLessThan", o["_$not"]);
        }
        return o;
    }
    // 小数が有効数字 c ケタで n に等しいか.
    o.toBeCloseTo = function(n, c) {
        try {
            var n0, ne, p;
            n = "" + parseInt(n);
            c = parseInt(c);
            var nn = "" + parseFloat(value);
            if(nn == "NaN") {
                _error("toBeCloseTo", o["_$not"]);
                return;
            }
            if((p = nn.indexOf(".")) == -1) {
                n0 = nn;
                ne = "";
            } else {
                n0 = nn.substring(0, p);
                ne = nn.substring(p + 1);
            }
            if(c == 0) {
                if(n0 == n) {
                    _result("toBeCloseTo", o["_$not"], true, value, n, c);
                } else {
                    _result("toBeCloseTo", o["_$not"], false, value, n, c);
                }
            } else {
                if(ne.substring(c-1, c) == n) {
                    _result("toBeCloseTo", o["_$not"], true, value, n, c);
                } else {
                    _result("toBeCloseTo", o["_$not"], false, value, n, c);
                }
            }
        } catch(e) {
            _error("toBeCloseTo", o["_$not"]);
        }
        return o;
    }
    // 関数 value が何らかの例外を投げることを期待.
    o.toThrow = function(t) {
        if(typeof(value) != "function") {
            _error("toThrow", o["_$not"]);
        } else if(typeof(t) == "undefined") {
            try {
                value();
                _result("toThrow", o["_$not"], false, value, t);
            } catch(e) {
                _result("toThrow", o["_$not"], true, value, t);
            }
        } else {
            try {
                value();
                _result("toThrow", o["_$not"], false, value, t);
            } catch(e) {
                if(e == t) {
                    _result("toThrow", o["_$not"], true, value, t);
                } else {
                    _result("toThrow", o["_$not"], false, value, t);
                }
            }
        }
        return o;
    }
    return o;
}

// モジュール返却.
module.exports = {
    types: types
    ,describe: describe
    ,beforeEach: beforeEach
    ,afterEach: afterEach
    ,it: it
    ,expect: expect
    ,result: result
};

})();