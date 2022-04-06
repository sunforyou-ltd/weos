/*------------------------------------------*/
/*----- 【WeoS】ログイン画面           -----*/
/*------------------------------------------*/
(function($) {

  /*------------------------------------------*/
  /*----- インスタンス変数               -----*/
  /*------------------------------------------*/
  var targetkey;                              //処理対象行のキー
  var searchlist;                             //検索結果一覧JSON
  var searchdata;                             //検索結果JSON
  var searchcommit;                           //検索確定フラグ
  var kaizouinfo;                             //改造情報
  var optioninfo = [];                        //オプション情報
  var estimatemode;                           //見積モード
  var userkey;                                //ユーザ権限と見積状態
  var khontai = 0;                            //本体価格
  var kmitumori = 0;                          //最終見積額
  var ktax = 0;                               //消費税
  /*------------------------------------------*/
  /*----- 初期表示                       -----*/
  /*------------------------------------------*/
  function init() {
    $.ajax({
      url: '/initestimate',
      type: 'GET',
      complete:function(data, status, jqXHR){         //処理成功時
        var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
        /*----- 項目制御を先に行う為、先にモードを確定させる -----*/
        if ("Retrieval" == jsonResult.mode) {
          estimatemode = "Retrieval";
        }
        else {
          estimatemode = "Add";
        }

        if ("Retrieval" == jsonResult.mode) {
          //----- 基本情報 ------
          $("#mtmno").text(('0000000000' + jsonResult.mtmno).slice( -10 ));				//見積番号
          $("#edaban").text(('00' + jsonResult.edaban).slice( -2 ));						  //枝番
          $("#hmtmno").val(('0000000000' + jsonResult.mtmno).slice( -10 ));       //見積番号
          $("#hedaban").val(('00' + jsonResult.edaban).slice( -2 ));              //枝番
          $("#kanri").val(jsonResult.kanri);												//管理番号
          $("#kouji").val(jsonResult.kouji);												//工事件名
          $("#haturei").val(jsonResult.haturei);											//発令元コード
          $("#mkaisya").text(jsonResult.kaisyaName);										//見積依頼者(会社名)
          $("#mshiten").text(jsonResult.shitenName + " " + jsonResult.bumonName);			//見積依頼者(支店名＋部門名)
          $("#mtantou").text(jsonResult.shimeiKanji);										//見積依頼者(依頼者名)
          $("#irainouki").val(jsonResult.irainouki);										//依頼納期
          $("#kaitounouki").val(jsonResult.kaitounouki);									//回答納期
          $("#iraibi").text(jsonResult.iraibi);											//見積依頼日
          $("#kaitoubi").text(jsonResult.kaitoubi);										//見積回答日
          $("#tokuicode").val(jsonResult.tokuicode);										//得意先コード
          $("#tokuiname").val(jsonResult.tokuiname);										//得意先名称
          $("#postcode").val(jsonResult.postcode);										//郵便番号
          $("#addr1").val(jsonResult.addr1);												//住所１
          $("#addr2").val(jsonResult.addr2);												//住所２
          $("#tel").val(jsonResult.tel);													//電話番号
          $("#fax").val(jsonResult.fax);													//FAX番号
          //----- 改造情報 ------
          CardKaizoList($("#kaizolist"));
          CardKaizoList.clear();
          CardKaizoList.put(jsonResult.mtsList);
          //----- 特殊改造情報 ------
          CardTokuList($("#tokusyulist"));
          CardTokuList.clear();
          CardTokuList.put(jsonResult.mtkList);
          /*----- 日付オブジェクト再設定 -----*/
          datepickerset();
          /*----- 削除ボタン再設定 -----*/
          deletebtnset();
          //----- 備考情報 ------
          $("#kaizono").val(jsonResult.kaizono);											//改造承認No.
          $("#koubaino").val(jsonResult.koubaino);										//購買管理No.
          $("#psitenname").val(jsonResult.psitenname);									//見積支店名
          $("#ptantoname").val(jsonResult.ptantoname);									//見積担当名
          $("#kaizoseikyu").val(jsonResult.kaizoseikyu);									//改造費請求先
          $("#unchin").val(jsonResult.unchin);											//運賃請求先
          $("#panel").val(jsonResult.panel);												//パネル色
          $("#setti").val(jsonResult.setti);												//設置個所
          $("#biko1").val(jsonResult.biko1);												//備考１
          $("#biko2").val(jsonResult.biko2);												//備考２
          $("#biko3").val(jsonResult.biko3);												//備考３
          $("#koutei1").val(jsonResult.koutei1);											//工程納期１
          $("#koutei2").val(jsonResult.koutei2);											//工程納期２
          $("#koutei3").val(jsonResult.koutei3);											//工程納期３
          //----- 見積金額 ------
          $("#kmitumori").text(jsonResult.kmitumori.toLocaleString() + ' 円');				//最終見積合計金額
          $("#khontai").text(jsonResult.khontai.toLocaleString() + ' 円');					//本体価格
          $("#kmitusanko").text((jsonResult.khontai + jsonResult.knebiki).toLocaleString()  + ' 円');       //見積合計(参考)
          $("#knebiki").val(jsonResult.knebiki.toLocaleString());							//値引金額
          $("#kritu").val(jsonResult.kritu.toLocaleString());								//値引率
          $("#kshouhizei").text(jsonResult.kshouhizei.toLocaleString() + ' 円');			//消費税
          //----- 受注確定 ------
          if ("1" == jsonResult.kakutei) {
            $("#mikakutei").hide();
            $("#kakutei").show();
          }
          else {
            $("#mikakutei").show();
            $("#kakutei").hide();
          }
        }
        else {
          //----- 基本情報 ------
          $("#hmtmno").val("");                                     //見積番号
          $("#hedaban").val("");                                    //枝番
          $("#postcode").val(jsonResult.postcode);										//郵便番号
          $("#addr1").val(jsonResult.addr1);												//住所１
          $("#addr2").val(jsonResult.addr2);												//住所２
          $("#tel").val(jsonResult.tel);													//電話番号
          $("#fax").val(jsonResult.fax);													//FAX番号
          //----- 改造情報 ------
          CardKaizoList($("#kaizolist"));
          CardKaizoList.clear();
          CardKaizoList.add();
          //----- 特殊改造情報 ------
          CardTokuList($("#tokusyulist"));
          CardTokuList.clear();
          CardTokuList.add();
          //----- 備考情報 ------
          $("#kaizoseikyu").val(jsonResult.kaizoseikyu);									//改造費請求先

          /*----- 必須項目編集 -----*/
          /*----- モード別項目編集 -----*/
          //----- 受注確定 ------
          if ("1" == jsonResult.kakutei) {
            $("#mikakutei").hide();
            $("#kakutei").show();
          }
          else {
            $("#mikakutei").show();
            $("#kakutei").hide();
          }
        }
        deletebtnset(); //行削除ボタン
        /*----- 項目制御情報の取得 -----*/
        var kengen = jsonResult.kengen;
        if (kengen == "02" && jsonResult.naigai == 0) { //LE系の場合
          kengen = "99"; //権限を読み替える
        }
        estimateControl(kengen, jsonResult.stts);
        //----------------------------------------------------------------------------
        $("#tax").val(jsonResult.tax);            //消費税率
        //----------------------------------------------------------------------------
        if (kengen == "01") { //サンデンテクノの場合
          $('#mikakutei').nocommitclick();        //「受注未確定」ボタン
          $('#kakutei').commitclick();            //「受注確定」ボタン
        }
        else {
          $('#mikakutei').unbind("click");        //「受注未確定」ボタン
          $('#kakutei').unbind("click");          //「受注確定」ボタン
        }
        //----------------------------------------------------------------------------
        if ("Retrieval" == jsonResult.mode) {
          if (jsonResult.stts == "10") {
            $("#addbtn").hide();
            $("#modbtn").show();
            $("#estdeletebtn").show();
          }
          //----- 変更発注ボタン制御 ------
          if (jsonResult.kengen == "03" && jsonResult.stts == "20") { //フロント営業、且つ見積回答済
        	  if ($("#kaizono").val() != "" && $("#koubaino").val() != "") {
                  $("#modbtn").hide();
                  $("#hachubtn").show();
        	  }
          }
        }
        else {
          $("#addbtn").show();
          $("#modbtn").hide();
          $("#estdeletebtn").hide();
        }
        //----------------------------------------------------------------------------
        calcbtnset();							  //金額算出イベント再設定
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
  }
  /*------------------------------------------*/
  /*----- ドキュメント読み込み完了時     -----*/
  /*------------------------------------------*/
  $(document).ready(function(){
    //----------------------------------------------------------------------------
    init(); 									              //見積検索初期処理
    katabaninit();								          //製品型番検索初期処理
    optioninit();								            //オプション部品検索初期処理
    //----------------------------------------------------------------------------
    $("#knebiki").nebikichange();
    $("#kritu").nebikiperchange();
    $("#tankanebiki").tankanebikiclick();
    if (userkey.substring(0,2) == "03") {
      $("#kaizono").kaizonochange();
    }
    //----------------------------------------------------------------------------
    $("#kaizoadd").kaizoaddclick();				  //改造情報行追加ボタン
    $("#tokuadd").tokuaddclick();				    //特殊改造行追加ボタン
    $("#kataban-btn").katabansearch();			//型番検索ボタン
    $("#optioncommitbtn").optionkakutei();	//オプション部品確定ボタン
    //----------------------------------------------------------------------------
    $('#addbtn').addclick();                //「登録」ボタン
    $('#modbtn').modclick();                //「変更」ボタン
    $('#hachubtn').hachuclick();            //「変更(発注)」ボタン
    $('#hachudelbtn').hachudelclick();      //「発注取消」ボタン
    $('#clearbtn').clearclick();            //「クリア」ボタン
    $('#backbtn').backclick();					    //「戻る」ボタン
    $('#estdeletebtn').estimatedelete();		//「削除」ボタン
    $('.estimatebtn.print').estimateprint();//「見積依頼書」ボタン
    //----------------------------------------------------------------------------
    $('#button-modal').modal();					    //実行ボタンモーダル表示
  });
  /*------------------------------------------*/
  /*----- 日付イベント再設定         -----*/
  /*------------------------------------------*/
  function datepickerset() {
      $('.datepicker').pickadate({
          selectMonths: true, // Creates a dropdown to control month
          selectYears: 10, // Creates a dropdown of 15 years to control year,
          today: '本日',
          clear: 'クリア',
          close: '確定',
          container: undefined, // ex. 'body' will append picker to body
          closeOnSelect: true // Close upon selecting a date,
      });
  }
  /*------------------------------------------*/
  /*----- 金額算出イベント再設定         -----*/
  /*------------------------------------------*/
  function calcbtnset() {
      /*----- 改造イベントの定義 -----*/
      $(".kaizo").unbind("change");
      $(".kaizo").kaizochange();
      /*----- 数量イベントの定義 -----*/
      $(".suryo").unbind("change");
      $(".suryo").suryochange();
      /*----- 金額イベントの定義 -----*/
      $(".kingaku").unbind("change");
      $(".kingaku").kingakuchange();
      /*----- 金額再計算-----*/
      calculation();
  }
  /*------------------------------------------*/
  /*----- 削除ボタン再設定               -----*/
  /*------------------------------------------*/
  function deletebtnset() {
    $(".delete").unbind('click');
    $(".delete").deleteclick();
  }
  /*------------------------------------------*/
  /*----- 分納ボタン再設定               -----*/
  /*------------------------------------------*/
  function bunnobtnset() {
    $(".bunno").unbind('click');
    $(".bunno").bunnoclick();
  }
  /*------------------------------------------*/
  /*----- 特殊分納ボタン再設定           -----*/
  /*------------------------------------------*/
  function tbunnobtnset() {
    $(".tbunno").unbind('click');
    $(".tbunno").tbunnoclick();
  }
  /*------------------------------------------*/
  /*----- 製品型番モーダル設定           -----*/
  /*------------------------------------------*/
  function katabaninit() {
    $('#kataban-modal').modal({
      ready: function(modal, trigger) {
        searchcommit = false;                 //確定フラグを初期化
        targetkey    = $(trigger).attr("key");//処理対象キーを保持
        $("#katabans").val($("#kataban" + targetkey).val());
        if ($("#katabans").val() != "") {
          $("#kataban-btn").click();
        }
      },
      complete: function() {
        if (searchcommit) {                   //型番確定の場合
          //各項目に検索結果をバインドする
          $("#kataban"     + targetkey).val(searchdata.kataban);
          $("#kataban"     + targetkey).addClass('active');
          $("#kBousyoku"   + targetkey).text(searchdata.bousyoku.toLocaleString()   + ' 円');
          $("#kJubousyoku" + targetkey).text(searchdata.jubousyoku.toLocaleString() + ' 円');
          $("#kTaiengai"   + targetkey).text(searchdata.taiengai.toLocaleString()   + ' 円');
          $("#kTaijuengai" + targetkey).text(searchdata.taijuengai.toLocaleString() + ' 円');
          $("#kJubousabi"  + targetkey).text(searchdata.jubousei.toLocaleString()   + ' 円');
          $("#pBousyoku"   + targetkey).val('0');
          $("#pJubousyoku" + targetkey).val('0');
          $("#pTaiengai"   + targetkey).val('0');
          $("#pTaijuengai" + targetkey).val('0');
          $("#pJubousabi"  + targetkey).val('0');
          var matchData = getkaizouinfo(targetkey);
          matchData.kataban    = searchdata.kataban;
          matchData.bousyoku   = searchdata.bousyoku;
          matchData.jubousyoku = searchdata.jubousyoku;
          matchData.taiengai   = searchdata.taiengai;
          matchData.taijuengai = searchdata.taijuengai;
          matchData.jubousei   = searchdata.jubousei;
          updatekaizouinfo(targetkey,matchData);
        }
      }
    });
  }
  /*------------------------------------------*/
  /*----- オプション部品モーダル設定     -----*/
  /*------------------------------------------*/
  function optioninit() {
    $('#option-modal').modal({
      ready: function(modal, trigger) {
        searchcommit = false;                 //確定フラグを初期化
        targetkey    = $(trigger).attr("key");//処理対象キーを保持
        optionsearch();
      },
      complete: function() {
        if (searchcommit) {                   //オプション確定の場合
          var addjson = [];
          var optioncd = $(".optioncd");
          var outidx=0;
          for (var idx = 0;idx < optioncd.length; idx++) {
            var data = $(optioncd[idx])[0];
            if (data.checked) {
              var infojson = {"optioncd":$(data).attr("key")
                  , "optionname":$("#hoptionname" + $(data).attr("key")).val()
                  , "optiontanka":$("#hoptiontanka" + $(data).attr("key")).val()
                  , "mashine":$("#omashine" + $(data).attr("key")).val()
                  , "nyuka":$("#onyuka" + $(data).attr("key")).val()
                  , "kibou":$("#okibou" + $(data).attr("key")).val()};
              addjson.push(infojson);
            }
          }
          var optionjson = {"key":targetkey, "option": addjson}
          updateoptioninfo(targetkey, optionjson);
          calculation();
        }
      }
    });
  }
  /*------------------------------------------*/
  /*----- オプション部品検索             -----*/
  /*------------------------------------------*/
  function optionsearch() {

    var json = {"gno":targetkey};

    $.ajax({
      url: '/searchoption',
      type: 'POST',
      data: JSON.stringify(json),
      complete:function(data, status, jqXHR){         //処理成功時
        var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
        //----- オプション情報 ------
        CardOptionList($("#optionlist"));
        CardOptionList.clear();
        CardOptionList.put(jsonResult.datalist);
        //指定行のオプション情報を取得し、該当があればチェックする
        var optiondata = getoptioninfo(targetkey)[0];
        if (optiondata != undefined) {
          var optionlist = optiondata.option;
          for (var datakey in optionlist) {
            var data = optionlist[datakey];
            $("#optioncd" + data.optioncd)[0].checked = true;
          }
        }

      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });

  }
  /*------------------------------------------*/
  /*----- オプション部品確定クリック時   -----*/
  /*------------------------------------------*/
  $.fn.optionkakutei = function() {
    $(this).click(function() {
      searchcommit = true;
    });
  }
  /*----------------------------------------------------------*/
  /*----- 改造情報より指定行のデータを取得する           -----*/
  /*----------------------------------------------------------*/
  function getkaizouinfo(key) {
    var matchData = kaizouinfo.filter(function(item, index){
      if (item.key == key) return true;
    });
    return matchData;
  }
  /*----------------------------------------------------------*/
  /*----- 改造情報より指定行のデータを更新する           -----*/
  /*----------------------------------------------------------*/
  function updatekaizouinfo(key, item) {
    var newData = kaizouinfo.filter(function(item, index){
      if (item.key != key) return true;
    });
    newData.push(item);
    kaizouinfo = newData;
  }
  /*----------------------------------------------------------*/
  /*----- オプション情報より指定行のデータを取得する     -----*/
  /*----------------------------------------------------------*/
  function getoptioninfo(key) {
    var matchData = optioninfo.filter(function(item, index){
      if (item.key == key) return true;
    });
    return matchData;
  }
  /*----------------------------------------------------------*/
  /*----- オプション情報より指定行のデータを更新する     -----*/
  /*----------------------------------------------------------*/
  function updateoptioninfo(key, item) {
    var newData = optioninfo.filter(function(item, index){
      if (item.key != key) return true;
    });
    newData.push(item);
    optioninfo = newData;
  }
  /*----------------------------------------------------------*/
  /*----- オプション情報より指定行のデータを削除する     -----*/
  /*----------------------------------------------------------*/
  function deleteoptioninfo(key) {
    var newData = optioninfo.filter(function(item, index){
      if (item.key != key) return true;
    });
    optioninfo = newData;
  }
  /*------------------------------------------*/
  /*----- 改造変更時                     -----*/
  /*------------------------------------------*/
  $.fn.kaizochange = function() {
    $(this).change(function() {
      calculation();
    });
  }
  /*------------------------------------------*/
  /*----- 金額変更時                     -----*/
  /*------------------------------------------*/
  $.fn.kingakuchange = function() {
    $(this).change(function() {
      calculation();
    });
  }
  /*------------------------------------------*/
  /*----- 数量変更時                     -----*/
  /*------------------------------------------*/
  $.fn.suryochange = function() {
    $(this).change(function() {
      calculation();
    });
  }
  /*------------------------------------------*/
  /*----- 値引金額変更時                 -----*/
  /*------------------------------------------*/
  $.fn.nebikichange = function() {
    $(this).change(function() {
      calculation();
    });
  }
  /*------------------------------------------*/
  /*----- 値引率変更時                   -----*/
  /*------------------------------------------*/
  $.fn.nebikiperchange = function() {
    $(this).change(function() {
      calculation();
    });
  }
  /*------------------------------------------*/
  /*----- 単価値引クリック時             -----*/
  /*------------------------------------------*/
  $.fn.tankanebikiclick = function() {
    $(this).click(function() {
      tankanebiki();
      calculation();
      return false;
    });
  }
  /*------------------------------------------*/
  /*----- 改造承認No.変更時              -----*/
  /*------------------------------------------*/
  $.fn.kaizonochange = function() {
    $(this).change(function() {
      var oKaizoNo = $("kaizono");
      if (oKaizoNo.val() != "") { //改造承認No.入力時
        $("#hachubtn").show();
        $("#modbtn").hide();
      }
      else {
        $("#hachubtn").hide();
        $("#modbtn").show();
      }
    });
  }
  /*-------------------- 各種イベント --------------------*/
  /*------------------------------------------*/
  /*----- 金額算出                       -----*/
  /*------------------------------------------*/
  function calculation() {
    //----- 改造情報 -----
    var licount   = getlistCount("kaizolist");
    var tax       = 1 + ($("#tax").val() / 100);
    var total     = 0;
    var totalsuryo= 0;
    for (var idx = 0; idx < licount; idx++) {
      if($("#kataban" + idx).val() == "") {   //型番が未入力の場合は対象外
        continue;
      }
      var suryo = parseInt($("#suryo" + idx).val());
      if (suryo == undefined || suryo == 0) { //数量が入力されていない場合は対象外
        continue;
      }
      //----- 分納分を数量に加算 -----
      var oPreantCard = $("#kaizo" + idx);
      if (oPreantCard != undefined) {
        var bcount = oPreantCard.children("li").length;
        for (var key2 = 0; key2 < bcount; key2++) {
          var gkey = ((parseInt(idx) * 100) + parseInt(key2));
          var gsuryo = parseInt($("#bsuryo" + gkey).val());
          if (gsuryo != undefined && gsuryo != 0) { //数量が入力されていない場合は対象外
            suryo += gsuryo;
          }
        }
      }
      var tanka = kaizouinfo[idx];
      if ($("#bousyoku" + idx).prop("checked")) {
        if (parseInt($("#pBousyoku" + idx).val()) > 0) {
          total += parseInt($("#pBousyoku" + idx).val()) * suryo;
        }
        else {
          total += parseInt(tanka.bousyoku) * suryo;
        }
      }
      if ($("#Jubousyoku" + idx).prop("checked")) {
        if (parseInt($("#pJubousyoku" + idx).val()) > 0) {
          total += parseInt($("#pJubousyoku" + idx).val()) * suryo;
        }
        else {
          total += parseInt(tanka.jubousyoku) * suryo;
        }
      }
      if ($("#taiengai" + idx).prop("checked")) {
        if (parseInt($("#pTaiengai" + idx).val()) > 0) {
          total += parseInt($("#pTaiengai" + idx).val()) * suryo;
        }
        else {
          total += parseInt(tanka.taiengai) * suryo;
        }
      }
      if ($("#taijuengai" + idx).prop("checked")) {
        if (parseInt($("#pTaijuengai" + idx).val()) > 0) {
          total += parseInt($("#pTaijuengai" + idx).val()) * suryo;
        }
        else {
          total += parseInt(tanka.taijuengai) * suryo;
        }
      }
      if ($("#jubousabi" + idx).prop("checked")) {
        if (parseInt($("#pJubousabi" + idx).val()) > 0) {
          total += parseInt($("#pJubousabi" + idx).val()) * suryo;
        }
        else {
          total += parseInt(tanka.jubousei) * suryo;
        }
      }
      //----- 改造情報 -----
      //指定行のオプション情報を取得し、該当があれば金額加算する
      var optiondata = getoptioninfo(idx)[0];
      if (optiondata != undefined) {
        var optionlist = optiondata.option;
        for (var datakey in optionlist) {
          var data = optionlist[datakey];
          total += data.optiontanka * suryo;
        }
      }
      totalsuryo += parseInt(suryo); //数量を合計数量に加算
    }
    //----- 特殊改造 -----
    var tlicount   = getlistCount("tokusyulist");
    for (var idx = 0; idx < tlicount; idx++) {
      if($("#tkataban" + idx).val() == "") {   //型番が未入力の場合は対象外
        continue;
      }
      var suryo = parseInt($("#tsuryo" + idx).val());
      if (suryo == undefined || suryo == 0) { //数量が入力されていない場合は対象外
        continue;
      }
      //----- 分納分を数量に加算 -----
      var oPreantCard = $("#tokusyu" + idx);
      if (oPreantCard != undefined) {
        var bcount = oPreantCard.children("li").length;
        for (var key2 = 0; key2 < bcount; key2++) {
          var gkey = ((parseInt(idx) * 100) + parseInt(key2));
          var gsuryo = parseInt($("#btsuryo" + gkey).val());
          if (gsuryo != undefined && gsuryo != 0) { //数量が入力されていない場合は対象外
            suryo += gsuryo;
          }
        }
      }
      var tanka = $("#tkingaku" + idx).val();
      total += tanka * suryo;
      totalsuryo += parseInt(suryo); //数量を合計数量に加算
    }
    //----- 値引金額 -----
    var oKnebiki = $("#knebiki");
    var knebiki =0;
    if (oKnebiki.val() != "" && parseInt(oKnebiki.val()) != 0) {
        knebiki = parseInt(oKnebiki.val());
        if (knebiki > total) {
            Materialize.toast('値引金額が合計金額を超えています', 4000, 'rounded');
            oKnebiki.addClass('error-text');
            return false;
        }
        else {
        	oKnebiki.removeClass('error-text');
        	total = total - knebiki;
        }
    }
    else {
        var oKritu = $("#kritu");
        if (oKritu.val() != "" && parseInt(oKritu.val()) != 0) {
            if (parseInt(oKritu.val()) > 99) {
                Materialize.toast('100%以上の値引は行えません', 4000, 'rounded');
                oKritu.addClass('error-text');
                return false;
            }
        	oKnebiki.val(total * (parseInt(oKritu.val())/100));
        	total = total - oKnebiki.val();
        }
    	$("#knebiki").removeClass('error-text');
    }
    //----- 金額設定 -----
    var kingaku = Math.floor(total * tax);
    $("#khontai").text(total.toLocaleString() + ' 円');                //本体価格
    $("#kmitusanko").text((total + knebiki).toLocaleString()  + ' 円');//見積合計(参考)
    $("#kmitumori").text(kingaku.toLocaleString() + ' 円');            //最終見積合計金額
    $("#kshouhizei").text((kingaku - total).toLocaleString() + ' 円'); //消費税
    khontai   = total;                                                 //本体価格
    kmitumori = kingaku;                                               //最終見積額
    ktax      = (kingaku - total);                                     //消費税
    //-----単価値引情報設定-----
    if (knebiki > 0 && totalsuryo > 0) {
      $("#sankotanka").text((knebiki / totalsuryo).toLocaleString() + ' 円');
    }
  }
  /*------------------------------------------*/
  /*----- 単価値引                       -----*/
  /*------------------------------------------*/
  function tankanebiki() {
    //----- 改造情報 -----
    var licount   = getlistCount("kaizolist");
    var tankanebi = parseInt($("#ktanka").val());
    if (tankanebi <= 0) {
      return false;
    }

    for (var idx = 0; idx < licount; idx++) {
      if($("#kataban" + idx).val() == "") {   //型番が未入力の場合は対象外
        continue;
      }
      var tanka = kaizouinfo[idx];
      if ($("#bousyoku" + idx).prop("checked")) {
          $("#pBousyoku" + idx).val(parseInt(tanka.bousyoku) - tankanebi);
      }
      if ($("#Jubousyoku" + idx).prop("checked")) {
        $("#pJubousyoku" + idx).val(parseInt(tanka.jubousyoku) - tankanebi);
      }
      if ($("#taiengai" + idx).prop("checked")) {
        $("#pTaiengai" + idx).val(parseInt(tanka.taiengai) - tankanebi);
      }
      if ($("#taijuengai" + idx).prop("checked")) {
        $("#pTaijuengai" + idx).val(parseInt(tanka.taijuengai) - tankanebi);
      }
      if ($("#jubousabi" + idx).prop("checked")) {
        $("#pJubousabi" + idx).val(parseInt(tanka.jubousei) - tankanebi);
      }
    }
    //----- 特殊改造 -----
    var tlicount   = getlistCount("tokusyulist");
    for (var idx = 0; idx < tlicount; idx++) {
      if($("#tkataban" + idx).val() == "") {   //型番が未入力の場合は対象外
        continue;
      }
      var tanka = parseInt($("#tkingaku" + idx).val());
      if (tanka > 0) {
        $("#ptkingaku" + idx).val((tanka - tankanebi));
      }
    }
  }
  /*------------------------------------------*/
  /*----- 改造情報追加ボタンクリック時   -----*/
  /*------------------------------------------*/
  $.fn.kaizoaddclick = function() {
    $(this).click(function() {
      CardKaizoList.add();
      calcbtnset();
      deletebtnset();
      //----- 制御情報の反映 -----
      ContorolInfo.set(1, userkey);
      /*----- 日付オブジェクト再設定 -----*/
      datepickerset();
    });
  }
  /*------------------------------------------*/
  /*----- 特殊改造追加ボタンクリック時   -----*/
  /*------------------------------------------*/
  $.fn.tokuaddclick = function() {
    $(this).click(function() {
      CardTokuList.add();
      deletebtnset();
      calcbtnset();
      //----- 制御情報の反映 -----
      ContorolInfo.set(2, userkey);
      /*----- 日付オブジェクト再設定 -----*/
      datepickerset();
    });
  }
  /*------------------------------------------*/
  /*----- 削除ボタンクリック時           -----*/
  /*------------------------------------------*/
  $.fn.deleteclick = function() {
    $(this).click(function() {
      var key = $(this).attr('key');
      swal({title: "確認",
        text: "選択されたカードを削除しますか？",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "はい",
        cancelButtonText: "いいえ",
        html: true,
        closeOnConfirm: true },
        function(){
          var oCard = $('#' + key);
          if (oCard != undefined) {
            oCard.remove();
          }
          /*----- 金額再計算-----*/
          deleteoptioninfo(key);
          calculation();
        });
    });
  }
  /*------------------------------------------*/
  /*----- 未受注確定ボタンクリック時     -----*/
  /*------------------------------------------*/
  $.fn.nocommitclick = function() {
    $(this).click(function() {
      $("#mikakutei").hide();
      $("#kakutei").show();
    });
  }
  /*------------------------------------------*/
  /*----- 受注確定ボタンクリック時       -----*/
  /*------------------------------------------*/
  $.fn.commitclick = function() {
    $(this).click(function() {
      $("#mikakutei").show();
      $("#kakutei").hide();
    });
  }
  /*------------------------------------------*/
  /*----- 分納ボタンクリック時           -----*/
  /*------------------------------------------*/
  $.fn.bunnoclick = function() {
    $(this).click(function() {
      CardKaizoList.bunno($(this).attr("key"));
      deletebtnset(); //行削除ボタン
      datepickerset();//日付オブジェクト再設定
      calcbtnset();   //金額算出イベント再設定
    });
  }
  /*------------------------------------------*/
  /*----- 特殊改造分納ボタンクリック時   -----*/
  /*------------------------------------------*/
  $.fn.tbunnoclick = function() {
    $(this).click(function() {
      CardTokuList.bunno($(this).attr("key"));
      deletebtnset(); //行削除ボタン
      datepickerset();//日付オブジェクト再設定
      calcbtnset();   //金額算出イベント再設定
    });
  }
  /*------------------------------------------*/
  /*----- エラーチェック                 -----*/
  /*------------------------------------------*/
  function errorCheck() {
    //エラーテキストクリア
    ContorolInfo.clearError();
    //必須チェック
    if (!ContorolInfo.checkRequired(userkey)) {
      return false;
    }
    //改造情報の必須チェック
    if (!kaizoRequired(userkey)) {
        return false;
    }
    //特殊改造の必須チェック
    if (!tokuRequired(userkey)) {
        return false;
    }
    //単項目チェック
    if (!ContorolInfo.singleCheck(0)) {
      return false;
    }
    if (!ContorolInfo.singleCheck(1)) {
      return false;
    }
    if (!ContorolInfo.singleCheck(2)) {
      return false;
    }
    if (!ContorolInfo.singleCheck(3)) {
      return false;
    }
    return true;
  }
  /*------------------------------------------*/
  /*----- 改造情報必須チェック           -----*/
  /*------------------------------------------*/
  function kaizoRequired(controlKey) {
	var result = true;
	var kaizoList 	= ContorolInfo.getInfo(1);
	var licount 	= getlistCount("kaizolist");
	for (var idx = 0; idx < licount; idx++) {
		if($("#kataban" + idx).val() == "") { //型番が未入力の場合はノーチェックとする
			continue;
		}
		for (var key in kaizoList) {
			var data 	= kaizoList[key];
			var control = ContorolInfo.getControl(data.control, controlKey);
			if(control == undefined) { //指定したキー制御情報が存在しない場合
				control = ContorolInfo.getControl(data.control, "0000"); //デフォルトキーで再検索
				if(control == undefined) { //デフォルトキー制御情報が存在しない場合
					continue;
				}
			}
			if(control.required && (data.item != "kataban")) {
		      var item = $("#" + data.item + idx);
		      if (item.val() == "") { //対象項目が未入力の場合
		        Materialize.toast('必須入力項目です。改造情報(' + $("#" + data.item + idx + "-l").attr("data") + '(' + (idx + 1) + '行目))', 4000, 'rounded');
		        item.addClass('error-text');
		        item.select();
		        result = false;
		        break;
		      }
			}
		}
	}
	return result;
  }
  /*------------------------------------------*/
  /*----- 特殊改造必須チェック           -----*/
  /*------------------------------------------*/
  function tokuRequired(controlKey) {
	var result = true;
	var tokuList 	= ContorolInfo.getInfo(2);
	var licount 	= getlistCount("tokusyulist");
	for (var idx = 0; idx < licount; idx++) {
		if($("#tkataban" + idx).val() == "") { //型番が未入力の場合はノーチェックとする
			continue;
		}
		for (var key in tokuList) {
			var data 	= tokuList[key];
			var control = ContorolInfo.getControl(data.control, controlKey);
			if(control == undefined) { //指定したキー制御情報が存在しない場合
				control = ContorolInfo.getControl(data.control, "0000"); //デフォルトキーで再検索
				if(control == undefined) { //デフォルトキー制御情報が存在しない場合
					continue;
				}
			}
			if(control.required && (data.item != "tkataban")) {
		      var item = $("#" + data.item + idx);
		      if (item.val() == "") { //対象項目が未入力の場合
		        Materialize.toast('必須入力項目です。特殊改造(' + $("#" + data.item + idx + "-l").attr("data") + '(' + (idx + 1) + '行目))', 4000, 'rounded');
		        item.addClass('error-text');
		        item.select();
		        result = false;
		        break;
		      }
			}
		}
	}
	return result;
  }
  /*-------------------- 実行ボタンイベント --------------------*/
  /*------------------------------------------*/
  /*----- 登録ボタンクリック時           -----*/
  /*------------------------------------------*/
  $.fn.addclick = function() {
    $(this).click(function() {
      if (!errorCheck()) {
        $('#button-modal').modal('close');
        return;
      }
      swal({title: "確認",
        text: "この見積件名を登録します。<br />よろしいですか？",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "はい",
        cancelButtonText: "いいえ",
        html: true,
        closeOnConfirm: true },
        function(){
          estimateUpdate();
        });
    });
  }
  /*------------------------------------------*/
  /*----- 変更ボタンクリック時           -----*/
  /*------------------------------------------*/
  $.fn.modclick = function() {
    $(this).click(function() {
      if (!errorCheck()) {
        $('#button-modal').modal('close');
        return;
      }
      swal({title: "確認",
        text: "この見積件名を変更します。<br />よろしいですか？",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "はい",
        cancelButtonText: "いいえ",
        html: true,
        closeOnConfirm: true },
        function(){
          estimateUpdate();
        });
    });
  }
  /*------------------------------------------*/
  /*----- 変更(発注)ボタンクリック時     -----*/
  /*------------------------------------------*/
  $.fn.hachuclick = function() {
    $(this).click(function() {
      if (!errorCheck()) {
        $('#button-modal').modal('close');
        return;
      }
      var update = false;
      $("#hachu").val("0");
      if(window.confirm('この見積件名を変更します。\nよろしいですか？')) {
        update = true;
        if(window.confirm('この内容で注文を確定しますか？\n（変更のみの場合は「いいえ」を選択して下さい）')){
            $("#hachu").val("1");
        }
        if (update) {
          estimateUpdate();
        }
      }
    });
  }
  /*------------------------------------------*/
  /*----- 発注取消ボタンクリック時       -----*/
  /*------------------------------------------*/
  $.fn.hachudelclick = function() {
    $(this).click(function() {
      swal({title: "確認",
        text: "この注文を取消しますか？",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "はい",
        cancelButtonText: "いいえ",
        html: true,
        closeOnConfirm: true },
        function(){
          $("#hachu").val("0");
          estimateUpdate();
        });
    });
  }
  /*------------------------------------------*/
  /*----- 見積件名を反映します           -----*/
  /*------------------------------------------*/
  function estimateUpdate() {
    //----- 金額算出 -----
    calculation()
    //----- 改造情報 -----
    var kaizojson = [];
    CardKaizoList.get(kaizojson);
    //----- 特殊改造 -----
    var tokujson = [];
    CardTokuList.get(tokujson);
    //----- 受注確定 -----
    //受注確定の状態を取得
    var kakuteiBtn = $('#kakutei').is(':visible');
    var kakutei    = "0";
    if (kakuteiBtn) {
      kakutei = "1";
    }
    //----- 基本情報：備考情報 -----
    var json = {
        "kanri":$("#kanri").val()
        ,"kouji":$("#kouji").val()
        ,"haturei":$("#haturei").val()
        ,"irainouki":$("#irainouki").val()
        ,"kaitounouki":$("#kaitounouki").val()
        ,"tokuicode":$("#tokuicode").val()
        ,"tokuiname":$("#tokuiname").val()
        ,"postcode":$("#postcode").val()
        ,"addr1":$("#addr1").val()
        ,"addr2":$("#addr2").val()
        ,"tel":$("#tel").val()
        ,"fax":$("#fax").val()
        ,"kaizono":$("#kaizono").val()
        ,"koubaino":$("#koubaino").val()
        ,"psitenname":$("#psitenname").val()
        ,"ptantoname":$("#ptantoname").val()
        ,"kaizoseikyu":$("#kaizoseikyu").val()
        ,"unchin":$("#unchin").val()
        ,"panel":$("#panel").val()
        ,"setti":$("#setti").val()
        ,"biko1":$("#biko1").val()
        ,"biko2":$("#biko2").val()
        ,"biko3":$("#biko3").val()
        ,"koutei1":$("#koutei1").val()
        ,"koutei2":$("#koutei2").val()
        ,"koutei3":$("#koutei3").val()
        ,"kakutei":kakutei
        ,"hachu":$("#hachu").val()
        ,"kaizo":kaizojson
        ,"option":optioninfo
        ,"toku":tokujson
        ,"kmitumori":kmitumori
        ,"khontai":khontai
        ,"kshouhizei":ktax
        ,"mtmno":$("#hmtmno").val()
        ,"edaban":$("#hedaban").val()
    };
    $.ajax({
      url: '/estimatecommit',
      type: 'POST',
      data: JSON.stringify(json),
      complete:function(data, status, jqXHR){               //処理成功時
        var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
        if (jsonResult.result == "success") {
          if (jsonResult.hachuok) {
            Materialize.toast('御注文承りました。ありがとうございました', 4000, 'rounded');
          }
          else {
            Materialize.toast('見積件名の反映が完了しました', 4000, 'rounded');
          }
          init();                                 //見積検索初期処理
        }
        else {
          Materialize.toast('見積件名の反映に失敗しました', 4000, 'rounded');
        }
        $('#button-modal').modal('close');
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
  }
  /*------------------------------------------*/
  /*----- 見積件名を削除します           -----*/
  /*------------------------------------------*/
  $.fn.estimatedelete = function() {
    $(this).click(function() {
      swal({title: "確認",
        text: "この見積件名を削除してもよろしいですか？",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "はい",
        cancelButtonText: "いいえ",
        html: true,
        closeOnConfirm: true },
        function(){
          $.ajax({
            url: '/estimatedelete',
            type: 'GET',
            complete:function(data, status, jqXHR){         //処理成功時
              var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
              if (jsonResult.result == "success") {
                Materialize.toast('見積件名を削除しました', 4000, 'rounded');
                if (estimatemode == "Retrieval") {
                  window.location = "/Retrieval/moveretrieval";
                }
                else {
                  window.location = "/movemenu";
                }
              }
              else {
                Materialize.toast('見積件名の削除に失敗しました', 4000, 'rounded');
              }
            },
            dataType:'json',
            contentType:'text/json',
            async: false
          });
        });
    });
  }
  /*------------------------------------------*/
  /*----- 見積書を印刷します             -----*/
  /*------------------------------------------*/
  $.fn.estimateprint = function() {
    $(this).click(function() {

        var oPrint = $(this);

        var json = {"printid": oPrint.attr("printid")};

        $.ajax({
          url: '/estimateprint',
          type: 'POST',
          data: JSON.stringify(json),
          complete:function(data, status, jqXHR){         //処理成功時
            var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
            var printdata = "";

            for (var i=1; i<=jsonResult.printcount; i++) {
            	printdata += jsonResult["print" + i];
            }

            switch (jsonResult.printid) {
			case "C010500010":
	            oPrint.attr("download", "納品仕様書.xls");
            	oPrint.attr("href", jsonResult.dataurl);
				break;

			default:
	            oPrint.attr("download", jsonResult.printtitle + ".pxd");
            	oPrint.attr("href", "data:application/octet-stream;," + encodeURIComponent(printdata));
				break;
			}

          },
          dataType:'json',
          contentType:'text/json',
          async: false
        });
    });
  }
  /*------------------------------------------*/
  /*----- クリアボタンクリック時         -----*/
  /*------------------------------------------*/
  $.fn.clearclick = function() {
    $(this).click(function() {
      swal({title: "確認",
        text: "見積画面表示時の状態に戻しますか？<br />(現在入力中の情報は破棄されます)",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "はい",
        cancelButtonText: "いいえ",
        html: true,
        closeOnConfirm: true },
        function(){
          //見積作成画面に遷移しなおす事で保持しているセッション情報より初期化する
          window.location = "/moveestimate";
        });
    });
  }
  /*------------------------------------------*/
  /*----- 戻るボタンクリック時           -----*/
  /*------------------------------------------*/
  $.fn.backclick = function() {
    $(this).click(function() {
      swal({title: "確認",
        text: "前画面に戻りますか？<br />(登録／変更を行ってない場合、<br />現在入力中の情報は破棄されます)",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "はい",
        cancelButtonText: "いいえ",
        html: true,
        closeOnConfirm: true },
        function(){
          if (estimatemode == "Retrieval") {
            window.location = "/Retrieval/moveretrieval";
          }
          else {
            window.location = "/movemenu";
          }
        });
    });
  }
  /*-------------------- 入力チェック等 --------------------*/
  /*------------------------------------------*/
  /*----- 基本情報チェック               -----*/
  /*------------------------------------------*/
  function checkKihon() {

    //----- 見積要求納期 -----
    if (!checkFuture("irainouki")) {
      return false;
    }
    //----- 見積回答納期 -----
    if (checkFuture("kaitounouki")) {
      return false;
    }
    return true;
  }
  /*------------------------------------------*/
  /*----- 改造情報チェック               -----*/
  /*------------------------------------------*/
  function checkKaizo() {

    var kaizolist = $("#kaizolist").children("li");
    var licount   = kaizolist.length;

    for (var key = 0; key < licount; key++) {
      //----- 製品型番のチェック -----
      var oKataban = $("#kataban" + key);
      if (oKataban.val() != "") {
        singleRequired("suryo" , key);
        if (!checkKataban(oKataban.val())) { //製品型番存在チェック
          Materialize.toast('製品型番が存在しません(' + $("#kataban" + key +"-l").attr("data") + ')', 4000, 'rounded');
          oKataban.addClass('error-text');
          oKataban.select();
        }
      }
    }

  }
  /*------------------------------------------*/
  /*----- 製品型番存在チェック           -----*/
  /*------------------------------------------*/
  function checkKataban(kataban) {

    var json = {"kataban":kataban};

    $.ajax({
      url: '/checkkataban',
      type: 'POST',
      data: JSON.stringify(json),
      complete:function(data, status, jqXHR){         //処理成功時
        var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
        if (jsonResult.result == "notfound") {
          return false;
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
    return true;
  }
  /*-------------------- モーダルイベント --------------------*/
  /*------------------------------------------*/
  /*----- 製品型番検索                   -----*/
  /*------------------------------------------*/
  $.fn.katabansearch = function() {
    $(this).click(function() {
      var katabans = new String($("#katabans").val());

      var json = {"kataban":katabans};

      $.ajax({
        url: '/searchkataban',
        type: 'POST',
        data: JSON.stringify(json),
        complete:function(data, status, jqXHR){         //処理成功時
          var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
          if (jsonResult.result == "notfound") {
            Materialize.toast('入力された製品型番は存在しません', 4000, 'rounded');
            return false;
          }
          var datalist = jsonResult.datalist;
          searchlist = datalist;  //インスタンスにJSONを保持
          var json = [];
          if (jsonResult.count == 1) {	//検索結果が一件の場合は即反映
            for (var key in datalist) {
              var data = datalist[key];
              //インスタンス変数に出力する
              searchdata = {
                  "kataban":data.kataban
                  ,"bousyoku":data.bousyoku
                  ,"jubousyoku":data.jubousyoku
                  ,"taiengai":data.taiengai
                  ,"taijuengai":data.taijuengai
                  ,"jubousei":data.jubousei
              };
              searchcommit = true; //確定フラグを有効化
              $('#kataban-modal').modal('close');
              break;
            }
          }
          else {
            for (var key in datalist) {
              var data = datalist[key];
              var jd = {
                  "製品型番":data.kataban
                  ,"防蝕単価":data.bousyoku.toLocaleString()
                  ,"重防蝕単価":data.jubousyoku.toLocaleString()
                  ,"耐塩害単価":data.taiengai.toLocaleString()
                  ,"耐重塩害単価":data.taijuengai.toLocaleString()
                  ,"重防錆単価":data.jubousei.toLocaleString()
              };
              json.push(jd);
            }
            // JSON配列をGRIDにバインド
            $("#jsGridKataban").jsGrid({
              height: "100%",
              width: "100%",
              autoload: true,
              selecting: false,
              shrinkToFit : true,
              data: json,
              fields: [{
                name: "製品型番",
                type: "text",
                align: "center",
                width: 10,
                itemTemplate: function(value) {
                  return $('<a href="#" class="katabansearch" kataban="' + value + '">' + value + '</a>');
                }
              }
              ,{
                name: "防蝕単価",
                type: "text",
                align: "right",
                width: 12
              }
              ,{
                name: "重防蝕単価",
                type: "text",
                align: "right",
                width: 12
              }
              ,{
                name: "耐塩害単価",
                type: "text",
                align: "right",
                width: 12
              }
              ,{
                name: "耐重塩害単価",
                type: "text",
                align: "right",
                width: 12
              }
              ,{
                name: "重防錆単価",
                type: "text",
                align: "right",
                width: 12
              }
              ]
            });
            //製品型番クリックイベントを設定
            $(".katabansearch").unbind('click');
            $(".katabansearch").katabanclick();
          }
        },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
    });
    return false;
  }
  /*------------------------------------------*/
  /*----- 型番クリック                   -----*/
  /*------------------------------------------*/
  $.fn.katabanclick = function() {
    $(this).click(function() {
      var kataban = $(this).attr("kataban");  //型番を取得
      //----- 保持していた検索結果から対象の型番を検索する -----
      for (var key in searchlist) {
        var data = searchlist[key];
        if (kataban == data.kataban) {
          //インスタンス変数に出力する
          searchdata = {
              "kataban":data.kataban
              ,"bousyoku":data.bousyoku
              ,"jubousyoku":data.jubousyoku
              ,"taiengai":data.taiengai
              ,"taijuengai":data.taijuengai
              ,"jubousei":data.jubousei
          };
          searchcommit = true; //確定フラグを有効化
          break;
        }
      }
      $('#kataban-modal').modal('close');
    });
  }
  /*-------------------- 改造情報専用カードリスト --------------------*/
  var oCardKaizoList; //カードリストオブジェクト

  /*------------------------------------------*/
  /*----- カードリストオブジェクト生成   -----*/
  /*------------------------------------------*/
  var CardKaizoList = function(list) { //
    oCardKaizoList = list;
    oCardKaizoList.addClass('kaizo-list');
  }
  /*------------------------------------------*/
  /*----- 要素を全て削除します           -----*/
  /*------------------------------------------*/
  CardKaizoList.clear = function() {
    if (oCardKaizoList != undefined) {
      oCardKaizoList.empty();
      kaizouinfo = [];
    }
  }
  /*------------------------------------------*/
  /*----- 要素を表示します               -----*/
  /*------------------------------------------*/
  CardKaizoList.put = function(json) {
    var idx = 0;
    if (oCardKaizoList != undefined) {
      for (var key in json) {
    	//----- 改造情報のJSON化 -----
        var data = json[key];
        var addJson = {"key":key, "kataban":data.kataban, "bousyoku":data.kBousyoku, "jubousyoku":data.kJubousyoku, "taiengai":data.kTaiengai, "taijuengai":data.kTaijuengai, "jubousei":data.kJubousabi};
        kaizouinfo.push(addJson);
    	//----- オプション改造のJSON化 -----
        var datajson = [];
        for (var idx in data.option.option) {
        	var opdata = data.option.option[idx];
            var infojson = {"optioncd":opdata.optioncd
                    , "optionname":opdata.optionname
                    , "optiontanka":opdata.optiontanka
                    , "mashine":opdata.mashine
                    , "nyuka":opdata.nyuka
                    , "kibou":opdata.kibou};
            datajson.push(infojson);
        }
        var optionjson = {"key":key, "option": datajson}
        optioninfo.push(optionjson);

        var oString = '<li class="card-panel" id="kaizo' + key + '">'
        + '<div class="row">'
        + '<div class="input-field col s12 l4">'
        + '<input id="kataban' + key + '"  name="kataban" type="text" placeholder="XXXXXXXXXXXXXXXXXXXX" value="' + data.kataban + '">'
        + '<label for="kataban' + key + '" class="center-align active" id="kataban' + key + '-l" data="製品型番">製品型番</label>'
        + '</div>'
        + '<div class="col s6 l1 center">'
        + '<p>'
        + '<div class="chip cyan white-text z-depth-2 stts-chip" id="seihin' + key + '">'
        + '<a href="#kataban-modal" class="white-text modal-trigger" key="' + key + '">製品</a>'
        + '</div>'
        + '</p>'
        + '</div>'
        + '<div class="col s6 l1 center">'
        + '<p>'
        + '<div class="chip cyan white-text z-depth-2 stts-chip bunno" id="bunno' + key + '" key="' + key + '">'
        + '<a href="#" class="white-text">分納</a>'
        + '</div>'
        + '</p>'
        + '</div>'
        + '<div class="col s6 l2 center">'
        + '<p>'
        + '<div class="chip cyan white-text z-depth-2 stts-chip" id="option' + key + '">'
        + '<a href="#option-modal" class="white-text modal-trigger" key="' + key + '">オプション</a>'
        + '</div>'
        + '</p>'
        + '</div>'
        + '<div class="col s12 offset-l3 l1">'
        + '<p>'
        + '<a href="#" class="delete btn-floating waves-effect waves-light red white-text" id="delete' + key + '" key="kaizo' + key + '"><i class="material-icons prefix">clear</i></a>'
        + '</p>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="col s6 l4">'
        + '<p>'
        + '<input id="bousyoku' + key + '"  name="bousyoku" class="kaizo" type="checkbox" ' + data.bousyoku + '>'
        + '<label for="bousyoku' + key + '" class="left-align">防蝕</label>'
        + '</p>'
        + '</div>'
        + '<div class="col s6 l2">'
        + '<p>'
        + '<span id="kBousyoku' + key + '">' + ('            ' + data.kBousyoku.toLocaleString()).slice( -12 ) + ' 円</span>'
        + '</p>'
        + '</div>'
        + '<div class="col s12 l3">'
        + '<div class="" style="">'
        + '<input id="pBousyoku' + key + '"  name="pBousyoku" type="text" placeholder="帳票用金額" class="right-text" value="' + data.pBousyoku + '">'
        + '</div>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="col s6 l4">'
        + '<p>'
        + '<input id="Jubousyoku' + key + '"  name="Jubousyoku" class="kaizo" type="checkbox"' + data.Jubousyoku + '>'
        + '<label for="Jubousyoku' + key + '" class="left-align">重防蝕</label>'
        + '</p>'
        + '</div>'
        + '<div class="col s6 l2">'
        + '<p>'
        + '<span id="kJubousyoku' + key + '">' + ('            ' + data.kJubousyoku.toLocaleString()).slice( -12 ) + ' 円</span>'
        + '</p>'
        + '</div>'
        + '<div class="col s12 l3">'
        + '<div class="" style="">'
        + '<input id="pJubousyoku' + key + '"  name="pJubousyoku" type="text" placeholder="帳票用金額" class="right-text" value="' + data.pJubousyoku + '">'
        + '</div>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="col s6 l4">'
        + '<p>'
        + '<input id="taiengai' + key + '"  name="taiengai" class="kaizo" type="checkbox"' + data.taiengai + '>'
        + '<label for="taiengai' + key + '" class="left-align">耐塩害</label>'
        + '</p>'
        + '</div>'
        + '<div class="col s6 l2">'
        + '<p>'
        + '<span id="kTaiengai' + key + '">' + ('            ' + data.kTaiengai.toLocaleString()).slice( -12 ) + ' 円</span>'
        + '</p>'
        + '</div>'
        + '<div class="col s12 l3">'
        + '<div class="" style="">'
        + '<input id="pTaiengai' + key + '"  name="pTaiengai" type="text" placeholder="帳票用金額" class="right-text" value="' + data.pTaiengai + '">'
        + '</div>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="col s6 l4">'
        + '<p>'
        + '<input id="taijuengai' + key + '"  name="taijuengai" class="kaizo" type="checkbox"' + data.taijuengai + '>'
        + '<label for="taijuengai' + key + '" class="left-align">耐重塩害</label>'
        + '</p>'
        + '</div>'
        + '<div class="col s6 l2">'
        + '<p>'
        + '<span id="kTaijuengai' + key + '">' + ('            ' + data.kTaijuengai.toLocaleString()).slice( -12 ) + ' 円</span>'
        + '</p>'
        + '</div>'
        + '<div class="col s12 l3">'
        + '<div class="" style="">'
        + '<input id="pTaijuengai' + key + '"  name="pTaijuengai" type="text" placeholder="帳票用金額" class="right-text" value="' + data.pTaijuengai + '">'
        + '</div>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="col s6 l4">'
        + '<p>'
        + '<input id="jubousabi' + key + '"  name="jubousabi" class="kaizo" type="checkbox"' + data.jubousabi + '>'
        + '<label for="jubousabi' + key + '" class="left-align">重防錆</label>'
        + '</p>'
        + '</div>'
        + '<div class="col s6 l2">'
        + '<p>'
        + '<span id="kJubousabi' + key + '">' + ('            ' + data.kJubousabi.toLocaleString()).slice( -12 ) + ' 円</span>'
        + '</p>'
        + '</div>'
        + '<div class="col s12 l3">'
        + '<div class="" style="">'
        + '<input id="pJubousabi' + key + '"  name="pJubousabi" type="text" class="right-text" placeholder="帳票用金額" value="' + data.pJubousabi + '">'
        + '</div>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="input-field col s6 l4">'
        + '<input id="mashine' + key + '"  name="mashine" type="text" class="right-text" placeholder="XXXXXXXXXX" value="' + data.mashine + '">'
        + '<label for="mashine' + key + '" class="center-align active" id="mashine' + key + '-l" data="マシンNo.">マシンNo.</label>'
        + '</div>'
        + '<div class="input-field col s6 l2">'
        + '<input id="suryo' + key + '"  name="suryo" type="text" class="right-text suryo" placeholder="99" value="' + data.suryo + '">'
        + '<label for="suryo' + key + '" class="center-align active" id="suryo' + key + '-l" data="数量">数量</label>'
        + '</div>'
        + '<div class="input-field col s12 l6">'
        + '<input id="nyuka' + key + '"  name="nyuka" type="text" placeholder="yyyy/mm/dd" value="' + data.nyuka + '" class="datepicker">'
        + '<label for="nyuka' + key + '" class="center-align active" id="nyuka' + key + '-l" data="入荷予定日">入荷予定日</label>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="input-field col s12 l6">'
        + '<input id="kibou' + key + '"  name="kibou" type="text" placeholder="yyyy/mm/dd" value="' + data.kibou + '" class="datepicker">'
        + '<label for="kibou' + key + '" class="center-align active" id="kibou' + key + '-l" data="入荷希望日">入荷希望日</label>'
        + '</div>'
        + '<div class="input-field col s12 l6">'
        + '<input id="hikitori' + key + '"  name="hikitori" type="text" placeholder="yyyy/mm/dd" value="' + data.hikitori + '" class="datepicker">'
        + '<label for="hikitori' + key + '" class="center-align active" id="hikitori' + key + '-l" data="引取希望日">引取希望日</label>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="input-field col s12 l6">'
        + '<input id="haise' + key + '"  name="haise" type="text" placeholder="yyyy/mm/dd" value="' + data.haise + '" class="datepicker">'
        + '<label for="haise' + key + '" class="center-align active" id="haise' + key + '-l" data="配セ戻入日">配セ戻入日</label>'
        + '</div>'
        + '<div class="input-field col s12 l6">'
        + '<input id="kend' + key + '"  name="kend" type="text" placeholder="yyyy/mm/dd" value="' + data.kend + '" class="datepicker">'
        + '<label for="kend' + key + '" class="center-align active" id="kend' + key + '-l" data="製造完了予定日">製造完了予定日</label>'
        + '</div>'
        + '</div>'
        + '</li>';
        oCardKaizoList.append(oString);
        var bunnolist = data.bunnoinfo;
        for (var key2 in bunnolist) {
          //----- 改造情報のJSON化 -----
            var data2 = bunnolist[key2];
            var oPreantCard = $("#kaizo" + key);
            var gkey = ((parseInt(key) * 100) + parseInt(key2));
            if (oPreantCard != undefined) {
              var oString = '<li class="card-panel bunno" id="bkaizo' + gkey + '" preantkey="#kaizo' + key + '">'
              + '<input id="bkataban' + gkey + '"type="hidden" value="' + $("#kataban" + key).val() + '">'
              + '<div class="row">'
              + '<div class="input-field col s12 l4">'
              + '<p>'
              + '</p>'
              + '</div>'
              + '<div class="col s6 l1 center">'
              + '<p>'
              + '</p>'
              + '</div>'
              + '<div class="col s6 l1 center">'
              + '<p>'
              + '</p>'
              + '</div>'
              + '<div class="col s6 l2 center">'
              + '<p>'
              + '</p>'
              + '</div>'
              + '<div class="col s12 offset-l3 l1">'
              + '<p>'
              + '<a href="#" class="delete btn-floating waves-effect waves-light red white-text" id="bdelete' + gkey + '" key="bkaizo' + gkey + '"><i class="material-icons prefix">clear</i></a>'
              + '</p>'
              + '</div>'
              + '</div>'
              + '<div class="row">'
              + '<div class="input-field col s6 l4">'
              + '<input id="bmashine' + gkey + '"  name="bmashine" type="text" placeholder="XXXXXXXXXX" value="' + data2.mashine +'">'
              + '<label for="bmashine' + gkey + '" class="center-align active" id="bmashine' + gkey + '-l" data="マシンNo.">マシンNo.</label>'
              + '</div>'
              + '<div class="input-field col s6 l2">'
              + '<input id="bsuryo' + gkey + '"  name="bsuryo" type="text" class="right-text suryo" placeholder="99" value="' + data2.suryo +'">'
              + '<label for="bsuryo' + gkey + '" class="center-align active" id="bsuryo' + gkey + '-l" data="数量">数量</label>'
              + '</div>'
              + '<div class="input-field col s12 l6">'
              + '<input id="bnyuka' + gkey + '"  name="bnyuka" type="text" value="' + data2.nyuka +'" placeholder="yyyy/mm/dd" class="datepicker">'
              + '<label for="bnyuka' + gkey + '" class="center-align active" id="bnyuka' + gkey + '-l" data="入荷予定日">入荷予定日</label>'
              + '</div>'
              + '</div>'
              + '<div class="row">'
              + '<div class="input-field col s12 l6">'
              + '<input id="bkibou' + gkey + '"  name="bkibou" type="text" value="' + data2.kibou +'" placeholder="yyyy/mm/dd" class="datepicker">'
              + '<label for="bkibou' + gkey + '" class="center-align active" id="bkibou' + gkey + '-l" data="入荷希望日">入荷希望日</label>'
              + '</div>'
              + '<div class="input-field col s12 l6">'
              + '<input id="bhikitori' + gkey + '"  name="bhikitori" type="text" value="' + data2.hikitori +'" placeholder="yyyy/mm/dd" class="datepicker">'
              + '<label for="bhikitori' + gkey + '" class="center-align active" id="bhikitori' + gkey + '-l" data="引取希望日">引取希望日</label>'
              + '</div>'
              + '</div>'
              + '<div class="row">'
              + '<div class="input-field col s12 l6">'
              + '<input id="bhaise' + gkey + '"  name="bhaise" type="text" value="' + data2.haise +'" placeholder="yyyy/mm/dd" class="datepicker">'
              + '<label for="bhaise' + gkey + '" class="center-align active" id="bhaise' + gkey + '-l" data="配セ戻入日">配セ戻入日</label>'
              + '</div>'
              + '<div class="input-field col s12 l6">'
              + '<input id="bkend' + gkey + '"  name="bkend" type="text" value="' + data2.kend +'" placeholder="yyyy/mm/dd" class="datepicker">'
              + '<label for="bkend' + gkey + '" class="center-align active" id="bkend' + gkey + '-l" data="製造完了予定日">製造完了予定日</label>'
              + '</div>'
              + '</div>'
              + '</li>';
              oPreantCard.append(oString);
            }
        }
      }
    }
    bunnobtnset(); //分納ボタン再セット
  }
  /*------------------------------------------*/
  /*----- 要素を追加します               -----*/
  /*------------------------------------------*/
  CardKaizoList.add = function() {
    if (oCardKaizoList != undefined) {
      var key = oCardKaizoList.children("li").length;
      var addJson = {"key":key, "kataban":"", "bousyoku":0, "Jubousyoku":0, "taiengai":0, "taijuengai":0, "jubousabi":0};
      kaizouinfo.push(addJson);
      var oString = '<li class="card-panel" id="kaizo' + key + '">'
      + '<div class="row">'
      + '<div class="input-field col s12 l4">'
      + '<input id="kataban' + key + '"  name="kataban" type="text" placeholder="XXXXXXXXXXXXXXXXXXXX" value="">'
      + '<label for="kataban' + key + '" class="center-align active" id="kataban' + key + '-l" data="製品型番">製品型番</label>'
      + '</div>'
      + '<div class="col s6 l1 center">'
      + '<p>'
      + '<div class="chip cyan white-text z-depth-2 stts-chip" id="seihin' + key + '">'
      + '<a href="#kataban-modal" class="white-text modal-trigger" key="' + key + '">製品</a>'
      + '</div>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l1 center">'
      + '<p>'
      + '<div class="chip cyan white-text z-depth-2 stts-chip bunno" id="bunno' + key + '" key="' + key + '" bunnoKbn="0">'
      + '<a href="#" class="white-text">分納</a>'
      + '</div>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l2 center">'
      + '<p>'
      + '<div class="chip cyan white-text z-depth-2 stts-chip" id="option' + key + '">'
      + '<a href="#option-modal" class="white-text modal-trigger" key="' + key + '">オプション</a>'
      + '</div>'
      + '</p>'
      + '</div>'
      + '<div class="col s12 offset-l3 l1">'
      + '<p>'
      + '<a href="#" class="delete btn-floating waves-effect waves-light red white-text" id="delete' + key + '" key="kaizo' + key + '"><i class="material-icons prefix">clear</i></a>'
      + '</p>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="col s6 l4">'
      + '<p>'
      + '<input id="bousyoku' + key + '"  name="bousyoku" class="kaizo" type="checkbox">'
      + '<label for="bousyoku' + key + '" class="left-align">防蝕</label>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l2">'
      + '<p>'
      + '<span id="kBousyoku' + key + '">' + ('            ' + '0').slice( -12 ) + ' 円</span>'
      + '</p>'
      + '</div>'
      + '<div class="col s12 l3">'
      + '<div class="" style="">'
      + '<input id="pBousyoku' + key + '"  name="pBousyoku" type="text" class="right-text" placeholder="帳票用金額" value="">'
      + '</div>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="col s6 l4">'
      + '<p>'
      + '<input id="Jubousyoku' + key + '"  name="Jubousyoku" class="kaizo" type="checkbox">'
      + '<label for="Jubousyoku' + key + '" class="left-align">重防蝕</label>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l2">'
      + '<p>'
      + '<span id="kJubousyoku' + key + '">' + ('            ' + '0').slice( -12 ) + ' 円</span>'
      + '</p>'
      + '</div>'
      + '<div class="col s12 l3">'
      + '<div class="" style="">'
      + '<input id="pJubousyoku' + key + '"  name="pJubousyoku" type="text" class="right-text" placeholder="帳票用金額" value="">'
      + '</div>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="col s6 l4">'
      + '<p>'
      + '<input id="taiengai' + key + '"  name="taiengai" class="kaizo" type="checkbox">'
      + '<label for="taiengai' + key + '" class="left-align">耐塩害</label>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l2">'
      + '<p>'
      + '<span id="kTaiengai' + key + '">' + ('            ' + '0').slice( -12 ) + ' 円</span>'
      + '</p>'
      + '</div>'
      + '<div class="col s12 l3">'
      + '<div class="" style="">'
      + '<input id="pTaiengai' + key + '"  name="pTaiengai" type="text" class="right-text" placeholder="帳票用金額" value="">'
      + '</div>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="col s6 l4">'
      + '<p>'
      + '<input id="taijuengai' + key + '"  name="taijuengai" class="kaizo" type="checkbox">'
      + '<label for="taijuengai' + key + '" class="left-align">耐重塩害</label>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l2">'
      + '<p>'
      + '<span id="kTaijuengai' + key + '">' + ('            ' + '0').slice( -12 ) + ' 円</span>'
      + '</p>'
      + '</div>'
      + '<div class="col s12 l3">'
      + '<div class="" style="">'
      + '<input id="pTaijuengai' + key + '"  name="pTaijuengai" type="text" class="right-text" placeholder="帳票用金額" value="">'
      + '</div>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="col s6 l4">'
      + '<p>'
      + '<input id="jubousabi' + key + '"  name="jubousabi" class="kaizo" type="checkbox">'
      + '<label for="jubousabi' + key + '" class="left-align">重防錆</label>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l2">'
      + '<p>'
      + '<span id="kJubousabi' + key + '">' + ('            ' + '0').slice( -12 ) + ' 円</span>'
      + '</p>'
      + '</div>'
      + '<div class="col s12 l3">'
      + '<div class="" style="">'
      + '<input id="pJubousabi' + key + '"  name="pJubousabi" type="text" class="right-text" placeholder="帳票用金額" value="">'
      + '</div>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s6 l4">'
      + '<input id="mashine' + key + '"  name="mashine" type="text" placeholder="XXXXXXXXXX" value="">'
      + '<label for="mashine' + key + '" class="center-align active" id="mashine' + key + '-l" data="マシンNo.">マシンNo.</label>'
      + '</div>'
      + '<div class="input-field col s6 l2">'
      + '<input id="suryo' + key + '"  name="suryo" type="text" class="right-text suryo" placeholder="99" value="">'
      + '<label for="suryo' + key + '" class="center-align active" id="suryo' + key + '-l" data="数量">数量</label>'
      + '</div>'
      + '<div class="input-field col s12 l6">'
      + '<input id="nyuka' + key + '"  name="nyuka" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="nyuka' + key + '" class="center-align active" id="nyuka' + key + '-l" data="入荷予定日">入荷予定日</label>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s12 l6">'
      + '<input id="kibou' + key + '"  name="kibou" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="kibou' + key + '" class="center-align active" id="kibou' + key + '-l" data="入荷希望日">入荷希望日</label>'
      + '</div>'
      + '<div class="input-field col s12 l6">'
      + '<input id="hikitori' + key + '"  name="hikitori" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="hikitori' + key + '" class="center-align active" id="hikitori' + key + '-l" data="引取希望日">引取希望日</label>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s12 l6">'
      + '<input id="haise' + key + '"  name="haise" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="haise' + key + '" class="center-align active" id="haise' + key + '-l" data="配セ戻入日">配セ戻入日</label>'
      + '</div>'
      + '<div class="input-field col s12 l6">'
      + '<input id="kend' + key + '"  name="kend" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="kend' + key + '" class="center-align active" id="kend' + key + '-l" data="製造完了予定日">製造完了予定日</label>'
      + '</div>'
      + '</div>'
      + '</li>';
      oCardKaizoList.append(oString);
    }
    bunnobtnset(); //分納ボタン再セット
  }
  /*------------------------------------------*/
  /*----- 要素を分納します               -----*/
  /*------------------------------------------*/
  CardKaizoList.bunno = function(idx) {
    var oPreantCard = $("#kaizo" + idx);
    if (oPreantCard != undefined) {
      var key = oPreantCard.children("li").length;
      var gkey = ((parseInt(idx) * 100) + parseInt(key));
      var oString = '<li class="card-panel bunno" id="bkaizo' + gkey + '" preantkey="#kaizo' + idx + '">'
      + '<input id="bkataban' + gkey + '"type="hidden" value="' + $("#kataban" + idx).val() + '">'
      + '<div class="row">'
      + '<div class="input-field col s12 l4">'
      + '<p>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l1 center">'
      + '<p>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l1 center">'
      + '<p>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l2 center">'
      + '<p>'
      + '</p>'
      + '</div>'
      + '<div class="col s12 offset-l3 l1">'
      + '<p>'
      + '<a href="#" class="delete btn-floating waves-effect waves-light red white-text" id="bdelete' + gkey + '" key="bkaizo' + gkey + '"><i class="material-icons prefix">clear</i></a>'
      + '</p>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s6 l4">'
      + '<input id="bmashine' + gkey + '"  name="bmashine" type="text" placeholder="XXXXXXXXXX" value="">'
      + '<label for="bmashine' + gkey + '" class="center-align active" id="bmashine' + gkey + '-l" data="マシンNo.">マシンNo.</label>'
      + '</div>'
      + '<div class="input-field col s6 l2">'
      + '<input id="bsuryo' + gkey + '"  name="bsuryo" type="text" class="right-text suryo" placeholder="99" value="">'
      + '<label for="bsuryo' + gkey + '" class="center-align active" id="bsuryo' + gkey + '-l" data="数量">数量</label>'
      + '</div>'
      + '<div class="input-field col s12 l6">'
      + '<input id="bnyuka' + gkey + '"  name="bnyuka" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="bnyuka' + gkey + '" class="center-align active" id="bnyuka' + gkey + '-l" data="入荷予定日">入荷予定日</label>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s12 l6">'
      + '<input id="bkibou' + gkey + '"  name="bkibou" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="bkibou' + gkey + '" class="center-align active" id="bkibou' + gkey + '-l" data="入荷希望日">入荷希望日</label>'
      + '</div>'
      + '<div class="input-field col s12 l6">'
      + '<input id="bhikitori' + gkey + '"  name="bhikitori" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="bhikitori' + gkey + '" class="center-align active" id="bhikitori' + gkey + '-l" data="引取希望日">引取希望日</label>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s12 l6">'
      + '<input id="bhaise' + gkey + '"  name="bhaise" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="bhaise' + gkey + '" class="center-align active" id="bhaise' + gkey + '-l" data="配セ戻入日">配セ戻入日</label>'
      + '</div>'
      + '<div class="input-field col s12 l6">'
      + '<input id="bkend' + gkey + '"  name="bkend" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="bkend' + gkey + '" class="center-align active" id="bkend' + gkey + '-l" data="製造完了予定日">製造完了予定日</label>'
      + '</div>'
      + '</div>'
      + '</li>';
      oPreantCard.append(oString);
      ContorolInfo.set(1, userkey);
    }
  }
  /*------------------------------------------*/
  /*----- 要素の値をJSON形式で取得します -----*/
  /*------------------------------------------*/
  CardKaizoList.get = function(json) {
    if (oCardKaizoList != undefined) {
      var list = oCardKaizoList.children("li");
      for (var i=0; i < list.length; i++) {
        //----- 先に分納分の集約を行う -----
        var oPreantCard = $("#kaizo" + i);
        var bunnolist = oPreantCard.children("li");
        var gsuryo = 0;
        var bunnojson = [];
        for (var i2=0; i2 < bunnolist.length; i2++) {
          var jsondata = {gno:i2
              , kataban:$("#kataban" + i).val()
              , bousyoku:$("#bousyoku" + i).prop("checked")
              , pBousyoku:$("#pBousyoku" + i).val()
              , Jubousyoku:$("#Jubousyoku" + i).prop("checked")
              , pJubousyoku:$("#pJubousyoku" + i).val()
              , taiengai:$("#taiengai" + i).prop("checked")
              , pTaiengai:$("#pTaiengai" + i).val()
              , taijuengai:$("#taijuengai" + i).prop("checked")
              , pTaijuengai:$("#pTaijuengai" + i).val()
              , jubousabi:$("#jubousabi" + i).prop("checked")
              , pJubousabi:$("#pJubousabi" + i).val()
              , mashine:$("#bmashine" + ((i * 100) + i2)).val()
              , suryo:$("#bsuryo" + ((i * 100) + i2)).val()
              , gsuryo:0
              , nyuka:$("#bnyuka" + ((i * 100) + i2)).val()
              , kibou:$("#bkibou" + ((i * 100) + i2)).val()
              , hikitori:$("#bhikitori" + ((i * 100) + i2)).val()
              , haise:$("#bhaise" + ((i * 100) + i2)).val()
              , kend:$("#bkend" + ((i * 100) + i2)).val()
          };
          gsuryo += parseInt($("#bsuryo" + ((i * 100) + i2)).val());
          bunnojson.push(jsondata);
        }

        gsuryo += parseInt($("#suryo" + i).val());
        var jsondata = {gno:i
            , kataban:$("#kataban" + i).val()
            , bousyoku:$("#bousyoku" + i).prop("checked")
            , pBousyoku:$("#pBousyoku" + i).val()
            , Jubousyoku:$("#Jubousyoku" + i).prop("checked")
            , pJubousyoku:$("#pJubousyoku" + i).val()
            , taiengai:$("#taiengai" + i).prop("checked")
            , pTaiengai:$("#pTaiengai" + i).val()
            , taijuengai:$("#taijuengai" + i).prop("checked")
            , pTaijuengai:$("#pTaijuengai" + i).val()
            , jubousabi:$("#jubousabi" + i).prop("checked")
            , pJubousabi:$("#pJubousabi" + i).val()
            , mashine:$("#mashine" + i).val()
            , suryo:$("#suryo" + i).val()
            , gsuryo:gsuryo
            , nyuka:$("#nyuka" + i).val()
            , kibou:$("#kibou" + i).val()
            , hikitori:$("#hikitori" + i).val()
            , haise:$("#haise" + i).val()
            , kend:$("#kend" + i).val()
            , bunnoinfo:bunnojson
        };
        json.push(jsondata);
      }
    }
  }
  /*-------------------- 特殊改造情報専用カードリスト --------------------*/
  var oCardTokuList; //カードリストオブジェクト

  /*------------------------------------------*/
  /*----- カードリストオブジェクト生成   -----*/
  /*------------------------------------------*/
  var CardTokuList = function(list) { //
    oCardTokuList = list;
    oCardTokuList.addClass('tokusyu-list');
  }
  /*------------------------------------------*/
  /*----- 要素を全て削除します           -----*/
  /*------------------------------------------*/
  CardTokuList.clear = function() {
    if (oCardTokuList != undefined) {
      oCardTokuList.empty();
    }
  }
  /*------------------------------------------*/
  /*----- 要素を表示します               -----*/
  /*------------------------------------------*/
  CardTokuList.put = function(json) {
    var idx = 0;
    if (oCardTokuList != undefined) {
      for (var key in json) {
        var data = json[key];
        var oString = '<li class="card-panel" id="tokusyu' + key + '">'
        + '<div class="row">'
        + '<div class="input-field col s12 l4">'
        + '<input id="tkataban' + key + '"  name="tkataban" type="text" placeholder="XXXXXXXXXXXXXXXXXXXX" value="' + data.tkataban + '">'
        + '<label for="tkataban' + key + '" class="center-align active" id="tkataban' + key + '-l" data="製品型番">製品型番</label>'
        + '</div>'
        + '<div class="col offset-s6 s6 offset-l1 l1 center">'
        + '<p>'
        + '<div class="chip cyan white-text z-depth-2 stts-chip tbunno" id="tbunno' + key + '" key="' + key + '">'
        + '<a href="#" class="white-text">分納</a>'
        + '</div>'
        + '</p>'
        + '</div>'
        + '<div class="col s12 offset-l5 l1">'
        + '<p>'
        + '<a href="#" class="delete btn-floating waves-effect waves-light red white-text" id="tdelete' + key + '" key="tokusyu' + key + '"><i class="material-icons prefix">clear</i></a>'
        + '</p>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="input-field col s12 l6">'
        + '<input id="tkaizou' + key + '"  name="tkaizou" type="text" placeholder="〇〇〇〇〇〇〇〇〇〇〇〇〇〇〇〇〇〇〇〇" value="' + data.tkaizou + '">'
        + '<label for="tkaizou' + key + '" class="center-align active" id="tkaizou' + key + '-l" data="改造内容">改造内容</label>'
        + '</div>'
        + '<div class="input-field col s12 l3">'
        + '<div class="" style="">'
        + '<input id="tkingaku' + key + '"  name="tkingaku" type="text" class="right-text kingaku" placeholder="999999999" value="' + data.tkingaku + '">'
        + '<label for="tkingaku' + key + '" class="center-align active" id="tkingaku' + key + '-l" data="金額">金額</label>'
        + '</div>'
        + '</div>'
        + '<div class="input-field col s12 l3">'
        + '<div class="" style="">'
        + '<input id="ptkingaku' + key + '"  name="ptkingaku" type="text" class="right-text" placeholder="999999999" value="' + data.ptkingaku + '">'
        + '<label for="ptkingaku' + key + '" class="center-align active" id="ptkingaku' + key + '-l" data="帳票用金額">帳票用金額</label>'
        + '</div>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="input-field col s6 l4">'
        + '<input id="tmashine' + key + '"  name="tmashine" type="text" placeholder="XXXXXXXXXX" value="' + data.tmashine + '">'
        + '<label for="tmashine' + key + '" class="center-align active" id="tmashine' + key + '-l" data="マシンNo.">マシンNo.</label>'
        + '</div>'
        + '<div class="input-field col s6 l2">'
        + '<input id="tsuryo' + key + '"  name="tsuryo" type="text" class="right-text suryo" placeholder="99" value="' + data.tsuryo + '">'
        + '<label for="tsuryo' + key + '" class="center-align active" id="tsuryo' + key + '-l" data="数量">数量</label>'
        + '</div>'
        + '<div class="input-field col s12 l6">'
        + '<input id="tnyuka' + key + '"  name="tnyuka" type="text" value="' + data.tnyuka + '" placeholder="yyyy/mm/dd" class="datepicker">'
        + '<label for="tnyuka' + key + '" class="center-align active" id="tnyuka' + key + '-l" data="入荷予定日">入荷予定日</label>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="input-field col s12 l6">'
        + '<input id="tkibou' + key + '"  name="kibou" type="text" value="' + data.tkibou + '" placeholder="yyyy/mm/dd" class="datepicker">'
        + '<label for="tkibou' + key + '" class="center-align active" id="tkibou' + key + '-l" data="入荷希望日">入荷希望日</label>'
        + '</div>'
        + '<div class="input-field col s12 l6">'
        + '<input id="thikitori' + key + '"  name="thikitori" type="text" value="' + data.thikitori + '" placeholder="yyyy/mm/dd" class="datepicker">'
        + '<label for="thikitori' + key + '" class="center-align active" id="thikitori' + key + '-l" data="引取希望日">引取希望日</label>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="input-field col s12 l6">'
        + '<input id="thaise' + key + '"  name="thaise" type="text" value="' + data.thaise + '" placeholder="yyyy/mm/dd" class="datepicker">'
        + '<label for="thaise' + key + '" class="center-align active" id="thaise' + key + '-l" data="配セ戻入日">配セ戻入日</label>'
        + '</div>'
        + '<div class="input-field col s12 l6">'
        + '<input id="tkend' + key + '"  name="tkend" type="text" value="' + data.tkend + '" placeholder="yyyy/mm/dd" class="datepicker">'
        + '<label for="tkend' + key + '" class="center-align active" id="tkend' + key + '-l" data="製造完了予定日">製造完了予定日</label>'
        + '</div>'
        + '</div>'
        + '</li>';
        oCardTokuList.append(oString);
        var bunnolist = data.bunnoinfo;
        for (var key2 in bunnolist) {
          //----- 改造情報のJSON化 -----
            var data2 = bunnolist[key2];
            var oPreantCard = $("#tokusyu" + key);
            var gkey = ((parseInt(key) * 100) + parseInt(key2));
            if (oPreantCard != undefined) {
              var oString = '<li class="card-panel bunno" id="btokusyu' + gkey + '" preantkey="#tokusyu' + key + '">'
              + '<input id="btkataban' + gkey + '"type="hidden" value="' + $("#tkataban" + key).val() + '">'
              + '<div class="row">'
              + '<div class="input-field col s12 l4">'
              + '<p>'
              + '</p>'
              + '</div>'
              + '<div class="col s6 l1 center">'
              + '<p>'
              + '</p>'
              + '</div>'
              + '<div class="col s6 l1 center">'
              + '<p>'
              + '</p>'
              + '</div>'
              + '<div class="col s6 l2 center">'
              + '<p>'
              + '</p>'
              + '</div>'
              + '<div class="col s12 offset-l3 l1">'
              + '<p>'
              + '<a href="#" class="delete btn-floating waves-effect waves-light red white-text" id="btdelete' + gkey + '" key="btokusyu' + gkey + '"><i class="material-icons prefix">clear</i></a>'
              + '</p>'
              + '</div>'
              + '</div>'
              + '<div class="row">'
              + '<div class="input-field col s6 l4">'
              + '<input id="btmashine' + gkey + '"  name="btmashine' + gkey + '" type="text" placeholder="XXXXXXXXXX" value="' + data2.tmashine +  '">'
              + '<label for="btmashine' + gkey + '" class="center-align active" id="btmashine' + gkey + '-l" data="マシンNo.">マシンNo.</label>'
              + '</div>'
              + '<div class="input-field col s6 l2">'
              + '<input id="btsuryo' + gkey + '"  name="btsuryo' + gkey + '" type="text" class="right-text suryo" placeholder="99" value="' + data2.tsuryo +  '">'
              + '<label for="btsuryo' + gkey + '" class="center-align active" id="btsuryo' + gkey + '-l" data="数量">数量</label>'
              + '</div>'
              + '<div class="input-field col s12 l6">'
              + '<input id="btnyuka' + gkey + '"  name="btnyuka' + gkey + '" type="text" value="' + data2.tnyuka +  '" placeholder="yyyy/mm/dd" class="datepicker">'
              + '<label for="btnyuka' + gkey + '" class="center-align active" id="btnyuka' + gkey + '-l" data="入荷予定日">入荷予定日</label>'
              + '</div>'
              + '</div>'
              + '<div class="row">'
              + '<div class="input-field col s12 l6">'
              + '<input id="btkibou' + gkey + '"  name="btkibou' + gkey + '" type="text" value="' + data2.tkibou +  '" placeholder="yyyy/mm/dd" class="datepicker">'
              + '<label for="btkibou' + gkey + '" class="center-align active" id="btkibou' + gkey + '-l" data="入荷希望日">入荷希望日</label>'
              + '</div>'
              + '<div class="input-field col s12 l6">'
              + '<input id="bthikitori' + gkey + '"  name="bthikitori' + gkey + '" type="text" value="' + data2.thikitori +  '" placeholder="yyyy/mm/dd" class="datepicker">'
              + '<label for="bthikitori' + gkey + '" class="center-align active" id="bthikitori' + gkey + '-l" data="引取希望日">引取希望日</label>'
              + '</div>'
              + '</div>'
              + '<div class="row">'
              + '<div class="input-field col s12 l6">'
              + '<input id="bthaise' + gkey + '"  name="bthaise' + gkey + '" type="text" value="' + data2.thaise +  '" placeholder="yyyy/mm/dd" class="datepicker">'
              + '<label for="bthaise' + gkey + '" class="center-align active" id="bthaise' + gkey + '-l" data="配セ戻入日">配セ戻入日</label>'
              + '</div>'
              + '<div class="input-field col s12 l6">'
              + '<input id="btkend' + gkey + '"  name="btkend' + gkey + '" type="text" value="' + data2.tkend +  '" placeholder="yyyy/mm/dd" class="datepicker">'
              + '<label for="btkend' + gkey + '" class="center-align active" id="btkend' + gkey + '-l" data="製造完了予定日">製造完了予定日</label>'
              + '</div>'
              + '</div>'
              + '</li>';
              oPreantCard.append(oString);
            }
        }
      }
    }
    tbunnobtnset(); //分納ボタン再セット
  }
  /*------------------------------------------*/
  /*----- 要素を追加します               -----*/
  /*------------------------------------------*/
  CardTokuList.add = function(json) {
    if (oCardTokuList != undefined) {
      var key = oCardTokuList.children("li").length;
      var oString = '<li class="card-panel" id="tokusyu' + key + '">'
      + '<div class="row">'
      + '<div class="input-field col s12 l4">'
      + '<input id="tkataban' + key + '"  name="tkataban" type="text" placeholder="XXXXXXXXXXXXXXXXXXXX" value="">'
      + '<label for="tkataban' + key + '" class="center-align active" id="tkataban' + key + '-l" data="製品型番">製品型番</label>'
      + '</div>'
      + '<div class="col offset-s6 s6 offset-l1 l1 center">'
      + '<p>'
      + '<div class="chip cyan white-text z-depth-2 stts-chip tbunno" id="tbunno' + key + '" key="' + key + '">'
      + '<a href="#" class="white-text">分納</a>'
      + '</div>'
      + '</p>'
      + '</div>'
      + '<div class="col s12 offset-l5 l1">'
      + '<p>'
      + '<a href="#" class="delete btn-floating waves-effect waves-light red white-text" id="tdelete' + key + '" key="tokusyu' + key + '"><i class="material-icons prefix">clear</i></a>'
      + '</p>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s12 l6">'
      + '<input id="tkaizou' + key + '"  name="tkaizou" type="text" placeholder="〇〇〇〇〇〇〇〇〇〇〇〇〇〇〇〇〇〇〇〇" value="">'
      + '<label for="tkaizou' + key + '" class="center-align active" id="tkaizou' + key + '-l" data="改造内容">改造内容</label>'
      + '</div>'
      + '<div class="input-field col s12 l3">'
      + '<div class="" style="">'
      + '<input id="tkingaku' + key + '"  name="tkingaku" type="text" placeholder="999999999" class="right-text kingaku" value="">'
      + '<label for="tkingaku' + key + '" class="center-align active" id="tkingaku' + key + '-l" data="金額">金額</label>'
      + '</div>'
      + '</div>'
      + '<div class="input-field col s12 l3">'
      + '<div class="" style="">'
      + '<input id="ptkingaku' + key + '"  name="ptkingaku" type="text" placeholder="999999999" class="right-text" value="">'
      + '<label for="ptkingaku' + key + '" class="center-align active" id="ptkingaku' + key + '-l" data="帳票用金額">帳票用金額</label>'
      + '</div>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s6 l4">'
      + '<input id="tmashine' + key + '"  name="tmashine" type="text" placeholder="XXXXXXXXXX" value="">'
      + '<label for="tmashine' + key + '" class="center-align active" id="tmashine' + key + '-l" data="マシンNo.">マシンNo.</label>'
      + '</div>'
      + '<div class="input-field col s6 l2">'
      + '<input id="tsuryo' + key + '"  name="tsuryo" type="text" class="right-text suryo" placeholder="99" value="">'
      + '<label for="tsuryo' + key + '" class="center-align active" id="tsuryo' + key + '-l" data="数量">数量</label>'
      + '</div>'
      + '<div class="input-field col s12 l6">'
      + '<input id="tnyuka' + key + '"  name="tnyuka" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="tnyuka' + key + '" class="center-align active" id="tnyuka' + key + '-l" data="入荷予定日">入荷予定日</label>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s12 l6">'
      + '<input id="tkibou' + key + '"  name="kibou" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="tkibou' + key + '" class="center-align active" id="tkibou' + key + '-l" data="入荷希望日">入荷希望日</label>'
      + '</div>'
      + '<div class="input-field col s12 l6">'
      + '<input id="thikitori' + key + '"  name="thikitori" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="thikitori' + key + '" class="center-align active" id="thikitori' + key + '-l" data="引取希望日">引取希望日</label>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s12 l6">'
      + '<input id="thaise' + key + '"  name="thaise" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="thaise' + key + '" class="center-align active" id="thaise' + key + '-l" data="配セ戻入日">配セ戻入日</label>'
      + '</div>'
      + '<div class="input-field col s12 l6">'
      + '<input id="tkend' + key + '"  name="tkend" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="tkend' + key + '" class="center-align active" id="tkend' + key + '-l" data="製造完了予定日">製造完了予定日</label>'
      + '</div>'
      + '</div>'
      + '</li>';
      oCardTokuList.append(oString);
    }
    tbunnobtnset(); //分納ボタン再セット
  }
  /*------------------------------------------*/
  /*----- 要素を分納します               -----*/
  /*------------------------------------------*/
  CardTokuList.bunno = function(idx) {
    var oPreantCard = $("#tokusyu" + idx);
    if (oPreantCard != undefined) {
      var key = oPreantCard.children("li").length;
      var gkey = ((parseInt(idx) * 100) + parseInt(key));
      var oString = '<li class="card-panel bunno" id="btokusyu' + gkey + '" preantkey="#tokusyu' + idx + '">'
      + '<input id="btkataban' + gkey + '"type="hidden" value="' + $("#tkataban" + idx).val() + '">'
      + '<div class="row">'
      + '<div class="input-field col s12 l4">'
      + '<p>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l1 center">'
      + '<p>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l1 center">'
      + '<p>'
      + '</p>'
      + '</div>'
      + '<div class="col s6 l2 center">'
      + '<p>'
      + '</p>'
      + '</div>'
      + '<div class="col s12 offset-l3 l1">'
      + '<p>'
      + '<a href="#" class="delete btn-floating waves-effect waves-light red white-text" id="btdelete' + gkey + '" key="btokusyu' + gkey + '"><i class="material-icons prefix">clear</i></a>'
      + '</p>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s6 l4">'
      + '<input id="btmashine' + gkey + '"  name="btmashine" type="text" placeholder="XXXXXXXXXX" value="">'
      + '<label for="btmashine' + gkey + '" class="center-align active" id="btmashine' + gkey + '-l" data="マシンNo.">マシンNo.</label>'
      + '</div>'
      + '<div class="input-field col s6 l2">'
      + '<input id="btsuryo' + gkey + '"  name="btsuryo" type="text" class="right-text suryo" placeholder="99" value="">'
      + '<label for="btsuryo' + gkey + '" class="center-align active" id="btsuryo' + gkey + '-l" data="数量">数量</label>'
      + '</div>'
      + '<div class="input-field col s12 l6">'
      + '<input id="btnyuka' + gkey + '"  name="btnyuka" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="btnyuka' + gkey + '" class="center-align active" id="btnyuka' + gkey + '-l" data="入荷予定日">入荷予定日</label>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s12 l6">'
      + '<input id="btkibou' + gkey + '"  name="btkibou" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="btkibou' + gkey + '" class="center-align active" id="btkibou' + gkey + '-l" data="入荷希望日">入荷希望日</label>'
      + '</div>'
      + '<div class="input-field col s12 l6">'
      + '<input id="bthikitori' + gkey + '"  name="bthikitori" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="bthikitori' + gkey + '" class="center-align active" id="bthikitori' + gkey + '-l" data="引取希望日">引取希望日</label>'
      + '</div>'
      + '</div>'
      + '<div class="row">'
      + '<div class="input-field col s12 l6">'
      + '<input id="bthaise' + gkey + '"  name="bthaise" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="bthaise' + gkey + '" class="center-align active" id="bthaise' + gkey + '-l" data="配セ戻入日">配セ戻入日</label>'
      + '</div>'
      + '<div class="input-field col s12 l6">'
      + '<input id="btkend' + gkey + '"  name="btkend" type="text" value="" placeholder="yyyy/mm/dd" class="datepicker">'
      + '<label for="btkend' + gkey + '" class="center-align active" id="btkend' + gkey + '-l" data="製造完了予定日">製造完了予定日</label>'
      + '</div>'
      + '</div>'
      + '</li>';
      oPreantCard.append(oString);
      ContorolInfo.set(2, userkey);
    }
  }
  /*------------------------------------------*/
  /*----- 要素の値をJSON形式で取得します -----*/
  /*------------------------------------------*/
  CardTokuList.get = function(json) {
    if (oCardTokuList != undefined) {
      var list = oCardTokuList.children("li");
      for (var i=0; i < list.length; i++) {
        var data = $(list.eq(i));
        //----- 先に分納分の集約を行う -----
        var oPreantCard = $("#tokusyu" + i);
        var bunnolist = oPreantCard.children("li");
        var gsuryo = 0;
        var bunnojson = [];
        for (var i2=0; i2 < bunnolist.length; i2++) {
          var jsondata = {
              gno:i2
             , tkataban:$("#tkataban" + i).val()
             , tkaizou:$("#tkaizou" + i).val()
             , tkingaku:$("#tkingaku" + i).val()
             , ptkingaku:$("#ptkingaku" + i).val()
             , tmashine:$("#btmashine" + ((i * 100) + i2)).val()
             , tsuryo:$("#btsuryo" + ((i * 100) + i2)).val()
             , tgsuryo:0
             , tnyuka:$("#btnyuka" + ((i * 100) + i2)).val()
             , tkibou:$("#btkibou" + ((i * 100) + i2)).val()
             , thikitori:$("#bthikitori" + ((i * 100) + i2)).val()
             , thaise:$("#bthaise" + ((i * 100) + i2)).val()
             , tkend:$("#btkend" + ((i * 100) + i2)).val()
          };
          gsuryo += parseInt($("#btsuryo" + ((i * 100) + i2)).val());
          bunnojson.push(jsondata);
        }
        gsuryo += parseInt($("#tsuryo" + i).val());
        var jsondata = {
            gno:i
           , tkataban:$("#tkataban" + i).val()
           , tkaizou:$("#tkaizou" + i).val()
           , tkingaku:$("#tkingaku" + i).val()
           , ptkingaku:$("#ptkingaku" + i).val()
           , tmashine:$("#tmashine" + i).val()
           , tsuryo:$("#tsuryo" + i).val()
           , tgsuryo:gsuryo
           , tnyuka:$("#tnyuka" + i).val()
           , tkibou:$("#tkibou" + i).val()
           , thikitori:$("#thikitori" + i).val()
           , thaise:$("#thaise" + i).val()
           , tkend:$("#tkend" + i).val()
           , bunnoinfo:bunnojson
        };
        json.push(jsondata);
      }
    }
  }
  /*-------------------- オプション専用カードリスト --------------------*/
  var oCardOptionList;  //オプションリストオブジェクト

  /*------------------------------------------*/
  /*----- オプションオブジェクト生成     -----*/
  /*------------------------------------------*/
  var CardOptionList = function(list) { //
    oCardOptionList = list;
    oCardOptionList.addClass('option-list');
  }
  /*------------------------------------------*/
  /*----- 要素を全て削除します           -----*/
  /*------------------------------------------*/
  CardOptionList.clear = function() {
    if (oCardOptionList != undefined) {
      oCardOptionList.empty();
    }
  }
  /*------------------------------------------*/
  /*----- 要素を表示します               -----*/
  /*------------------------------------------*/
  CardOptionList.put = function(json) {
    var idx = 0;
    if (oCardOptionList != undefined) {
      for (var key in json) {
        var data = json[key];
        var oString = '<li class="card-panel" id="option' + key + '">'
        + '<div class="row">'
        + '<div class="col s12 l8">'
        + '<p>'
        + '<input id="optioncd' + key + '"  name="optioncd" type="checkbox" class="optioncd" key="' + key + '">'
        + '<label for="optioncd' + key + '" class="left-align">' + data.optioncd + ' ' + data.optionname + '</label>'
        + '<input id="hoptionname' + key + '"  type="hidden" value="' + data.optionname + '">'
        + '</p>'
        + '</div>'
        + '<div class="col s12 l4">'
        + '<p>'
        + '<span id="optiontanka' + key + '">' + ('        ' + data.optiontanka.toLocaleString()).slice( -8 ) + ' 円</span>'
        + '<input id="hoptiontanka' + key + '"  type="hidden" value="' + data.optiontanka + '">'
        + '</p>'
        + '</div>'
        + '</div>'
        + '<div class="row">'
        + '<div class="input-field col s6 l4">'
        + '<input id="omashine' + key + '"  name="omashine" type="text" value="">'
        + '<label for="omashine' + key + '" class="center-align">マシンNo.</label>'
        + '</div>'
        + '<div class="input-field col s12 l4">'
        + '<input id="onyuka' + key + '"  name="onyuka" type="text" value="" class="datepicker">'
        + '<label for="onyuka' + key + '" class="center-align">入荷予定日</label>'
        + '</div>'
        + '<div class="input-field col s12 l4">'
        + '<input id="okibou' + key + '"  name="okibou" type="text" value="" class="datepicker">'
        + '<label for="okibou' + key + '" class="center-align">入荷希望日</label>'
        + '</div>'
        + '</div>'
        + '</li>';
        oCardOptionList.append(oString);
      }
    }
  }
  /*-------------------- 権限＆見積状態による項目制御 --------------------*/
  /*----------------------------------------------*/
  /*----- 権限と状態から制御情報を作成します -----*/
  /*----------------------------------------------*/
  function estimateControl(kengen, stts) {
    userkey = kengen + stts;
    ContorolInfo(); //項目制御オブジェクトの生成
    //----- 基本情報 -----
    ContorolInfo.add({"info":0 , "item":"kanri"      , control:{"0000":{"disabled":false, "required":false}, "0110":{"disabled":true, "required":false}, "0120":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"alnum"});
    ContorolInfo.add({"info":0 , "item":"kouji"      , control:{"0000":{"disabled":false, "required":false}, "0110":{"disabled":true, "required":false}, "0120":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":0 , "item":"haturei"    , control:{"0000":{"disabled":false, "required":false}, "0110":{"disabled":true, "required":false}, "0120":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"alnum"});
    ContorolInfo.add({"info":0 , "item":"irainouki"  , control:{"0000":{"disabled":false, "required":false}, "0110":{"disabled":true, "required":false}, "0120":{"disabled":true, "required":false}, "0210":{"disabled":false, "required":true}, "9910":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":0 , "item":"kaitounouki", control:{"0000":{"disabled":false, "required":false}, "0110":{"disabled":false, "required":true}, "0210":{"disabled":true , "required":false}, "9910":{"disabled":true, "required":false}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":0 , "item":"tokuicode"  , control:{"0000":{"disabled":false, "required":false}, "0110":{"disabled":true, "required":false}, "0210":{"disabled":false, "required":false}, "0120":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"alnum"});
    ContorolInfo.add({"info":0 , "item":"tokuiname"  , control:{"0000":{"disabled":false, "required":false}, "0110":{"disabled":true, "required":false}, "0210":{"disabled":false, "required":false}, "0120":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":0 , "item":"postcode"   , control:{"0000":{"disabled":false, "required":false}, "0320":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"post"});
    ContorolInfo.add({"info":0 , "item":"addr1"      , control:{"0000":{"disabled":false, "required":false}, "0110":{"disabled":false, "required":true}, "0210":{"disabled":false, "required":true}, "9910":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":false, "required":true}, "9920":{"disabled":false, "required":true}, "0320":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":0 , "item":"addr2"      , control:{"0000":{"disabled":false, "required":false}, "0320":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":0 , "item":"tel"        , control:{"0000":{"disabled":false, "required":false}, "0320":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"tel"});
    ContorolInfo.add({"info":0 , "item":"fax"        , control:{"0000":{"disabled":false, "required":false}, "0320":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"tel"});
    ContorolInfo.add({"info":0 , "item":"kaizoadd"   , control:{"0000":{"disabled":false, "required":false}, "0125":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0340":{"disabled":true, "required":false}}, type:"none"});
    ContorolInfo.add({"info":0 , "item":"tokuadd"    , control:{"0000":{"disabled":false, "required":false}, "0125":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0340":{"disabled":true, "required":false}}, type:"none"});
    //----- 改造情報 -----
    ContorolInfo.add({"info":1 , "item":"seihin"     , control:{"0000":{"disabled":false, "required":false}, "0125":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0340":{"disabled":true, "required":false}}, type:"none"});
    ContorolInfo.add({"info":1 , "item":"bunno"      , control:{"0000":{"disabled":false, "required":false}, "0125":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0340":{"disabled":true, "required":false}}, type:"none"});
    ContorolInfo.add({"info":1 , "item":"option"     , control:{"0000":{"disabled":false, "required":false}, "0125":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0340":{"disabled":true, "required":false}}, type:"none"});
    ContorolInfo.add({"info":1 , "item":"delete"     , control:{"0000":{"disabled":false, "required":false}, "0125":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0340":{"disabled":true, "required":false}}, type:"none"});
    ContorolInfo.add({"info":1 , "item":"kataban"    , control:{"0000":{"disabled":false, "required":false}, "0110":{"disabled":false, "required":true}, "0210":{"disabled":false, "required":true}, "9910":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"alnum"});
    ContorolInfo.add({"info":1 , "item":"bousyoku"   , control:{"0000":{"disabled":false, "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"none"});
    ContorolInfo.add({"info":1 , "item":"pBousyoku"  , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"num"});
    ContorolInfo.add({"info":1 , "item":"Jubousyoku" , control:{"0000":{"disabled":false, "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"none"});
    ContorolInfo.add({"info":1 , "item":"pJubousyoku", control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"num"});
    ContorolInfo.add({"info":1 , "item":"taiengai"   , control:{"0000":{"disabled":false, "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"none"});
    ContorolInfo.add({"info":1 , "item":"pTaiengai"  , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"num"});
    ContorolInfo.add({"info":1 , "item":"taijuengai" , control:{"0000":{"disabled":false, "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"none"});
    ContorolInfo.add({"info":1 , "item":"pTaijuengai", control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"num"});
    ContorolInfo.add({"info":1 , "item":"jubousabi"  , control:{"0000":{"disabled":false, "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"none"});
    ContorolInfo.add({"info":1 , "item":"pJubousabi" , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"num"});
    ContorolInfo.add({"info":1 , "item":"mashine"    , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":false, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"alnum"});
    ContorolInfo.add({"info":1 , "item":"suryo"      , control:{"0000":{"disabled":false, "required":false} , "0110":{"disabled":false, "required":true} , "0210":{"disabled":false, "required":true}, "9910":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"num"});
    ContorolInfo.add({"info":1 , "item":"nyuka"      , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":false, "required":true}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":1 , "item":"kibou"      , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":1 , "item":"hikitori"   , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":1 , "item":"haise"      , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":false, "required":true}, "9920":{"disabled":false, "required":true}, "0320":{"disabled":false, "required":true}, "0225":{"disabled":false, "required":true}, "0125":{"disabled":true, "required":false}, "0230":{"disabled":false, "required":true}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":false, "required":true}, "9925":{"disabled":false, "required":true}, "9930":{"disabled":false, "required":true}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":1 , "item":"kend"       , control:{"0000":{"disabled":true , "required":false}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":false, "required":true}, "0130":{"disabled":false, "required":true}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    //----- 改造情報(分納) -----
    ContorolInfo.add({"info":1 , "item":"bmashine"    , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":false, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"alnum"});
    ContorolInfo.add({"info":1 , "item":"bsuryo"      , control:{"0000":{"disabled":false, "required":false} , "0110":{"disabled":false, "required":true} , "0210":{"disabled":false, "required":true}, "9910":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"num"});
    ContorolInfo.add({"info":1 , "item":"bnyuka"      , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":false, "required":true}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":1 , "item":"bkibou"      , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":1 , "item":"bhikitori"   , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":1 , "item":"bhaise"      , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":false, "required":true}, "9920":{"disabled":false, "required":true}, "0320":{"disabled":false, "required":true}, "0225":{"disabled":false, "required":true}, "0125":{"disabled":true, "required":false}, "0230":{"disabled":false, "required":true}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":false, "required":true}, "9925":{"disabled":false, "required":true}, "9930":{"disabled":false, "required":true}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":1 , "item":"bkend"       , control:{"0000":{"disabled":true , "required":false}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":false, "required":true}, "0130":{"disabled":false, "required":true}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    //----- 特殊改造 -----
    ContorolInfo.add({"info":2 , "item":"tbunno"     , control:{"0000":{"disabled":false, "required":false}, "0125":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0340":{"disabled":true, "required":false}}, type:"none"});
    ContorolInfo.add({"info":2 , "item":"tdelete"    , control:{"0000":{"disabled":false, "required":false}, "0125":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0230":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0340":{"disabled":true, "required":false}}, type:"none"});
    ContorolInfo.add({"info":2 , "item":"tkataban"   , control:{"0000":{"disabled":false, "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0210":{"disabled":false, "required":true}, "9910":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"alnum"});
    ContorolInfo.add({"info":2 , "item":"tkaizou"    , control:{"0000":{"disabled":false, "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0210":{"disabled":false, "required":true}, "9910":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":2 , "item":"tkingaku"   , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"num"});
    ContorolInfo.add({"info":2 , "item":"ptkingaku"  , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"num"});
    ContorolInfo.add({"info":2 , "item":"tmashine"   , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":false, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"alnum"});
    ContorolInfo.add({"info":2 , "item":"tsuryo"     , control:{"0000":{"disabled":false, "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0210":{"disabled":false, "required":true}, "9910":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"num"});
    ContorolInfo.add({"info":2 , "item":"tnyuka"     , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":false, "required":true}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":2 , "item":"tkibou"     , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":2 , "item":"thikitori"  , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":2 , "item":"thaise"     , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":false, "required":true}, "9920":{"disabled":false, "required":true}, "0320":{"disabled":false, "required":true}, "0225":{"disabled":false, "required":true}, "0125":{"disabled":true, "required":false}, "0230":{"disabled":false, "required":true}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":false, "required":true}, "9925":{"disabled":false, "required":true}, "9930":{"disabled":false, "required":true}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":2 , "item":"tkend"      , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":false, "required":true}, "0130":{"disabled":false, "required":true}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    //----- 特殊改造(分納) -----
    ContorolInfo.add({"info":2 , "item":"btmashine"   , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":false, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"alnum"});
    ContorolInfo.add({"info":2 , "item":"btsuryo"     , control:{"0000":{"disabled":false, "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0210":{"disabled":false, "required":true}, "9910":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"num"});
    ContorolInfo.add({"info":2 , "item":"btnyuka"     , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":false, "required":true}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":2 , "item":"btkibou"     , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":2 , "item":"bthikitori"  , control:{"0000":{"disabled":true , "required":false} , "0110":{"disabled":false, "required":true}, "0120":{"disabled":false, "required":true}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":2 , "item":"bthaise"     , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":false, "required":true}, "9920":{"disabled":false, "required":true}, "0320":{"disabled":false, "required":true}, "0225":{"disabled":false, "required":true}, "0125":{"disabled":true, "required":false}, "0230":{"disabled":false, "required":true}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":false, "required":true}, "9925":{"disabled":false, "required":true}, "9930":{"disabled":false, "required":true}, "9940":{"disabled":true, "required":false}}, type:"date"});
    ContorolInfo.add({"info":2 , "item":"btkend"      , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":true, "required":false}, "9920":{"disabled":true, "required":false}, "0320":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":false, "required":true}, "0130":{"disabled":false, "required":true}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}}, type:"date"});
    //----- 備考 -----
    ContorolInfo.add({"info":3 , "item":"kaizono"    , control:{"0000":{"disabled":true , "required":false}, "0320":{"disabled":false, "required":true}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}}, type:"alnum"});
    ContorolInfo.add({"info":3 , "item":"koubaino"   , control:{"0000":{"disabled":true , "required":false}, "9920":{"disabled":false, "required":true}, "0320":{"disabled":false, "required":true}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"alnum"});
    ContorolInfo.add({"info":3 , "item":"psitenname" , control:{"0000":{"disabled":false, "required":false}, "0110":{"disabled":false, "required":false}, "9910":{"disabled":false, "required":true}, "0320":{"disabled":false, "required":true}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":3 , "item":"ptantoname" , control:{"0000":{"disabled":false, "required":false}, "0110":{"disabled":false, "required":false}, "9910":{"disabled":false, "required":true}, "0320":{"disabled":false, "required":true}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":3 , "item":"kaizoseikyu", control:{"0000":{"disabled":false, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":3 , "item":"unchin"     , control:{"0000":{"disabled":false, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":3 , "item":"panel"      , control:{"0000":{"disabled":false, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":3 , "item":"setti"      , control:{"0000":{"disabled":false, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":3 , "item":"biko1"      , control:{"0000":{"disabled":false, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":3 , "item":"biko2"      , control:{"0000":{"disabled":false, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":3 , "item":"biko3"      , control:{"0000":{"disabled":false, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":3 , "item":"koutei1"    , control:{"0000":{"disabled":false, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":3 , "item":"koutei2"    , control:{"0000":{"disabled":false, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    ContorolInfo.add({"info":3 , "item":"koutei3"    , control:{"0000":{"disabled":false, "required":false}, "0230":{"disabled":true, "required":false}, "9930":{"disabled":true, "required":false}, "0130":{"disabled":true, "required":false}, "0330":{"disabled":true, "required":false}, "0225":{"disabled":true, "required":false}, "0125":{"disabled":true, "required":false}, "0140":{"disabled":true, "required":false}, "0240":{"disabled":true, "required":false}, "9940":{"disabled":true, "required":false}, "0325":{"disabled":true, "required":false}, "9925":{"disabled":true, "required":false}}, type:"wide"});
    //----- 見積金額 -----
    ContorolInfo.add({"info":3 , "item":"knebiki"    , control:{"0000":{"disabled":true , "required":false}, "0110":{"disabled":false, "required":false}, "0120":{"disabled":false, "required":false}}, type:"num"});
    ContorolInfo.add({"info":3 , "item":"kritu"      , control:{"0000":{"disabled":true , "required":false}, "0110":{"disabled":false, "required":false}, "0120":{"disabled":false, "required":false}}, type:"num"});
    ContorolInfo.add({"info":3 , "item":"ktanka"     , control:{"0000":{"disabled":true , "required":false}, "0110":{"disabled":false, "required":false}, "0120":{"disabled":false, "required":false}}, type:"num"});
    ContorolInfo.add({"info":4 , "item":"tankanebiki", control:{"0000":{"disabled":true , "required":false}, "0110":{"disabled":false, "required":false}, "0120":{"disabled":false, "required":false}}, type:"btn"});
    //----- ボタン -----
    ContorolInfo.add({"info":4 , "item":"addbtn"      , control:{"0000":{"disabled":true , "required":false}, "0210":{"disabled":false, "required":false}, "9910":{"disabled":false, "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"modbtn"      , control:{"0000":{"disabled":true , "required":false}, "0110":{"disabled":false, "required":false}, "0210":{"disabled":false, "required":false}, "0120":{"disabled":false, "required":false}, "0220":{"disabled":true, "required":false}, "0320":{"disabled":false, "required":false}, "0225":{"disabled":false, "required":false}, "9910":{"disabled":false, "required":false}, "0325":{"disabled":false, "required":false}, "0125":{"disabled":false, "required":false}, "0130":{"disabled":false, "required":false}, "0230":{"disabled":false, "required":false}, "9920":{"disabled":false, "required":false}, "0325":{"disabled":false, "required":false}, "9925":{"disabled":false, "required":false}, "9930":{"disabled":false, "required":false}, "9940":{"disabled":true, "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"hachubtn"    , control:{"0000":{"disabled":true , "required":false}, "0220":{"disabled":false, "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"hachudelbtn" , control:{"0000":{"disabled":true , "required":false}, "0225":{"disabled":false, "required":false}, "0325":{"disabled":false, "required":false}, "0125":{"disabled":false, "required":false}, "0325":{"disabled":false, "required":false}, "9925":{"disabled":false, "required":true}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"estdeletebtn", control:{"0000":{"disabled":true , "required":false}, "0110":{"disabled":false, "required":false}, "0210":{"disabled":false, "required":false}, "9910":{"disabled":false, "required":false}, "0325":{"disabled":false, "required":false}, "9925":{"disabled":false, "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"clearbtn"    , control:{"0000":{"disabled":false, "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"backbtn"     , control:{"0000":{"disabled":false, "required":false}}, type:"btn"});
    //----- 帳票ボタン -----
    ContorolInfo.add({"info":4 , "item":"miraibtn"    , control:{"0000":{"disabled":true , "required":false},"0110":{"disabled":false , "required":false},"0210":{"disabled":false , "required":false},"9910":{"disabled":false , "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"mtmorbtn"    , control:{"0000":{"disabled":true , "required":false},"0120":{"disabled":false , "required":false},"0220":{"disabled":false , "required":false},"9920":{"disabled":false , "required":false},"0320":{"disabled":false , "required":false},"0125":{"disabled":false , "required":false},"0225":{"disabled":false , "required":false},"9925":{"disabled":false , "required":false},"0325":{"disabled":false , "required":false},"0130":{"disabled":false , "required":false},"0230":{"disabled":false , "required":false},"9930":{"disabled":false , "required":false},"0140":{"disabled":false , "required":false},"0240":{"disabled":false , "required":false},"9940":{"disabled":false , "required":false}, "9925":{"disabled":false, "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"kaizobtn"    , control:{"0000":{"disabled":true , "required":false},"0120":{"disabled":false , "required":false},"0320":{"disabled":false , "required":false},"0125":{"disabled":false , "required":false},"0325":{"disabled":false , "required":false},"0130":{"disabled":false , "required":false},"0330":{"disabled":false , "required":false},"0140":{"disabled":false , "required":false},"0240":{"disabled":false , "required":false},"0340":{"disabled":false , "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"nousibtn"    , control:{"0000":{"disabled":false, "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"uriagbtn"    , control:{"0000":{"disabled":true , "required":false},"0130":{"disabled":false , "required":false},"0140":{"disabled":false , "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"nouhnbtn"    , control:{"0000":{"disabled":true , "required":false},"0130":{"disabled":false , "required":false},"0140":{"disabled":false , "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"seikybtn"    , control:{"0000":{"disabled":true , "required":false},"0130":{"disabled":false , "required":false},"0140":{"disabled":false , "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"juryobtn"    , control:{"0000":{"disabled":true , "required":false},"0130":{"disabled":false , "required":false},"0140":{"disabled":false , "required":false}}, type:"btn"});
    ContorolInfo.add({"info":4 , "item":"sekoubtn"    , control:{"0000":{"disabled":true , "required":false},"0140":{"disabled":false , "required":false},"0240":{"disabled":false , "required":false},"9940":{"disabled":false , "required":false}}, type:"btn"});
    //----- 制御情報の反映 -----
    ContorolInfo.set(0, userkey);
    ContorolInfo.set(1, userkey);
    ContorolInfo.set(2, userkey);
    ContorolInfo.set(3, userkey);
    ContorolInfo.set(4, userkey);
  }
})(jQuery);
