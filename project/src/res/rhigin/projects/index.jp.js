/**
 * このjsは、rhiginが起動したときに最初に読み込まれて実行されます.
 * このjsで `originals` ファンクションを利用して、js実行時に利用できます.
 * 
 * ＜例＞
 * var a = "hoge";
 * originals("test", a);
 * 
 * ......
 * console.log(test);
 * > "hoge"
 *
 * 他にスクリプト実行後に終了処理を登録することが出来ます.
 * このjsで `endCall` ファンクションを利用して登録することができます.
 *
 * ＜例＞
 * endCall("endScript/endCall.js");
 * 
 * このjsは起動時に１度だけ実行されるので、初期化処理を行う場合にも利用されます.
 */
