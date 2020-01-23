// rhigin test.
//
//
(function(_g) {

// rtest ホーム.
var RTEST_HOME = RHIGIN_HOME + "/test/rtest";

// rtest 参照先フォルダ名.
var RTEST_LIB_DIR = RTEST_HOME + "/lib";

// rtest 用のフォルダ.
var RTEST_FOLDER = File.fullPath("./spec");

// spec フォルダが存在するかチェック.
if(!File.isDir(RTEST_FOLDER)) {
    errPrintln("Spec folder for running rtest does not exist.");
    return false;
}

// コンソール出力.
var print = function(n) {
    java.lang.System.out.print(n);
}

// コンソール出力.
var println = function(n) {
    java.lang.System.out.println(n);
}

// エラーコンソール出力.
var errPrint = function(n) {
    java.lang.System.err.print(n);
}

// エラーコンソール出力.
var errPrintln = function(n) {
    java.lang.System.err.println(n);
}

// rtest ファイル名判別.
var isRTestFile = function(name) {
    return name.trim().toLowerCase().endsWith("spec.js");
}

// specフォルダ配下から[*Spec.js] と言うファイル名を探して取得する.
var readFolderBySpecFiles = function(folder) {
    var _readFolderBySpecFiles = function(out, dir) {
        dir = File.fullPath(dir);
        var name;
        var list = File.list(dir);
        var len = list.length;
        for(var i = 0; i < len; i ++) {
            name = dir + "/" + list[i];
            if(File.isDir(name)) {
                _readFolderBySpecFiles(out, name);
            } else if(isRTestFile(name)) {
                if(File.isFile(name)) {
                    out.push(name);
                }
            }
        }
    };
    var ret = [];
    _readFolderBySpecFiles(ret, folder);
    return ret;
}

// コマンド実行パラメータで指定された[*Spec.js]のファイルがある場合は、その内容を取得する.
var readArgsBySpecFiles = function(folder) {
    var name;
    var ret = [];
    var len = args();
    // 後ろから読み込む.
    for(var i = len-1; i >= 0; i --) {
        name = args(i);
        if(name.startsWith("/")) {
            name = name.substring(1).trim();
        }
        if(isRTestFile(folder + "/" + name)) {
            name = File.fullPath(folder + "/" + name);
            if(File.isFile(name)) {
                ret.push(name);
            } else {
                throw new Error(
                    "Spec file set by command argument does not exist: "
                    + name);
            }
    } else {
            break;
        }
    }
    return ret;
}

// テスト実行.
var executeRTest = function(name) {
    try {
        return eval("""
            (function(_g) {
                var rtestCore = require("${RTEST_LIB_DIR}/rtest-core");
                var types = rtestCore.types;
                var describe = rtestCore.describe;
                var beforeEach = rtestCore.beforeEach;
                var afterEach = rtestCore.afterEach;
                var it = rtestCore.it;
                var expect = rtestCore.expect;
                ${File.readByString(name)}
                return rtestCore.result();
            })(this);
            """);
    } catch(e) {
        return "" + e;
    }
}


// コマンド引数から情報を取得.
var specList = readArgsBySpecFiles(RTEST_FOLDER);
if(specList.length == 0) {
    // コマンドでSpecファイルが設定されてない場合はspecフォルダ内のSpecファイルを検索.
    specList = readFolderBySpecFiles(RTEST_FOLDER);
}

// テスト実行.
var value;
var result = [];
var len = specList.length;
var startTime = Date.now();
for(var i = 0; i < len; i ++) {
    value = executeRTest(specList[i]);
    result.push({
        name: specList[i].substring(RTEST_FOLDER.length + 1),
        result: value
    });
}

// 終了時間を取得.
var exitTime = Date.now() - startTime;

//とりあえず表示させる.
console.log("len: " + len + " time: " + exitTime + " msec");
console.log(JSON.toString(result));

})(this);