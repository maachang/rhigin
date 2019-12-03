# rhigin は javaVM上で動作する javascript(rhino)で実装するRESTfulなマイクロサーバです。

以前node-jsでマイクロサービスを行っていましたが、RESTfulな処理を記載するにあたり、サーバ実装にもかかわらずnode-jsではブラウザでのクライアントJSのような実装が面倒になると感じました。

ブラウザでのクライアントJSのような実装と言うのは、node-jsの特性である「ノンブロッキングI/O」による「シングルスレッド」により、asyncやpromiseなど「余計な非同期」で実装しなければならなくなります。

そのため「簡単な実装」でも「いちいち以下のようにpromiseによる非同期」で実装をする必要があり、簡単な処理なのに「複雑」になってしまいます。

そして、これが単純なものではなく「複雑な処理になればその分非同期処理だらけ」の実装となり、本来やりたいことを「阻害するような」非同期だらけの実装になり「マイクロ開発なのに、どんどん面倒」になります。

とてもじゃないけど「node-js」でのマイクロサービスの開発は「処理が複雑になれば」その分「非同期だらけ」でとても「マイクロ」じゃなくなってしまいます。

_

_

なので、たとえば node-jsだと「DBからデータを取得して、コンソールに出力」のような簡単な実装ですら、こんな感じで「非同期の実装」が必要となります。

```javascript
//
// node-jsでの実装サンプル例.
//
const mysql = require('/usr/local/lib/node_modules/promise-mysql');

mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: '',
    database: 'testjs'
}).then(function(conn) {
    const result = conn.query('SELECT * FROM test_table');
    conn.end();
    return result;
}).then(function(rows) {
    console.log(rows);
});
```

_

_

だけど上記実装では、本来単純に「DBからデータを取得して、コンソールに出力」と単純な処理でしかなく。
```
1. DB接続
2. SQL実行
3. 処理結果をログ出力.
```

_

_

本来別にサーバサイト処理の場合は、たとえば以下のように「やりたいこと」だけをそのまま実装できれば良いんじゃないのか？って思います。

```javascript
//
// rhigin での実装サンプル例.
//
var jdbc = require("jdbc-pooling");

var conn = jdbc("test-mysql").connect("testjs");
var res = conn.query("SELECT * FROM test_table").execute();
console.log(res);
```
こんな感じで、スレッド単位でリクエストが動くjava系のサーバーサイト実装では、node-jsのような「非同期」実装を行う必要はなく、単純にSQLを書いて処理結果を取得するだけで済みます。

javaのサーバーサイトのように実装できる「javascript」のRESTfulなマイクロサーバがほしいと思い、作ったのが、この「rhigin」です。

_

_

# rhigin を利用する利点

rhigin サーバーの最大の利点としては、node-jsのような「非同期実装」を行わなくてよくなるので、複雑な実装にはなりません。

rhiginはjavaVM上で動作するので、javaの資産を使うことが出来ます。

通常のJavaのjspのような構成なので対象の「js」ファイルを更新すれば、自動リロードされるので、nodeのexpressと違ってサーバ再起動は必要ありません。

rhiginによって、node-jsと違い「非同期実装が不要」で、最も楽で「簡単」かつ「ファイル更新で自動リロード」されるので、マイクロサービスを開発することが「とても簡単」に行えます。

_

_

# rhigin の欠点

rhiginの最大の欠点はjsのエンジンである「rhino」がES5やES6に対応していないので、対応していない。  
ただ、現状の「rhino」のバージョンは「1.7.11」で「一部のES5」たとえば「let」などは利用可能である（constは利用できない）。  
(そのうち対応予定)

純粋なjsの速度ではv8エンジンであるnode-jsよりも、rhinoの方が遅い。  
ただ、HTTPのI/Oではnode-jsよりもrhiginの方が「倍ぐらい」早いので、その辺それほど速度差で困ることはない（と思う）。

