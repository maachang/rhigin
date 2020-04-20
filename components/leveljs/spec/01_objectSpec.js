var _DB_NAME = "test_object_string_db";
var _DB_KEY = "string";
var level = require("@rhigin/lib/Level");

var TEST_INFO = "Key: '" + _DB_KEY + "' name: '" + _DB_NAME + "'";

// オブジェクトテスト.
// key: string.
// DB名: test_object_string_db.
describe("LevelJs オブジェクト テスト " + TEST_INFO + " の I/Oテスト.", function() {

    // テストリスト.
    var TEST_LIST = [
        "鈴木", {name: "鈴木", kana: "スズキ", age: 22, sex: "男", details: {height: -168.5, weight: -61.5}}
        , "田中", {name: "田中", kana: "タナカ", age: 21, sex: "女", details: {height: 182.3, weight: 64.8}}
        , "佐藤", {name: "佐藤", kana: "サトウ", age: 43, sex: "他", details: {height: 156.2, weight: 83.2}}
        , "守", {name: "守", kana: "モリ", age: 55, sex: "女", details: {height: 160.7, weight: 60.3}}
        , "朝生", {name: "朝生", kana: "アソウ", age: 73, sex: "男", details: {height: 158.3, weight: 49.9}}
        ,"阿部", {name: "阿部", kana: "アベ", age: 65, sex: "他", details: {height: 172.5, weight: 74.3}}
    ];

    // テスト用アップデートデータ.
    var TEST_UPDATE_LIST = [
        "鈴木", {name: "鈴木", kana: "スズキ", age: 25, sex: "女", details: {height: 148.1, weight: 38.1}}
        , "田中", {name: "田中", kana: "タナカ", age: 8, sex: "他", details: {height: 123.6, weight: 21}}
        , "佐藤", {name: "佐藤", kana: "サトウ", age: 103, sex: "男", details: {height: 147.5, weight: 46.9}}
    ];

    // リスト情報のディープコピー.
    var _deepCopy = function(list) {
        return JSON.parse(JSON.stringify(list));
    }

    // 指定キーの要素を取得.
    var _getValue = function(list, name) {
        var len = list.length;
        for(var i = 0; i < len; i += 2) {
            if(name == list[i]) {
                return list[i+1];
            }
        }
        return null;
    }

    // キーリストを取得.
    var _getKeyList = function(list) {
        // テスト用のキー名を生成.
        var keyList = [];
        var len = list.length;
        for(var i = 0; i < len; i += 2) {
            keyList.push(list[i]);
        }
        // leveldbでは、キーがソートされて取得されるので、昇順ソートする.
        keyList.sort();
        return keyList;
    }

    // キー項番を取得.
    var _keyNo = function(list, addNo, name) {
        var len = list.length;
        for(var i = 0; i < len; i += addNo) {
            if(list[i] == name) {
                return i;
            }
        }
        return -1;
    }

    // データの削除.
    var _removeListByKey = function(list, key) {
        var len = list.length;
        for(var i = 0; i < len; i += 2) {
            if(list[i] == key) {
                list.splice(i, 2);
                return;
            }
        }
    }

    // データの削除.
    var _removeListByNo = function(list, no) {
        var len = list.length;
        for(var i = 0; i < len; i += 2) {
            if(i == no << 1) {
                list.splice(i, 2);
                return;
            }
        }
    }

    // 指定されたカラム名の要素を取得.
    var _getValueByColumns = function(res, value, columns) {
        res[0] = true;
        var len = columns.length;
        for(var i = 0; i < len; i ++) {
            value = value[columns[i]];
            if(typeof(value) != "object") {
                if(i + 1 != len) {
                    res[0] = false;
                    return value;
                }
            }
        }
        return value;
    }

    // インデックスキーカラム名を取得.
    var _indexKeyColumns = function(columns) {
        if(!Array.isArray(columns)) {
            if(typeof(columns) == "string") {
                if(columns.indexOf(".") != -1) {
                    columns = columns.split(".");
                } else if(columns.indexOf("/") != -1) {
                    columns = columns.split("/");
                } else {
                    columns = [columns];
                }
            } else {
                columns = ["" + columns];
            }
        }
        return columns; 
    }

    // インデックスのキーリストを取得.
    // indexKeyList = {key: [ListNo....]}
    var _getIndexKeyList = function(list, columns) {
        columns = _indexKeyColumns(columns);
        var n;
        var val;
        var res = [true];
        var keyList = {};
        var len = list.length;
        for(var i = 1; i < len; i += 2) {
            val = _getValueByColumns(res, list[i], columns);
            if(res[0]) {
                n = keyList[val];
                if(n == undefined) {
                    keyList[val] = [i];
                } else {
                    n.push(i);
                }
            }
        }
        return keyList;
    }

    // インデックスキー項番を取得.
    var _indexKeyNo = function(indexKeyList, value) {
        return indexKeyList[value];
    }

    // インデックスの昇順、降順の件数を取得.
    var _indexCount = function(reverse, indexKeyList, value) {
        var list = [];
        for(var k in indexKeyList) {
            list.push(k);
        }
        list.sort();
        var ret = 0;
        var len = list.length;
        if(reverse) {
            // 降順.
            for(var i = len - 1; i >= 0; i --) {
                if(list[i] <= value) {
                    ret += indexKeyList[list[i]].length;
                }
            }
        } else {
            // 昇順.
            for(var i = 0; i < len; i ++) {
                if(list[i] >= value) {
                    ret += indexKeyList[list[i]].length;
                }
            }
        }
        return ret;
    }

    // オブジェクト作成.
    it("オブジェクト作成 " + TEST_INFO , function() {
        var res = level.contains(_DB_NAME);
        expect(res).comment("非存在チェック").not().toBe(true);

        var res = level.createObject(_DB_NAME, "type", _DB_KEY);
        expect(res).comment("新規作成が行われた").toBe(true);
    });

    // オブジェクト存在チェック.
    it("作成オブジェクト存在チェック " + TEST_INFO, function() {
        var res = level.contains(_DB_NAME);
        expect(res).comment("存在チェック").toBe(true);
    });

    // データ追加.
    it("テスト用のデータを新規追加(" + (TEST_LIST.length / 2)+ "件) " + TEST_INFO, function() {
        var obj = level.get(_DB_NAME);
        var list = TEST_LIST;
        var len = list.length;

        // 追加前に存在しないことをチェック.
        for(var i = 0; i < len; i += 2) {
            expect(obj.contains(list[i])).comment("情報なし確認: " + list[i]).not().toBe(true);
        }

        // 追加処理.
        for(var i = 0;  i < len; i += 2) {
            obj.put(list[i], list[i+1]);
        }
        // 追加結果のチェック.
        for(var i = 0; i < len; i += 2) {
            expect(obj.contains(list[i])).comment("追加存在確認: " + list[i]).toBe(true);
        }
        // データの中身が一致するかチェック.
        for(var i = 0; i < len; i += 2) {
            expect(obj.get(list[i])).comment("追加Valueチェック: " + list[i]).toEqual(list[i+1]);
        }
    });

    // データの上書き.
    it("テスト用のデータを上書き " + TEST_INFO, function() {
        var obj = level.get(_DB_NAME);
        var ulist = TEST_UPDATE_LIST;
        var ulen = ulist.length;

        // アップデート.
        for(var i = 0; i < ulen; i += 2) {
            obj.put(ulist[i], ulist[i+1]);
        }

        // アップデート確認.
        for(var i = 0; i < ulen; i += 2) {
            expect(obj.get(ulist[i])).comment("更新Valueチェック: " + ulist[i]).toEqual(ulist[i+1]);
        }

        // 更新前データとの比較.
        var list = TEST_LIST;
        var len = list.length;

        // 更新していない情報のチェック.
        for(var i = 0; i < len; i += 2) {
            var flg = false;
            for(var j = 0; j < ulen; j ++) {
                if(list[i] == ulist[i]) {
                    flg = true;
                    break;
                }
            }
            if(!flg) {
                // 更新してないデータ.
                expect(obj.get(list[i])).comment("更新してないValueチェック: " + list[i]).toEqual(list[i+1]);
            } else {
                // 更新しているデータ.
                expect(obj.get(list[i])).comment("更新してるValueの元不一致チェック: " + list[i]).not().toEqual(list[i+1]);
            }
        }

        // 元に戻す.
        for(var i = 0; i < ulen; i += 2) {
            obj.put(list[i], list[i+1]);
        }
    });

    // key指定なし - Cursorで情報取得.
    it("key指定なし - Cursor で情報取得 " + TEST_INFO, function() {
        var obj = level.get(_DB_NAME);

        // テスト用のキー名を生成.
        var list = TEST_LIST;
        var keyList = _getKeyList(list);

        // データ件数
        var cursorLength = keyList.length;

        // 昇順のcursor.
        var k, n;
        var nextCount = 0;
        var cnt = 0;
        var cursor = obj.cursor();
        while(cursor.hasNext()) {
            n = cursor.next();
            k = cursor.key();
            expect(n).comment("[昇順]cursor(" + (cnt+1)  + "): " + k[0] + " " + keyList[cnt]).toEqual(_getValue(list, keyList[cnt++]));
            nextCount ++;
        }
        expect(cursorLength).comment("[昇順]処理件数(" + cursorLength + ", " + nextCount + ")").toBe(nextCount);

        // 降順のCursor.
        nextCount = 0;
        cnt = keyList.length;
        cursor = obj.cursor(true);
        while(cursor.hasNext()) {
            n = cursor.next();
            k = cursor.key();
            expect(n).comment("[降順]cursor(" + (cnt-1)  + "): " + k[0] + " " + keyList[cnt-1]).toEqual(_getValue(list, keyList[--cnt]));
            nextCount ++;
        }
        expect(cursorLength).comment("[降順]処理件数(" + cursorLength + ", " + nextCount + ")").toBe(nextCount);
    });

    // key指定 - Cursorで情報取得.
    (function() {
        var cursorKey = "守";
        it("key: " + cursorKey + " - Cursor で情報取得 " + TEST_INFO, function() {
            var obj = level.get(_DB_NAME);
    
            // テスト用のキー名を生成.
            var list = TEST_LIST;
            var keyList = _getKeyList(list);

            // 昇順のcursor.
            var k, n;
            var nextCount = 0;
            var cnt = _keyNo(keyList, 1, cursorKey);
            var cursorLength = keyList.length - _keyNo(keyList, 1, cursorKey);
            var cursor = obj.cursor(cursorKey);
            while(cursor.hasNext()) {
                n = cursor.next();
                k = cursor.key();
                expect(n).comment("[昇順]cursor(" + cnt + "): " + k[0] + " " + keyList[cnt]).toEqual(_getValue(list, keyList[cnt++]));
                nextCount ++
            }
            expect(cursorLength).comment("[昇順]処理件数(" + cursorLength + ", " + nextCount + ")").toBe(nextCount);
    
            // 降順のCursor.
            nextCount = 0;
            cnt = _keyNo(keyList, 1, cursorKey) + 1;
            cursorLength = _keyNo(keyList, 1, cursorKey) + 1;
            cursor = obj.cursor(true, cursorKey);
            while(cursor.hasNext()) {
                n = cursor.next();
                k = cursor.key();
                expect(n).comment("[降順]cursor(" + (cnt-1)  + "): " + k[0] + " " + keyList[cnt-1]).toEqual(_getValue(list, keyList[--cnt]));
                nextCount ++
            }
            expect(cursorLength).comment("[降順]処理件数(" + cursorLength + ", " + nextCount + ")").toBe(nextCount);
        });
    })();

    // Range Cursorで情報取得.
    (function() {
        var cursorStartKey = "守";
        var cursorEndKey = "鈴木";
        it("Range startKey: " + cursorStartKey + " endKey: " + cursorEndKey + " - Cursor で情報取得 " + TEST_INFO, function() {
            var obj = level.get(_DB_NAME);
    
            // テスト用のキー名を生成.
            var list = TEST_LIST;
            var keyList = _getKeyList(list);

            // 範囲検索の件数.
            var checkLen = _keyNo(keyList, 1, cursorEndKey) - _keyNo(keyList, 1, cursorStartKey) + 1;
    
            // 昇順のrange.
            var k, n;
            var nextCount = 0;
            var cnt = _keyNo(keyList, 1, cursorStartKey);
            var cursor = obj.range(cursorStartKey, cursorEndKey);
            while(cursor.hasNext()) {
                n = cursor.next();
                k = cursor.key();
                expect(n).comment("[昇順]range(" + cnt + "): " + k[0] + " " + keyList[cnt]).toEqual(_getValue(list, keyList[cnt++]));
                nextCount ++;
            }
            expect(checkLen).comment("[昇順]range処理カウント(" + checkLen + ", " + nextCount + ")").toBe(nextCount);

            // 降順のrange.
            nextCount = 0;
            cnt = _keyNo(keyList, 1, cursorEndKey) + 1;
            cursor = obj.range(true, cursorStartKey, cursorEndKey);
            while(cursor.hasNext()) {
                n = cursor.next();
                k = cursor.key();
                expect(n).comment("[降順]range(" + (cnt-1)  + "): " + k[0] + " " + keyList[cnt-1]).toEqual(_getValue(list, keyList[--cnt]));
                nextCount ++;
            }
            expect(checkLen).comment("[降順]range処理カウント(" + checkLen + ", " + nextCount + ")").toBe(nextCount);
        });
    })();

    // インデックスの作成.
    (function() {
        var TEST_PATTERNS = [
            {columns: "kana", type: "string", key: "タナカ"}
            ,{columns: "sex", type: "string", key: "女"}
            ,{columns: "age", type: "int", key: 21}
            ,{columns: "details.height", type: "double", key: 157}
        ];

        // インデックス作成.
        for(var testPattern = 0; testPattern < TEST_PATTERNS.length; testPattern ++) {
            var pattern = TEST_PATTERNS[testPattern];
            // TEST_PATTERN のリスト順でテスト.
            it("データ存在する内容にインデックス作成して、検索: " + pattern.columns + " key: " + pattern.key, function() {
                var obj = level.get(_DB_NAME);

                // インデックス作成.
                obj.createIndex(pattern.type, pattern.columns);
                expect(true).comment("インデックス作成 columns: " + pattern.columns + " type:" + pattern.type).toBe(obj.isIndex(pattern.columns));
            });
        }

        // インデックス検索.
        for(var testPattern = 0; testPattern < TEST_PATTERNS.length; testPattern ++) {
            var pattern = TEST_PATTERNS[testPattern];
            // testPatterns のリスト順でテスト.
            it("インデックスの検索: " + pattern.columns + " key: " + pattern.key, function() {
                obj = level.get(_DB_NAME);
                // インデックスキーリストを生成.
                var list = TEST_LIST;
                var indexKeyList = _getIndexKeyList(list, pattern.columns);

                // 昇順で取得.
                var k, n, v;
                var r = [false];
                var cs = _indexKeyColumns(pattern.columns);
                var cursorLength = _indexCount(false, indexKeyList, pattern.key);
                var nextCount = 0;
                var cursor = obj.index(false, pattern.key, pattern.columns);
                while(cursor.hasNext()) {
                    n = cursor.next();
                    k = cursor.key();
                    v = _getValueByColumns(r, n, cs);
                    expect(n).comment("[昇順]index(" + (nextCount + 1) + "): " + k[0] + " " + ((r[0]) ? v : "")).toEqual(_getValue(list, k[0]));
                    nextCount ++;
                }
                expect(cursorLength).comment("[昇順]index取得件数(" + cursorLength + ", " + nextCount + ")").toBe(nextCount);

                // 降順で取得.
                cursorLength = _indexCount(true, indexKeyList, pattern.key);
                nextCount = 0;
                cursor = obj.index(true, pattern.key, pattern.columns);
                while(cursor.hasNext()) {
                    n = cursor.next();
                    k = cursor.key();
                    v = _getValueByColumns(r, n, cs);
                    expect(n).comment("[降順]index(" + (nextCount + 1) + "): " + k[0] + " " + ((r[0]) ? v : "")).toEqual(_getValue(list, k[0]));
                    nextCount ++;
                }
                expect(cursorLength).comment("[降順]index取得件数(" + cursorLength + ", " + nextCount + ")").toBe(nextCount);

            });
        }

        // データ削除.
        var DELETE_KEY = "鈴木";
        (function() {
            var obj = level.get(_DB_NAME);
            // データの削除.
            if(obj.get(DELETE_KEY) != null) {
                obj.remove(DELETE_KEY);
            }
        })();

        // インデックス削除.
        for(var testPattern = 0; testPattern < TEST_PATTERNS.length; testPattern ++) {
            var pattern = TEST_PATTERNS[testPattern];
            // TEST_PATTERN のリスト順でテスト.
            it("作成したインデックスからデータを１件削除(key: " + DELETE_KEY + ")して、処理: " + pattern.columns + " key: " + pattern.key, function() {
                var obj = level.get(_DB_NAME);

                // インデックスキーリストを生成.
                var list = _deepCopy(TEST_LIST);
                _removeListByKey(list, DELETE_KEY);
                var indexKeyList = _getIndexKeyList(list, pattern.columns);

                // 昇順で取得.
                var k, n, v;
                var r = [false];
                var cs = _indexKeyColumns(pattern.columns);
                var cursorLength = _indexCount(false, indexKeyList, pattern.key);
                var nextCount = 0;
                var cursor = obj.index(false, pattern.key, pattern.columns);
                while(cursor.hasNext()) {
                    n = cursor.next();
                    k = cursor.key();
                    v = _getValueByColumns(r, n, cs);
                    expect(n).comment("[昇順]index(" + (nextCount + 1) + "): " + k[0] + " " + ((r[0]) ? v : "")).toEqual(_getValue(list, k[0]));
                    nextCount ++;
                }
                expect(cursorLength).comment("[昇順]index取得件数(" + cursorLength + ", " + nextCount + ")").toBe(nextCount);

                // 降順で取得.
                cursorLength = _indexCount(true, indexKeyList, pattern.key);
                nextCount = 0;
                cursor = obj.index(true, pattern.key, pattern.columns);
                while(cursor.hasNext()) {
                    n = cursor.next();
                    k = cursor.key();
                    v = _getValueByColumns(r, n, cs);
                    expect(n).comment("[降順]index(" + (nextCount + 1) + "): " + k[0] + " " + ((r[0]) ? v : "")).toEqual(_getValue(list, k[0]));
                    nextCount ++;
                }
                expect(cursorLength).comment("[降順]index取得件数(" + cursorLength + ", " + nextCount + ")").toBe(nextCount);
            });
        }

        // trancateで削除.
        (function() {
            var obj = level.get(_DB_NAME);
            obj.trancate();
        })();

        // インデックス削除.
        for(var testPattern = 0; testPattern < TEST_PATTERNS.length; testPattern ++) {
            var pattern = TEST_PATTERNS[testPattern];
            it("作成したインデックスを trancate、処理: " + pattern.columns + " key: " + pattern.key, function() {
                var obj = level.get(_DB_NAME);

                // 昇順.
                var nextCount = 0;
                var cursor = obj.index(false, pattern.key, pattern.columns);
                while(cursor.hasNext()) {
                    cursor.next();
                    nextCount ++;
                }
                expect(nextCount).comment("[昇順]indexなしを確認").toBe(0);
                
                // 降順.
                nextCount = 0;
                cursor = obj.index(true, pattern.key, pattern.columns);
                while(cursor.hasNext()) {
                    cursor.next();
                    nextCount ++;
                }
                expect(nextCount).comment("[降順]indexなしを確認").toBe(0);
            });
        }

        // インデックスをデータ追加で再作成.
        (function() {
            it("インデックスを再作成", function() {
                var obj = level.get(_DB_NAME);
                var list = TEST_LIST;
                var len = list.length;
        
                // 追加処理.
                for(var i = 0;  i < len; i += 2) {
                    obj.put(list[i], list[i+1]);
                }

                // 追加結果のチェック.
                for(var i = 0; i < len; i += 2) {
                    expect(obj.contains(list[i])).comment("追加存在確認: " + list[i]).toBe(true);
                }
            });
        })();

        // インデックス検索.
        for(var testPattern = 0; testPattern < TEST_PATTERNS.length; testPattern ++) {
            var pattern = TEST_PATTERNS[testPattern];
            // testPatterns のリスト順でテスト.
            it("インデックスを再作成したもので検索: " + pattern.columns + " key: " + pattern.key, function() {
                obj = level.get(_DB_NAME);
                // インデックスキーリストを生成.
                var list = TEST_LIST;
                var indexKeyList = _getIndexKeyList(list, pattern.columns);

                // インデックスが存在するかチェック.
                expect(true).comment("インデックス確認 columns: " + pattern.columns + " type:" + pattern.type).toBe(obj.isIndex(pattern.columns));

                // 昇順で取得.
                var k, n, v;
                var r = [false];
                var cs = _indexKeyColumns(pattern.columns);
                var cursorLength = _indexCount(false, indexKeyList, pattern.key);
                var nextCount = 0;
                var cursor = obj.index(false, pattern.key, pattern.columns);
                while(cursor.hasNext()) {
                    n = cursor.next();
                    k = cursor.key();
                    v = _getValueByColumns(r, n, cs);
                    expect(n).comment("[昇順]index(" + (nextCount + 1) + "): " + k[0] + " " + ((r[0]) ? v : "")).toEqual(_getValue(list, k[0]));
                    nextCount ++;
                }
                expect(cursorLength).comment("[昇順]index取得件数(" + cursorLength + ", " + nextCount + ")").toBe(nextCount);

                // 降順で取得.
                cursorLength = _indexCount(true, indexKeyList, pattern.key);
                nextCount = 0;
                cursor = obj.index(true, pattern.key, pattern.columns);
                while(cursor.hasNext()) {
                    n = cursor.next();
                    k = cursor.key();
                    v = _getValueByColumns(r, n, cs);
                    expect(n).comment("[降順]index(" + (nextCount + 1) + "): " + k[0] + " " + ((r[0]) ? v : "")).toEqual(_getValue(list, k[0]));
                    nextCount ++;
                }
                expect(cursorLength).comment("[降順]index取得件数(" + cursorLength + ", " + nextCount + ")").toBe(nextCount);
            });
        }

    })();

    // データの削除.
    it("テスト用のデータを全削除(" + (TEST_LIST.length / 2) + "件) " + TEST_INFO, function() {
        var obj = level.get(_DB_NAME);
        var list = TEST_LIST;
        var len = list.length;

        // 削除処理.
        for(var i = 0;  i < len; i += 2) {
            obj.remove(list[i]);
        }

        // 追加前に存在しないことをチェック.
        for(var i = 0; i < len; i += 2) {
            expect(obj.contains(list[i])).comment("情報なし確認: " + list[i]).not().toBe(true);
        }
    });
});