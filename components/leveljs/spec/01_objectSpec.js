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
        , "森", {name: "森", kana: "モリ", age: 55, sex: "女"}
        , "麻生", {name: "麻生", kana: "アソウ", age: 73, sex: "男"}
        ,"阿部", {name: "阿部", kana: "アベ", age: 65, sex: "他"}
    ];

    // テスト用アップデートデータ.
    var TEST_UPDATE_LIST = [
        "鈴木", {name: "鈴木", kana: "スズキ", age: 25, sex: "女"}
        , "田中", {name: "田中", kana: "タナカ", age: 8, sex: "他"}
        , "佐藤", {name: "佐藤", kana: "サトウ", age: 103, sex: "男"}
    ];

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
    });

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