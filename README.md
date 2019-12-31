# rhigin は javaVM上で動作する javascript(rhino)で実装するRESTfulなマイクロサーバです。

以前node-jsでマイクロサービスを作っていましたが、RESTfulな処理を記載するにあたり、まるでブラウザでのクライアントJSのような非同期実装が必須となり、それがとても面倒だと思いました。

node-jsの特性である「ノンブロッキングI/O」による「シングルスレッド」により、asyncやpromiseなど「余計な非同期」で実装する必要があり、それを実装することで「単純な処理」でも「複雑」なものになってしまいます。

そのため、複雑な処理を実装するにあたって「非同期地獄」そして非同期処理待ちをしてと、本来の処理を実装するよりも「複雑な非同期処理」のほうが「実装内容が多くなる」なんて状況も多々あります。

とてもじゃないけど「node-js」でのマイクロサービスの開発は「処理が複雑になれば」その分「非同期だらけ」でとても「マイクロ」じゃなくないのではないか？って思いました。

_

_

node-jsによる「非同期処理」の問題として「DBからデータを取得して、コンソールに出力」のような簡単な実装ですら、こんな感じの実装になります。

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

- DB接続
- SQL実行
- 処理結果をログ出力.

_

_

本来別にサーバサイト処理の場合は、たとえば以下のように「やりたいこと」だけをそのまま実装できれば良いんじゃないのか？って思います。

```javascript
//
// rhigin での実装サンプル例.
//
var jdbc = require("@/rhigin/lib/JDBC");

var conn = jdbc.connect("org.h2.Driver", "jdbc:h2:./h2db/h2db");
var res = conn.query("SELECT * FROM test_table;");

console.log(res);
```
こんな感じで、スレッド単位でリクエストが動くjava系のサーバーサイト実装では、node-jsのような「非同期」実装を行う必要はなく、単純にSQLを書いて処理結果を取得するだけで済みます。

javaのサーバーサイトのように実装できる「javascript」のRESTfulなマイクロサーバがほしいと思い、作ったのが、この「rhigin」です。

_

_

# rhigin の利点

rhigin サーバーの最大の利点としては、node-jsのような「非同期実装」を行わなくてよくなるので、複雑な実装にはなりません。

rhiginはjavaVM上で動作するので、javaの資産を使うことが出来ます。

通常のJavaのjspのような構成で、対象の「js」ファイルを更新すれば自動リロードされるので、nodeのexpressと違ってサーバ再起動の手間がなくなります。

node-jsと違い「非同期実装が不要」で、最も楽で「手間がかからず」かつ「ファイル更新で自動リロード」されることで「とても楽で手間がかからず」マイクロサービスをjavascriptで開発することが出来ます。

_

_

# rhigin の欠点

rhiginの最大の欠点はjsのエンジンである「rhino」がES5やES6に対応していないので、対応していない。  
ただ、現状の「rhino」のバージョンは「1.7.11」で「一部のES5」たとえば「let」などは利用可能ですが、constは利用できません。  
(そのうち対応予定です)

純粋なjsの速度ではv8エンジンであるnode-jsよりも、rhinoの方が遅い。  
ただ、HTTPのI/Oではnode-jsよりもrhiginの方が「倍ぐらい」早いので、その辺それほど速度差で困ることはない（と思います）。

node-jsのような豊富なライブラリやnpmのようなものが存在しない。  
ただ、引き換えとして「過去にあるjavaの資産」が利用できる利点はある。

rhiginには、javaでよく利用する「Gradle や maven」などのプロジェクト管理ツールに対応していません。  
(そのうち対応予定です)

テストコード、カバレッジなどに現状対応していません。  
(そのうち対応予定です)

_

_

# 興味をお持ちの方は、rhiginをインストールして、実行してみてください

当プロジェクトのrhiginに、もしご興味をお持ちの方はrhiginをインストールして、実行していただければ幸いです。

以下の流れによって、rhiginをインストールして実行します。

- [gitからrhiginをクローンする](#gitからrhiginをクローンして環境パスを通します)

- [RHIGIN_HOMEの環境パスを通す](#gitからrhiginをクローンして環境パスを通します)

- [rhiginプロジェクトを作成する](#rhigin用のプロジェクトを作成します)

- [rhiginプロジェクトを実行する](#作成したプロジェクトを実際に動かしてみます)

５分ほどのお時間で実行できると思いますので、お試しいただければ幸いです。

_

_

## gitからrhiginをクローンして、環境パスを通します

※ java8以上がインストールされていることが前提です。  
※ antはインストールされていることが前提です。  
※ linux系をベースに設定の説明をしています。  
※ ~/project/rhigin フォルダ配下にrhiginフォルダを作成して、インストールすることを説明しています。  

```shell
$ cd ~/
$ mkdir project
$ cd project

$ git clone https://github.com/maachang/rhigin.git

$ cd rhigin
$ chmod 755 ./bin/rhigin_setup
$ ./bin/rhigin_setup

$ ant
```

別途、RHIGIN_HOMEを定義する場合は `./bin/rhigin_setup` を実行する前に以下な感じで設定します。

```sh
$ echo "export RHIGIN_HOME=~/project/rhigin/bin" >> ~/.profile
$ echo 'export PATH=${PATH}:${RHIGIN_HOME}' >> ~/.profile
$ source ~/.profile
```

この設定は `./bin/rhigin_setup` を実行したときに設定されますが、既に設定されてる場合は勝手に設定されません。

_

_

## rhigin用のプロジェクトを作成します

※ ~/project/フォルダ配下にtestServerフォルダを作成して、rhiginプロジェクトを作成しています。  

```shell
$ cd ~/project

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

_

_

## 作成したプロジェクトを実際に動かしてみます

JSONで {"hello": "world"} と言う内容を返却する RESTfulなjsファイルを./application/index.jsファイルとして作成して rhiginサーバを起動します。

```shell
$ cd ~/project/testServer

$ echo "return {'hello': 'world'};" > application/index.js

$ ./rhigin
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

_

_

## ブラウザを開いて、以下のURLを入力します。

<http://127.0.0.1:3120/>

または  
<http://127.0.0.1:3120/index>

```
表示結果：
{"success":true,"status":200,"value":{"hello":"world"}}
```

_

_

簡単ですが、rhiginを導入から動作までを記載しました。

もしrhiginに興味を持ち、もう少し知りたいて思いましたら、以下を参照していただければ幸いです。





