/**
 * This js is first loaded and executed when rhigin starts.
 * In this js, you can use the `addOriginals` function to register the processing that can be used at the time of js execution and remove it using `removeOriginals`.
 *
 * ＜Example＞
 * var a = "hoge";
 * addOriginals("test", a);
 *
 * ......
 * console.log(test);
 * > "hoge"
 *
 * Since this js is executed only once at startup, it is also used for initialization processing.
 */