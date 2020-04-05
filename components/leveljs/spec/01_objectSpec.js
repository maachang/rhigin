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
        "鈴木", {name: "鈴木", kana: "スズキ", age: 18, sex: "男"}
        , "田中", {name: "田中", kana: "タナカ", age: 21, sex: "女"}
        , "佐藤", {name: "佐藤", kana: "サトウ", age: 43, sex: "他"}
        , "守", {name: "守", kana: "モリ", age: 55, sex: "女"}
        , "朝生", {name: "朝生", kana: "アソウ", age: 73, sex: "男"}
        ,"阿部", {name: "阿部", kana: "アベ", age: 65, sex: "他"}
    ];

    // テスト用アップデートデータ.
    var TEST_UPDATE_LIST = [
        "鈴木", {name: "鈴木", kana: "スズキ", age: 25, sex: "女"}
        , "田中", {name: "田中", kana: "タナカ", age: 8, sex: "他"}
        , "佐藤", {name: "佐藤", kana: "サトウ", age: 103, sex: "男"}
    ];

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

        // 昇順のcursor.
        var cnt = 0;
        var cursor = obj.cursor();
        var k = null;
        var n = null;
        while(cursor.hasNext()) {
            n = cursor.next();
            k = cursor.key();
            expect(n).comment("[昇順]cursor(" + (cnt+1)  + "): " + k[0] + " " + keyList[cnt]).toEqual(_getValue(list, keyList[cnt++]));
        }

        // 降順のCursor.
        cnt = keyList.length;
        cursor = obj.cursor(true);
        while(cursor.hasNext()) {
            n = cursor.next();
            k = cursor.key();
            expect(n).comment("[降順]cursor(" + (cnt-1)  + "): " + k[0] + " " + keyList[cnt-1]).toEqual(_getValue(list, keyList[--cnt]));
        }
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
            var cnt = _keyNo(keyList, 1, cursorKey);
            var cursor = obj.cursor(cursorKey);
            var k = null;
            var n = null;
            while(cursor.hasNext()) {
                n = cursor.next();
                k = cursor.key();
                expect(n).comment("[昇順]cursor(" + cnt + "): " + k[0] + " " + keyList[cnt]).toEqual(_getValue(list, keyList[cnt++]));
            }
    
            // 降順のCursor.
            cnt = _keyNo(keyList, 1, cursorKey) + 1;
            cursor = obj.cursor(true, cursorKey);
            while(cursor.hasNext()) {
                n = cursor.next();
                k = cursor.key();
                expect(n).comment("[降順]cursor(" + (cnt-1)  + "): " + k[0] + " " + keyList[cnt-1]).toEqual(_getValue(list, keyList[--cnt]));
            }
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
    
            // 昇順のrange.
            var checkLen = _keyNo(keyList, 1, cursorEndKey) - _keyNo(keyList, 1, cursorStartKey) + 1;
            var nextCount = 0;
            var cnt = _keyNo(keyList, 1, cursorStartKey);
            var cursor = obj.range(cursorStartKey, cursorEndKey);
            var k = null;
            var n = null;
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