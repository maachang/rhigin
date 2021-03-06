// rtest の レポート出力.
//
module.exports = function(out, result, exitTime, verbose) {

var SUCCESS_COLOR = "<#green>";
var SUCCESS_END_COLOR = "<#/green>";
var ERROR_COLOR = "<#red>";
var ERROR_END_COLOR = "<#/red>";
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
    n = "" + (n == 0 || isNaN(n) ? 0 : (n / 1000));
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

// 番号のゼロサプレス.
var _no = function(n) {
    n = "" + n;
    return "0000".substring(n.length) + n;
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
        var len = list == null || list == undefined ? 0 : list.length;
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
        print(_cs(1) + color + "● " + value[i].name + endColor);
        print(_cs(3) + res[0] + " spec, " + color + res[1] + " failures" + endColor);
        print(_cs(3) + " Finished in " + _sec(res[2]) + " seconds");

        var lenJ = value[i].result.length;
        for(var j = 0; j < lenJ; j ++) {
            detailDetailReport(false, 3 + 2, j + 1, value[i].result[j]);
        }
    }
}

// 処理内容を其々出力.
var detailDetailReport = function(verboseFlag, space, no, value) {
    verboseFlag = !verboseFlag ? verbose : verboseFlag;
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
        head = SUCCESS_COLOR + " ○ " + END_COLOR;
    }
    if(value.type != 2) {
        var errFlg = false;
        var list = value.inner;
        var len = list == null || list == undefined ?  0 : list.length;
        // describe(0)
        if(value.type == 0) {
            print(_cs(space) + color + "[" + value.name + "]\r\n" + _cs(space) + " Finished in " + _sec(value.time) + " seconds" + endColor);
        // it(1).
        } else if(len > 0) {
            var successCount = 0;
            var errorCount = 0;
            for(var i = 0; i < len; i ++) {
                if(list[i].type == 2) {
                    if(list[i].error > 0) {
                        errorCount ++;
                    } else {
                        successCount ++;
                    }
                }
            }
            noLinePrint(_cs(space) + color +
                (errorCount > 0 ? ERROR_COLOR + "✖ " + ERROR_END_COLOR : SUCCESS_COLOR + "○ " + SUCCESS_END_COLOR) +
                "(" + value.name + ")" + endColor);
            if(successCount > 0) {
                noLinePrint(SUCCESS_COLOR + " ○ = " + successCount + END_COLOR);
            }
            if(errorCount > 0) {
                noLinePrint(ERROR_COLOR + " ✖ = " + errorCount + END_COLOR);
                errFlg = true;
            }
            print("");
        }
        for(var i = 0; i < len; i ++) {
            detailDetailReport(errFlg, space + 2, i + 1, list[i]);
        }
        return;
    }
    // expect(2)
    // verboseがONもしくはエラーがある場合は表示.
    if(verboseFlag) {
        print(_cs(space) + color + head + "(" + _no(no) + ") " + value.name + endColor);
    }
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
} else {
    // エラーが存在する場合は、詳細を表示する.
    out.errPrintln(all[0] + " spec, " + ERROR_COLOR + all[1] + " failures" + END_COLOR);
    out.errPrintln("Finished in " + _sec(exitTime) + " seconds");
    detailReport(result);
    out.errPrintln("");

    // エラーを通知.
    return false;
}

}