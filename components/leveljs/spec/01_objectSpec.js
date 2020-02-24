var _DB_NAME = "test_object_string_db";
var level = require("@rhigin/lib/Level");
level.startup();

describe("object db name: '" + _DB_NAME + "' i/o test.", function() {

    it("create table '" + _DB_NAME + "'", function() {
        var res = level.createObject(_DB_NAME, "type", "string");
        expect(res).toBe(true);
    });

});