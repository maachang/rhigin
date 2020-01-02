<span style="font-size: 25px;"> [前の頁に戻る](https://github.com/maachang/rhigin/blob/master/components/jdbc/README.md) </span>

# ７）csvファイルを読み込んで、その内容をテーブルにinsertする機能

JDBC接続コンポーネントでは、CSVファイルをテーブルにインポートする機能を提供しています。

使い方は以下の通りです。

```sh
$ ./jcsv {CSVファイル名}
```

テーブルにインポートされるCSVファイルには、いくつかルールがあります。

_

## 7-1）JDBC接続名、テーブル名は、ファイル名で定義できる

たとえば、JDBC接続名が「h2db」で、テーブル名が「NAME_AGE_LIST」に対してインポートする場合。

```
h2db.NAME_AGE_LIST.csv
```

と言うファイル名にすることで、対応出来ます。

また、ファイル名で定義しない場合は、以下の方法で行います。

```sh
$ ./jcsv -j h2db -t NAME_AGE_LIST {CSVファイル名}
```

_

## 7-2）CSVファイル内容は、１行目に対象カラム名、２行目以降をインポート対象のカラムデータを設定します

h2db.NAME_AGE_LIST.csv
```csv
name,age
hoge,24
moge,18
suzuki,23
```

この場合、以下のSQL文を実行するのと同じになります。
```sql
INSERT INTO NAME_AGE_LIST(name, age) VALUES ('hoge', 24);
INSERT INTO NAME_AGE_LIST(name, age) VALUES ('moge', 18);
INSERT INTO NAME_AGE_LIST(name, age) VALUES ('suzuki', 23);
```

_

## 7-3）CSVファイルのデフォルトの文字コードは「UTF8」で保存します

別途文字コードを指定する場合は、以下の様に行います。

```sh
$ ./jcsv -s Windows-31J {CSVファイル名}
```
上記の場合は、文字コード「Windows-31J」でCSVファイルをオープンします。

_

## 7-4）一旦テーブルを全削除して、インサートすることができます

以下のオプションで行うことが出来ます。

```sh
./jcsv -d {csvファイル名}
```
データを追加する前に `DELETE FROM TABLE_NAME;` が実行されます。

対象のテーブル内のカラムに、AUTO_INCREMENTが存在する場合、その内容はリセットされないので注意が必要です。

_

## 7-5）数値のシーケンスIDを埋め込む

CSVファイルの１つの項目に対して以下のように設定することで、番号１から始まるシーケンスIDが利用出来ます。

```csv
{num}}

or

{number}
```

上記ワードをカラム項目の代わりに追加することで、番号１から始まるシーケンスIDをInsertすることが出来ます。

＜例＞

./TEST_SEQ_TABLE.sql
```sql
-- カレントのJDBC接続定義を設定.
CONNECT 'h2db';

-- テーブル削除.
DROP TABLE IF EXISTS test_seq_table;

-- テストシーケンステーブル
CREATE TABLE test_seq_table(
    id INT(11) PRIMARY KEY NOT NULL,  -- ID
    name VARCHAR(32) NOT NULL         -- 商品名
);

-- コミット
COMMIT;
```

./h2db.TEST_SEQ_TABLE.csv
```csv
id,name
{num}, "test"
{num}, "hoge"
{num}, "moge"
```

実行:
```sh
$ ./jdbc -f TEST_SEQ_TABLE.sql
$ ./jcsv h2.TEST_SEQ_TABLE.csv
JDBC csv import version (0.0.1)
target csv : h2db.TEST_SEQ_TABLE.csv
jdbc define: h2db
table name : TEST_SEQ_TABLE
delete flag: false

success    : 3

$ ./jdbc
JDBC> select * from test_seq_table;

> select * from test_seq_table;
[
  {
    "ID": 1,
    "NAME": "test"
  },
  {
    "ID": 2,
    "NAME": "hoge"
  },
  {
    "ID": 3,
    "NAME": "moge"
  }
]
```

また、jcsvの起動パラメータ[-n or --number] で数値をセットすると、その番号を開始番号として、シーケンスIDを設定します。

```sh
$ ./jcsv -n 100 TEST_SEQ_TABLE.sql
```

_

## 7-6）１６文字のシーケンスIDを埋め込む

CSVファイルの１つの項目に対して以下のように設定することで、１６文字のシーケンスIDが利用出来ます。

```csv
{seq}

or

{sequence}
```

上記ワードをカラム項目の代わりに追加することで、１６文字のシーケンスIDをInsertすることが出来ます。

この１６文字のシーケンスIDはUnixTimeと、マシンIDによる、ユニークIDを作成します。

＜例＞

./TEST_SEQ16_TABLE.sql
```sql
-- カレントのJDBC接続定義を設定.
CONNECT 'h2db';

-- テーブル削除.
DROP TABLE IF EXISTS test_seq16_table;

-- テストシーケンステーブル
CREATE TABLE test_seq_table(
    id CHAR(16) PRIMARY KEY NOT NULL, -- ID
    name VARCHAR(32) NOT NULL         -- 商品名
);

-- コミット
COMMIT;
```

./h2db.TEST_SEQ16_TABLE.csv
```csv
id,name
{seq}, "test"
{seq}, "hoge"
{seq}, "moge"
```

実行:
```sh
$ ./jdbc -f TEST_SEQ16_TABLE.sql
$ ./jcsv h2.TEST_SEQ16_TABLE.csv
JDBC csv import version (0.0.1)
target csv : h2db.TEST_SEQ16_TABLE.csv
jdbc define: h2db
table name : TEST_SEQ16_TABLE
delete flag: false

success    : 3

$ ./jdbc
JDBC> select * from test_seq16_table;

> select * from test_seq16_table;
[
  {
    "ID": "AAABb2SsongAAAAA",
    "NAME": "test"
  },
  {
    "ID": "AAABb2SsonsAAAAA",
    "NAME": "hoge"
  },
  {
    "ID": "AAABb2SsonsAAQAA",
    "NAME": "moge"
  }
]
```

_

## 7-7）コマンドの使い方はヘルプを指定すると閲覧出来ます

```sh
$ ./jcsv -h
jcsv [-c --conf --config] [-j --jdbc] [-t --table] [-s --charset] [-d --delete] {file}
 Read CSV and insert into database table.
  [-c] [--conf] [--config] {args}
    Set the configuration definition file name.
    If omitted, "jdbc" character is specified.
  [-j] [--jdbc] {args}
    Set the connection definition name of jdbc.
    If omitted, it must be set with the name of {file}.
  [-t] [--table] {args}
    Set the write destination table name.
    If omitted, it must be set with the name of {file}.
  [-s] [--charset] {args}
    Set the character code of the CSV file.
    If not specified, "UTF8" will be set.
  [-d] [--delete]
    Set to delete all database contents.
    If not set, all data will not be deleted.
  [-n] [--num] {number}
    Set the start number of the numeric sequence ID.
    If not set, start from 1.
  {file}
    If [-j or -t] is omitted, each is interpreted by the file name.
      {file} = [jdbc name].[table name].csv
    If [-j or -t] is not omitted, set an arbitrary file name.
```

_

_

<span style="font-size: 25px;"> [前の頁に戻る](https://github.com/maachang/rhigin/blob/master/components/jdbc/README.md) </span>
