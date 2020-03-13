var _DB_NAME = "test_object_string_db";
var _DB_KEY = "string";
var level = require("@rhigin/lib/Level");

describe("LevelJs オブジェクト テスト Key: '" + _DB_KEY + "' name: '" + _DB_NAME + "' の I/Oテスト.", function() {

    it("オブジェクト作成 Key: '" + _DB_KEY + "' name: '" + _DB_NAME + "'", function() {
        var res = level.createObject(_DB_NAME, "type", _DB_KEY);
        expect(res).toBe(true);
    });

    it("作成オブジェクト存在チェック '" + _DB_NAME + "'", function() {
        var res = level.contains(_DB_NAME);
        expect(res).toBe(true);
    });

    
});