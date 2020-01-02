<span style="font-size: 25px;"> [前の頁に戻る](https://github.com/maachang/rhigin/blob/master/README.md) </span>

# チュートリアルを始めよう

まずはじめに本チュートリアルを経て、Rhiginサーバを使って簡単なアプリケーションを作りながら、使い方を把握していただければと思います。

この項では、既にTOPドキュメントで説明した、rhiginの動作環境はすでにセットアップ済みであることを元に説明します。

_

_

## １）開発環境を整える

昨今では、データベースを使った開発はSQL,NoSQLにかかわらず「必須」であると言えます。

ここでは、よくあるRDBMSをrhiginで利用できるようにしたいと思います。

_

### 1-1）プロジェクトを作る

はじめに、今回のチュートリアル用のプロジェクトを登録します。

チュートリアルとして、簡易的な「productMan」と言う「商品管理」用のRESTfulなプロジェクトを作成します。

```sh
$ cd ~/
$ mkdir rhigin

$ mkdir productMan
$ cd productMan

$ rproj -n productMan -v 0.0.1
```

これで「productMan」と言うrhiginのプロジェクトの雛形は完成です。

_

### 1-2）Rhigin向けのJDBC接続コンポーネントを導入

次にデーターベースにアクセスするための準備を行います。

rhiginでは、JDBC接続コンポーネントが対応していて、以下のコマンドで、H2データベースが即時利用することが出来ます。

```sh
$ cd ~/rhigin/productMan

$ rlib jdbc h2
```

これで、H2データベースを組み込み形式で利用するための環境が構築されました。

JDBC接続定義名 `h2db` として、組み込み形式のH2データベースが利用可能になります。

JDBC接続コンポーネントの詳細につきましては [ここ](https://github.com/maachang/rhigin/blob/master/components/jdbc/README.md) に詳しく記載してます。

_

_

## ２）データベースの環境を整える

イメージとして、以下のテーブルを作成します。

＜商品マスタテーブル＞ product_master

|商品ID(id)|商品名(name)|商品区分(section)|
|:---|:---|:---|
|1|メロン|果物|
|2|カニ|甲殻類|
|3|みかん|果物|
|4|魚|魚介類|
|5|牛肉|牛肉|
|6|豚肉|豚肉|
|7|鶏肉|鶏肉|

商品IDは手動採番で、商品名、商品区分は必須で設定するものとします。

＜商品在庫テーブル＞ product_inventory

|在庫ID(id)|商品ID(product_id)|表示名(name)|価格(price)|在庫数(inventory)|
|:---|:---|:---|:---|:---|
|1|1|マスクメロン|2,500|30|
|2|1|夕張メロン|2,000|15|
|3|2|ワタリガニ|1,500|20|
|4|2|ズワイガニ|3,000|10|
|5|3|ハウスみかん|500|100|
|6|4|寒ブリ|800|35|
|7|5||650|150|
|8|6||398|300|
|9|7||290|500|

在庫IDは自動採番で、商品IDは必須、表示名が存在しない場合は商品マスタの商品名を利用し、価格は必須で在庫数が指定されていない場合は０を入れるものとします。

簡単ではありますが、この内容を元にチュートリアル内容を進めていきたいと思います。

_

### 2-1）テーブルを作成する

先程示した「イメージ」を元に以下のようにテーブルを作成します。

~/rhigin/productMan/product.sql
```sql
-- カレントのJDBC接続定義を設定.
CONNECT 'h2db';

-- テーブル削除.
DROP TABLE IF EXISTS product_master;
DROP TABLE IF EXISTS product_inventory;

-- 商品マスタ
CREATE TABLE product_master(
    id INT(11) PRIMARY KEY NOT NULL,        -- 商品ID
    name VARCHAR(32) NOT NULL ,             -- 商品名
    section VARCHAR(32) NOT NULL,           -- 商品区分
);

-- 商品在庫
CREATE TABLE product_inventory(
    id INT(11) AUTO_INCREMENT NOT NULL,     -- 在庫ID
    product_id INT(11) NOT NULL,            -- 商品ID
    name VARCHAR(32) NOT NULL ,             -- 表示名
    price INT(8) NOT NULL,                  -- 価格
    inventory INT(6) NOT NULL,              -- 在庫数
    PRIMARY KEY (id));

-- 確定.
COMMIT;
```
この内容をファイルに保存して、以下のコマンドを実行します。

```sh
$ cd ~/rhigin/productMan
$ ./jdbc -f ./product.sql
```

これで、データベースにテーブルが作成されました。

_

### 2-2）データをInsertする

先程作成したテーブルに対して、CSVファイルのデータをInsertします。

~/rhigin/productMan/h2db.product_master.csv
```csv
name,section
1,メロン,果物
2,カニ,甲殻類
3,みかん,果物
4,魚,魚介類
5,牛肉,牛肉
6,豚肉,豚肉
7,鶏肉,鶏肉
```

~/rhigin/productMan/h2db.product_inventory.csv
```csv
product_id,name,price,inventory
1,マスクメロン,2500,30
1,夕張メロン,2000,15
2,ワタリガニ,1500,20
2,ズワイガニ,3000,10
3,ハウスみかん,500,100
4,寒ブリ,800,35
5,,650,150
6,,398,300
7,,290,500
```

上記CSVファイルを其々ファイルに保存して、以下のコマンドを実行します。

```sh
$ cd ~/rhigin/productMan

$ ./jcsv h2db.product_master.csv
JDBC csv import version (0.0.1)
target csv : h2db.product_master.csv
jdbc define: h2db
table name : product_master
delete flag: false

success    : 7

$ ./jcsv h2db.product_inventory.csv
JDBC csv import version (0.0.1)
target csv : h2db.product_inventory.csv
jdbc define: h2db
table name : product_inventory
delete flag: false

success    : 9
```
これで、当初のイメージの通りにテーブルが作成され、データを格納することが出来ました。

_

_

## ３）其々の機能を実装してみる

まずはRESTfulでアクセスするクライアント向けの雛形を作成します。

~/rhigin/productMan/application/product.html

```html
<!DOCTYPE HTML SYSTEM "about:legacy-compat">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ja">
<head>
<meta charset="UTF-8" />
<title>product client.</title>
<meta http-equiv="content-type" content="application/xhtml+xml; charset=utf-8"/>
<meta http-equiv="content-style-type" content="text/css; charset=utf-8" />
<meta http-equiv="content-script-type" content="text/javascript; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge"/>

<script>
////////////////////////////////////////////////////////////////////////////////
// http_client.
// method : POST(JSON) or GET.
// URL : 接続先URL.
// option: 以下がオプションで設定できます.
//         params : パラメータ設定(Map定義).
//         noCache : キャッシュなしの場合は[true].
//         headers : ヘッダ情報.
// func : コールバックファンクション.
//        コールバックファンクションを設定しない場合は、同期取得(非推奨).
//        func(status, body, headers);
// errorFunc : エラー発生時のコールバックファンクション.
//             errorFunc(status, body, headers);
////////////////////////////////////////////////////////////////////////////////
var http_client=function(){var e=function(e,n,t){if(t["Content-Type"]||("POST"==e?n.setRequestHeader("Content-Type","application/x-www-form-urlencoded"):"JSON"==e&&n.setRequestHeader("Content-Type","application/json")),t)for(var r in t)"Content-Length"!=r&&n.setRequestHeader(r,t[r])},n=function(e){return"JSON"==e?"POST":e};return function(t,r,o,a,s){o||(o={});var i=o.params,u=o.noCache,f=o.headers;f=f||{},s="function"!=typeof s?a:s,t=(t+"").toUpperCase(),1!=u&&(r+=(-1==r.indexOf("?")?"?":"&")+(new Date).getTime());var l="";if(i)if("string"==typeof i||i instanceof Blob||i instanceof File||i instanceof Uint8Array||i instanceof ArrayBuffer)l=i;else{var p=0;for(var c in i)0!=p&&(l+="&"),l+=c+"="+encodeURIComponent(i[c]),p++}if("GET"==t&&(r+=l,l=null),a==_u){var d;(d=new XMLHttpRequest).open(n(t),r,!1),e(t,d,f),d.send(l);var v=d.status;0==v&&(v=500);var y=d.responseText;if(d.abort(),v<300)return y;throw new Error("response status:"+v+" error")}(d=new XMLHttpRequest).open(n(t),r,!0),d.onload=function(){if(4==d.readyState)try{var e=d.status;e&&0!=e||(e=500),e<300?a(e,d.responseText):s(e,d.responseText)}finally{d.abort(),d=null,a=null,s=null}},d.onerror=function(){var e=d.status;e&&0!=e||(e=500);try{s(e,d.responseText)}finally{d.abort(),d=null,a=null,s=null}},e(t,d,f),d.send(l)}}();
</script>

<style>
html{height:100%}body{margin-left:1;padding:1;-webkit-tap-highlight-color:transparent;position:absolute;top:0;left:0;width:100%;}.base_button{margin-top:5px;font-size:16px;position:relative;display:inline-block;padding:.25em .5em;text-decoration:none;color:#333;background:#a9c403;border:solid 1px #9aaa0f;border-radius:4px;box-shadow:inset 0 1px 0 rgba(255,255,255,.2);text-shadow:0 1px 0 rgba(0,0,0,.2)}.base_button:active{border:solid 1px #03a9f4;box-shadow:none;text-shadow:none}.base_input_text{border:0;padding:5px;color:#333;border:solid 1px #ccc;margin:0 0 10px;border-radius:5px}
</style>

</head>
<body style="background:#000;color:#fff;">



</body>
</html>
```


