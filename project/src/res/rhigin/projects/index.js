/**
 * This js is first loaded and executed when rhigin starts.
 * In this js, you can use the `originals` function to register the
 *
 * ＜Example＞
 * var a = "hoge";
 * originals("test", a);
 *
 * ......
 * console.log(test);
 * > "hoge"
 *
 * You can also register end processing after script execution.
 * This js can be registered using the `endCall` function.
 *
 * <Example>
 * endCall("endScript/endCall.js");
 *
 * Since this js is executed only once at startup, it is also used for 
 * initialization processing.
 */
