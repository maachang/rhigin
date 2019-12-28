# rhigin 向け jdbc接続コンポーネント

Javaアプリケーションからデータベースを操作するAPIである、JDBCこれを利用したデータベース接続・操作を行うことができる「rhiginコンポーネント」です。

jdbcに対して、薄いwapper実装で、javascriptで利用することができます。

当コンポーネントは、以下の機能を有します。

```
jdbc接続定義

jdbcコネクションプーリング機能

sql操作ができる機能

javascriptから、このコンポーネントが利用できる

jdbc接続して操作するコンソール、ファイル実行機能

csvファイルを読み込んで、その内容をテーブルにinsertする機能
```
以下、上記内容について説明を行います。

_

_

## jdbc接続定義

jdbcの接続定義は、接続名を定義しJDBC接続詳細設定を行います。
例として、H2データベースの組み込み接続サンプルを元に、以下の様に行います。

conf/jdbc.json
```json
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
※ 上記定義についての説明詳細は、以下リンク先に記載しています。
＜説明リンク（のちほど記載）＞

上記の接続例では、H2データベースに対して組み込み形式によるMVCC（複数I/Oが可能）でのデータベース接続が可能な定義が、定義名として「h2db」として定義出来ます.

