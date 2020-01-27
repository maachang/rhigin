// rtest の レポート出力.
//
module.exports = function(out, result, exitTime) {

var ERROR_COLOR = "<#red>";
var END_COLOR = "<#/end>";

// スペースをセット.
var _cs = function(len) {
    var ret = "";
    for(var i = 0; i < len; i ++) {
        ret += " ";
    }
    return ret;
}

// 小数点第３桁まで表示.
var _sec = function(n) {
    var f = "";
    n = "" + (n / 1000);
    var p = n.indexOf(".");
    if(p != -1) {
        f = n.substring(p + 1);
        f = f + "000".substring(f.length);
        n = n.substring(0, p) + "." + f;
    } else {
        n = n + ".000";
    }
    return n;
}

// 全Spec結果のSpec件数と失敗件数を取得.
var allCount = function(value) {
    var res;
    var spec = 0;
    var failures = 0;
    var len = value.length;
    for(var i = 0; i < len; i ++) {
        res = resultAllCount(value[i].result);
        spec += res[0];
        failures += res[1];
    }
    return [spec, failures];
}

// １つのSpecファイル結果のSpec件数と失敗件数を取得.
var resultAllCount = function(value) {
    var res;
    var spec = 0;
    var failures = 0;
    var time = 0;
    var len = value.length;
    for(var i = 0; i < len; i ++) {
        res = specElementCount(value[i]);
        spec += res[0];
        failures += res[1];
        time += value[i].time;
    }
    return [spec, failures, time];
}

// １つのSpecファイル以下のSpec件数と失敗件数を取得.
var specElementCount = function(value) {
    if(value.type != 2) {
        // describe(0), it(1) 
        var errFlg = false;
        var list = value.inner;
        var len = list.length;
        var spec = 0;
        var failures = 0;
        for(var i = 0; i < len; i ++) {
            res = specElementCount(list[i]);
            spec += res[0];
            failures += res[1];
            list[i].errFlg = (res[1] > 0);
            if(res[1] > 0) {
                errFlg = true;
            }
        }
        value.errFlg = errFlg;
        return [spec, failures];
    }
    // expect(2)
    return [1, value.error];
}

// 詳細を表示.
var detailReport = function(value) {
    // 各Specファイル毎の結果を出力.
    var res, print, color, endColor;
    var len = value.length;
    for(var i = 0; i < len; i ++) {
        out.println("");
        var res = resultAllCount(value[i].result);
        if(res[1] > 0) {
            print = out.errPrintln;
            color = ERROR_COLOR;
            endColor = END_COLOR;
        } else {
            print = out.println;
            color = "";
            endColor = "";
        }
        print(_cs(1) + color + "● " + endColor + value[i].name);
        print(_cs(3) + res[0] + " spec, " + color + res[1] + " failures" + endColor);
        print(_cs(3) + "Finished in " + _sec(res[2]) + " seconds");

        var lenJ = value[i].result.length;
        for(var j = 0; j < lenJ; j ++) {
            detailDetailReport(3 + 2, value[i].result[j]);
        }
    }
}

// 処理内容を其々出力.
var detailDetailReport = function(space, value) {
    var print, noLinePrint, color, endColor, head;
    if(value.errFlg) {
        print = out.errPrintln;
        noLinePrint = out.errPrint;
        color = ERROR_COLOR;
        endColor = END_COLOR;
        head = ERROR_COLOR + " ✖ " + END_COLOR;
    } else {
        print = out.println;
        noLinePrint = out.print;
        color = "";
        endColor = "";
        head = "<#green> ○ " + END_COLOR;
    }
    if(value.type != 2) {
        var list = value.inner;
        var len = list.length;
        // describe(0)
        if(value.type == 0) {
            print(_cs(space) + color + "[" + value.name + "] Finished in " + _sec(value.time) + " seconds" + endColor);
        // it(1).
        } else {
            noLinePrint(_cs(space) + color + "(" + value.name + ")" + endColor);
            for(var i = 0; i < len; i ++) {
                if(list[i].type == 2) {
                    if(list[i].error > 0) {
                        noLinePrint(ERROR_COLOR + " ✖" + END_COLOR);
                    } else {
                        noLinePrint("<#green>" + " ○" + END_COLOR);
                    }
                }
            }
            print("");
        }
        for(var i = 0; i < len; i ++) {
            detailDetailReport(space + 2, list[i]);
        }
        return;
    }
    // expect(2)
    print(_cs(space) + color + head + value.name + endColor);
}

// 全処理結果の件数を取得.
var all = allCount(result);

// エラーがゼロの場合は、テスト結果のみを表示.
if(all[1] == 0) {
    out.println(all[0] + " spec, " + all[1] + " failures");
    out.println("Finished in " + _sec(exitTime) + " seconds");
    detailReport(result);
    out.println("");

    // 正常を通知.
    return true;
}

// エラーが存在する場合は、詳細を表示する.
out.errPrintln(all[0] + " spec, " + ERROR_COLOR + all[1] + " failures" + END_COLOR);
out.errPrintln("Finished in " + _sec(exitTime) + " seconds");
detailReport(result);
out.errPrintln("");

// エラーを通知.
return false;
}