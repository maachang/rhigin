# rhigin 向け jdbc接続コンポーネント

Javaアプリケーションからデータベースを操作するAPIである、JDBCこれを利用したデータベース接続・操作を行うことができる「rhiginコンポーネント」です。

jdbcドライバを使ったデータベースI/Oに対して、薄いwapper実装で、javascriptで利用することができます。

当コンポーネントは、以下の導入方法と、機能を有します。

- [１）jdbc接続コンポーネント導入方法](#１jdbc接続コンポーネント導入方法)

- [２）jdbc接続コンポーネントをjavascriptから呼び出せる機能](#２jdbc接続コンポーネントをjavascriptから呼び出せる機能)

- [３）jdbc接続定義](#３jdbc接続定義)

- [４）jdbcコネクションプーリング機能](#４jdbcコネクションプーリング機能)

- [５）jdbcコネクションオブジェクト機能](#５jdbcコネクションオブジェクト機能)

- [６）jdbc接続してSQL実行用コンソール、ファイル実行機能](#６jdbc接続してSQL実行用コンソールファイル実行機能)

- [７）csvファイルを読み込んで、その内容をテーブルにinsertする機能](#７csvファイルを読み込んでその内容をテーブルにinsertする機能)

_


以下、上記内容について説明を行います。

_

_

## １）jdbc接続コンポーネント導入方法

jdbc接続コンポーネントを、対象のrhiginプロジェクトに導入するには、以下のように行います。

```sh
$ cd {対象のrhiginプロジェクトフォルダ}

$ rlib jdbc
```
これによりjdbc接続コンポーネントが利用可能になります。

ただ、この場合は別途「対象データベース」向けのJDBCドライバを入れて、JDBCの接続定義を行う必要があります。

```
[対象のrhiginプロジェクトフォルダ]
  |
  +-- [jar]
  |     |
  |     +-- 利用したいjdbcドライバをここに格納します。
  |
  +-- [conf]
        |
        +-- jdbc.json (JDBCの接続定義サンプルが配置されます)
```

また、データベースを組み込み式で動作させたい場合は、以下のように行うことで、H2データベースと、H2データベース用のJDBC接続定義が行われたものが設定されます。

```sh
$ cd {対象のrhiginプロジェクトフォルダ}

$ rlib jdbc h2
```
データベースI/Oが必要なプロトタイプサービスを開発する場合は、上記の呼び出しで行うと、環境構築をする必要がなくお手軽です。

```
[対象のrhiginプロジェクトフォルダ]
  |
  +-- [jar]
  |     |
  |     +-- [jdbc_driver]
  |            |
  |            +-- h2-X.X.XXX.jar (H2のJDBC組み込みドライバがセットされます)
  |
  +-- [conf]
        |
        +-- jdbc.json (H2のJDBC接続定義が行われているJSONファイルが配置されます)
```

あと、jdbc接続コンポーネントをrhigin上で初期化して利用可能にするためにはもう１ステップが必要になります。

./index.js
```js
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
 *
 * <Example>
 * var jdbc = require("@rhigin/lib/JDBC");
 * jdbc.startup();
 */

// JDBC接続コンポーネントの初期化.
var jdbc = require("@rhigin/lib/JDBC");
jdbc.startup();
```
rhiginスタートアップ用javascript内に、JDBC接続コンポーネントの初期化を行います。

これで、JDBC接続コンポーネントが利用可能になります。

_

_

## ２）jdbc接続コンポーネントをjavascriptから呼び出せる機能

JDBCはjavaのAPIですが、これをrhigin上で動作するjavascriptで、利用することが出来ます。

jdbc接続コンポーネントを試すために、はじめに以下のSQLを実行して、テーブルを作成します。

```sql
CREATE TABLE name_age_list(
    id INT(11) AUTO_INCREMENT NOT NULL, 
    name VARCHAR(30) NOT NULL ,
    age INT(3) NOT NULL,
    PRIMARY KEY (id));

INSERT INTO name_age_list(NAME, AGE) VALUES ('hoge', 24);
INSERT INTO name_age_list(NAME, AGE) VALUES ('moge', 18);
INSERT INTO name_age_list(NAME, AGE) VALUES ('suzuki', 23);

COMMIT;
```
その後、以下のjavascriptを実行します.

```js
// jdbc接続コンポーネントを取得.
var jdbc = require("@rhigin/lib/JDBC");

// jdbcコネクションを取得.
var conn = jdbc.connect("h2db");

// データ取得.
var result = conn.query("select * from name_age_list;")

// 内容表示.
console.log(result);
```
処理結果.

```js
[
  {
    "ID": 1,
    "NAME": "hoge",
    "AGE": 24
  },
  {
    "ID": 2,
    "NAME": "moge",
    "AGE": 18
  },
  {
    "ID": 3,
    "NAME": "suzuki",
    "AGE": 23
  }
]
```
このような感じでjavascriptからJDBCドライバを利用することが出来ます。

_

_

## ３）jdbc接続定義

jdbcの接続定義は、接続名を定義しJDBC接続詳細設定を行い、JSON形式でconfディレクトリ以下に「jdbc.json」で保存します。

以下例として、H2データベースの組み込み接続サンプルを元に説明いたします。

conf/jdbc.json
```js
{
    // JDBC接続設定を個別に設定します.
    // ここでの名前は「ユニーク名」で、JDBC管理名を設定します.
    "h2db": {
        "driver":            "org.h2.Driver"    // JDBCドライバーパッケージ＋クラス名を設定します.
        // 接続先URL、データベース名など.
        ,"url":              "jdbc:h2:./h2db/h2db"
        ,"user":             ""                 // 接続先ユーザ名.
        ,"password":         ""                 // 接続先パスワード.
        ,"readOnly":         false              // リードオンリのみの接続ならtrue.
        ,"busyTimeout":      -1                 // 問い合わせタイムアウト(0以下で無効).
        // トランザクションレベル.
        ,"transactionLevel": "TRANSACTION_READ_UNCOMMITTED"
        ,"fetchSize":        -1                 // フェッチサイズ(0以下で無効).
        ,"poolSize":         -1                 // プーリングサイズ(0以下で最大プーリング数).
        ,"poolTimeout":      -1                 // プーリングタイムアウト.
        ,"machineId":        0                  // マシンID(0 - 511).
        ,"params":
        // JDBCパラメータ.
        {
        }
        ,"urlType":          false              // URLドライバパラメータ区切りタイプ.
                                                // [true] url + "?" + urlParams + "&" ...
                                                // [false] url + ";" + urlParams + ";" ...
        // URLの後にドライバに追加するパラメータ.
        ,"urlParams":
        {
            "MVCC": "TRUE"                      // MVCCモード(多版型同時実行制御)
            ,"LOCK_MODE": 0                     // 低レベルロック(READ_UNCOMMITTED).
            ,"LOCK_TIMEOUT": 120000             // ビジータイムアウト(120秒).
            ,"DB_CLOSE_ON_EXIT": "TRUE"         // VM終了時にDBクローズ.
            ,"CACHE_SIZE": 131072               // キャッシュは12k.
            ,"PAGE_SIZE": 32768                 // ページサイズは32768.
            ,"IFEXISTS": "FALSE"                // ファイルが存在しない場合はファイル作成.
            ,"AUTOCOMMIT": "FALSE"              // 通常コミットモード.
            ,"CACHE_TYPE": "TQ"                 // scan-resistantキャッシュタイプ"TQ"(two queue).
            ,"LOG": 0                           // ログは必要なし
            ,"UNDO_LOG": 0
            ,"AUTO_SERVER": "TRUE"
            ,"TRACE_LEVEL_FILE": 0
        }
    }
}
```
上記の接続例では、H2データベースに対して組み込み形式によるMVCC（複数I/Oが可能）でのデータベース接続が可能な定義が、定義名として「h2db」として利用できるようになります。

_

JDBC接続定義内容については、[詳細リンク](https://github.com/maachang/rhigin/blob/master/components/jdbc/docs/jdbcConfig.md)より参照できます。

_

_

## ４）jdbcコネクションプーリング機能

javaのAPIであるJDBCドライバは、基本的にプーリング（コネクションの再利用）されていないので、効率的ではありません。

JDBC接続コンポーネントでは、JDBCコネクションプーリングに対応しています。

使い方は、以下のように利用することが出来ます。

（JDBC接続名：H2データベースを組み込みで利用できるようにしていて、前項のJDBC接続定義（jdbc.json）にあるように「h2db」定義が利用可能であることが前提)

```js
var jdbc = require("@rhigin/lib/JDBC");

// jdbc接続コンポーネント初期化が行われていない場合は、以下の処理を行う必要がある。
// この手の処理は、rhiginではスタートアップスクリプト[./index.js]で行う.
// jdbc.startup();

// 「h2db」と言う接続定義のコネクションを生成・プーリングされたものを取得.
var conns = jdbc.connect("h2db");
```

このように「特別なことをしなくても」「最適なJDBCコネクション」が利用出来ます。

_

_

## ５）jdbcコネクションオブジェクト機能

JDBC接続コンポーネントでは、以下の機能が提供されます。

- [5-1）JDBCオブジェクト](https://github.com/maachang/rhigin/blob/master/components/jdbc/docs/jdbcObject.md#5-1jdbcオブジェクト)

- [5-2）JDBCConnectオブジェクト](https://github.com/maachang/rhigin/blob/master/components/jdbc/docs/jdbcObject.md#5-2jdbcconnectオブジェクト)

- [5-3）JDBCRowオブジェクト](https://github.com/maachang/rhigin/blob/master/components/jdbc/docs/jdbcObject.md#5-3jdbcrowオブジェクト)

_

詳細説明については、[詳細リンク](https://github.com/maachang/rhigin/blob/master/components/jdbc/docs/jdbcObject.md)より参照出来ます。

_

_

## ６）jdbc接続してSQL実行用コンソール、ファイル実行機能

JDBCドライバ経由でデータベースにアクセスするには、Javascriptからアクセスすることが出来ますが、単にデータベース内の内容をSQL文で確認したい場合は、効率が良いとは言えません。

JDBC接続コンポーネントでは、設定されているJDBC接続定義に対して、SQLを直接利用できる「コンソール機能」が提供されます。

使い方は以下の通りです。

```sh
$ ./jdbc
JDBC console version (0.0.1)
 Set ";" at the end of the command.

JDBC> 
```
これでコンソールモードが起動します。

1つ注意なのですが、SQL文やコマンドを確定させるためには `;` を入力する必要があります。

```sql
JDBC> help
JDBC>
JDBC>
```
上記では、処理が確定されない。

```sql
JDBC> help;
exit [quit]     Exit the console.
close           Destroys all current connections.
commit          Commit for the current connection.
rollback        Rollback for the current connection.
list            Get a list of connection definition names.
kind {name}     Displays the specified connection definition details.
                {name} Set the connection name.
connect {name}  Set and display current connection name.
                {name} Set the connection name.
```

逆に言えば、複数行に渡って入力ができるようになっています。

ファイルを指定することで、コンソールで入力する代わりにファイルのSQL文を実行出来ます。

```sh
$ ./jdbc -f {実行対象のSQLファイル名}
```

詳しくは、コマンドの使い方はヘルプを指定すると閲覧出来ます。

```sh
$ ./jdbc -h
jdbc [-c --conf --config] [-f --file]
 Executes SQL statement console and file execution.
  [-c] [--conf] [--config] {args}
    Set the configuration definition file name.
  [-f] [--file] {args}
    Set the SQL execution file name.
```

_

コンソールで利用可能なJDBC接続コンポーネントの機能については、[詳細リンク](https://github.com/maachang/rhigin/blob/master/components/jdbc/docs/jdbcConsole.md)より参照できます。

_

_

## ７）csvファイルを読み込んで、その内容をテーブルにinsertする機能

JDBC接続コンポーネントでは、CSVファイルをテーブルにインポートする機能を提供しています。

使い方は以下の通りです。

```sh
$ ./jcsv {CSVファイル名}
```

テーブルにインポートされるCSVファイルには、いくつかルールがあります。

_

jcsvコマンドの詳しいルールは、[詳細リンク](https://github.com/maachang/rhigin/blob/master/components/jdbc/docs/jdbcCsv.md)より参照できます。

_

_

## 最後に

皆様のお役になれば幸いです。

_

_

