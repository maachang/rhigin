<span style="font-size: 25px;"> [前の頁に戻る](https://github.com/maachang/rhigin/blob/master/components/jdbc/README.md) </span>

# ５）jdbcコネクションオブジェクト機能

JDBC接続コンポーネントでは、以下の機能が提供されます。

- [5-1）JDBCオブジェクト](#5-1jdbcオブジェクト)

- [5-2）JDBCConnectオブジェクト](#5-2jdbcconnectオブジェクト)

- [5-3）JDBCRowオブジェクト](#5-3jdbcrowオブジェクト)

- [5-4）Selectオブジェクト](#5-4Selectオブジェクト)

- [5-5）Deleteオブジェクト](#5-5Deleteオブジェクト)

- [5-6）Insertオブジェクト](#5-6Insertオブジェクト)

- [5-7）Updateオブジェクト](#5-7Updateオブジェクト)

_

以下より、これらの利用方法について説明します。

_

_

## 5-1）JDBCオブジェクト

JDBCオブジェクトの使い方について説明します。

```js
var jdbc = require("@rhigin/lib/JDBC");
```

jdbc接続コンポーネントを利用する場合は、上記のようにjavascript上から呼び出します。

_

### 5-1-1）connect

jdbc接続定義に対するJDBCコネクションを取得します。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");
```

または、jdbc接続定義にないJDBCコネクションを取得したい場合は、以下のように行います。

```js
var jdbc = require("@rhigin/lib/JDBC");

var driver = "org.h2.Driver";       // jdbcドライバ名.
var url = "jdbc:h2:./h2db/h2db";    // jdbc接続先URL.
var user = "";                      // jdbc接続ユーザ名.
var password = "";                  // jdbc接続パスワード.
var conn = jdbc.connect(driver, url, user, password);
```
ただし、このコネクションはプーリングされません。

_

### 5-1-2）abort

現在接続中の全JDBCコネクションをすべてクローズします。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");
console.log("isClose: " + conn.isClose());
jdbc.abort();
console.log("isClose: " + conn.isClose());
```

処理結果:
```js
isClose: false
isClose: true
```

_

### 5-1-3）length,names,isRegister,kind

jdbc接続定義の情報を参照出来ます。

```js
var jdbc = require("@rhigin/lib/JDBC");

console.log("jdbc.length(): " + jdbc.length());
console.log("jdbc.names(): " + jdbc.names());
console.log("jdbc.isRegister(\"h2db\"): " + jdbc.isRegister("h2db"));
console.log("jdbc.kind(\"h2db\"): " + jdbc.kind("h2db"));
```

処理結果:
```js
jdbc.length(): 1
jdbc.names(): [h2db]
jdbc.isRegister("h2db"): true
jdbc.kind("h2db"): {"name": "h2db","driver": "org.h2.Driver","url": "jdbc:h2:./h2db/h2db",
"user": "","password": "null","readOnly": "false",
"urlParams": ";MVCC=TRUE;LOCK_MODE=0;LOCK_TIMEOUT=120000;DB_CLOSE_ON_EXIT=TRUE;CACHE_SIZE=131072;" +
"PAGE_SIZE=32768;IFEXISTS=FALSE;AUTOCOMMIT=FALSE;CACHE_TYPE=TQ;LOG=0;UNDO_LOG=0;AUTO_SERVER=TRUE;" +
"TRACE_LEVEL_FILE=0;",
"busyTimeout": "-1","transactionLevel": "1","fetchSize": "-1","params": "{}","poolingSize": "-1",
"poolingTimeout": "-1","notSemicolon": "false", "machineId": "0"}
```

_

### 5-1-4）startup

jdbcの初期化処理関連を行います。

```js
var jdbc = require("@rhigin/lib/JDBC");

console.log("isStartup: " + jdbc.isStartup());

// スタートアップ処理.
var result = jdbc.startup();
console.log("startup result: " + result);

console.log("isStartup: " + jdbc.isStartup());
```

処理結果：
```js
isStartup: false
startup result: true
isStartup: true
```
jdbcの初期化に対して、第一引数で読み込み先のjdbc接続定義を変更出来ます。

./conf/jdbc2.json を読み込む場合.
```js
var jdbc = require("@rhigin/lib/JDBC");

var result = jdbc.startup("jdbc2");
```

_

_

## 5-2）JDBCConnectオブジェクト

JDBCコネクションオブジェクトの使い方について、説明します。

JDBCコネクションオブジェクトは、javaのJDBCで言う所のjava.sql.Connectionに相当します.

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");
```
JDBCコネクションを利用する場合は、上記のようにjavascript上から呼び出します。

また、特殊ワード ``TIME12` を使って、PreparedStatementのパラメータに設定することで、１２バイトのシーケンスIDがセットされます。

＜例＞
```js
var jdbc = require("@rhigin/lib/JDBC");
var conn = jdbc.connect("h2db");

conn.execInsert("INSERT INTO NAME_AGE_TABLE(NAME, AGE) VALUES (?, ?);", TIME12, 999);
conn.commit();

var row = conn.query("SELECT * FROM NAME_AGE_TABLE WHERE AGE=?", 999);
console.log(row);
```

```js
[
  {
    "ID": 4,
    "NAME": "AAABb2SKdjAAAAAA",
    "AGE": 999
  }
]
```

この１２バイトのシーケンスIDは、JSON接続定義で設定されている、マシンIDを含む８バイトのUnixTimeを含んだユニークなシーケンスIDが生成されます。

ただ、この機能は [JDBCConnect.TIME12()](#5-2-10TIME12) と特性が同じなので、JDBC接続定義のJDBCConnectでない場合は、正しく取得出来ません。

_

### 5-2-1）query

queryは、SQL文のSelectを利用する場合に使います。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

// 全件を取得.
var res = jdbc.query("select * from name_age_list;");
```
fqueryは、件数がたくさんあっても先頭の１件のみを取得します。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

// 先頭の１件のみ取得.
var res = jdbc.first("select * from name_age_list;");
```

_

### 5-2-2）execInsert

execInsertは、SQL文のInsertを利用する場合に使います。

「AUTO_INCREMENT」定義のカラムが存在する場合は、その値が返却されます。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

// 1件のデータを追加.
var result = conn.execInsert("INSERT INTO name_age_list(NAME, AGE) VALUES (?, ?);", "tanaka", 38);
conn.commit();

console.log(result);
```

処理結果：

```js
[
  {
    "ID": 4
  }
]
```

_

### 5-2-3）execUpdate

execUpdateは、SQL文のSelectや「AUTO_INCREMENT」があるInsert「以外」の返却値がResultSetで無いSQL文の場合に利用します。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

// 1件のデータを更新.
var result = conn.execUpdate("UPDATE name_age_list SET age=? WHERE name=?;", 41, "suzuki");
conn.commit();

console.log(result);
```

処理結果：

```js
1
```

_

### 5-2-4）commit,rollback

insert や update でテーブル内容を更新したものを、コミット（確定）、ロールバック（取り消し）を行います。

ただし、対象コネクションがAutoCommitの場合は、この処理をしても意味がありません。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");
try {
    // 1件のデータを更新.
    var result = conn.update("UPDATE name_age_list SET age=? WHERE name=?;", 41, "suzuki");
    // 成功したらコミット.
    conn.commit();
} catch(e) {
    // 失敗したらロールバック.
    conn.rollback();
    throw e;
}
```

_

### 5-2-5）close

コネクションの利用を意図的に終了させる場合に利用します。

通常は、HTTPリクエスト終了後に、自動クローズするので、毎回呼び出す必要はありません。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

console.log("isClose: " + conn.isClose());
conn.close();
console.log("isClose: " + conn.isClose());
```

処理結果：

```js
isClose: false
isClose: true
```

_

### 5-2-6）kind

このコネクションの接続定義を取得します。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

console.log(conn.kind());
```

処理結果：

```js
{"name": "h2db","driver": "org.h2.Driver","url": "jdbc:h2:./h2db/h2db",
"user": "","password": "null","readOnly": "false",
"urlParams": ";MVCC=TRUE;LOCK_MODE=0;LOCK_TIMEOUT=120000;DB_CLOSE_ON_EXIT=TRUE;CACHE_SIZE=131072;" +
"PAGE_SIZE=32768;IFEXISTS=FALSE;AUTOCOMMIT=FALSE;CACHE_TYPE=TQ;LOG=0;UNDO_LOG=0;AUTO_SERVER=TRUE;" +
"TRACE_LEVEL_FILE=0;",
"busyTimeout": "-1","transactionLevel": "1","fetchSize": "-1","params": "{}","poolingSize": "-1",
"poolingTimeout": "-1","notSemicolon": "false", "machineId": "0"}
```

_

### 5-2-7）autoCommit

オートコミットで処理するか否かを設定します。

オートコミットをONにすることで、書き込み系の処理が成功したら、自動的にコミットされます。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

console.log("autoCommit: " + conn.isAutoCommit());
conn.setAutoCommit(true);
console.log("autoCommit: " + conn.isAutoCommit());
```

処理結果：

```js
autoCommit: false
autoCommit: true
```

_

### 5-2-8）fetch

フェッチサイズ、select文での処理結果を1度に取得するサイズを設定します。

この値を変更することで、処理速度が向上する場合があります。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

console.log("fetchSize: " + conn.getFetchSize());
conn.setFetchSize(100);
console.log("fetchSize: " + conn.getFetchSize());
```

処理結果：

```js
fetchSize: -1
fetchSize: 100
```

_

### 5-2-9）batch

バッチ実行を行います。

SQLの実行処理は、１SQL実行毎に毎度サーバに送られますが、バッチ実行の場合はまとめて送信します。

**addBatch:**

バッチ実行用のSQLを設定します.
```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

// バッチ実行用のSQLを設定します.
conn.addBatch("INSERT INTO name_age_list(NAME, AGE) VALUES (?, ?);", "sato", 26);
```

**executeBatch:**

登録されたバッチ実行の内容をまとめて送信します.
```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

// バッチ実行用のSQLを設定します.
conn.addBatch("INSERT INTO name_age_list(NAME, AGE) VALUES (?, ?);", "sato", 26);
conn.addBatch("INSERT INTO name_age_list(NAME, AGE) VALUES (?, ?);", "fujita", 30);
conn.addBatch("INSERT INTO name_age_list(NAME, AGE) VALUES (?, ?);", "nikaido", 45);

// 登録されたバッチ実行の内容をまとめて送信します.
conn.executeBatch();
```

**clearBatch:**

実行中のバッチをキャンセルします。
```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

// バッチ実行用のSQLを設定します.
conn.addBatch("INSERT INTO name_age_list(NAME, AGE) VALUES (?, ?);", "sato", 26);
conn.addBatch("INSERT INTO name_age_list(NAME, AGE) VALUES (?, ?);", "fujita", 30);
conn.addBatch("INSERT INTO name_age_list(NAME, AGE) VALUES (?, ?);", "nikaido", 45);

// 実行中のバッチをキャンセルします.
conn.clearBatch();
```

**batchSize:**

登録されているバッチ数を取得します。
```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

// バッチ実行用のSQLを設定します.
conn.addBatch("INSERT INTO name_age_list(NAME, AGE) VALUES (?, ?);", "sato", 26);
conn.addBatch("INSERT INTO name_age_list(NAME, AGE) VALUES (?, ?);", "fujita", 30);
conn.addBatch("INSERT INTO name_age_list(NAME, AGE) VALUES (?, ?);", "nikaido", 45);

// 登録されているバッチ数を取得します.
console.log(conn.batchSize());
```

実行結果：
```sh
3
```

_

### 5-2-10）TIME12

１２バイトのバイナリ、１６文字（Base64)のシーケンスIDを取得します。

```js
var jdbc = require("@rhigin/lib/JDBC");

// JDBC接続定義から取得.
var conn = jdbc.connect("h2db");

// シーケンスIDを出力.
console.log("JDBC接続定義 h2db: " + conn.TIME12());

// 直接接続.
conn = jdbc.connect("org.h2.Driver", "jdbc:h2:./h2db/h2db");

// 直接接続の場合は、シーケンスIDは取得出来ない。
console.log("直接接続: " + conn.TIME12());
```

実行結果：
```sh
JDBC接続定義 h2db: AAABb2SKdjAAAAAA
直接接続: f///////////////
```
直接接続では、シーケンスIDの発行は行なえないので `f///////////////` が返却されます。

_

### 5-2-11）Select,Delete,Insert,Update

各種よく利用する専用のSQL命令用のオブジェクトが提供されます。

**Selectオブジェクト**

Select文のオブジェクト。

```js
var jdbc = require("@rhigin/lib/JDBC");
var conn = jdbc.connect("h2db");

// NAME_AGE_LIST テーブル向けのSelectオブジェクトを取得.
var sel = conn.select("name_age_list");

var rows = sel.where("id=?").execute(2);
```

Selectオブジェクトの使い方については [5-4）Selectオブジェクト](#5-4Selectオブジェクト) を参照してください。

_

**Deleteオブジェクト**

Delete文のオブジェクト。

```js
var jdbc = require("@rhigin/lib/JDBC");
var conn = jdbc.connect("h2db");

// NAME_AGE_LIST テーブル向けのDeleteオブジェクトを取得.
var del = conn.delete("name_age_list");

// または、conn.deleteBatchでバッチ実行用オブジェクトを取得できる.
// var del = conn.deleteBatch("name_age_list");

del.where("id=?").execute(2);
```

Deleteオブジェクトの使い方については [5-5）Deleteオブジェクト](#5-5Deleteオブジェクト) を参照してください。

_

**Insertオブジェクト**

Insert文のオブジェクト。

```js
var jdbc = require("@rhigin/lib/JDBC");
var conn = jdbc.connect("h2db");

// NAME_AGE_LIST テーブル向けのInsertオブジェクトを取得.
var ist = conn.insert("name_age_list");

// または、conn.insertBatchでバッチ実行用オブジェクトを取得できる.
// var ist = conn.insertBatch("name_age_list");

ist.execute("name", "xyz", "age", 99);
```

Insertオブジェクトの使い方については [5-6）Insertオブジェクト](#5-6Insertオブジェクト) を参照してください。

_

**Updateオブジェクト**

Update文のオブジェクト。

```js
var jdbc = require("@rhigin/lib/JDBC");
var conn = jdbc.connect("h2db");

// NAME_AGE_LIST テーブル向けのUpdateオブジェクトを取得.
var upd = conn.update("name_age_list");

// または、conn.updateBatchでバッチ実行用オブジェクトを取得できる.
// var upd = conn.updateBatch("name_age_list");

upd.set("age", 48).where("name=?").execute("fujita");
```

Updateオブジェクトの使い方については [5-7）Updateオブジェクト](#5-7Updateオブジェクト) を参照してください。

_

_

## 5-3）JDBCRowオブジェクト

JDBC行取得オブジェクトの使い方について、説明します。

JDBC行取得オブジェクトは、javaのJDBCで言う所のjava.sql.ResultSetに相当します。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

var result = jdbc.query("select * from name_age_list;");
```
JDBC行取得オブジェクトを利用する場合は、上記のようにjavascript上から呼び出します。

_

### 5-3-1）next

次の行情報を取得、取得確認を行います。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

var result = jdbc.query("select * from name_age_list;");

// 情報が存在するまで取得.
while(result.hasNext()) {
    // 次の情報を取得して、コンソールに出力する.
    console.log(result.next());
}
```
`hasNext()` で、次の行情報が取得できる `true` か確認して、取得可能な場合は `next()` で１行の情報を取得します。

_

### 5-3-2）rows

行をまとめて取得します。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

var result = jdbc.query("select * from name_age_list;");

console.log(result.rows());
```

```js
result.rows(10);
```
のようにすると最大１０件分のリストをまとめて取得出来ます。

_

### 5-3-3）close

行取得オブジェクトをクローズ処理します。

```js
var jdbc = require("@rhigin/lib/JDBC");

var conn = jdbc.connect("h2db");

var result = jdbc.query("select * from name_age_list;");

console.log("isClose: " + result.isClose());

result.close();

console.log("isClose: " + result.isClose());
```

処理結果：

```js
isClose: false
isClose: true
```

_

_

## 5-4）Selectオブジェクト

Selectオブジェクトの使い方について、説明します。

```js
var jdbc = require("@rhigin/lib/JDBC");
var conn = jdbc.connect("h2db");

// NAME_AGE_LIST テーブル向けのSelectオブジェクトを取得.
var sel = conn.select("name_age_list");

var rows = sel.where("id=?").execute(2);
```

_

### 5-4-1）select

Selectオブジェクトを生成します。

```js
JDBCConnect.select(tableName, columns ...);
```

```sql
SELECT columns ... FROM tableName;
```

第一引数にテーブル名、第二引数以降に表示させるカラム名群を設定します。

```js
var jdbc = require("@rhigin/lib/JDBC");
var conn = jdbc.connect("h2db");

var sel = conn.select("name_age_list", "name", "age");

// SELECT name, age FROM name_age_list; で実行.
var rows = sel.execute();
```

_

### 5-4-2）name

テーブル名を設定します。

```js
Select.name(tableName);
```

```sql
SELECT * FROM tableName;
```

_

### 5-4-3）columns

表示するカラム名を設定します。

```js
Select.columns(columns...);
```

```sql
SELECT columns... FROM テーブル名;
```

たとえば、カウントを取得したい場合は、以下のように設定します。

```js
var jdbc = require("@rhigin/lib/JDBC");
var conn = jdbc.connect("h2db");

var sel = conn.select("name_age_list", "COUNT(ID) AS id_count");

// SELECT COUNT(ID) FROM name_age_list; で実行.
var rows = sel.execute();
```

_

### 5-4-4）where




_

_

<span style="font-size: 25px;"> [前の頁に戻る](https://github.com/maachang/rhigin/blob/master/components/jdbc/README.md) </span>
