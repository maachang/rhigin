/**
 * このjsは、rhiginが起動したときに最初に読み込まれて実行されます.
 * このjsで `addOriginals` ファンクションを利用して、js実行時に利用できる処理を登録したり `removeOriginals` で削除することができます.
 * 
 * ＜例＞
 * var a = "hoge";
 * addOriginals("test", a);
 * 
 * ......
 * console.log(test);
 * > "hoge"
 * 
 * このjsは起動時に１度だけ実行されるので、初期化処理を行う場合にも利用されます.
 */