node-jsのような豊富なライブラリやnpmのようなものが存在しない。  
ただ、引き換えとして「過去にあるjavaの資産」が利用できる利点はある。

rhiginには、javaでよく利用する「Gradle や maven」などのプロジェクト管理ツールに対応していません。  
(そのうち対応予定)

テストコード、カバレッジなどに現状対応していません。  
(そのうち対応予定)

_

_


# rhigin のセットアップと、実行について

rhiginのセットアップは、非常に簡単です。

以下のように行います。

※ java8以上はインストールされていることが前提です。  
※ antはインストールされていることが前提です。  
※ linux系をベースに設定の説明をしています。  
※ opt/bin/フォルダ配下にrhiginフォルダを作成して、インストールすることを説明しています。  

```shell
$ cd /opt
$ mkdir bin
$ cd bin

$ git clone https://github.com/maachang/rhigin.git

$ echo "export RHIGIN_HOME=/opt/bin/rhigin/bin" >> ~/.profile
$ echo "export PATH=${PATH}:${RHIGIN_HOME}" >> ~/.profile
$ source ~/.profile

$ cd rhigin
$ ant
```

次に rhigin 用のプロジェクトを作成します。

※ opt/project/フォルダ配下にtestServerフォルダを作成して、rhiginプロジェクトを作成しています。  

```shell
$ cd /opt
$ mkdir project
$ cd project

$ mkdir testServer
$ cd testServer

$ rproj -n testServer -v 0.0.1
$ ls
application  conf  index.js  jar  log  rbatch  rhigin

$ cat conf/rhigin.json
{
  "projectName": "testServer"
  ,"version": "0.0.1"
}
```

作成したプロジェクトを実際に動かしてみます。

JSONで {"hello": "world"} と言う内容を返却する RESTfulなjsファイルを./application/index.jsファイルとして作成して rhiginサーバを起動します。

```shell
$ cd /opt/project/testServer

$ echo "return {'hello': 'world'};" > application/index.js

$ ./rhigin
start rhigin.
[2019/12/04 00:39:34.422] [DEBUG] start rhigin version (0.0.1). 
[2019/12/04 00:39:34.735] [DEBUG]  start Http nio: 16 threads. 
[2019/12/04 00:39:34.750] [DEBUG]  * start rhigin workerThread(1). 
[2019/12/04 00:39:34.750] [DEBUG]  * start rhigin workerThread(0). 
[2019/12/04 00:39:34.751] [DEBUG]  * start rhigin workerThread(3). 
[2019/12/04 00:39:34.751] [DEBUG]  * start rhigin workerThread(4). 
[2019/12/04 00:39:34.752] [DEBUG]  * start rhigin workerThread(5). 
[2019/12/04 00:39:34.751] [DEBUG]  * start rhigin workerThread(2). 
[2019/12/04 00:39:34.758] [DEBUG]  * start rhigin workerThread(7). 
[2019/12/04 00:39:34.759] [DEBUG]  * start rhigin workerThread(8). 
[2019/12/04 00:39:34.759] [DEBUG]  * start rhigin workerThread(9). 
[2019/12/04 00:39:34.759] [DEBUG]  * start rhigin workerThread(10). 
[2019/12/04 00:39:34.760] [DEBUG]  * start rhigin workerThread(6). 
[2019/12/04 00:39:34.760] [DEBUG]  * start rhigin workerThread(11). 
[2019/12/04 00:39:34.760] [DEBUG]  * start rhigin workerThread(12). 
[2019/12/04 00:39:34.778] [DEBUG]  * start rhigin workerThread(13). 
[2019/12/04 00:39:34.780] [DEBUG]  * start rhigin workerThread(14). 
[2019/12/04 00:39:34.784] [DEBUG]  * start rhigin workerThread(15). 
```

ブラウザを開いて、以下のURLを入力します。

<http://127.0.0.1:3120/>

または  
<http://127.0.0.1:3120/index>

```
表示結果：
{"success":true,"status":200,"value":{"hello":"world"}}
```




