// Leveldbの内容を削除.
//

// 初期処理.
if(OS_NAME == "Linux" || OS_NAME == "Mac") {
    // linux 系の場合のみ、削除.
    if(File.isDir(config.level.path)) {
        new ExecCmd().exec("rm", "-rf", config.level.path);
    }
} else {
    // windows 系の場合は、処理しない.
    throw new Error("windows is not supported.");
}

// スタートアップ処理.
var level = require("@rhigin/lib/Level");
level.startup();
