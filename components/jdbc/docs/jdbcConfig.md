<span style="font-size: 25px;"> [前の頁に戻る](https://github.com/maachang/rhigin/blob/master/components/jdbc/README.md) </span>

# ３）jdbc接続定義

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

以下より、それぞれの設定に対する説明を行います。

_

_

## 3-1）JDBC接続定義名

```js
{
    "name1": { .... },
    "name2": { .... }
    .....
}
```

JDBC接続定義は複数定義することが出来ます。

_

## 3-2）driver

JDBCのドライバ名を設定します。

以下代表的なデータベースのJDBCドライバ名を記載いたします。

|データベース名|JDBCドライバ名|
|:---|:---|
| mysql | com.mysql.jdbc.Driver |
| mariadb | org.mariadb.jdbc.Driver |
| postgresql | org.postgresql.Driver |
| firebird | org.firebirdsql.jdbc.FBDriver |
| H2 | org.h2.Driver |
| HSQL | org.hsqldb.jdbc.JDBCDriver |
| sqlite | org.sqlite.JDBC |
| Derby | org.apache.derby.jdbc.ClientDriver |
| oracle | oracle.jdbc.driver.OracleDriver |
| sql server | com.microsoft.sqlserver.jdbc.SQLServerDriver |
| db2 | com.ibm.db2.jcc.DB2Driver |
| symfoware | com.fujitsu.symfoware.jdbc.SYMDriver |
| HiRDB | JP.co.Hitachi.soft.HiRDB.JDBC.HiRDBDriver |

_


## 3-3）url

JDBC接続先のURLを設定します。

以下代表的なデータベースのURL設定内容例を記載いたします。

|データベース名|url例|説明|
|:---|:---|:---|
| mysql | jdbc:mysql://[ホスト名]:[ポート番号]/[データベース名(パス)] |  |
| mariadb | jdbc:mymariadbsql://[ホスト名]:[ポート番号]/[データベース名(パス)] |  |
| firebird | jdbc:firebirdsql:[ホスト名]/[ポート番号]:[データベースファイル名] |  |
| postgresql | jdbc:postgresql://[ホスト名]:[ポート番号]/[データベース名] |  |
| H2 | jdbc:h2:tcp://[ホスト名]:[ポート番号]/[データベース名(パス)] | サーバ接続 |
| H2 | jdbc:h2:[ファイル・ディレクトリ名] | 組み込み接続 |
| HSQL | jdbc:hsqldb:hsql://[ホスト名]:[ポート番号]/[データベース名(パス)] | サーバ接続 |
| HSQL | jdbc:hsqldb:file://[ファイル・ディレクトリ名] | 組み込み接続 |
| sqlite | jdbc:sqlite:[ファイル・ディレクトリ名] |  |
| Derby | jdbc:derby://[ホスト名]:[ポート番号]/[データベース名(パス)] |  |
| oracle | jdbc:oracle:thin:@[ホスト名]:[ポート番号]:[データベース名] |  |
| sql server | jdbc:sqlserver://[ホスト名];databaseName=[データベース名] |  |
| db2 | jdbc:db2://[ホスト名]:[ポート番号]/[データベース名] |  |
| symfoware | jdbc:symford://[ホスト名]:[ポート番号]/[データベース名] |  |
| HiRDB | jdbc:hitachi:hirdb://DBID=[ポート番号],DBHOST=[ホスト名] |  |

_

## 3-4）user,password

JDBC接続に必要なユーザ名、パスワードを設定します。

設定しない場合は、空文字を設定します.

_

## 3-5）readOnly

これをONにすると、読み込み専用でアクセスします。

```js
readOnly: true
```

_

## 3-6）busyTimeout

データベース接続タイムアウトをミリ秒単位で設定します。

設定しない場合は -1 を設定します。

_

## 3-7）transactionLevel

以下のトランザクションレベルを設定します。

|トランザクションレベル|説明|
|:---|:---|
| TRANSACTION_NONE | トランザクションがサポートされていないことを示す定数です。|
| TRANSACTION_READ_COMMITTED | ダーティ読込みは抑制され、繰返し不可の読み込みおよびファントム読込みが起こることを示す定数です。|
| TRANSACTION_READ_UNCOMMITTED | ダーティ読み込み、繰返し不可の読み込み、およびファントム読込みが起こることを示す定数です。|
| TRANSACTION_REPEATABLE_READ | ダーティ読み込みおよび繰返し不可の読込みは抑制され、ファントム読込みが起こることを示す定数です。|
| TRANSACTION_SERIALIZABLE | ダーティ読み込み、繰返し不可の読み込み、およびファントム読込みが抑制されることを示す定数です。|

設定しない場合は、空文字か -1 を設定します。

_

## 3-8）fetchSize

デフォルトのフェッチサイズを設定します。

この値を大きくすることで、SELECT文などのリスト返却での処理が向上する場合があります。

設定しない場合は -1 を設定します。

_

## 3-9）poolSize

コネクションプーリングサイズを設定します。

この値は基本 -1 に設定設定することで、最大プーリング数を設定します。

特別問題ない場合は -1 で問題ありません。

_

## 3-10）poolTimeout

JDBCコネクション接続に対するタイムアウト値を設定します。

設定しない場合は -1 を設定します。

_

## 3-11）params

JDBCのコネクション作成時にセットするプロパティ値を設定します。

_

## 3-12）urlParams

URLパラメータを設定します。

文字列で設定した場合、その内容がそのままURLに連結されます。

連想排列の場合は、urlTypeに従って、URLに連結されます。

_

## 3-13）urlType

URLドライバパラメータ区切りタイプを設定します。

```
[urlType: true] の場合は、 url + "?" + urlParams + "&" ... と連結されます。
[urlType: false] の場合は、 url + ";" + urlParams + ";" ... と連結されます。
```

ただし `urlParams` が文字列で設定されている場合は、この値に意味を持ちません。

_

<span style="font-size: 25px;"> [前の頁に戻る](https://github.com/maachang/rhigin/blob/master/components/jdbc/README.md) </span>