<span style="font-size: 25px;"> [前の頁に戻る](https://github.com/maachang/rhigin/blob/master/components/jdbc/README.md) </span>

# ６）jdbc接続してSQL実行用コンソール、ファイル実行機能


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

JDBCコンソール機能では、JDBC経由でのSQL文の実行を行うことが出来ますが、それ以外に、本コンポーネント専用のコマンドも利用することが出来ます。

- [help](#6-1help)
- [exit,quit](#6-2exitquit)
- [close](#6-3close)
- [commit](#6-4commit)
- [rollback](#6-5rollback)
- [list](#6-6list)
- [kind](#6-7kind)
- [connect](#6-8connect)

以下より、それらのコマンドに対する説明を行います。

_

_

## 6-1）help

ヘルプ情報を表示します。

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

_

## 6-2）exit,quit

コンソールを終了します。

```sql
JDBC> exit;
```

_

## 6-3）close

現在の接続中のコネクションなどを、全てクローズします。

```sql
JDBC> close;
```

_

## 6-4）commit

現在の接続中のコネクションをすべて、コミット実行します。

```sql
JDBC> commit;
```

_

## 6-5）rollback

現在の接続中のコネクションをすべて、ロールバック実行します。

```sql
JDBC> rollback;
```

_

## 6-6）list

JDBC接続定義名群を取得します。

```sql
JDBC> list;

> list;
[
  "h2db"
]
```

_

## 6-7）kind

JDBC接続定義詳細を取得します。

```sql
JDBC> kind h2db;

> kind h2db;
{
  "name": "h2db",
  "driver": "org.h2.Driver",
  "url": "jdbc:h2:./h2db/h2db",
  "user": "",
  "password": null,
  "readOnly": false,
  "urlParams": ";MVCC=TRUE;LOCK_MODE=0;LOCK_TIMEOUT=120000;DB_CLOSE_ON_EXIT=TRUE;CACHE_SIZE=131072;PAGE_SIZE=32768;IFEXISTS=FALSE;AUTOCOMMIT=FALSE;CACHE_TYPE=TQ;LOG=0;UNDO_LOG=0;AUTO_SERVER=TRUE;TRACE_LEVEL_FILE=0;",
  "busyTimeout": -1,
  "transactionLevel": 1,
  "fetchSize": -1,
  "params": {},
  "poolingSize": -1,
  "poolingTimeout": -1,
  "notSemicolon": false
}
```

_

## 6-8）connect

現在利用するJDBC接続名を取得、設定します。

現在利用しているJDBC接続名を取得する場合は、以下の通りとなります。

```sql
JDBC> connect;

> connect;
[
  "h2db"
]
```

現在利用するJDBC接続名を設定する場合は、以下の通りとなります。

```sql
JDBC> connect h2db;
```

_

_

<span style="font-size: 25px;"> [前の頁に戻る](https://github.com/maachang/rhigin/blob/master/components/jdbc/README.md) </span>
