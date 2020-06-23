var _DB_NAME = "test_latlon_none_db";
var _DB_KEY = "none";
var level = require("@rhigin/lib/Level");

var TEST_INFO = "Key: '" + _DB_KEY + "' name: '" + _DB_NAME + "'";

var csvFileName = config.level.csvPath + "/test_latlon_none_db.all_stations.csv";

// オブジェクトテスト.
// key: none.
// DB名: test_latlon_none_db.
describe("LevelJs 緯度経度 テスト " + TEST_INFO + " の I/Oテスト.", function() {

    // オブジェクト作成.
    it("オブジェクト作成 " + TEST_INFO , function() {
        var res = level.contains(_DB_NAME);
        expect(res).comment("非存在チェック").not().toBe(true);

        var res = level.createLatLon(_DB_NAME, "type", _DB_KEY);
        expect(res).comment("新規作成が行われた").toBe(true);
    });

    // オブジェクト存在チェック.
    it("作成オブジェクト存在チェック " + TEST_INFO, function() {
        var res = level.contains(_DB_NAME);
        expect(res).comment("存在チェック").toBe(true);
    });

    // データ追加.
    it("テスト用のデータを新規追加(" + csvFileName + ") " + TEST_INFO, function() {
        // CSVインポート.
        var res = level.csvImport(csvFileName);
        // 現在のデータ数を取得する.
        var cnt = 0;
        var op = level.get(_DB_NAME);
        var itr = op.cursor();
        try {
            while(itr.hasNext()) {
                itr.next();
                cnt ++;
            }
        } finally {
            itr.close();
        }
        // 比較.
        expect(res).comment("処理結果").toBe(cnt);
    // オブジェクトオペレータの削除.
    it("オペレータの削除 " + TEST_INFO, function() {
        level.delete(_DB_NAME);
        expect(level.contains(_DB_NAME)).comment("オペレータ削除の確認: " + _DB_NAME).toBe(false);
    });
});
